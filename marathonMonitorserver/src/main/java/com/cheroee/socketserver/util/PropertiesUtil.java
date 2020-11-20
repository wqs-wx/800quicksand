/**
 * Copyright (C), 2019-2019, XXX有限公司
 * FileName: PropertiesUtil
 * Author:   Administrator
 * Date:     2019/5/14/014 8:58
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.cheroee.socketserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/5/14/014
 * @since 1.0.0
 */
public class PropertiesUtil {

    public  static String  getPropertiesValue(String key){
        Properties properties = new Properties();
           // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties");
           // 使用properties对象加载输入流
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取key对应的value值
       return     properties.getProperty( key);
    }

}
