package com.xiao.restaurants.mapper;

import com.xiao.commons.model.pojo.Reviews;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface ReviewsMapper {

    // 插入餐厅评论
    @Insert("insert into t_reviews (fk_restaurant_id, fk_diner_id, content, like_it, is_valid, create_date, update_date)" +
            " values (#{fkRestaurantId}, #{fkDinerId}, #{content}, #{likeIt}, 1, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveReviews(Reviews reviews);

}