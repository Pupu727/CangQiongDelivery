package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    public User login(UserLoginDTO userLoginDTO) {
        //获取openid
        String openId = getOpenId(userLoginDTO);
        //判断openid是否为空
        if(openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //根据openid判断是否为新用户
        User user = userMapper.getByOpenid(openId);
        if(user == null){
            //新用户，插入user表
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getOpenId(UserLoginDTO userLoginDTO) {
        HashMap<String, String> claims = new HashMap<>();
        claims.put("appid",weChatProperties.getAppid());
        claims.put("secret",weChatProperties.getSecret());
        claims.put("js_code", userLoginDTO.getCode());
        String json = HttpClientUtil.doGet(WX_LOGIN, claims);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
