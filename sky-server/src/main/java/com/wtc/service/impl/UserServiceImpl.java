package com.wtc.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.constant.MessageConstant;
import com.wtc.dto.UserLoginDTO;
import com.wtc.entity.User;
import com.wtc.exception.LoginFailedException;
import com.wtc.mapper.UserMapper;
import com.wtc.properties.WeChatProperties;
import com.wtc.service.UserService;
import com.wtc.utils.HttpClientUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 获取微信 openid
        Map<String, String> params = Map.of(
                "appid", weChatProperties.getAppid(),
                "secret", weChatProperties.getSecret(),
                "js_code", userLoginDTO.getCode(),
                "grant_type", "authorization_code");

        String result = HttpClientUtil.doGet(WX_LOGIN_URL, params);
        // 判断是否为空
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.createObjectNode();
        try {
            node = objectMapper.readTree(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String openid = node.get("openid").asText();
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

}
