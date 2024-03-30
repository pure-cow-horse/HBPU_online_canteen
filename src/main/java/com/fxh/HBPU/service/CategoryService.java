package com.fxh.HBPU.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fxh.HBPU.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long id);

}
