package com.wtc.vo;

import com.wtc.entity.OrderDetail;
import com.wtc.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {

    // 订单菜品信息
    private String orderDishes;

    // 订单详情
    private List<OrderDetail> orderDetailList;

}
