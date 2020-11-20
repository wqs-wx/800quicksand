package com.cheroee.socketserver;

import com.cheroee.socketserver.server.ServerChannelInitializer;
import com.cheroee.socketserver.util.PropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import com.cheroee.socketserver.server.bean.ChannelInfo;
import com.cheroee.socketserver.server.consts.CommonConsts;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//websocket长连接示例
public class Server {
    public static void main(String[] args) throws Exception{
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        // 主线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程组
        EventLoopGroup wokerGroup = new NioEventLoopGroup();
        Integer port= Integer.valueOf(PropertiesUtil.getPropertiesValue("monitor.port"));
        cleanInvalidChannel();
        //查询机构服务是否存在，不存在则插入
//        ApiUtil.addServerIfNotExist(orgId,ip,port.toString());
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,wokerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ServerChannelInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(port)).sync();
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            wokerGroup.shutdownGracefully();
        }
        
    }

    /**
     * 清除五分钟连接但没有鉴权的连接
     */
    private static void   cleanInvalidChannel(){
        long timeOutTime=10000*60*5L;//定时时间 连接5分钟并未鉴权的  断开连接
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Map<Channel, ChannelInfo> channelMap=CommonConsts.channelMap;
                for (Channel channel : channelMap.keySet()) {
                    ChannelInfo channelInfo=channelMap.get(channel);
                    if((System.currentTimeMillis()-channelInfo.getRegisterTime().getTime()>timeOutTime)){
                        channel.close();
                        channelMap.remove(channelInfo);
                    }
                }
            }
        };
        Timer timer = new Timer();
        long delay = 0;
        timer.scheduleAtFixedRate(task, delay, timeOutTime);
    }
}