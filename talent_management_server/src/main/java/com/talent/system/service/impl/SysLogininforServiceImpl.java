package com.talent.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.system.entity.SysLogininfor;
import com.talent.system.mapper.SysLogininforMapper;
import com.talent.system.service.ISysLogininforService;
import com.talent.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统访问日志情况信息 服务层处理
 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService
{

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * 新增系统登录日志
     * 
     * @param logininfor 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininfor logininfor)
    {
        logininforMapper.insert(logininfor);
    }

    /**
     * 查询系统登录日志集合
     * 
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)
    {
        Map<String, Object> params = logininfor.getParams();
        Object beginTime = (params != null) ? params.get("beginTime") : null;
        Object endTime = (params != null) ? params.get("endTime") : null;

        List<SysLogininfor> sysLogininforList = logininforMapper.selectList(
                new LambdaQueryWrapper<SysLogininfor>()
                        .like(StringUtils.isNotEmpty(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
                        .like(StringUtils.isNotEmpty(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName())
                        .eq(StringUtils.isNotNull(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
                        .ge(beginTime != null, SysLogininfor::getLoginTime, beginTime)
                        .le(endTime != null, SysLogininfor::getLoginTime, endTime)
                        .orderByDesc(SysLogininfor:: getLoginTime)
        );
        return sysLogininforList;
    }

    /**
     * 批量删除系统登录日志
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteBatchIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor()
    {
        logininforMapper.delete(new LambdaQueryWrapper<>());
    }
}
