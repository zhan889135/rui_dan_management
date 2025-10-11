package com.talent.speak.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * 聊天消息表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;           // 主键ID

    private String groupId;     // 群ID

    private String fromUser;    // 发送人用户ID

    private String content;     // 消息内容

    private Date sendTime;      // 发送时间
}
