package com.talent.common.utils;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class PluginUtil {

    /**
     * 获取目标对象的真实代理（解开多层包装）
     */
    public static Object realTarget(Object target) {
        MetaObject metaObject = SystemMetaObject.forObject(target);
        while (metaObject.hasGetter("h")) {
            target = metaObject.getValue("h");
            metaObject = SystemMetaObject.forObject(target);
        }
        while (metaObject.hasGetter("target")) {
            target = metaObject.getValue("target");
            metaObject = SystemMetaObject.forObject(target);
        }
        return target;
    }

    /**
     * 动态设置 SQL 内容
     */
    public static void setSql(StatementHandler handler, String sql) {
        MetaObject metaObject = SystemMetaObject.forObject(handler);
        metaObject.setValue("delegate.boundSql.sql", sql);
    }
}
