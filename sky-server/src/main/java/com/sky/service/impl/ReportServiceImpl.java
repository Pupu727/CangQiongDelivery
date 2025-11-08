package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    // 在类中注入UserMapper
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate tmpDate = begin;
        while(!tmpDate.isAfter(end)) {
            dateList.add(tmpDate);
            tmpDate = tmpDate.plusDays(1);
        }
        // 一次性查询所有日期的用户统计数据
        Map<String, Object> params = new HashMap<>();
        params.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        params.put("end", LocalDateTime.of(end, LocalTime.MAX));
        List<Map<String, Object>> userStatsData = userMapper.getUserStatisticsByDateRange(params);
        
        // 将查询结果转换为Map，方便按日期查找
        Map<String, Map<String, Object>> statsMap = new HashMap<>();
        if (userStatsData != null) {
            for (Map<String, Object> data : userStatsData) {
                String dateStr = ((java.sql.Date) data.get("date")).toLocalDate().toString();
                statsMap.put(dateStr, data);
            }
        }
        
        // 构建用户统计列表，确保每个日期都有数据
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        
        // 初始化总用户数为0
        int cumulativeTotalUsers = 0;
        
        for (LocalDate date : dateList) {
            String dateStr = date.toString();
            Map<String, Object> stats = statsMap.get(dateStr);
            
            // 当日新增用户数
            Integer newUsers = (stats != null && stats.get("new_users") != null) ? 
                              ((Number) stats.get("new_users")).intValue() : 0;
            newUserList.add(newUsers);
            
            // 累计总用户数
            cumulativeTotalUsers += newUsers;
            totalUserList.add(cumulativeTotalUsers);
        }
        
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate tmpBegin = begin;
        while(!tmpBegin.isAfter(end)){
            dateList.add(tmpBegin);
            tmpBegin = tmpBegin.plusDays(1);
        }
        
        // 一次性查询所有日期的订单统计数据
        Map<String, Object> params = new HashMap<>();
        params.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        params.put("end", LocalDateTime.of(end, LocalTime.MAX));
        params.put("completedStatus", Orders.COMPLETED);
        List<Map<String, Object>> orderStatsData = orderMapper.getOrderStatisticsByDateRange(params);
        
        // 将查询结果转换为Map，方便按日期查找
        Map<String, Map<String, Object>> statsMap = new HashMap<>();
        if (orderStatsData != null) {
            for (Map<String, Object> data : orderStatsData) {
                Object dateObj = data.get("date");
                String dateStr;
                if (dateObj instanceof java.sql.Date) {
                    dateStr = ((java.sql.Date) dateObj).toLocalDate().toString();
                } else if (dateObj instanceof String) {
                    dateStr = (String) dateObj;
                } else {
                    dateStr = dateObj != null ? dateObj.toString() : "";
                }
                statsMap.put(dateStr, data);
            }
        }
        
        // 构建订单统计列表，确保每个日期都有数据
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        
        for (LocalDate date : dateList) {
            String dateStr = date.toString();
            Map<String, Object> stats = statsMap.get(dateStr);
            
            // 当日总订单数
            Integer orderCount = (stats != null && stats.get("total_orders") != null) ? 
                               ((Number) stats.get("total_orders")).intValue() : 0;
            orderCountList.add(orderCount);
            
            // 当日有效订单数
            Integer validOrderCount = (stats != null && stats.get("valid_orders") != null) ? 
                                    ((Number) stats.get("valid_orders")).intValue() : 0;
            validOrderCountList.add(validOrderCount);
        }
        
        // 计算总数和完成率
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).orElse(0);
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).orElse(0);
        double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount;
        }
        
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10Orders(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> top10Orders = orderMapper.getTop10Orders(beginTime, endTime);
        List<String> nameList = top10Orders.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = top10Orders.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库获取营业数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);
        //2. 通过POI将数据写入Excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }
            //3. 通过输出流下载到浏览器
            OutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate tmpBegin = begin;
        while(!tmpBegin.isAfter(end)){
            dateList.add(tmpBegin);
            tmpBegin = tmpBegin.plusDays(1);
        }
        // 一次性查询所有日期的营业额数据
        Map<String, Object> params = new HashMap<>();
        params.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        params.put("end", LocalDateTime.of(end, LocalTime.MAX));
        params.put("status", Orders.COMPLETED);
        List<Map<String, Object>> turnoverData = orderMapper.getTurnoverByDateRange(params);

        // 将查询结果转换为Map，方便日期查找
        Map<String, Double> turnoverMap = new HashMap<>();
        if (turnoverData != null) {
            for (Map<String, Object> data : turnoverData) {
                // 处理日期类型转换 - 修复java.sql.Date到LocalDate的转换问题
                Object dateObj = data.get("date");
                String dateStr;
                if (dateObj instanceof java.sql.Date) {
                    dateStr = ((java.sql.Date) dateObj).toLocalDate().toString();
                } else if (dateObj instanceof String) {
                    dateStr = (String) dateObj;
                } else {
                    dateStr = dateObj != null ? dateObj.toString() : "";
                }
                // 处理营业额类型转换 - 修复BigDecimal转Double的问题
                Object turnoverObj = data.get("turnover");
                Double turnover = 0.0;
                if (turnoverObj instanceof BigDecimal) {
                    turnover = ((BigDecimal) turnoverObj).doubleValue();
                } else if (turnoverObj instanceof Double) {
                    turnover = (Double) turnoverObj;
                } else if (turnoverObj instanceof Number) {
                    turnover = ((Number) turnoverObj).doubleValue();
                }

                turnoverMap.put(dateStr, turnover);
            }
        }

        // 构建营业额列表，确保每个日期都有对应的营业额数据
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            String dateStr = date.toString();
            Double turnover = turnoverMap.getOrDefault(dateStr, 0.0);
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }
}