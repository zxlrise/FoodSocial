package com.xiao.restaurants.mapper;

import com.xiao.commons.model.pojo.Restaurant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RestaurantMapper {

    // 查询餐厅信息
    @Select("select id, name, cnName, x, y, location, cnLocation, area, telephone, " +
            "email, website, cuisine, average_price, introduction, thumbnail, like_votes," +
            "dislike_votes, city_id, is_valid, create_date, update_date" +
            " from t_restaurants")
    List<Restaurant> findAll();

    // 根据餐厅 ID 查询餐厅信息
    @Select("select id, name, cnName, x, y, location, cnLocation, area, telephone, " +
            "email, website, cuisine, average_price, introduction, thumbnail, like_votes," +
            "dislike_votes, city_id, is_valid, create_date, update_date" +
            " from t_restaurants where id = #{id}")
    Restaurant findById(@Param("id") Integer id);

}