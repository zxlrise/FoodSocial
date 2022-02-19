package com.xiao.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xiao.commons.constant.ApiConstant;
import com.xiao.commons.constant.RedisKeyConstant;
import com.xiao.commons.exception.ParameterException;
import com.xiao.commons.model.domain.ResultInfo;
import com.xiao.commons.model.pojo.Follow;
import com.xiao.commons.model.vo.ShortDinerInfo;
import com.xiao.commons.model.vo.SignInDinerInfo;
import com.xiao.commons.utils.AssertUtil;
import com.xiao.commons.utils.ResultInfoUtil;
import com.xiao.follow.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注/取关业务逻辑层
 */
@Service
public class FollowService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;
    @Value("${service.name.ms-feeds-server}")
    private String feedsServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private FollowMapper followMapper;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 获取粉丝列表
     *
     * @param dinerId
     * @return
     */
    public Set<Integer> findFollowers(Integer dinerId) {
        AssertUtil.isNotNull(dinerId, "请选择要查看的用户");
        Set<Integer> followers = redisTemplate.opsForSet()
                .members(RedisKeyConstant.followers.getKey() + dinerId);
        return followers;
    }

    /**
     * 共同关注列表
     *
     * @param dinerId
     * @param accessToken
     * @param path
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultInfo findCommonsFriends(Integer dinerId, String accessToken, String path) {
        // 是否选择了查看对象
        AssertUtil.isTrue(dinerId == null ||dinerId < 1 , "请选择要查看的人");
        // 获取登录用户信息
        SignInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        // 获取登录用户的关注信息
        String loginDinerKey = RedisKeyConstant.following.getKey() + dinerInfo.getId();
        // 获取登录用户查看对象的关注信息
        String dinerKey = RedisKeyConstant.following.getKey() + dinerId;
        // 计算交集
        Set<Integer> dinerIds = redisTemplate.opsForSet().intersect(loginDinerKey, dinerKey);
        // 没有
        if(dinerIds == null ||dinerIds.isEmpty()){
            return ResultInfoUtil.buildSuccess(path, new ArrayList<ShortDinerInfo>());
        }
        // 调用食客服务根据 ids 查询食客信息
        ResultInfo resultInfo = restTemplate.getForObject(dinersServerName + "findByIds?access_token={accessToken}&ids={ids}",
                ResultInfo.class, accessToken, StrUtil.join(",", dinerIds));
        if(resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 处理结果集
        List<LinkedHashMap> dinnerInfoMaps = (ArrayList) resultInfo.getData();
        List<ShortDinerInfo> dinerInfos = dinnerInfoMaps.stream()
                .map(diner -> BeanUtil.fillBeanWithMap(diner, new ShortDinerInfo(), true))
                .collect(Collectors.toList());

        return ResultInfoUtil.buildSuccess(path, dinerInfos);
    }

    /**
     * 关注/取关
     *
     * @param followDinerId 关注的食客ID
     * @param isFoolowed    是否关注 1=关注 0=取关
     * @param accessToken   登录用户token
     * @param path          访问地址
     * @return
     */
    public ResultInfo follow(Integer followDinerId, int isFoolowed,
                             String accessToken, String path) {
        // 是否选择了关注对象
        AssertUtil.isTrue(followDinerId == null || followDinerId < 1,
                "请选择要关注的人");
        // 获取登录用户信息 (封装方法)
        SignInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        // 获取当前登录用户与需要关注用户的关注信息
        Follow follow = followMapper.selectFollow(dinerInfo.getId(), followDinerId);

        // 如果没有关注信息，且要进行关注操作 -- 添加关注
        if (follow == null && isFoolowed == 1) {
            // 添加关注信息
            int count = followMapper.save(dinerInfo.getId(), followDinerId);
            // 添加关注列表到 Redis
            if (count == 1) {
                addToRedisSet(dinerInfo.getId(), followDinerId);
                // 保存 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,
                    "关注成功", path, "关注成功");
        }

        // 如果有关注信息，且目前处于关注状态，且要进行取关操作 -- 取关关注
        if (follow != null && follow.getIsValid() == 1 && isFoolowed == 0) {
            // 取关
            int count = followMapper.update(follow.getId(), isFoolowed);
            // 移除 Redis 关注列表
            if (count == 1) {
                removeFromRedisSet(dinerInfo.getId(), followDinerId);
                // 移除 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 0);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,
                    "成功取关", path, "成功取关");
        }

        // 如果有关注信息，且目前处于取关状态，且要进行关注操作 -- 重新关注
        if (follow != null && follow.getIsValid() == 0 && isFoolowed == 1) {
            // 重新关注
            int count = followMapper.update(follow.getId(), isFoolowed);
            // 添加关注列表到 Redis
            if (count == 1) {
                addToRedisSet(dinerInfo.getId(), followDinerId);
                // 添加 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,
                    "关注成功", path, "关注成功");
        }

        return ResultInfoUtil.buildSuccess(path, "操作成功");
    }

    /**
     * 发送请求添加或者移除关注人的Feed列表
     *
     * @param followDinerId 关注好友的ID
     * @param accessToken   当前登录用户token
     * @param type          0=取关 1=关注
     */
    private void sendSaveOrRemoveFeed(Integer followDinerId, String accessToken, int type) {
        String feedsUpdateUrl = feedsServerName + "updateFollowingFeeds/"
                + followDinerId + "?access_token=" + accessToken;
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求体（请求参数）
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(feedsUpdateUrl, entity, ResultInfo.class);
    }

    /**
     * 添加关注列表到 Redis
     *
     * @param dinerId
     * @param followDinerId
     */
    private void addToRedisSet(Integer dinerId, Integer followDinerId) {
        redisTemplate.opsForSet().add(RedisKeyConstant.following.getKey() + dinerId, followDinerId); //关注集合
        redisTemplate.opsForSet().add(RedisKeyConstant.followers.getKey() + followDinerId, dinerId); //粉丝集合
    }

    /**
     * 移除 Redis 关注列表
     *
     * @param dinerId
     * @param followDinerId
     */
    private void removeFromRedisSet(Integer dinerId, Integer followDinerId) {
        redisTemplate.opsForSet().remove(RedisKeyConstant.following.getKey() + dinerId, followDinerId);
        redisTemplate.opsForSet().remove(RedisKeyConstant.followers.getKey() + followDinerId, dinerId);
    }

    /**
     * 获取登录用户信息
     *
     * @param accessToken
     * @return
     */
    private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
        // 必须登录
        AssertUtil.mustLogin(accessToken);
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        if (dinerInfo == null) {
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE, ApiConstant.NO_LOGIN_MESSAGE);
        }
        return dinerInfo;
    }

}
