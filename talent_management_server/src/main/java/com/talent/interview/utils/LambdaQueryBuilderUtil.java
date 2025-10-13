package com.talent.interview.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.ServletUtils;
import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.entity.Report;
import com.talent.interview.entity.Requirement;


/**
 * 通用 LambdaQueryWrapper 构建工具
 */
public class LambdaQueryBuilderUtil {

    /**
     * 面试点位
     */
    public static LambdaQueryWrapper<Location> buildLocationQueryWrapper(Location entity) {
        LambdaQueryWrapper<Location> wrapper;

        // 部门等级，如果是最高级，就不用拼接数据权限了，直接显示全部
        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();
        if(null != deptLevel && Constants.RET_CODE_1_NUM == deptLevel){
            wrapper = new LambdaQueryWrapper<>();
        }else{
            wrapper = DeptPermissionUtil.buildDeptScopeWrapper(Location::getDeptId);
        }

        if (entity != null) {
            // 精确匹配：供应商ID
            wrapper.eq(entity.getDeptId() != null, Location::getDeptId, entity.getDeptId());
            // 模糊查询：供应商名称
            wrapper.like(StringUtils.isNotBlank(entity.getDeptName()), Location::getDeptName, entity.getDeptName());
            // 模糊查询：公司名称
            wrapper.like(StringUtils.isNotBlank(entity.getCompanyName()), Location::getCompanyName, entity.getCompanyName());
            // 模糊查询：点位名称
            wrapper.like(StringUtils.isNotBlank(entity.getName()), Location::getName, entity.getName());
            // 精确匹配：招聘状态
            wrapper.eq(StringUtils.isNotBlank(entity.getStatus()), Location::getStatus, entity.getStatus());

            // 日期范围查询
            if (entity.getDateRange() != null && entity.getDateRange().length == 2) {
                String beginDate = entity.getDateRange()[0];
                String endDate = entity.getDateRange()[1];
                wrapper.between(Location::getDate, beginDate, endDate);
            }
        }

        wrapper.orderByDesc(Location::getStatus);
        wrapper.orderByDesc(Location::getCreateTime);
        return wrapper;
    }

    /**
     * 面试报备
     */
    public static LambdaQueryWrapper<Report> buildReportQueryWrapper(Report entity) {

        LambdaQueryWrapper<Report> wrapper;

        // 部门等级，如果是最高级，就不用拼接数据权限了，直接显示全部
        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();
        if(null != deptLevel && Constants.RET_CODE_1_NUM == deptLevel){
            wrapper = new LambdaQueryWrapper<>();
        }else{
            wrapper = DeptPermissionUtil.buildSubDeptScopeWrapper(Report::getSubDeptId);
        }

        if (entity != null) {
            // 精确匹配：供应商ID
            wrapper.eq(entity.getSubDeptId() != null, Report::getSubDeptId, entity.getSubDeptId());
            // 模糊查询：子部门
            wrapper.like(StringUtils.isNotBlank(entity.getSubDeptName()), Report::getSubDeptName, entity.getSubDeptName());
            // 模糊查询：供应商名称
            wrapper.like(StringUtils.isNotBlank(entity.getDeptName()), Report::getDeptName, entity.getDeptName());
            // 精确匹配：面试点位ID
            wrapper.eq(entity.getLocationId() != null, Report::getLocationId, entity.getLocationId());
            // 模糊查询：面试点位名称
            wrapper.like(StringUtils.isNotBlank(entity.getLocationName()), Report::getLocationName, entity.getLocationName());
            // 模糊查询：姓名
            wrapper.like(StringUtils.isNotBlank(entity.getName()), Report::getName, entity.getName());
            // 精确匹配：性别
            wrapper.eq(StringUtils.isNotBlank(entity.getSex()), Report::getSex, entity.getSex());
            // 模糊查询：联系电话
            wrapper.like(StringUtils.isNotBlank(entity.getPhone()), Report::getPhone, entity.getPhone());
            // 精确匹配：学历
            wrapper.eq(StringUtils.isNotBlank(entity.getEducation()), Report::getEducation, entity.getEducation());
            // 精确匹配：招聘人
            wrapper.eq(StringUtils.isNotBlank(entity.getCreateBy()), Report::getCreateBy, entity.getCreateBy());
            // 模糊查询：创建人名称
            wrapper.like(StringUtils.isNotBlank(entity.getCreateName()), Report::getCreateName, entity.getCreateName());

            // 面试日期（支持精确、区间查询）默认查询第二天的
            if (entity.getInterviewDate() != null) {
                wrapper.eq(Report::getInterviewDate, entity.getInterviewDate());
            }

            // 日期范围查询
            String[] range = entity.getDateRange();
            if (range != null && range.length == 2) {
                wrapper.between(Report::getInterviewDate, range[0], range[1]);
            }
        }

        wrapper.orderByDesc(Report::getCreateTime);
        return wrapper;
    }

    /**
     * 面试反馈
     */
    public static LambdaQueryWrapper<Feedback> buildFeedbackQueryWrapper(Feedback entity) {

        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();

        if (entity != null) {
            // 模糊查询：面试点位名称
            wrapper.like(StringUtils.isNotBlank(entity.getLocationName()), Feedback::getLocationName, entity.getLocationName());
            // 模糊查询：供应商名称
            wrapper.like(StringUtils.isNotBlank(entity.getDeptName()), Feedback::getDeptName, entity.getDeptName());
            // 模糊查询：候选人姓名
            wrapper.like(StringUtils.isNotBlank(entity.getName()), Feedback::getName, entity.getName());
            // 精确匹配：性别
            wrapper.eq(StringUtils.isNotBlank(entity.getSex()), Feedback::getSex, entity.getSex());
            // 精确匹配：学历
            wrapper.eq(StringUtils.isNotBlank(entity.getEducation()), Feedback::getEducation, entity.getEducation());
            // 精确匹配：硬性条件Y-是；N-否
            wrapper.eq(StringUtils.isNotBlank(entity.getHardRequirements()), Feedback::getHardRequirements, entity.getHardRequirements());
            // 精确匹配：是否计费Y-是；N-否
            wrapper.eq(StringUtils.isNotBlank(entity.getIsBilling()), Feedback::getIsBilling, entity.getIsBilling());
            // 精确匹配：硬性条件2Y-是；N-否
            wrapper.eq(StringUtils.isNotBlank(entity.getHardRequirements2()), Feedback::getHardRequirements2, entity.getHardRequirements2());
            // 精确匹配：是否计费2Y-是；N-否
            wrapper.eq(StringUtils.isNotBlank(entity.getIsBilling2()), Feedback::getIsBilling2, entity.getIsBilling2());

            // 分为三个列表，deptLevel：1总部，能看所有的，2供应商，能看总部推送的，3员工，能看供应商推送的
            if(null != entity.getDeptLevel()){
                // 1总部，能看所有的
                if(Constants.RET_CODE_1_NUM == entity.getDeptLevel()){

                }else
                    // 2供应商，能看总部推送的
                    if(Constants.RET_CODE_2_NUM == entity.getDeptLevel()){
                        wrapper.eq(Feedback::getDeptId, SecurityUtils.getDeptId());
                        wrapper.isNotNull(Feedback::getLevel1Person);
                        wrapper.isNotNull(Feedback::getLevel1Time);
                    }else
                    // 3员工，能看供应商推送的
                    if(Constants.RET_CODE_3_NUM == entity.getDeptLevel()){
                        wrapper.eq(Feedback::getCreateBy, SecurityUtils.getUsername());
                        wrapper.isNotNull(Feedback::getLevel2Person);
                        wrapper.isNotNull(Feedback::getLevel2Time);
                    }
            }

            // 日期范围查询
            String[] range = entity.getDateRange();
            if (range != null && range.length == 2) {
                wrapper.between(Feedback::getInterviewDate, range[0], range[1]);
            }
        }

        wrapper.orderByDesc(Feedback::getCreateTime);
        return wrapper;
    }

    /**
     * 招聘需求
     */
    public static LambdaQueryWrapper<Requirement> buildRequirementQueryWrapper(Requirement entity) {
        LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<>();

        if (entity != null) {
            // 模糊查询：标题
            wrapper.like(StringUtils.isNotBlank(entity.getTitle()), Requirement::getTitle, entity.getTitle());
            // ✅ 动态排序
            if ("desc".equalsIgnoreCase(entity.getOrderDirection())) {
                wrapper.orderByDesc(Requirement::getOrderNum);
            } else {
                // 默认 asc
                wrapper.orderByAsc(Requirement::getOrderNum);
            }
        }
        return wrapper;
    }
}
