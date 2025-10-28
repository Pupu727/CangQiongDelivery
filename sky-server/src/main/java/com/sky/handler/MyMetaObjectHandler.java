package com.sky.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus公共字段自动填充处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.CREATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(AutoFillConstant.UPDATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(AutoFillConstant.CREATE_USER, BaseContext.getCurrentId(), metaObject);
        this.setFieldValByName(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId(), metaObject);
    }

    /**
     * 更新操作时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.UPDATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId(), metaObject);
    }
}