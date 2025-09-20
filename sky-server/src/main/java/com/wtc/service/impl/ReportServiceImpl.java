package com.wtc.service.impl;

import com.wtc.dto.GoodsSalesDTO;
import com.wtc.entity.Orders;
import com.wtc.mapper.OrderMapper;
import com.wtc.mapper.UserMapper;
import com.wtc.service.ReportService;
import com.wtc.service.WorkspaceService;
import com.wtc.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 计算日期
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1)).toList();
        String dateString = dates.stream().map(LocalDate::toString).collect(Collectors.joining(","));

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(dateString);

        // 获取数据
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> map = Map.of(
                    "beginTime", beginTime,
                    "endTime", endTime,
                    "status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover);
        }

        String turnoverString = turnoverList.stream().map(Object::toString).collect(Collectors.joining(","));
        turnoverReportVO.setTurnoverList(turnoverString);

        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1)).toList();

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            HashMap<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(dates.stream().map(LocalDate::toString).collect(Collectors.joining(",")));
        userReportVO.setNewUserList(newUserList.stream().map(Object::toString).collect(Collectors.joining(",")));
        userReportVO.setTotalUserList(totalUserList.stream().map(Object::toString).collect(Collectors.joining(",")));
        return userReportVO;
    }

    @Override
    public OrderReportVO geOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1)).toList();

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        int totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        int validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();

        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(dates.stream().map(LocalDate::toString).collect(Collectors.joining(",")));
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        orderReportVO.setOrderCountList(orderCountList.stream().map(Object::toString).collect(Collectors.joining(",")));
        orderReportVO.setValidOrderCountList(validOrderCountList.stream().map(Object::toString).collect(Collectors.joining(",")));
        orderReportVO.setOrderCompletionRate(totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        String names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(","));
        String numbers = salesTop10.stream().map(dto -> String.valueOf(dto.getNumber())).collect(Collectors.joining(","));

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(names);
        salesTop10ReportVO.setNumberList(numbers);
        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        LocalDateTime beginTime = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        if (is != null) {
            try {
                XSSFWorkbook excel = new XSSFWorkbook(is);
                XSSFSheet sheet = excel.getSheet("Sheet1");

                sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
                XSSFRow row = sheet.getRow(3);
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(6).setCellValue(businessData.getNewUsers());

                row = sheet.getRow(4);
                row.getCell(2).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getUnitPrice());

                for (int i = 0; i < 30; i++) {
                    LocalDate date = dateBegin.plusDays(i);
                    BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                    row = sheet.getRow(7 + i);
                    row.getCell(1).setCellValue(date.toString());
                    row.getCell(2).setCellValue(data.getTurnover());
                    row.getCell(3).setCellValue(data.getValidOrderCount());
                    row.getCell(4).setCellValue(data.getOrderCompletionRate());
                    row.getCell(5).setCellValue(data.getUnitPrice());
                    row.getCell(6).setCellValue(data.getNewUsers());
                }

                ServletOutputStream outputStream = response.getOutputStream();
                excel.write(outputStream);

                outputStream.close();
                excel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }
}
