package com.talent.interview.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.talent.common.constant.Constants;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.bean.SpringContextUtils;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.login.LoginUser;
import com.talent.system.mapper.SysDeptMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据权限工具类
 * 用于处理基于部门的查询权限限制。
 * 所有业务表中都有 deptId 字段，表示所属部门。
 * 通过此工具可以获取当前登录人所在部门及其所有子部门 ID，
 * 并统一在查询时拼接数据权限条件。
 */
@Slf4j
public class DeptPermissionUtil {

    /** 注入部门 Mapper（通过 Spring 上下文工具类获取） */
    public static final SysDeptMapper deptMapper = SpringContextUtils.getBean(SysDeptMapper.class);

    /**
     * 获取当前部门及其所有子部门 ID
     *
     * @param deptId 部门 ID
     * @return 当前部门 + 子部门 ID 列表
     */
    public static List<Long> getDeptAndChildrenIds(Long deptId) {
        // 先加自己
        List<Long> ids = new ArrayList<>();
        ids.add(deptId);

        // 查询所有子部门 ID（仅状态正常的部门）
        List<SysDept> children = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getStatus, Constants.RET_CODE_0)
                        .apply("FIND_IN_SET({0}, ancestors)", deptId)  // ancestors 包含当前部门 ID
                        .select(SysDept::getDeptId)   // 只查 dept_id 字段，避免拉全表
        );

        if (children != null && !children.isEmpty()) {
            ids.addAll(children.stream().map(SysDept::getDeptId).toList());
        }

        return ids;
    }

    /**
     * 构建带有数据权限的查询条件
     * 用法示例：
     * LambdaQueryWrapper<Feedback> wrapper =
     *     DeptPermissionUtil.buildDeptScopeWrapper(Feedback::getDeptId);
     * List<Feedback> list = feedbackMapper.selectList(wrapper);
     *
     * @param column 实体类中 deptId 字段的 Lambda 表达式（例如 Feedback::getDeptId）
     * @param <T>    表对应的实体类型
     * @return 带有 deptId IN (当前部门 + 子部门) 条件的 LambdaQueryWrapper
     */
    public static <T> LambdaQueryWrapper<T> buildDeptScopeWrapper(SFunction<T, ?> column) {
        // 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptId = loginUser.getDeptId();

        // 获取当前部门及子部门 ID
        List<Long> deptIds = getDeptAndChildrenIds(deptId);

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();

        if (deptIds != null && !deptIds.isEmpty()) {
            // ✅ 直接固定字段名为 dept_id，不再动态解析
            String columnName = "dept_id";

            // 构建兼容 IN + FIND_IN_SET 查询条件
            StringBuilder condition = new StringBuilder("(");
            condition.append(columnName)
                    .append(" IN (")
                    .append(deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")))
                    .append(")");

            for (Long id : deptIds) {
                condition.append(" OR FIND_IN_SET(")
                        .append(id)
                        .append(", ")
                        .append(columnName)
                        .append(")");
            }

            condition.append(")");
            wrapper.apply(condition.toString());
        }

        return wrapper;
    }

    /**
     * 构建带有数据权限的查询条件
     * 用法示例：
     * LambdaQueryWrapper<Feedback> wrapper =
     *     DeptPermissionUtil.buildDeptScopeWrapper(Feedback::getDeptId);
     * List<Feedback> list = feedbackMapper.selectList(wrapper);
     *
     * @param column 实体类中 deptId 字段的 Lambda 表达式（例如 Feedback::getDeptId）
     * @param <T>    表对应的实体类型
     * @return 带有 deptId IN (当前部门 + 子部门) 条件的 LambdaQueryWrapper
     */
    public static <T> LambdaQueryWrapper<T> buildSubDeptScopeWrapper(SFunction<T, ?> column) {
        // 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptId = loginUser.getDeptId();

        // 获取当前部门及子部门 ID
        List<Long> deptIds = getDeptAndChildrenIds(deptId);

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();

        if (deptIds != null && !deptIds.isEmpty()) {
            // ✅ 直接固定字段名为 sub_dept_id，不再动态解析
            String columnName = "sub_dept_id";

            // 构建兼容 IN + FIND_IN_SET 查询条件
            StringBuilder condition = new StringBuilder("(");
            condition.append(columnName)
                    .append(" IN (")
                    .append(deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")))
                    .append(")");

            for (Long id : deptIds) {
                condition.append(" OR FIND_IN_SET(")
                        .append(id)
                        .append(", ")
                        .append(columnName)
                        .append(")");
            }

            condition.append(")");
            wrapper.apply(condition.toString());
        }

        return wrapper;
    }

    // ========================
    // 内部静态类：用于返回二级部门信息
    // ========================
    @Data
    public static class DeptInfo {
        private Integer deptId;
        private String deptName;

        public DeptInfo() {}

        public DeptInfo(Integer deptId, String deptName) {
            this.deptId = deptId;
            this.deptName = deptName;
        }
    }

    // ========================
    // 公共方法：根据子部门ID查找二级部门
    // ========================
    /**
     * 根据子部门ID查找其对应的二级部门（deptLevel = 2）
     *
     * @param subDeptId   子部门ID（可能为三级或二级）
     * @return DeptInfo 包含二级部门的 id 和 name，未找到返回 null
     */
    public static DeptInfo findSecondLevelDept(Integer subDeptId, String subDeptName) {
        List<SysDept> sysDeptList = deptMapper.selectList(null);

        if (subDeptId == null || sysDeptList == null || sysDeptList.isEmpty()) {
            return new DeptInfo();
        }

        // 找到 subDeptId 对应的部门
        SysDept subDept = sysDeptList.stream()
                .filter(d -> d.getDeptId().equals(Long.valueOf(subDeptId)))
                .findFirst()
                .orElse(null);

        if (subDept != null) {
            if (subDept.getDeptLevel() != null && subDept.getDeptLevel() != Constants.RET_CODE_3_NUM) {
                // 不是三级 → 直接用它自己
                return new DeptInfo(subDeptId, subDeptName);
            } else {
                // 是三级 → 向上找二级部门
                SysDept current = subDept;
                while (current != null && current.getDeptLevel() != null && current.getDeptLevel() != Constants.RET_CODE_2_NUM) {
                    Long parentId = current.getParentId();
                    if (parentId == null) break;
                    current = sysDeptList.stream()
                            .filter(d -> d.getDeptId().equals(parentId))
                            .findFirst()
                            .orElse(null);
                }
                // 找到二级部门
                if (current != null && current.getDeptLevel() != null && current.getDeptLevel() == Constants.RET_CODE_2_NUM) {
                    return new DeptInfo(current.getDeptId().intValue(), current.getDeptName());
                }
            }
        }

        return new DeptInfo();
    }
}
