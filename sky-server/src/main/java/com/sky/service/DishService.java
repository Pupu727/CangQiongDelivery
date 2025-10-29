package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

public interface DishService {
    void saveWithFlavor(DishDTO dishDto);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);
}
