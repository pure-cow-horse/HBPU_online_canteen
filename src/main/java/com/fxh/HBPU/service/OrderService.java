package com.fxh.HBPU.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fxh.HBPU.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     * @return
     */
    String submit(Orders orders) throws AlipayApiException;
}
