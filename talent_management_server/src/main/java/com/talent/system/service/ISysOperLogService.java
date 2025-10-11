package com.talent.system.service;


import com.talent.system.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志 服务层
 * 
 * @author JamesRay
 */
public interface ISysOperLogService
{
    /**
     * 新增操作日志
     */
    void insertOperlog(SysOperLog operLog);

    /**
     * 查询系统操作日志集合
     */
    List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     * 批量删除系统操作日志
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 查询操作日志详细
     */
    SysOperLog selectOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
