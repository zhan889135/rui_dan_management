package com.talent.speak.server;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talent.speak.entity.ChatGroupRead;
import com.talent.speak.entity.ChatMessage;
import com.talent.speak.mapper.ChatGroupReadMapper;
import com.talent.speak.mapper.ChatMessageMapper;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Date;
import java.util.Map;

public class ChatWsHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup allUsers;
    private final Map<String, ChannelGroup> groups;
    private final ChatMessageMapper messageMapper;
    private final ChatGroupReadMapper chatGroupReadMapper; // ✅ 新增

    public ChatWsHandler(ChannelGroup allUsers,
                         Map<String, ChannelGroup> groups,
                         ChatMessageMapper messageMapper,
                         ChatGroupReadMapper chatGroupReadMapper) {
        this.allUsers = allUsers;
        this.groups = groups;
        this.messageMapper = messageMapper;
        this.chatGroupReadMapper = chatGroupReadMapper;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        allUsers.add(ctx.channel()); // 所有连接都加入全局
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(frame.text());
        String type = json.get("type").asText();

        if ("init".equals(type)) {
            // 只绑定 userId，不绑定 groupId
            String userId = json.get("userId").asText();
            ctx.channel().attr(AttributeKey.valueOf("uid")).set(userId);
            // 全局用户列表早在 handlerAdded 里已经加过
        }
        else if ("join".equals(type)) {
            // 加入群
            String groupId = json.get("groupId").asText();
            String userId = json.get("userId").asText();
            ctx.channel().attr(AttributeKey.valueOf("uid")).set(userId);
            ctx.channel().attr(AttributeKey.valueOf("gid")).set(groupId);
            ChannelGroup cg = groups.computeIfAbsent(groupId,
                    id -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
            cg.add(ctx.channel());
        }
        else if ("chat".equals(type)) {
            String groupId = ctx.channel().attr(AttributeKey.valueOf("gid")).get() != null
                    ? ctx.channel().attr(AttributeKey.valueOf("gid")).get().toString()
                    : json.get("groupId").asText();
            String userId = ctx.channel().attr(AttributeKey.valueOf("uid")).get().toString();
            String content = json.get("content").asText();

            Date now = new Date();
            // 插入数据库
            messageMapper.insert(new ChatMessage(null, groupId, userId, content, now));

            // ✅ 这里直接标记当前用户在这个群的最新已读位置
            // ✅ 自己发消息时直接标记已读
            // 先用 LambdaUpdateWrapper 尝试更新
            LambdaUpdateWrapper<ChatGroupRead> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ChatGroupRead::getGroupId, groupId)
                    .eq(ChatGroupRead::getUserId, userId)
                    .set(ChatGroupRead::getLastReadTime, now);

            // update 返回受影响行数
            int rows = chatGroupReadMapper.update(null, wrapper);

            // 如果 rows == 0，说明还没有记录，就插入一条
            if (rows == 0) {
                ChatGroupRead record = new ChatGroupRead();
                record.setGroupId(Long.valueOf(groupId));
                record.setUserId(Long.valueOf(userId));
                record.setLastReadTime(now);
                chatGroupReadMapper.insert(record);
            }
            // ↑ markRead 是你自己封装的更新方法，把该用户在该群的已读时间/已读消息ID更新掉

            // 群发
            ChannelGroup cg = groups.get(groupId);
            if (cg != null) {
                ObjectNode msg = mapper.createObjectNode();
                msg.put("type", "chat");
                msg.put("groupId", groupId);
                msg.put("from", userId);
                msg.put("content", content);
                cg.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(msg)));
            }

            // 给所有在线但不在此群的用户推送“未读提醒”
            for (Channel ch : allUsers) {
                String gid = (String) ch.attr(AttributeKey.valueOf("gid")).get();
                if (gid == null || !gid.equals(groupId)) {
                    ObjectNode unread = mapper.createObjectNode();
                    unread.put("type", "unread");
                    unread.put("groupId", groupId);
                    ch.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(unread)));
                }
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String gid = (String) ctx.channel().attr(AttributeKey.valueOf("gid")).get();
        ChannelGroup cg = groups.get(gid);
        if (cg != null) cg.remove(ctx.channel());
        allUsers.remove(ctx.channel());
    }
}
