package com.nzh.simple_okhttp.net.core;


import com.nzh.simple_okhttp.net.http_entity.HttpUrl;
import com.nzh.simple_okhttp.net.http_entity.Request;
import com.nzh.simple_okhttp.net.http_entity.RequestBody;

import java.util.Map;
import java.util.Set;

/**
 * 负责生成  http 协议数据。
 */

public class HttpEncoder extends HttpInfo {


    /**
     * 根据请求对象的数据  组装成http协议格式数据。
     *
     * @param url
     * @param method
     * @param headers
     * @param requestBody
     * @return
     */
    private static String encode(HttpUrl url, String method, Map<String, String> headers, RequestBody requestBody) {

        StringBuffer sb = new StringBuffer();


        // 1：请求行

        sb.append(method).append(SPACE).append(url.getFile()).append(SPACE).append(VERSION);
        sb.append(CRLF);


        // 2：请求头
        Set<Map.Entry<String, String>> set = headers.entrySet();

        for (Map.Entry<String, String> entry : set) {
            // 一行请求头
            sb.append(entry.getKey()).append(COLON).append(SPACE).append(entry.getValue());
            sb.append(CRLF);
        }

        // 3：再写入回车换行
        sb.append(CRLF);

        // 4：请求体(如果存在)

        if (requestBody != null) {
            sb.append(requestBody.content);
        }

        return sb.toString();

    }

    public static String encode(Request request) {
        return encode(request.getUrl(), request.getMethod(), request.getHeaders(), request.getRequestBody());
    }


}
