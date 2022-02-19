package com.xiao.feeds.mapper;

import com.xiao.commons.model.pojo.Feeds;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

public interface FeedsMapper {

    // 添加 Feed
    @Insert("insert into t_feeds (content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_date, update_date, is_valid) " +
            " values (#{content}, #{fkDinerId}, #{praiseAmount}, #{commentAmount}, #{fkRestaurantId}, " +
            " now(), now(), 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Feeds feeds);

    // 查询 Feed
    @Select("select id, content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_date, update_date, is_valid " +
            " from t_feeds where id = #{id} and is_valid = 1")
    Feeds findById(@Param("id") Integer id);

    // 逻辑删除 Feed
    @Update("update t_feeds set is_valid = 0 where id = #{id} and is_valid = 1")
    int delete(@Param("id") Integer id);

    // 根据食客 ID 查询 Feed
    @Select("select id, content, update_date from t_feeds " +
            " where fk_diner_id = #{dinerId} and is_valid = 1")
    List<Feeds> findByDinerId(@Param("dinerId") Integer dinerId);

    // 根据多主键查询 Feed
    @Select("<script> " +
            " select id, content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_date, update_date, is_valid " +
            " from t_feeds where is_valid = 1 and id in " +
            " <foreach item=\"id\" collection=\"feedIds\" open=\"(\" separator=\",\" close=\")\">" +
            "   #{id}" +
            " </foreach> order by id desc" +
            " </script>")
    List<Feeds> findFeedsByIds(@Param("feedIds") Set<Integer> feedIds);

}