package dispatcher;

import com.cheroee.socketserver.server.bean.PublisherRequest;
import com.cheroee.socketserver.server.bean.RedisMarathonUserOnline;
import com.cheroee.socketserver.server.bean.SubscriberRequest;
import com.cheroee.socketserver.thread.DispatcherRunnable;
import com.cheroee.socketserver.util.GsonUtil;
import com.cheroee.socketserver.util.JedisUtil;
import com.cheroee.socketserver.probuff.MonitorExchangeDataProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ChDispatcherManager {
    private static ChDispatcherManager sInstance;
    //患者chanel信息
    private ConcurrentHashMap<Channel, ChDataPublisher> publisherMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String , ChDataPublisher> userIdPublisherMap = new ConcurrentHashMap<>();
    //医生chanel信息
    private ConcurrentHashMap<Channel , ChDataSubscriber> subscribeMap = new ConcurrentHashMap<>();
    //医生ID和对应的Channel
    private ConcurrentHashMap<String , HashSet<Channel>> doctorIdSubscribeMap = new ConcurrentHashMap<>();
    //医生信息消息处理线程
    private Map<String, DispatcherRunnable> doctorDispatcherRunnableMap =new ConcurrentHashMap<>();
    //处理医生消息的thread
    private Map<String, Thread> dispatcherRunnableThreadMap =new ConcurrentHashMap<>();

    public static ChDispatcherManager getInstance() {
        if (sInstance == null) {
            synchronized (ChDispatcherManager.class) {
                if (sInstance == null) {
                    sInstance = new ChDispatcherManager();
                }
            }
        }
        return sInstance;
    }

    private ChDispatcherManager() {

    }


    /**
     * 新连接到来
     */
    public synchronized void registerPublisher(PublisherRequest request, Channel channel) {
        ChDataPublisher publisher = publisherMap.get(channel);
        if (publisher != null) {//如果连接已存在
            publisher.responseLoginResult();
        } else {//如果publisherMap中不存在
            ChDataPublisher oldPublic = userIdPublisherMap.get(request.getUserInfoId());
            if (oldPublic == null) {
                publisher = new ChDataPublisher();
                publisher.setChannel(channel);
                publisher.setDoctorId("1");//默认是1
                publisher.setRequest(request);
                publisherMap.put(channel, publisher);
                userIdPublisherMap.put(request.getUserInfoId(), publisher);
            } else {
                publisher = oldPublic;
                publisherMap.put(channel, publisher);
                publisherMap.remove(oldPublic.getChannel());
                publisher.setChannel(channel);
                publisher.setRequest(request);
            }
            publisher.responseLoginResult();
        }
    }



    /**
     * 连接移除
     * @param channel
     */
    public void unRegisterPublisher(Channel channel) {
        ChDataPublisher publisher = publisherMap.get(channel);
        if(publisher!=null){
            //ApiUtil.deleteUserOnline(publisher.getRequest().getUserInfoId());//删除在线用户
            if (publisher != null) {
                publisherMap.remove(channel);
            }
            log.info("用户---"+publisher.getRequest().getUserInfoId()+"--已离线");
            deleteUserOneLine(publisher.getRequest().getUserInfoId());
        }
        //如果是医生离线
        ChDataSubscriber chDataSubscriber= subscribeMap.get(channel);
        if(chDataSubscriber!=null){
            log.info("医生---"+chDataSubscriber.getRequest().toString()+"--已离线");
            List<ChDataPublisher> dataPublishers  = chDataSubscriber.getPublishers();
//            if(dataPublishers!=null){
//                dataPublishers.stream().forEach(dataPublisher->{//通知被订阅者停止消息发送
//                    try{
//                        if(dataPublisher!=null){
//                            Channel  publisherChannel=dataPublisher.getChannel();
//                            log.info("在线查看该跑者的用户---"+dataPublisher.channels.size()+"个");
//                            if(publisherChannel!=null && dataPublisher.channels.size()==1){
//                                MonitorExchangeDataProto.MtSubscriptionInfo.Builder subscriptionInfo= MonitorExchangeDataProto.MtSubscriptionInfo.newBuilder().setActionType(2);
//                                MonitorExchangeDataProto.MonitorExchangeData  back = MonitorExchangeDataProto.MonitorExchangeData.newBuilder()
//                                        .setDataTypeValue(MonitorExchangeDataProto.DateType.SUBSCRIPTION_TYPE_VALUE)
//                                        .setSubscriptionInfo(subscriptionInfo).build();
//                                ByteBuf result = Unpooled.buffer();
//                                result.writeBytes(back.toByteArray());
//                                publisherChannel.writeAndFlush(new BinaryWebSocketFrame(result));
//                            }
//                        }
//                        //医生离线去除处理消息线程
//                        if(publisher!=null&&publisher.getDoctorId()!=null&&dispatcherRunnableThreadMap.containsKey(publisher.getDoctorId())  && dataPublisher.channels.size()==1){//查询线程是否已存在
//                            Thread dispatcherRunnableThread= dispatcherRunnableThreadMap.get(publisher.getDoctorId());
//                            dispatcherRunnableThread.interrupt();//终止线程
//                            dispatcherRunnableThreadMap.remove(publisher.getDoctorId());
//                        }
//                        if(publisher!=null&&publisher.getDoctorId()!=null&&doctorDispatcherRunnableMap.containsKey(publisher.getDoctorId()) && dataPublisher.channels.size()==1){//移除doctorDispatcherRunnableMap中的对象
//                            doctorDispatcherRunnableMap.remove(publisher.getDoctorId());
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//
//                });
//            }
            if (publisher!=null)
                log.info("医生已---"+publisher.getRequest().getUserInfoId()+"--离线");
        }

    }
    /**
     * 删除缓存
     */
    public void deleteUserOneLine(String userInfoId) {
        Jedis jedis=JedisUtil.getJedis();
        try {
            String  usersOnlineList= JedisUtil.getValue("marathonUserOnline_");
            if(usersOnlineList!=null && !"".equals(usersOnlineList)){
                List<RedisMarathonUserOnline> onlineUsersList= GsonUtil.gsonToList(usersOnlineList, RedisMarathonUserOnline.class);
                List<RedisMarathonUserOnline> onlineUsersListNew=new ArrayList <>();
                if(onlineUsersList!=null && onlineUsersList.size()>0){
                    for(RedisMarathonUserOnline user:onlineUsersList){
                        if(!(user.getUserInfoId().equals(userInfoId))){
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


    public void unRegisterSubcriber(Channel channel) {
        ChDataSubscriber subscriber = subscribeMap.get(channel);
        if (subscriber == null) {
            return;
        } else {
            ChDataPublisher publisher = userIdPublisherMap.get(subscriber.getRequest().getUserInfoId());
            publisher.removeChannel(channel);
            subscribeMap.remove(channel);
        }
    }

    public void removeChannel(Channel channel) {
        if (publisherMap.contains(channel)) {
            unRegisterPublisher(channel);
        } else if (subscribeMap.contains(channel)) {
            unRegisterSubcriber(channel);
        }
    }

    /**
     * 监测数据转发
     * @param monitorExchangeData
     */
    public void dispatchData( MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {
        ChDataPublisher publisher = userIdPublisherMap.get(monitorExchangeData.getUserInfoId());
        if (publisher != null && publisher.getDoctorId()!=null) {
            if(!doctorDispatcherRunnableMap.containsKey(publisher.getDoctorId())){//查询线程是否已存在
                DispatcherRunnable dispatcherRunnable=new DispatcherRunnable(publisher.getDoctorId());
                Thread   dispatcherRunblThread=new Thread(dispatcherRunnable);
                dispatcherRunblThread.start();//将现场放入线程池
                doctorDispatcherRunnableMap.put(publisher.getDoctorId(),dispatcherRunnable);
                dispatcherRunnableThreadMap.put(publisher.getDoctorId(),dispatcherRunblThread);
            }
            DispatcherRunnable dispatcherRunnable= doctorDispatcherRunnableMap.get(publisher.getDoctorId());
            dispatcherRunnable.addData(monitorExchangeData);
        }
    }

    /**
     * 向医生转发数据
     * @param doctorId
     * @param receiveDataBuilder
     */
    public void dispatchDataToDoctor(String doctorId, MonitorExchangeDataProto.MtReceiveData.Builder  receiveDataBuilder) {
        receiveDataBuilder.getMonitorExchangeDataListList().forEach(a->{
            if(a.getDataTypeValue()==10){
                log.info("获取到经纬度数据");
            }
        });
        HashSet channels=  doctorIdSubscribeMap.get(doctorId);
        if(channels!=null){
            Iterator<Channel> channel = channels.iterator();
            while(channel.hasNext()) {
                ByteBuf result = Unpooled.buffer();
                Channel currentChannel=channel.next();
                result.writeBytes(receiveDataBuilder.build().toByteArray());
//                log.info("发送的数据--------------数据长度为："+receiveDataBuilder.build().getMonitorExchangeDataListList().size());
               currentChannel.writeAndFlush(new BinaryWebSocketFrame(result));
               result.resetReaderIndex();
            }
        }
    }
    /**
     * 向患者发送订阅数据请求
     * @param monitorExchangeData
     */
  public synchronized   void sendSubscribeToPatient(SubscriberRequest request, Channel channel,MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {

      if(request.getDoctorUserId()!=null){
          if(!doctorDispatcherRunnableMap.containsKey(request.getDoctorUserId())){
              DispatcherRunnable dispatcherRunnable=new DispatcherRunnable(request.getDoctorUserId());
              Thread   dispatcherRunnableThread=new Thread(dispatcherRunnable);
              dispatcherRunnableThread.start();//将现场放入线程池
              doctorDispatcherRunnableMap.put(request.getDoctorUserId(),dispatcherRunnable);
              dispatcherRunnableThreadMap.put(request.getDoctorUserId(),dispatcherRunnableThread);
          }
          registerSubscriber(request,channel);
          ChDataPublisher dataPublisher = userIdPublisherMap.get(request.getUserInfoId());
          if(dataPublisher!=null){
              dataPublisher.setDoctorId(request.getDoctorUserId());
              Channel userChannel=dataPublisher.getChannel();//获取该用户的chanel
              ByteBuf result = Unpooled.buffer();
              result.writeBytes(monitorExchangeData.toByteArray());
              userChannel.writeAndFlush(new BinaryWebSocketFrame(result));
              log.info("订阅用户信息发送成功--------------："+request.getUserInfoId());
          }else {
              log.info("订阅用户信息失败--------------："+request.getUserInfoId());
          }
      }

  }

    /**
     *  侦听加入
     */
    private void registerSubscriber(SubscriberRequest request, Channel channel) {
        ChDataSubscriber subscriber = subscribeMap.get(channel);
        if (subscriber == null) {
            subscriber = new ChDataSubscriber();
            subscriber.setChannel(channel);
            subscriber.setRequest(request);
            subscribeMap.put(channel, subscriber);
        }
        ChDataPublisher publisher = userIdPublisherMap.get(request.getUserInfoId());
        subscriber.setPublisher(publisher);
        if (publisher == null) {//如果不存在
            return;
        } else {
            if(doctorIdSubscribeMap.containsKey(request.getDoctorUserId())){
                HashSet<Channel>  channels=  doctorIdSubscribeMap.get(request.getDoctorUserId());
                channels.add(channel);
            }else {
                HashSet<Channel>  channels=new  HashSet<>();
                channels.add(channel);
                doctorIdSubscribeMap.put(request.getDoctorUserId(),channels);
            }
            subscriber.setPublisher(publisher);//设置订阅对象
            publisher.setDoctorId(request.getDoctorUserId());//设置医生ID
            publisher.addSubscriberChannel(channel);//设置发布者channel
        }
    }
}
