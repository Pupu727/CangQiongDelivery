package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @MapKey("date")
    List<Map<String, Object>> getUserStatisticsByDateRange(Map<String, Object> params);

    /**
     * 根据动态条件统计用户数量
     * @param map 动态条件参数
     * @return 用户数量
     */
    Integer countByMap(Map map);
}