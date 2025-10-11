package com.talent.speak.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("chat_group_read")
public class ChatGroupRead {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId; // 群组id
    private Long userId;  // 用户id

    private Date lastReadTime;      // 最后已读时间

    private Date updateTime;// 更新时间
}
