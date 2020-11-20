/**
 * Author:   徐志林
 * Date:     2019/5/14/014 11:20
 */
package com.cheroee.socketserver.util;

import com.google.gson.internal.LinkedTreeMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Administrator
 * @create 2019/5/14/014
 */
public class RestTemplateUtil {

    public static RestTemplate restTemplate = new RestTemplate();

    public static Map  checkToken(String token,String checkTokenUrl){
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<String, String>();
        param.add("token",token);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        ResponseEntity<Object> data = restTemplate.exchange(checkTokenUrl, HttpMethod.POST, httpEntity, Object.class);
        com.google.gson.internal.LinkedTreeMap responseMap =(com.google.gson.internal.LinkedTreeMap) data.getBody();
        Map result = (Map<String, String>) (responseMap.get("body"));
        return result;
    }

    public static LinkedTreeMap doPost(String[] paramsStrs, String httpUrl){
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = setParams(paramsStrs);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(setParams(paramsStrs), headers);
        ResponseEntity<Object> data = restTemplate.exchange(httpUrl, HttpMethod.POST, httpEntity, Object.class);
        com.google.gson.internal.LinkedTreeMap responseMap =(com.google.gson.internal.LinkedTreeMap) data.getBody();
        return responseMap;
    }

    public static MultiValueMap<String, String> setParams(String[] data){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        int dataCount = data.length;
        for(Integer i = 0;i<dataCount;i++){
            params.add(data[i],data[++i]);
        }
        return params;
    }
}