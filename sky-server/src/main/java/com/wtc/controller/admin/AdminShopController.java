package com.wtc.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wtc.result.Result;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
public class AdminShopController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PutMapping("/{status}")
    public Result<Object> setStatus(@PathVariable Integer status) {
        log.info("修改商户状态: {}", status);
        redisTemplate.opsForValue().set("SHOP_STATUS", status.toString());
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = Integer.valueOf(redisTemplate.opsForValue().get("SHOP_STATUS"));
        return Result.success(status);
    }
}
