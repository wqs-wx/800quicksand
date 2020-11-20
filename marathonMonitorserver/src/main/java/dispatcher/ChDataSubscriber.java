package dispatcher;

import com.cheroee.socketserver.server.bean.SubscriberRequest;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class ChDataSubscriber {

    private Channel channel;

    private SubscriberRequest request;

    private List<ChDataPublisher> publishers=new ArrayList<>();


    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SubscriberRequest getRequest() {
        return request;
    }

    public void setRequest(SubscriberRequest request) {
        this.request = request;
    }



    public List<ChDataPublisher> getPublishers() {
        return publishers;
    }

    public void setPublisher(ChDataPublisher publisher) {
        publishers.add(publisher);
    }
}
