package com.cheroee.socketserver.server.process;

import com.cheroee.socketserver.probuff.MonitorExchangeDataProto;
import com.cheroee.socketserver.server.bean.PublisherRequest;
import com.cheroee.socketserver.server.bean.RedisMarathonUserOnline;
import com.cheroee.socketserver.server.bean.SubscriberRequest;
import com.cheroee.socketserver.server.consts.CommonConsts;
import com.cheroee.socketserver.util.GsonUtil;
import com.cheroee.socketserver.util.JedisUtil;
import dispatcher.ChDispatcherManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息处理核心类
 */
@Slf4j
public class MessageProcessor {

    public void processMsg(Channel channel, MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {
        int dataType = monitorExchangeData.getDataTypeValue();
        if (dataType==MonitorExchangeDataProto.DateType.PATIENT_AUTH_VALUE) {//APP端分享鉴
            publisherValidateToken(channel,monitorExchangeData);
        }else if(dataType==MonitorExchangeDataProto.DateType.DOCTOR_AUTH_VALUE){//医生端查看鉴权
            subscriberValidateToken(channel,monitorExchangeData.getSecretKey());//医生订阅患者发送APP
        }else if(dataType==MonitorExchangeDataProto.DateType.PATIENT_ONLINE_VALUE){//测量者上线下线信息
            handlerPatientOnlineInfo(monitorExchangeData.getPatientOnlineInfo());
        }else if(dataType==MonitorExchangeDataProto.DateType.SUBSCRIPTION_TYPE_VALUE){//医生订阅患者数据
            sendSubscribeToPatient(channel,monitorExchangeData);
        }else if(dataType==MonitorExchangeDataProto.DateType.AVG_HEART_RATE_VALUE){//平均心率
            log.info("收到平均心率信息-------------------");
            setUserAvgHeartRate(monitorExchangeData.getUserInfoId(),monitorExchangeData.getHeartRate().getHeartRate());//修改平均心率
        }else if(dataType==MonitorExchangeDataProto.DateType.LONGITUDE_LATITUDE_VALUE){//经度纬度
            log.info("收到经纬度信息-------------------");
            setUserLocation(monitorExchangeData.getUserInfoId(),monitorExchangeData.getLongitudeLatitudeInfo());//经度纬度
        }else{//转发监测数据
            dispatchData(monitorExchangeData);
        }

    }

    /**
     * 测量者在线状态处理
     * @param mtPatientOnlineInfo
     */
    private void  handlerPatientOnlineInfo(MonitorExchangeDataProto.MtPatientOnlineInfo mtPatientOnlineInfo){
       if(mtPatientOnlineInfo.getStatus()==1) {//上线
           setUserOneLine(mtPatientOnlineInfo,0);
       }else{//离线
           deleteUserOneLine(mtPatientOnlineInfo);
       }
    }
    /**
     * 设置
     */
    public  void setUserLocation(String userInfoId,MonitorExchangeDataProto.LongitudeLatitudeInfo  longitudeLatitudeInfo) {
        String userOnlines = JedisUtil.getValue("marathonUserOnline_");
        List <RedisMarathonUserOnline> onlineUsersList=new ArrayList <>();
        if(userOnlines!=null && !"".equals(userOnlines)){
            onlineUsersList= GsonUtil.gsonToList(userOnlines, RedisMarathonUserOnline.class);
            for(RedisMarathonUserOnline user:onlineUsersList){
                if(user.getUserInfoId().equals(userInfoId)){
                    user.setLatitude(longitudeLatitudeInfo.getLatitude());
                    user.setLongitude(longitudeLatitudeInfo.getLongitude());
                }
            }
        }
        userOnlines=  GsonUtil.listToGson(onlineUsersList);
        JedisUtil.setValue("marathonUserOnline_",userOnlines);//设置完成后重新设置回redis
    }
    /**
     * 设置
     */
    public  void setUserAvgHeartRate(String userInfoId,Integer heartRate) {
        String userOnlines = JedisUtil.getValue("marathonUserOnline_");
        List <RedisMarathonUserOnline> onlineUsersList=new ArrayList <>();
        if(userOnlines!=null && !"".equals(userOnlines)){
            onlineUsersList= GsonUtil.gsonToList(userOnlines, RedisMarathonUserOnline.class);
            for(RedisMarathonUserOnline user:onlineUsersList){
                if(user.getUserInfoId().equals(userInfoId)){
                    user.setHeartRate(heartRate);
                }
            }
        }
        userOnlines=  GsonUtil.listToGson(onlineUsersList);
        JedisUtil.setValue("marathonUserOnline_",userOnlines);//设置完成后重新设置回redis
    }
    /**
     * 设置
     */
    public  void setUserOneLine(MonitorExchangeDataProto.MtPatientOnlineInfo mtPatientOnlineInfo,Integer heartRate) {
        RedisMarathonUserOnline redisUserOnline=new RedisMarathonUserOnline();
        redisUserOnline.setUsername(mtPatientOnlineInfo.getUserInfoName());
        redisUserOnline.setUserInfoId(mtPatientOnlineInfo.getUserInfoId());
        redisUserOnline.setUserRunCode(mtPatientOnlineInfo.getUserRunCode());//跑者编号
        redisUserOnline.setHeartRate(heartRate);//平均心率
        String userOnlines = JedisUtil.getValue("marathonUserOnline_");
        boolean isExists=false;
        List <RedisMarathonUserOnline> onlineUsersList=new ArrayList <>();
        if(userOnlines!=null && !"".equals(userOnlines)){
            onlineUsersList= GsonUtil.gsonToList(userOnlines, RedisMarathonUserOnline.class);
            for(RedisMarathonUserOnline user:onlineUsersList){
                if(user.getUserInfoId().equals(redisUserOnline.getUserInfoId())){
                    isExists=true;
                }
            }
        }
        if(isExists==false){
            onlineUsersList.add(redisUserOnline);
        }
        userOnlines=  GsonUtil.listToGson(onlineUsersList);
        JedisUtil.setValue("marathonUserOnline_",userOnlines);//设置完成后重新设置回redis
    }
    /**
     * 删除缓存
     */
    public void deleteUserOneLine(MonitorExchangeDataProto.MtPatientOnlineInfo mtPatientOnlineInfo) {
        Jedis jedis=JedisUtil.getJedis();
        try {
            String  usersOnlineList= JedisUtil.getValue("marathonUserOnline_");
            if(usersOnlineList!=null && !"".equals(usersOnlineList)){
                List<RedisMarathonUserOnline> onlineUsersList= GsonUtil.gsonToList(usersOnlineList, RedisMarathonUserOnline.class);
                List<RedisMarathonUserOnline> onlineUsersListNew=new ArrayList<>();
                if(onlineUsersList!=null && onlineUsersList.size()>0){
                    for(RedisMarathonUserOnline user:onlineUsersList){
                        if(!(user.getUserInfoId().equals(mtPatientOnlineInfo.getUserInfoId()))){
                            onlineUsersListNew.add(user);
                        }
                    }
                    usersOnlineList=  GsonUtil.listToGson(onlineUsersListNew);
                    JedisUtil.setValue("marathonUserOnline_",usersOnlineList);
                }
            }
        }finally {
            JedisUtil.close(jedis);
        }


    }
    /**
     * 发布数据token鉴权
     * @param channel
     * @param monitorExchangeData
     */
    private  void publisherValidateToken(Channel channel,MonitorExchangeDataProto.MonitorExchangeData  monitorExchangeData){
//          Boolean  result= ApiUtil.checkToken(monitorExchangeData.getSecretKey());
//
//          if(result){//鉴权失败  发送鉴权失败信息 并断开连接
//              channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(false,"2")));
//              channel.close();//关闭连接
//          }else{//鉴权成功
//              PublisherRequest request =new PublisherRequest();
//              request.setToken(monitorExchangeData.getSecretKey());
//              request.setUserInfoId(monitorExchangeData.getUserInfoId());
//              ChDispatcherManager.getInstance().registerPublisher(request, channel);
//              channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(true,"2")));
//              CommonConsts.channelMap.remove(channel);//授权通过从 CommonConsts.channel中删除
//              //用户在线+1
//              //ApiUtil.addUserOnline(,)
//         }

        PublisherRequest request =new PublisherRequest();
        request.setToken(monitorExchangeData.getSecretKey());
        request.setUserInfoId(monitorExchangeData.getUserInfoId());
        ChDispatcherManager.getInstance().registerPublisher(request, channel);
        channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(true,"2")));
        CommonConsts.channelMap.remove(channel);//授权通过从 CommonConsts.channel中删除
    }


    /**
     * 发布数据token鉴权
     * @param channel
     * @param token
     */
    private  void subscriberValidateToken(Channel channel,String token){
//        Boolean  result=ApiUtil.checkToken(token);
//        if(result){//鉴权失败  发送鉴权失败信息 并断开连接
//            channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(false,"1")));
//            channel.close();//关闭连接
//        }else {//鉴权成功
//            channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(true,"1")));
//            CommonConsts.channelMap.remove(channel);//授权通过从 CommonConsts.channel中删除
//        }

        channel.writeAndFlush(new BinaryWebSocketFrame(getAuthErrorInfo(true,"1")));
        CommonConsts.channelMap.remove(channel);//授权通过从 CommonConsts.channel中删除
    }
    //返回鉴权失败信息

    /**
     *
     * @param isSucess
     * @param from 1: 医生 3：APP患者
     * @return
     */
    private ByteBuf getAuthErrorInfo(boolean isSucess,String from){
        MonitorExchangeDataProto.MtConnectStatusInfo.Builder connectStatusInfo= MonitorExchangeDataProto.MtConnectStatusInfo.newBuilder();
        if(isSucess==true){
            connectStatusInfo.setCodeValue(MonitorExchangeDataProto.MtStateCode.STATE_SUCCESS_VALUE);
        }else{
            connectStatusInfo.setCodeValue(MonitorExchangeDataProto.MtStateCode.STATE_UNAUTH_VALUE);
        }
        MonitorExchangeDataProto.MonitorExchangeData.Builder  backBuilder = MonitorExchangeDataProto.MonitorExchangeData.newBuilder();
        backBuilder.setDataTypeValue(MonitorExchangeDataProto.DateType.CONNECTS_TATUS_VALUE);
        backBuilder.setStatusInfo(connectStatusInfo).build();
        if("1".equals(from)){//医生
            MonitorExchangeDataProto.MtReceiveData.Builder  sendData=  MonitorExchangeDataProto.MtReceiveData.newBuilder();
            sendData.addMonitorExchangeDataList(backBuilder);
            ByteBuf result = Unpooled.buffer();
            result.writeBytes(sendData.build().toByteArray());
            return result;
        }else{//APP
            backBuilder.setStatusInfo(connectStatusInfo).build();
            ByteBuf result = Unpooled.buffer();
            result.writeBytes(backBuilder.build().toByteArray());
            return result;
        }


    }


    /**
     * 向患者发送订阅数据请求
     * @param monitorExchangeData
     */
    private void sendSubscribeToPatient(Channel channel,MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {
        SubscriberRequest request=new SubscriberRequest();
        request.setDoctorUserId(monitorExchangeData.getUserInfoId());
        request.setUserInfoId(monitorExchangeData.getSubscriptionInfo().getUserInfoId());//患者的用户ID
        ChDispatcherManager.getInstance().sendSubscribeToPatient(request,channel,monitorExchangeData);
    }

    /**
     * 转发数据
     * @param monitorExchangeData
     */
    private void dispatchData(MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {
        ChDispatcherManager.getInstance().dispatchData(monitorExchangeData);
    }


}
