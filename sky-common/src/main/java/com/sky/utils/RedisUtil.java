package com.sky.utils;

import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate; // 注入工具类实例

    public Result accurateDeleteRedisDataByCateId(Long cateId) {
        String key = "dish_" + cateId;
        redisTemplate.delete(key);
        return Result.success();
    }

    public Result DeleteAllRedisData(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
        return Result.success();
    }


}
