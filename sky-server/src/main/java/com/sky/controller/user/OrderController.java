package com.sky.controller.user;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.generic.LocalVariableGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("激活用户下单,参数为:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("激活历史订单查询,传入参数为:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.searchHistoryOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> searchOrderDetails(@PathVariable Integer id) {
        log.info("激活订单详情查询,订单Id为:{}",id);
        OrderVO orderVO = orderService.searchOrderDetails(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("激活订单取消,取消订单号为:{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }


    @PostMapping("/repetition/{id}")
    public Result repetitionOrder(@PathVariable Long id) {
        log.info("激活再来一单,订单id为:{}",id);
        orderService.repetitionOrder(id);
        return Result.success();
    }


    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable("id") Integer id) {
        orderService.reminder(id);
        return Result.success();
    }
}
