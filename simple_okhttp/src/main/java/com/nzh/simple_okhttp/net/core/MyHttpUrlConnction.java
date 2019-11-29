package com.nzh.simple_okhttp.net.core;

import com.nzh.simple_okhttp.net.http_entity.HttpUrl;
import com.nzh.simple_okhttp.net.http_entity.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 使用Socket 来执行网络请求
 */

public class MyHttpUrlConnction {


    Socket socket;

    // 服务器返回流
    InputStream inputStream;
    // 向服务器写入数据
    OutputStream outputStream;

    // 请求对象。
    Request request;


    long lastUseTime;

    private void createSocket() throws IOException {
        HttpUrl url = request.getUrl();
        if (socket == null || socket.isClosed()) {  // 由于连接时可以复用的，所以这里socket可能是复用的socket,就不用重新connect了。

            if (url.getProtocol().equalsIgnoreCase("https")) {
                // 创建 支持 Https 的 socket
            } else {
                socket = new Socket();
            }

            socket.connect(new InetSocketAddress(url.getHost(), url.getPort()));
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();

        }


    }


    /**
     * 真实的执行网络请求
     *
     * @param request
     * @throws IOException
     */
    public InputStream doRequestBySocket(Request request) {
        this.request = request;

        try {
            createSocket();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("创建socket异常,请检查网络或者网络权限。");
        }
        // 组装出协议数据
        String protocol = HttpEncoder.encode(request);
        System.out.println(protocol);

        try {
            byte[] data = protocol.getBytes();
            // 写入服务器
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("组装出协议数据异常");
        }

        return inputStream;
    }

    /**
     * 设置最近一次的使用时间 ， 单位毫秒
     *
     * @param lastUseTime
     */
    public void setLastUseTime(long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public void close() {

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
