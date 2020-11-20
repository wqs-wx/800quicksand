package com.cheroee.socketserver.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChWsLoger {
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void log(String info) {
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        System.out.println("【" + dateString + "】socket server log : " + info);
    }
    public static void error(String info) {
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        System.out.println("【" + dateString + "】socket server error : " + info);
    }
}
