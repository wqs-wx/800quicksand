package dispatcher;

import com.cheroee.socketserver.log.ChWsLoger;
import com.cheroee.socketserver.probuff.MonitorExchangeDataProto;
import com.cheroee.socketserver.server.bean.PublisherRequest;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChDataPublisher {

    public ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private PublisherRequest request;
    private String doctorId; //订阅消息的医生ID
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void responseLoginResult() {
        ChWsLoger.log("response : " + channel.isActive());
        if (channel.isActive()) {
            MonitorExchangeDataProto.MtConnectStatusInfo.Builder connectStatusInfoBuilder= MonitorExchangeDataProto.MtConnectStatusInfo.newBuilder();
            connectStatusInfoBuilder.setCodeValue(MonitorExchangeDataProto.MtStateCode.STATE_SUCCESS_VALUE);
            connectStatusInfoBuilder.setStatusDesc("已连接成功");
            MonitorExchangeDataProto.MonitorExchangeData  back = MonitorExchangeDataProto.MonitorExchangeData.newBuilder()
                    .setDataTypeValue(MonitorExchangeDataProto.DateType.CONNECTS_TATUS_VALUE)
                    .setStatusInfo(connectStatusInfoBuilder).build();
            ChWsLoger.log("发送数据：" + back);
            ByteBuf result = Unpooled.buffer();
            result.writeBytes(back.toByteArray());
            channel.writeAndFlush(new BinaryWebSocketFrame(result));
        }
    }

    public PublisherRequest getRequest() {
        return request;
    }

    public void setRequest(PublisherRequest request) {
        this.request = request;
    }

    public void addSubscriberChannel(Channel channel) {
        if (channels != null) {
            channels.add(channel);
        }
    }

    public void removeChannel(Channel channel) {
        if (channels != null) {
            channels.remove(channel);
        }
    }

    public void dispatchMsg(Object object) {
        //ChWsLoger.log("dispatch Msg : " + channels.size());

        if (channels != null) {
//            OnDataComeBean odb = (OnDataComeBean) object;
//            if ("raw".equals(odb.getType()) && "success".equals(odb.getStatus())) {
//                odb.getValue();
//            }
            Gson gson = new Gson();
            String json = gson.toJson(object);
            channels.writeAndFlush(new TextWebSocketFrame(json));
        }
    }


    public void release() {
        channels.close();
        channels.clear();
        channels = null;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
}
