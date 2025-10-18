package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.talent.system.config.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * 面试反馈
 */
@Data
public class FeedbackDept3Excel{

    /** 主键ID */
    @TableId
    @Excel(name = "ID", width = 10)
    private Integer id;

    /** 面试点位 */
    @Excel(name = "面试点位", width = 35)
    private String locationName;

    /** 供应商名称 */
    @Excel(name = "归属供应商", width = 20)
    private String deptName;

    /** 姓名 */
    @Excel(name = "姓名", width = 10)
    private String name;

    /** 性别 */
    @Excel(name = "性别", width = 10)
    private String sex;

    /** 电话 */
    @Excel(name = "电话", width = 18)
    private String phone;

    /** 年龄 */
    @Excel(name = "年龄", width = 10)
    private String age;

    /** 学历 */
    @Excel(name = "学历", width = 10)
    private String education;

    /** 面试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "面试日期", width = 15, dateFormat = "yyyy-MM-dd")
    private Date interviewDate;

    /** 反馈原因2 */
    @Excel(name = "反馈原因", width = 50)
    private String reason2;

    /** 硬性条件2Y-是；N-否 */
    @Excel(name = "硬性条件", readConverterExp = "Y=是,N=否", width = 10)
    private String hardRequirements2;

    /** 是否计费2Y-是；N-否 */
    @Excel(name = "是否计费", readConverterExp = "Y=是,N=否", width = 10)
    private String isBilling2;
}

