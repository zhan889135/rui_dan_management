package com.talent.system.service.login;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysUser;
import com.talent.system.entity.login.LoginUser;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.mapper.SysUserMapper;
import com.talent.common.utils.MessageUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户验证处理
 *
 * @author JamesRay
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserName, username)
                        .last("LIMIT 1")
        );
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (Constants.IS_DEL_Y.equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (Constants.RET_CODE_1.equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }
        else if (Constants.RET_CODE_2.equals(user.getStatus()))
        {
            log.info("登录用户：{} 未审核通过.", username);
            throw new ServiceException(MessageUtils.message("user.review.blocked"));
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        if(StringUtils.isNotNull(user.getDeptId())){
            SysDept sysDept = deptMapper.selectById(user.getDeptId());
            user.setDeptName(sysDept.getDeptName());
        }
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));

    }
}
