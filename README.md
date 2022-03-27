# 美食社交APP后台

## 1、应用介绍

美食社交APP后台API接口设计，涉及APP中用户、好友、订单为基础的相关业务，分为用户、优惠券、好友、Feed、订单五个微服务。完成用户登录、交友、发朋友圈以及购买优惠券这个业务流程。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2b0c420ac5f5490b8f6c972a2f5d99d4.png)

## 2、应用总结

截至目前我们已经开发的功能如下：

| 业务场景     | 数据类型        | 操作指令                           | 作用                                 |
| ------------ | --------------- | ---------------------------------- | ------------------------------------ |
| 单点登录     | String          | SET、GET                           | 存储 Token 与登录食客信息            |
| 抢购代金券   | Hash、Lua       | HGET、HSET、HINCRBY                | 防止超卖、限购(分布式锁)             |
| 好友功能     | Set             | SADD、SMEMBERS、SINTER             | 存储关注集合、粉丝集合、共同关注列表 |
| Feed功能     | Sorted Set      | ZADD、ZREVRANGE                    | 关注的好友的 Feed 流集合             |
| 签到功能     | Bitmap、String  | SETBIT、GETBIT、BITCOUNT、BITFIELD | 位图存储食客签到信息                 |
| 积分排行榜   | Sorted Set      | ZINCRBY、ZREVRANK、ZREVRANGE       | 存储食客总积分集合方便排序           |
| 附近的人     | Geo、Sorted Set | GEOADD、GEOREDIUS                  | 存储与查询食客地理位置信息           |
| 餐厅缓存     | Hash            | HSET、HGETALL、HINCRBY             | 存储餐厅热点数据                     |
| 最新餐厅评论 | List            | LPUSH、LRANGE                      | 存储最新餐厅评论                     |



### 2.1、单点登录

我们使用 Spring Security 和 OAuth2 实现了授权认证中心及单点登录的功能。

这个功能中 Redis 主要用于存储 Token 令牌信息，使用了 String 数据类型。

### 2.2、抢购优惠券

这个功能中我们实现了抢购秒杀完整的一套业务，解决了超卖、限制一人一单的问题。

这个功能中 Redis 主要用于实现分布式锁、Lua脚本，使用了 Hash 数据类型，讲解了原生的方式和 Redisson 的方式。

### 2.3、好友功能

这个功能中我们实现了关注、取关、获取共同关注列表功能。

这个功能中 Redis 主要用于存储关注列表和粉丝列表中的相关用户信息，使用了 Set 数据类型。

### 2.4、Feed功能

这个功能中我们实现了添加 Feed、删除 Feed、关注取关时变更 Feed、查询 Feed 功能。

这个功能中 Redis 主要用于存储每个用户关注好友添加的 Feed 流集合，使用了 Sorted Set 数据类型。

### 2.5、签到功能

这个功能中我们实现了签到、补签、获取连续签到次数、获取签到总次数、获取签到详情功能。

这个功能中 Redis 主要用于存储签到信息，使用了 Bitmap 数据类型。

### 2.6、积分功能

这个功能中我们实现了添加积分、获取积分排行榜功能。

这个功能中 Redis 主要用于存储积分信息，使用了 Sorted Set 数据类型。

### 1.7、附近的人

这个功能中我们实现了上传用户坐标、获取附近的人功能。

这个功能中 Redis 主要用于存储地理位置信息，使用了 GEO 数据类型。

### 2.8、餐厅缓存

这个功能中我们实现了餐厅热点数据缓存、查询餐厅缓存功能。

这个功能中 Redis 主要用于存储餐厅信息，使用了 Hash 数据类型。

### 2.9、最新餐厅评论

这个功能中我们实现了添加餐厅评论、获取餐厅评论功能。

这个功能中 Redis 主要用于存储餐厅评论信息，使用了 List 数据类型。

## 3、开发文档

文档是对项目开发过程中遇到的一些问题的详细记录，主要是为了帮助没有基础的小伙伴快速理解这个项目。

1. [需求分析与数据库设计](https://github.com/zxlrise/FoodSocial/wiki/1.%E9%9C%80%E6%B1%82%E5%88%86%E6%9E%90%E4%B8%8E%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1) 
2. [认证中心代码逻辑](https://github.com/zxlrise/FoodSocial/wiki/2.%E8%AE%A4%E8%AF%81%E4%B8%AD%E5%BF%83%E4%BB%A3%E7%A0%81%E9%80%BB%E8%BE%91)
3. [用户注册](https://github.com/zxlrise/FoodSocial/wiki/3.%E7%94%A8%E6%88%B7%E6%B3%A8%E5%86%8C)
4. [抢购代金券](https://github.com/zxlrise/FoodSocial/wiki/4.%E6%8A%A2%E8%B4%AD%E4%BB%A3%E9%87%91%E5%88%B8)
5. [好友功能](https://github.com/zxlrise/FoodSocial/wiki/5.%E5%A5%BD%E5%8F%8B%E5%8A%9F%E8%83%BD)
6. [Feed功能](https://github.com/zxlrise/FoodSocial/wiki/6.Feed%E5%8A%9F%E8%83%BD)
7. [用户签到功能](https://github.com/zxlrise/FoodSocial/wiki/7.%E7%94%A8%E6%88%B7%E7%AD%BE%E5%88%B0%E5%8A%9F%E8%83%BD)
8. [用户积分功能](https://github.com/zxlrise/FoodSocial/wiki/8.%E7%94%A8%E6%88%B7%E7%A7%AF%E5%88%86%E5%8A%9F%E8%83%BD)
9. [附近的人功能](https://github.com/zxlrise/FoodSocial/wiki/9.%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA)
10. [缓存餐厅](https://github.com/zxlrise/FoodSocial/wiki/10.%E7%BC%93%E5%AD%98%E9%A4%90%E5%8E%85)
11. [最新餐厅评论](https://github.com/zxlrise/FoodSocial/wiki/11.%E6%9C%80%E6%96%B0%E9%A4%90%E5%8E%85%E8%AF%84%E8%AE%BA)


