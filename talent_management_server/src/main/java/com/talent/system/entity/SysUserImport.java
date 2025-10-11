package com.talent.system.entity;

import com.talent.system.config.annotation.Excel;
import lombok.Data;

/**
 * 用户导入
 */
@Data
public class SysUserImport {

    /** 部门ID */
    @Excel(name = "部门编号", type = Excel.Type.IMPORT)
    private Long deptId;

    /** 用户昵称 */
    @Excel(name = "用户昵称", type = Excel.Type.IMPORT)
    private String nickName;

    /** 登录账号 */
    @Excel(name = "登录账号", type = Excel.Type.IMPORT)
    private String userName;

    /** 密码 */
    @Excel(name = "用户密码", type = Excel.Type.IMPORT)
    private String password;

    /** 手机号码 */
    @Excel(name = "手机号码", cellType = Excel.ColumnType.TEXT, type = Excel.Type.IMPORT)
    private String phoneNumber;
}
