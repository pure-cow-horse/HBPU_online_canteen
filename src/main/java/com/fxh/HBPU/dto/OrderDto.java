package com.fxh.HBPU.dto;

import com.fxh.HBPU.entity.OrderDetail;
import com.fxh.HBPU.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {

    private List<OrderDetail> orderDetails;
}