package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 */
@Data
@TableName("sys_user_role")
public class SysUserRole
{
    /** 用户ID */
    private Long userId;
    
    /** 角色ID */
    private Long roleId;
}
