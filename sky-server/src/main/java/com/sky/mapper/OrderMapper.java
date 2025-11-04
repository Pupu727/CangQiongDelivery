package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    @Update("update orders set pay_status = #{orderPaidStatus}, status = #{orderStatus}, checkout_time = #{checkOutTime} " +
            "where number = #{orderNumber}")
    void updateStatus(String orderNumber, Integer orderPaidStatus, Integer orderStatus, LocalDateTime checkOutTime);

    @Delete("delete from orders where number = #{orderNumber}")
    void deleteByNumber(String orderNumber);
}
