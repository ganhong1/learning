package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> searchOrderWithCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("激活订单条件分页查询,来源管理端,传入条件为:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.searchOrderWithCondition(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> ordersStatusStatistics() {
        log.info("激活订单状态统计查询");
        OrderStatisticsVO orderStatisticsVO= orderService.ordersStatusStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> searchOrderDetail(@PathVariable Integer id) {
        log.info("激活订单详情查询,订单id为:{}",id);
        OrderVO orderVO = orderService.searchOrderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("激活接单,订单号为:{}",ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("激活拒单,订单详情为:{}",ordersRejectionDTO);
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    public Result adminCancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("激活取消订单,订单详情为:{}",ordersCancelDTO);
        orderService.adminCancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result DeliveryOrder(@PathVariable Integer id) {
        log.info("激活派送订单,订单号为:{}",id);
        orderService.DeliveryOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Integer id) {
        log.info("激活完成订单,订单号为:{}",id);
        orderService.completeOrder(id);
        return Result.success();
    }

}
