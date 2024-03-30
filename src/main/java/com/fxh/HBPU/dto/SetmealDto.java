package com.fxh.HBPU.dto;

import com.fxh.HBPU.entity.Setmeal;
import com.fxh.HBPU.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
