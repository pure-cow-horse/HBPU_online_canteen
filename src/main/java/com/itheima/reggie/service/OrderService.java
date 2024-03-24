package com.itheima.reggie.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     * @return
     */
    String submit(Orders orders) throws AlipayApiException;
}
