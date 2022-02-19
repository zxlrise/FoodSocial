package com.xiao.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiao.commons.constant.ApiConstant;
import com.xiao.commons.constant.RedisKeyConstant;
import com.xiao.commons.exception.ParameterException;
import com.xiao.commons.model.domain.ResultInfo;
import com.xiao.commons.model.vo.NearMeDinerVO;
import com.xiao.commons.model.vo.ShortDinerInfo;
import com.xiao.commons.model.vo.SignInDinerInfo;
import com.xiao.commons.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NearMeService {

    @Resource
    private DinersService dinersService;
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 更新食客坐标
     *
     * @param accessToken 登录用户 token
     * @param lon         经度
     * @param lat         纬度
     */
    public void updateDinerLocation(String accessToken, Float lon, Float lat) {
        // 参数校验
        AssertUtil.isTrue(lon == null, "获取经度失败");
        AssertUtil.isTrue(lat == null, "获取纬度失败");
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 获取 key diner:location
        String key = RedisKeyConstant.diner_location.getKey();
        // 将用户地理位置信息存入 Redis
        RedisGeoCommands.GeoLocation geoLocation = new RedisGeoCommands
                .GeoLocation(signInDinerInfo.getId(), new Point(lon, lat));
        redisTemplate.opsForGeo().add(key, geoLocation);

    }

    /**
     * 获取附近的人
     *
     * @param accessToken 用户登录 token
     * @param radius      半径，默认 1000m
     * @param lon         经度
     * @param lat         纬度
     * @return
     */
    public List<NearMeDinerVO> findNearMe(String accessToken,
                                          Integer radius,
                                          Float lon, Float lat) {
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 食客 ID
        Integer dinerId = signInDinerInfo.getId();
        // 处理半径，默认 1000m
        if (radius == null) {
            radius = 1000;
        }
        // 获取 key
        String key = RedisKeyConstant.diner_location.getKey();
        // 获取用户经纬度
        Point point = null;
        if (lon == null || lat == null) {
            // 如果经纬度没传，那么从 Redis 中获取
            List<Point> points = redisTemplate.opsForGeo().position(key, dinerId);
            AssertUtil.isTrue(points == null || points.isEmpty(),
                    "获取经纬度失败");
            point = points.get(0);
        } else {
            point = new Point(lon, lat);
        }
        // 初始化距离对象，单位 m
        Distance distance = new Distance(radius,
                RedisGeoCommands.DistanceUnit.METERS);
        // 初始化 Geo 命令参数对象
        RedisGeoCommands.GeoRadiusCommandArgs args =
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
        // 附近的人限制 20，包含距离，按由近到远排序
        args.limit(20).includeDistance().sortAscending();
        // 以用户经纬度为圆心，范围 1000m
        Circle circle = new Circle(point, distance);
        // 获取附近的人 GeoLocation 信息
        GeoResults<RedisGeoCommands.GeoLocation> geoResult =
                redisTemplate.opsForGeo().radius(key, circle, args);
        // 构建有序 Map
        Map<Integer, NearMeDinerVO> nearMeDinerVOMap = Maps.newLinkedHashMap();
        // 完善用户头像昵称信息
        geoResult.forEach(result -> {
            RedisGeoCommands.GeoLocation<Integer> geoLocation = result.getContent();
            // 初始化 Vo 对象
            NearMeDinerVO nearMeDinerVO = new NearMeDinerVO();
            nearMeDinerVO.setId(geoLocation.getName());
            // 获取距离
            Double dist = result.getDistance().getValue();
            // 四舍五入精确到小数点后 1 位，方便客户端显示
            String distanceStr = NumberUtil.round(dist, 1).toString() + "m";
            nearMeDinerVO.setDistance(distanceStr);
            nearMeDinerVOMap.put(geoLocation.getName(), nearMeDinerVO);
        });
        // 获取附近的人的信息（根据 Diner 服务接口获取）
        Integer[] dinerIds = nearMeDinerVOMap.keySet().toArray(new Integer[]{});
        List<ShortDinerInfo> shortDinerInfos = dinersService.findByIds(StrUtil.join(",", dinerIds));
        // 完善昵称头像信息
        shortDinerInfos.forEach(shortDinerInfo -> {
            NearMeDinerVO nearMeDinerVO = nearMeDinerVOMap.get(shortDinerInfo.getId());
            nearMeDinerVO.setNickname(shortDinerInfo.getNickname());
            nearMeDinerVO.setAvatarUrl(shortDinerInfo.getAvatarUrl());
        });
        return Lists.newArrayList(nearMeDinerVOMap.values());
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
