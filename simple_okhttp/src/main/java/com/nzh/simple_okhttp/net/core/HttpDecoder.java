package com.nzh.simple_okhttp.net.core;

import android.util.Log;

import com.nzh.simple_okhttp.net.http_entity.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


/**
 * 负责解析  http 协议数据。
 */

public class HttpDecoder extends HttpInfo {

    public Response decode(InputStream inputStream) throws Exception {


        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();


        // 解析响应码
        int code = parseHttpRespCode(line);

        // 响应头数据结构
        if (code != RESP_CODE_OK) {
            return new Response(code);
        }

        // 保存响应头
        HashMap<String, String> respHeaders = new HashMap<>();

        // 保存响应体 body
        StringBuffer bodyline = null;
        int contentLen = 0;
        boolean isKeepAlive = false;
        while ((line = reader.readLine()) != null) {

            // 解析 响应头
            if (!isEmptyLine(line)) {
                String headerKey = line.substring(0, line.indexOf(COLON));
                String headerValue = line.substring(line.indexOf(COLON) + 1, line.length());
                respHeaders.put(headerKey, headerValue.trim().replace(CRLF, ""));  // 去掉空格 和 回车换行
            } else {

                // 解析 body 前 从响应头中获取 信息。
                boolean isChunked = HEAD_VALUE_TRANSFER_ENCODING.equals(respHeaders.get(RESP_HEADER_TRANSFER_ENCODING));
                isKeepAlive = isKeepAlive(respHeaders.get(REQ_RESP_CONNECTION));
                String contentLength = respHeaders.get(RESP_HEADER_CONTENT_LENGHT);
                try {
                    contentLen = Integer.parseInt(contentLength);
                } catch (NumberFormatException e) {
                    // e.printStackTrace();
                    Log.e("ignore", "忽略解析contentLength转int异常");
                }

                // 解析 响应body  :
                // 如果要一边产生数据，一边发给客户端，比如下载, 服务器就需要使用"Transfer-Encoding: chunked"这样的方式来代替Content-Length。
                // 总之： 如果存在Transfer-Encoding（重点是chunked），则在header中不能有Content-Length 。

                bodyline = new StringBuffer();
                if (contentLen > 0) {
                    while ((line = reader.readLine()) != null) {    // 一直读到最后 就是body。
                        bodyline.append(line);
                    }

                } else if (isChunked) {
                    // chunked 格式 就是  一行表示长度(16进制)，一行表示内容。
                    while ((line = reader.readLine()) != null) {    // 读到 0\r\n结束标志

                        // 判断长度
                        try {
                            int chunkedSize = Integer.valueOf(line, 16);  // 将16进制字符串转整数
                            if (chunkedSize > 0) {

                                // 继续往下一行读取内容
                                bodyline.append(reader.readLine());
                            } else {
                                break;
                            }
                        } catch (NumberFormatException e) {
                            //e.printStackTrace();
                        }

                    }
                    break;
                }

            }
        }


        return new Response(code, respHeaders, isKeepAlive, bodyline.toString());

    }


    /**
     * 根据http 协议响应格式 ，header后面是\r\n. 表示响应头结束了
     *
     * @param line
     * @return
     */

    private boolean isEmptyLine(String line) {
        return "".equals(line) || line.equals("\r\n");
    }

    private boolean isKeepAlive(String s) {
        return HEAD_VALUE_CONNECTION1.equals(s) || HEAD_VALUE_CONNECTION2.equals(s);
    }


    private int parseHttpRespCode(String s) {

        try {
            String[] str = s.split(" ");
            return Integer.parseInt(str[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
