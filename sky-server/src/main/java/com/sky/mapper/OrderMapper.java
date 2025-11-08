package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    Page<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime time);

    @MapKey("date")
    List<Map<String, Object>> getTurnoverByDateRange(Map<String, Object> params);

    @MapKey("date")
    List<Map<String, Object>> getOrderStatisticsByDateRange(Map<String, Object> params);

    List<GoodsSalesDTO> getTop10Orders(LocalDateTime beginTime, LocalDateTime endTime);
}