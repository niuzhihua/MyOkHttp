package com.nzh.simple_okhttp.net.http_entity;


import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

/**
 * 封装http 协议数据，总结起来就是：
 * <br/>
 * <p>
 * 请求方式  ： GET POST
 * 请求url  :
 * 请求头   :  ContentType:xxxx
 * 请求体   :  a="123"
 * <p>
 * 以上四部分就是 http请求协议格式 的全部数据了。
 */
public class Request {
    // 请求头
    HashMap<String, String> headers;
    // 请求方式
    String method;
    // url
    HttpUrl url;
    // post 方式下的请求体
    RequestBody requestBody;

    // 当前请求的id,用于取消网络请求。
    String id;

    private Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.requestBody = builder.requestBody;
        this.headers = builder.headers;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return method;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * 使用构建着模式 构建请求，也可以不用，直接用普通的new Obj 方式 。
     */
    public static class Builder {

        // 默认为get 请求方式
        String method = "GET";

        // post请求方式下 ，body部分的封装
        RequestBody requestBody;

        // 存放请求头，默认为get请求方式 添加空的http协议头部。
        HashMap<String, String> headers = new HashMap<>();

        //
        HttpUrl url;


        public Builder setUrl(String url1) {
            try {
                url = new HttpUrl(url1);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("url 格式错误！");
            }
            return this;
        }

        public Builder setRequestBody(RequestBody body) {

            if (body != null) {
                requestBody = body;
                method = "POST";
            }
            return this;
        }

        public Builder addHeaders(HashMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Request build() {
            return new Request(this);
        }

    }

}
