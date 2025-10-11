package com.talent.common.utils.mybatisPlus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.talent.common.constant.Constants;
import com.talent.common.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.plugin.*;

import java.util.Date;

/**
 * @ClassName MybatisPlusConfig
 * @Description 启用逻辑删除
 * @Author JamesRay
 * @Date 2023/12/4 15:37
 */
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    @Bean
    public ISqlInjector sqlInjector() {
        return new LogicSqlInjector();
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        String userName = SecurityUtils.getUsername();
        this.setFieldValByName("delFlag", Constants.IS_DEL_N, metaObject);
        this.setFieldValByName("createBy", userName, metaObject);
        this.setFieldValByName("createTime", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userName = SecurityUtils.getUsername();
        this.setFieldValByName("updateBy", userName, metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

    // 拦截删除SQL 更换为更新方法
    @Bean
    public Interceptor logicDeleteFillInterceptor() {
        return new LogicDeleteFillInterceptor();
    }
}
