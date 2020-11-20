package com.cheroee.socketserver.thread;

import com.cheroee.socketserver.probuff.MonitorExchangeDataProto;
import dispatcher.ChDispatcherManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DispatcherRunnable implements Runnable {
    private boolean flag =true;
    private String  doctorId;
    private BlockingQueue<MonitorExchangeDataProto.MonitorExchangeData> msgQueue=new ArrayBlockingQueue<>(100000);
    public DispatcherRunnable(String   doctorId){
        this.doctorId=doctorId;
    }
    @Override
    public void run() {
        while (flag) {
            MonitorExchangeDataProto.MtReceiveData.Builder dispatcherData=  MonitorExchangeDataProto.MtReceiveData.newBuilder();
            try {
                while (msgQueue.size()>0){
                    MonitorExchangeDataProto.MonitorExchangeData msg=msgQueue.take();
                    if(msg!=null){
                        dispatcherData.addMonitorExchangeDataList(msg);
                    }
                }
                if(dispatcherData.build().getMonitorExchangeDataListList().size()>0){
                    ChDispatcherManager.getInstance().dispatchDataToDoctor(doctorId,dispatcherData);
                }
                //休眠25毫秒
                TimeUnit.MILLISECONDS.sleep(25L);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 添加数据
     * @param monitorExchangeData
     */
    public synchronized void addData(MonitorExchangeDataProto.MonitorExchangeData monitorExchangeData) {
        msgQueue.add(monitorExchangeData);
    }

    /**
     * 开启队列监听
     */
    public void start(){
        this.flag = true;
    }

    /**
     * 停止队列监听
     */
    public void stop(){
        this.flag = false;
    }
}
