package com.fxh.HBPU.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fxh.HBPU.entity.OrderDetail;
import com.fxh.HBPU.mapper.OrderDetailMapper;
import com.fxh.HBPU.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}