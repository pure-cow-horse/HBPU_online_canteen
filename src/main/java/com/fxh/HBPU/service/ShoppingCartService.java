package com.fxh.HBPU.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fxh.HBPU.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    void clean();
}
