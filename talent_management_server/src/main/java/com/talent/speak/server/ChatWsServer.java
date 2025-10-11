package com.talent.speak.server;

import com.talent.speak.mapper.ChatGroupReadMapper;
import com.talent.speak.mapper.ChatMessageMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天服务
 */
@Component
@RequiredArgsConstructor
public class ChatWsServer implements CommandLineRunner {
    private final ChatMessageMapper messageMapper;

    // 内存存储群ID -> ChannelGroup，用于群发消息
    private final Map<String, ChannelGroup> groups = new ConcurrentHashMap<>();

    // 内存存储所有在线用户，不区分群
    private final ChannelGroup allUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final ChatGroupReadMapper chatGroupReadMapper; // ✅ 注入未读消息

    @Override
    public void run(String... args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler("/ws"))
                                // 👇把allUsers传给handler
                                .addLast(new ChatWsHandler(allUsers, groups, messageMapper, chatGroupReadMapper)); // ✅ 现在能传参
                    }
                })
                .bind(8096).sync();
    }
}
