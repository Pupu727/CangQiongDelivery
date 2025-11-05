package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    //每分钟处理一次订单超时支付
    @Scheduled(cron = "0 * * * * ?")
    public void overTimePay(){
        log.info("每分钟处理一次订单超时支付{}", LocalDateTime.now());
        //查询超时订单
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> orders = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, time);
        if(orders != null && !orders.isEmpty()){
            for(Orders order : orders){
                //更新订单状态为已取消
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public void clean(){
        //自动完成前一天还在派送中的订单
        LocalDateTime time = LocalDateTime.now().minusHours(1);
        List<Orders> orders = orderMapper.getByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(orders != null && !orders.isEmpty()){
            for(Orders order : orders){
                //更新订单状态为已完成
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
