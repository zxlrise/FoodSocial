package com.xiao.restaurants.service;

import cn.hutool.core.bean.BeanUtil;
import com.xiao.commons.constant.RedisKeyConstant;
import com.xiao.commons.model.pojo.Restaurant;
import com.xiao.commons.utils.AssertUtil;
import com.xiao.restaurants.mapper.RestaurantMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

@Service
@Slf4j
public class RestaurantService {

    @Resource
    public RestaurantMapper restaurantMapper;
    @Resource
    public RedisTemplate redisTemplate;

    /**
     * 根据餐厅 ID 查询餐厅数据
     *
     * @param restaurantId
     * @return
     */
    public Restaurant findById(Integer restaurantId) {
        // 请选择餐厅
        AssertUtil.isTrue(restaurantId == null, "请选择餐厅查看");
        // 获取 Key
        String key = RedisKeyConstant.restaurants.getKey() + restaurantId;
        // 获取餐厅缓存
        LinkedHashMap restaurantMap = (LinkedHashMap) redisTemplate.opsForHash().entries(key);
        // 如果缓存不存在，查询数据库
        Restaurant restaurant = null;
        if (restaurantMap == null || restaurantMap.isEmpty()) {
            log.info("缓存失效了，查询数据库：{}", restaurantId);
            // 查询数据库
            restaurant = restaurantMapper.findById(restaurantId);
            if (restaurant != null) {
                // 更新缓存
                redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(restaurant));
            } else {
                // 写入缓存一个空数据，设置一个失效时间，60s
            }
        } else {
            restaurant = BeanUtil.fillBeanWithMap(restaurantMap,
                    new Restaurant(), false);
        }
        return restaurant;
    }

}