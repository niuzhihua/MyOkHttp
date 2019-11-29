package com.nzh.simple_okhttp.net.http_entity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL 类
 */

public class HttpUrl {

    String host;
    String file;
    String protocol;
    int port;

    public HttpUrl(String url) throws MalformedURLException {

        URL u = new URL(url);

        host = u.getHost(); //主机名
        file = u.getFile(); // 资源路径
        protocol = u.getProtocol(); //协议名: 例如 file http https ftp
        port = u.getPort();

        port = (port <= 0) ? u.getDefaultPort() : port;   // 端口
    }

    public String getHost() {
        return host;
    }

    public String getFile() {
        return file;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }
}
