package com.talent.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.system.entity.SysOperLog;
import com.talent.system.mapper.SysOperLogMapper;
import com.talent.common.utils.StringUtils;
import com.talent.system.service.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 操作日志 服务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService
{
    @Autowired
    private SysOperLogMapper operLogMapper;

    /**
     * 新增操作日志
     * 
     * @param operLog 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLog operLog)
    {
        operLogMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     * 
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog)
    {
        Map<String, Object> params = operLog.getParams();
        Object beginTime = (params != null) ? params.get("beginTime") : null;
        Object endTime = (params != null) ? params.get("endTime") : null;

        List<SysOperLog> sysOperLogList = operLogMapper.selectList(
                new LambdaQueryWrapper<SysOperLog>()
                        .like(StringUtils.isNotEmpty(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
                        .like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
                        .like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
                        .eq(StringUtils.isNotNull(operLog.getBusinessType()), SysOperLog::getBusinessType, operLog.getBusinessType())
                        .eq(StringUtils.isNotNull(operLog.getStatus()), SysOperLog::getStatus, operLog.getStatus())
                        .ge(beginTime != null, SysOperLog::getOperTime, beginTime)
                        .le(endTime != null, SysOperLog::getOperTime, endTime)
                        .orderByDesc(SysOperLog:: getOperTime)
        );
        return sysOperLogList;
    }

    /**
     * 批量删除系统操作日志
     * 
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds)
    {
        return operLogMapper.deleteBatchIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     * 
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLog selectOperLogById(Long operId)
    {
        return operLogMapper.selectById(operId);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog()
    {
        operLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
