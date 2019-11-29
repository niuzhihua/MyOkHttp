package com.nzh.simple_okhttp.net.http_entity;


import android.util.ArrayMap;

import com.nzh.simple_okhttp.net.core.HttpInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;


public class RequestBody {

    private ArrayMap<String, String> body;

    private RequestBody(Builder builder) {
        this.body = builder.body;
    }

    // post 请求下的 body 数据
    public String content;

    // body 数据长度
    public String caluContentLength() {

        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> sets = body.entrySet();
        for (Map.Entry<String, String> entry : sets) {
            sb.append(entry.getKey()).append("=").append(entry.getValue())
                    .append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        content = sb.toString();
        return content;
    }


    public static class Builder {

        private ArrayMap<String, String> body;

        public Builder() {
            body = new ArrayMap<>();
        }

        public Builder addValueWithUTF_8(String key, String value) {

            try {
                body.put(URLEncoder.encode(key, HttpInfo.HEAD_VALUE_ACCEPT_CHARSET), URLEncoder.encode(value, HttpInfo.HEAD_VALUE_ACCEPT_CHARSET));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
//            body.put(key, value);
            return this;
        }

        public RequestBody build() {
            return new RequestBody(this);
        }
    }
}
