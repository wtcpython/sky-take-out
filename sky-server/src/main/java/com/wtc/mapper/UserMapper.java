package com.wtc.mapper;

import com.wtc.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE openid = #{openid}")
    User getByOpenid(String openid);

    void insert(User user);

    Integer countByMap(Map<String, Object> map);
}
