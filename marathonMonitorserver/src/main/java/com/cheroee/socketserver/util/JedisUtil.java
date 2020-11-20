package com.cheroee.socketserver.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/*jedis工具类*/

public final class JedisUtil {
    private JedisUtil() {}
    private static JedisPool jedisPool;
    private static int maxTotal;
    private static int maxWaitMillis;
    private static String host;
    private static int port;
    private static String password;
 
    /*读取jedis.properties配置文件*/
    static{
        maxTotal = 1000;
        maxWaitMillis = 3000;
           //host = "172.16.96.82"; //正式环境
        //host ="47.96.111.125"; //测试环境
        host = "127.0.0.1";
        port = 6379;
        //password = "chero@123";
        password = "hq123";
    }
 
    /*创建连接池*/
    static{
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPool = new JedisPool(jedisPoolConfig,host,port);
    }
 
    /*获取jedis*/
    public static Jedis getJedis(){
        Jedis jedis= jedisPool.getResource();
        jedis.auth(password);
        return jedis;
    }
 
    /*关闭Jedis*/
    public static void close(Jedis jedis){
        if(jedis!=null){
            jedis.close();
        }
    }

    /**
     * 设置值
     * @param key
     * @param value
     */
    public static  void setValue(String key ,String value){
        Jedis jedis=getJedis();
        try{
            jedis.set(key,value);//设置完成后重新设置回redis
        }finally {
            JedisUtil.close(jedis);
        }
    }

    /**
     * 获取值
     * @param key
     * @return
     */
    public static  String getValue(String key){
        Jedis jedis=getJedis();
        try{
         return   jedis.get(key);//设置完成后重新设置回redis
        }finally {
            JedisUtil.close(jedis);
        }
    }
}