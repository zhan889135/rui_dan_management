package com.talent.system.service;


import com.talent.system.entity.SysLogininfor;

import java.util.List;

/**
 * 系统访问日志情况信息 服务层
 * 
 * @author JamesRay
 */
public interface ISysLogininforService
{
    /**
     * 新增系统登录日志
     */
    void insertLogininfor(SysLogininfor logininfor);

    /**
     * 查询系统登录日志集合
     */
    List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量删除系统登录日志
     */
    int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清空系统登录日志
     */
    void cleanLogininfor();
}
