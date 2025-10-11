package com.talent.common.utils.mybatisPlus;

import com.talent.common.utils.PluginUtil;
import com.talent.common.utils.SecurityUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class LogicDeleteFillInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) PluginUtil.realTarget(invocation.getTarget());
        String sql = handler.getBoundSql().getSql();

        // 判断是否为逻辑删除语句：包含 del_flag = 'Y'（忽略大小写和空格）
        if (sql.matches("(?is).*set\\s+.*del_flag\\s*=\\s*'Y'.*")) {
            String username = SecurityUtils.getUsername();
            // 在 set 后追加 update_by 和 update_time（保留原 sql 其他字段顺序和大小写）
            String insertSegment = String.format("update_by = '%s', update_time = now(), ", username);
            String modifiedSql = sql.replaceFirst("(?i)set\\s+", "set " + insertSegment);
            PluginUtil.setSql(handler, modifiedSql);
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}

