/**
 * Copyright (C), 2019-2019, XXX有限公司
 * FileName: CommonConsts
 * Author:   Administrator
 * Date:     2019/5/14/014 18:58
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.cheroee.socketserver.server.consts;

import com.cheroee.socketserver.server.bean.ChannelInfo;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/5/14/014
 * @since 1.0.0
 */
public class CommonConsts {
    //当前连接信息，认证完成后清除
public static Map<Channel, ChannelInfo> channelMap=new ConcurrentHashMap() ;
}
