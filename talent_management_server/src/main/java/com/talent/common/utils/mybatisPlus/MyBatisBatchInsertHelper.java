package com.talent.common.utils.mybatisPlus;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper;
import com.talent.common.domain.BaseEntity;
import com.talent.common.utils.SecurityUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description: mybatis通用批量保存新增
 * @author JamesRay
 * @date: 2025/5/29 9:04
 */ 
public class MyBatisBatchInsertHelper {

    /**
     * @description: 批量新增/修改子表
     * @author JamesRay
     * @date 2025/5/29 9:01
     * @param mapper 映射
     * @param list 子表集合
     * @param fieldMap 是否有需要赋值的主表ID  key:"projectId", value:entity.getId()
     * @param deleteIdList 是否有需要删除的子表ID
     */
    public static  <T> void saveOrUpdateBatch(BaseMapper<T> mapper, List<T> list,
                                       Map<String, Object> fieldMap, List<Integer> deleteIdList) {
        // 如果有删除 ID 列表，先处理删除
        if (deleteIdList != null && !deleteIdList.isEmpty()) {
            for (Integer id : deleteIdList) {
                if (id != null) {
                    mapper.deleteById(id);
                }
            }
        }

        if (list == null || list.isEmpty()) return;

        for (T item : list) {
            try {
                // 动态设置额外字段（如 projectId、taskId 等）
                for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
                    Field field = getField(item.getClass(), entry.getKey());
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(item, entry.getValue());
                    }
                }

                // 处理主键 ID 判断逻辑
                Field idField = getField(item.getClass(), "id");
                if (idField != null) {
                    idField.setAccessible(true);
                    Object id = idField.get(item);
                    if (id != null) {
                        updateAllFieldsById(item, mapper);
                    } else {
                        mapper.insert(item);
                    }
                } else {
                    mapper.insert(item); // fallback
                }
            } catch (Exception e) {
                // 可记录日志或抛出业务异常
                throw new RuntimeException("保存或更新失败: " + item.getClass().getSimpleName(), e);
            }
        }
    }


    /**
     * 强制更新实体中所有字段（包括 null 值）
     * @param entity 要更新的实体，必须包含 id 字段
     * @param mapper 对应的 BaseMapper
     * @param <T> 实体类型
     */
    public static <T> void updateAllFieldsById(T entity, BaseMapper<T> mapper) {
        try {
            Class<?> clazz = entity.getClass();

            // 取 ID 值
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (id == null) throw new IllegalArgumentException("ID 字段不能为空");

            TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
            if (tableInfo == null) throw new RuntimeException("未找到实体类的表映射信息");

            UpdateWrapper<T> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);  // 设置主键更新条件

            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                String fieldName = fieldInfo.getProperty();
                // 更新到备注就可以
                if(fieldName.equals("remark")){
                    // 直接用 getter 获取值（假设 entity 有 getRemark 方法）
                    Object value = ((BaseEntity) entity).getRemark(); // 如果 entity 继承了 BaseEntity
                    wrapper.set(fieldInfo.getColumn(), value);
                    break;
                }
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(entity);
                wrapper.set(fieldInfo.getColumn(), value); // 即使为 null 也设置
            }
            wrapper.set("update_by", SecurityUtils.getUsername());
            wrapper.set("update_time", new Date());

            mapper.update(null, wrapper);
        } catch (Exception e) {
            throw new RuntimeException("强制更新失败", e);
        }
    }

    /**
     * @description: 映射获取数据库字段名
     * @author: JamesRay
     * @date: 2025/5/29 9:07
     * @param clazz 实体类
     * @param fieldName 属性名
     * @return Field
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
