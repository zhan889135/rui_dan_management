package com.talent.system.service.impl;

import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysUserOnline;
import com.talent.system.entity.login.LoginUser;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.service.ISysUserOnlineService;
import com.talent.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 在线用户 服务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService
{
    @Autowired
    private SysDeptMapper deptMapper;
    /**
     * 通过登录地址查询信息
     * 
     * @param ipaddr 登录地址
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user)
    {
        if (StringUtils.equals(ipaddr, user.getIpaddr()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过用户名称查询信息
     * 
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user)
    {
        if (StringUtils.equals(userName, user.getUsername()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过登录地址/用户名称查询信息
     * 
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user)
    {
        if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 设置在线用户信息
     * 
     * @param user 用户信息
     * @return 在线用户
     */
    @Override
    public SysUserOnline loginUserToUserOnline(LoginUser user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUser()))
        {
            return null;
        }
        SysUserOnline sysUserOnline = new SysUserOnline();
        sysUserOnline.setTokenId(user.getToken());
        sysUserOnline.setUserName(user.getUsername());
        sysUserOnline.setIpaddr(user.getIpaddr());
        sysUserOnline.setLoginLocation(user.getLoginLocation());
        sysUserOnline.setBrowser(user.getBrowser());
        sysUserOnline.setOs(user.getOs());
        sysUserOnline.setLoginTime(user.getLoginTime());
        if (StringUtils.isNotNull(user.getDeptId())) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null && StringUtils.isNotEmpty(dept.getDeptName())) {
                sysUserOnline.setDeptName(dept.getDeptName());
            }
        }

        return sysUserOnline;
    }
}
