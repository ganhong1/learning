package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin
            ,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
            ) {
        log.info("触发营业额数据统计,查询时间段为:{}到{}",begin,end);
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin
            ,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        log.info("激活用户数据统计,查询时间端为:{}到{}",begin,end);
        return Result.success(reportService.getUserStatistics(begin,end));
    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin
            ,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        log.info("激活订单数据统计,查询时间段为:{}到{}",begin,end);
        return Result.success(reportService.getOrderStatistics(begin,end));
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin
            ,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        log.info("激活销量排名前十统计,查询时间段为:{}到{}",begin,end);
        return Result.success(reportService.getSalesTop10(begin,end));
    }
}
