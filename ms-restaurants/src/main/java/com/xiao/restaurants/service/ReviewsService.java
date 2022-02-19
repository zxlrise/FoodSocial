package com.xiao.restaurants.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.xiao.commons.constant.ApiConstant;
import com.xiao.commons.constant.RedisKeyConstant;
import com.xiao.commons.exception.ParameterException;
import com.xiao.commons.model.domain.ResultInfo;
import com.xiao.commons.model.pojo.Restaurant;
import com.xiao.commons.model.pojo.Reviews;
import com.xiao.commons.model.vo.ReviewsVO;
import com.xiao.commons.model.vo.ShortDinerInfo;
import com.xiao.commons.model.vo.SignInDinerInfo;
import com.xiao.commons.utils.AssertUtil;
import com.xiao.restaurants.mapper.ReviewsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewsService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestaurantService restaurantService;
    @Resource
    private ReviewsMapper reviewsMapper;

    /**
     * 添加餐厅评论
     *
     * @param restaurantId 餐厅ID
     * @param accessToken  登录用户Token
     * @param content      评论内容
     * @param likeIt       是否喜欢
     */
    @Transactional(rollbackFor = Exception.class)
    public void addReview(Integer restaurantId, String accessToken,
                          String content, int likeIt) {
        // 参数校验
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1, "请选择评论的餐厅");
        AssertUtil.isNotEmpty(content, "请输入评论内容");
        AssertUtil.isTrue(content.length() > 800, "评论内容过长，请重新输入");
        // 判断餐厅是否存在
        Restaurant restaurant = restaurantService.findById(restaurantId);
        AssertUtil.isTrue(restaurant == null, "该餐厅不存在");
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 插入数据库
        Reviews reviews = new Reviews();
        reviews.setContent(content);
        reviews.setFkDinerId(signInDinerInfo.getId());
        reviews.setFkRestaurantId(restaurantId);
        // 这里需要后台操作处理餐厅数据(喜欢/不喜欢餐厅)做自增处理
        reviews.setLikeIt(likeIt);
        int count = reviewsMapper.saveReviews(reviews);
        if (count == 0) {
            return;
        }
        // 写入餐厅最新评论
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
        redisTemplate.opsForList().leftPush(key, reviews);
        // 保证队列中只需要十条 作业
    }

    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;
    private static final int NINE = 9;

    /**
     * 获取餐厅最新评论
     *
     * @param restaurantId 餐厅id
     * @param accessToken  登录Token
     * @return
     */
    public List<ReviewsVO> findNewReviews(Integer restaurantId, String accessToken) {
        // 参数校验
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1, "请选择餐厅进行查看");
        // 获取 Key
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
        // 取前十条
        List<LinkedHashMap> reviews = redisTemplate.opsForList().range(key, 0, NINE);
        // 初始化 VO 集合
        List<ReviewsVO> reviewsVOS = Lists.newArrayList();
        // 初始化用户 ID 集合
        List<Integer> dinerIds = Lists.newArrayList();
        // 循环处理评论集合
        reviews.forEach(review -> {
            ReviewsVO reviewsVO = BeanUtil.fillBeanWithMap(review,
                    new ReviewsVO(), true);
            reviewsVOS.add(reviewsVO);
            dinerIds.add(reviewsVO.getFkDinerId());
        });
        // 查询评论用户信息
        ResultInfo resultInfo = restTemplate.getForObject(dinersServerName +
                        "findByIds?access_token=${accessToken}&ids={ids}",
                ResultInfo.class, accessToken, StrUtil.join(",", dinerIds));
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        List<LinkedHashMap> dinerInfoMaps = (ArrayList) resultInfo.getData();
        Map<Integer, ShortDinerInfo> dinerInfos = dinerInfoMaps.stream()
                .collect(Collectors.toMap(
                        diner -> (int) diner.get("id"),
                        diner -> BeanUtil.fillBeanWithMap(diner,
                                new ShortDinerInfo(), true))
                );
        // 循环处理 VO 集合插入用户信息
        reviewsVOS.forEach(review -> {
            ShortDinerInfo dinerInfo = dinerInfos.get(review.getFkDinerId());
            if (dinerInfo != null) {
                review.setDinerInfo(dinerInfo);
            }
        });
        return reviewsVOS;
    }

    /**
     * 获取登录用户信息
     *
     * @param accessToken
     * @return
     */
    private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
        // 登录校验
        AssertUtil.mustLogin(accessToken);
        // 获取登录用户信息
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        // 这里的data是一个LinkedHashMap，SignInDinerInfo
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), true);
        if (dinerInfo == null) {
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE, ApiConstant.NO_LOGIN_MESSAGE);
        }
        return dinerInfo;
    }

}