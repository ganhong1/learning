package com.sky.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        /*1.判断和处理业务异常*/
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartsList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartsList == null || shoppingCartsList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        /*2向订单表插入一条数据*/
        Orders orders = Orders.builder()
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .status(Orders.PENDING_PAYMENT)
                .number(String.valueOf(System.currentTimeMillis()))
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .userId(userId)
                .address(addressBook.getDetail())
                        .build();

        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orderMapper.insert(orders);
        /*3.向订单明细表插入n条数据*/
        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        for (ShoppingCart cart : shoppingCartsList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
            log.info("成功封装数据:{}", orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        log.info("批量插入订单明细成功");
        /*4.清空当前用户的购物车数据*/
        shoppingCartMapper.deleteByUserId(userId);
        /*5.分装VO返回数据*/
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询-微信端
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        /*写好分页组件语法*/
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        /*拿到order和orderDetail,并且封装到orderVO里面*/
            /*准备好VOList*/
        List<OrderVO> orderVOList = new ArrayList<>();
            /*拿到order*/
        Page<Orders> page = orderMapper.searchHistoryOrders(ordersPageQueryDTO);
        if (page.getResult() == null || page.getResult().size() == 0) {
            log.error("查询失败,所查用户的订单不存在,用户Id为:{}",ordersPageQueryDTO.getUserId());
            return null;
        }
        for (Orders order : page.getResult()) {
            /*拿到orderDetail*/
            List<OrderDetail> orderDetailList = orderDetailMapper.searchDetailByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order,orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            orderVOList.add(orderVO);
        }
        Long total = page.getTotal();
        return new PageResult(total, orderVOList);
    }

    @Override
    public OrderVO searchOrderDetails(Integer id) {
        Orders orders = orderMapper.searchOrder(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.searchDetailByOrderId(Long.valueOf(id));
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        LocalDateTime cancelTime = LocalDateTime.now();
        String cancelReason = "人为取消";
        orderMapper.cancelOrder(id,cancelTime,Orders.CANCELLED,cancelReason);
    }

    @Override
    @Transactional
    public void repetitionOrder(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.searchDetailByOrderId(id);
        ShoppingCart shoppingCart = new ShoppingCart();
        for (OrderDetail orderDetail : orderDetailList) {
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public PageResult searchOrderWithCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        /*写好分页语法*/
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        /*拿到所有订单对象和订单详情对象并且封装*/
        List<OrderVO> orderVOList = new ArrayList<>();
        Page<Orders> ordersPage = orderMapper.searchHistoryOrders(ordersPageQueryDTO);
        List<Orders> ordersList = ordersPage.getResult();
        for (Orders order : ordersList) {
            List<OrderDetail> orderDetailList = orderDetailMapper.searchDetailByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order,orderVO);
            StringBuilder temName = new StringBuilder();
            for (OrderDetail orderDetail : orderDetailList) {
                temName.append(orderDetail.getName()+";");
            }
            orderVO.setOrderDishes(temName.toString());
            orderVO.setOrderDetailList(orderDetailList);
            orderVOList.add(orderVO);
        }
        Long total = ordersPage.getTotal();
        log.info(orderVOList.toString());
        if (orderVOList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return new PageResult(total, orderVOList);

    }

    @Override
    public OrderStatisticsVO ordersStatusStatistics() {
        OrderStatisticsVO orderStatisticsVO = orderMapper.ordersStatusStatistics();
        return orderStatisticsVO;
    }

    @Override
    public OrderVO searchOrderDetail(Integer id) {

        Orders orders = orderMapper.searchOrder(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.searchDetailByOrderId(Long.valueOf(id));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;

    }

    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        orderMapper.confirmOrder(ordersConfirmDTO);
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        /*先判断订单是否为"待接单"*/
            /*返回拒绝订单的数据*/
        Orders orders = orderMapper.searchOrder(Integer.valueOf(ordersRejectionDTO.getId().toString()));
        if (orders.getStatus() != 2){
            throw new OrderBusinessException("该状态的订单不能执行拒单操作");
        }

        /*准备退款*/
        Integer payStatus = orders.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        /*设置订单状态和原因*/
        orders.setStatus(Orders.CANCELLED);
        orders.setId(ordersRejectionDTO.getId());
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);

    }

    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception {

        /*返回订单数据*/
        Orders orders = orderMapper.searchOrder(Integer.valueOf(ordersCancelDTO.getId().toString()));
        /*准备退款*/
        Integer payStatus = orders.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }
        /*设置要更改的信息*/
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(orders);
    }

    @Override
    public void DeliveryOrder(Integer id) {
        /*返回订单数据*/
        Orders orders = orderMapper.searchOrder(id);
        if (orders.getStatus() != 3){
            throw new OrderBusinessException("该状态的订单不能执行派送操作");
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setEstimatedDeliveryTime(LocalDateTime.now());
        orders.setId(Long.valueOf(id));
        orderMapper.update(orders);
    }

    @Override
    public void completeOrder(Integer id) {
        Orders orders = orderMapper.searchOrder(id);
        if (orders.getStatus() != 4){
            throw new OrderBusinessException("该状态的订单不能执行完成操作");
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setId(Long.valueOf(id));
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Integer id) {
        Orders orders = orderMapper.searchOrder(id);

        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:"+orders.getNumber());

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }


}
