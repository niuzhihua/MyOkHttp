package com.nzh.simple_okhttp.net.http_entity;


import java.util.HashMap;

public class Response {

    // http 协议响应码
    public int respCode;

    // http协议的响应头
    public HashMap<String, String> respHeaders;

    // 响应头是否支持长链接
    public boolean isKeepAlive = false;

    // http 协议响应body
    public String body;

    public Response(int respCode, HashMap<String, String> respHeaders, boolean isKeepAlive, String body) {
        this.respCode = respCode;
        this.respHeaders = respHeaders;
        this.isKeepAlive = isKeepAlive;
        this.body = body;
    }

    public Response(int code) {

        this.respCode = code;
    }


}
