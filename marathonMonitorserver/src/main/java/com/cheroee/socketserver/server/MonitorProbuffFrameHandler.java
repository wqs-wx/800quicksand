package com.cheroee.socketserver.server;


import com.cheroee.socketserver.probuff.MonitorExchangeDataProto;
import com.cheroee.socketserver.server.bean.ChannelInfo;
import com.cheroee.socketserver.server.consts.CommonConsts;
import com.cheroee.socketserver.server.process.MessageProcessor;
import com.cheroee.socketserver.util.PropertiesUtil;
import dispatcher.ChDispatcherManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
@Slf4j
public class MonitorProbuffFrameHandler extends SimpleChannelInboundHandler<MonitorExchangeDataProto.MonitorExchangeData> {
    MessageProcessor messageProcessor=new MessageProcessor();
    Integer port= Integer.valueOf(PropertiesUtil.getPropertiesValue("monitor.port"));
    String ip=PropertiesUtil.getPropertiesValue("monitor.ip");
    String orgId=PropertiesUtil.getPropertiesValue("monitor.orgId");
    //读到客户端的内容并且向客户端去写内容
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MonitorExchangeDataProto.MonitorExchangeData msg) throws Exception {
        messageProcessor.processMsg(ctx.channel(),msg);//处理消息
    }
    //每个channel都有一个唯一的id值
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //打印出channel唯一值，asLongText方法是channel的id的全名
//        Integer maxOnline=ApiUtil.getMaxOnline(orgId);
//        Integer currentOnline=ApiUtil.getAllOneLineUserNum(orgId);
//        if(currentOnline>=maxOnline){//不允许连接
//            ctx.channel().close();//关闭连接
//        }else{
//            ApiUtil.addUserOnlineNum(orgId,ip,port);//用户数量加一
//        }
        //ApiUtil.addUserOnlineNum(orgId,ip,port);//用户数量加一
        ChannelInfo  channelInfo=new ChannelInfo();
        channelInfo.setChannel(ctx.channel());
        channelInfo.setRegisterTime(new Date());
        channelInfo.setTokenValidate(false);
        CommonConsts.channelMap.put(ctx.channel(),channelInfo);
        log.info("handlerAdded：" + ctx.channel().id().asLongText());

    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("链接已断开-----------------：" + ctx.channel().id().asLongText());
        ChDispatcherManager.getInstance().unRegisterPublisher(ctx.channel());//删除在线用户
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常发生");
        ChDispatcherManager.getInstance().unRegisterPublisher(ctx.channel());//删除在线用户
        ctx.close();
    }

}
