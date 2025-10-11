package com.talent.speak.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 群表实体类
 */
@Data
@TableName("chat_group")
public class ChatGroup {

    @TableId(type = IdType.AUTO)
    private Long id;             // 群ID（主键）

    private String name;         // 群名称

    private String userIds;      // 群成员：逗号分隔 "u1,u2,u3"

    private Date createTime;     // 创建时间

    @TableField(exist = false)
    private List<Long> userIdParam; // 用户集合参数,前台添加用户，集合

    // ChatGroup.java
    @TableField(exist = false)
    private Integer unreadCount; // 非持久化：后端计算后回填，群组未读数量
}


