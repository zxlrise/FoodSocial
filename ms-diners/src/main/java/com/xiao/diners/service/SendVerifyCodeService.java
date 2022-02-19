package com.xiao.diners.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiao.commons.constant.RedisKeyConstant;
import com.xiao.commons.utils.AssertUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 发送验证码业务逻辑层
 */
@Service
public class SendVerifyCodeService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送验证码
     *
     * @param phone
     */
    public void send(String phone) {
        // 检查非空
        AssertUtil.isNotEmpty(phone, "手机号不能为空");
        // 根据手机号查询是否已生成验证码，已生成 直接返回
        if (!checkCodeIsExpired(phone)) {
            return;
        }
        // 生成 6 位验证码
        String code = RandomUtil.randomNumbers(6);
        // 调用短信服务发送短信
        // 发送成功，将 code 保存至 Redis，失效时间 60s
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        redisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
    }

    /**
     * 根据手机号查询是否已生成验证码
     *
     * @param phone
     * @return
     */
    private boolean checkCodeIsExpired(String phone) {
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        String code = redisTemplate.opsForValue().get(key);  //获取key键对应的值
        return StrUtil.isBlank(code) ? true : false;
    }

    /**
     * 根据手机号获取验证码
     *
     * @param phone
     * @return
     */
    public String getCodeByPhone(String phone) {
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        return redisTemplate.opsForValue().get(key);
    }

}
