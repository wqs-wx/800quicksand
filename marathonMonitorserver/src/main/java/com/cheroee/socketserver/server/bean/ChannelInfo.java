package com.cheroee.socketserver.server.bean;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.Date;

/**
 * 渠道
 */
@Data
public class ChannelInfo {
    private Channel channel;  //Channel
    private Date registerTime;//注册时间
    private boolean isTokenValidate;//是否校验
}
