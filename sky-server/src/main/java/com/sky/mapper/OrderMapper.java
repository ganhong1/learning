package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders searchOrder(Integer id);

    void cancelOrder(Long id, LocalDateTime cancelTime, Integer cancelId, String cancelReason);


    @Select("SELECT " +
            "COUNT(CASE WHEN status = 3 THEN 1 END) AS confirmed, " +
            "COUNT(CASE WHEN status = 4 THEN 1 END) AS deliveryInProgress," +
            "COUNT(CASE WHEN status = 2 THEN 1 END) AS toBeConfirmed FROM orders")
    OrderStatisticsVO ordersStatusStatistics();

    @Update("update orders set status = #{status} where id = #{id}")
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    /**
     * 动态条件封装营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    Integer countByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
