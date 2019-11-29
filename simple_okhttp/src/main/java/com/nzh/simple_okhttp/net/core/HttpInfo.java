package com.nzh.simple_okhttp.net.core;


public class HttpInfo {

    // 空格
    public static final String SPACE = " ";
    //回车和换行
    public static final String CRLF = "\r\n";
    // 回车
    public static final int CR = 13;
    // 换行
    public static final int LF = 10;
    //协议版本
    public static final String VERSION = "HTTP/1.1";
    //冒号
    public static final String COLON = ":";

    // 请求方式类型
    public static final String REQ_METHOD_GET = "GET";
    public static final String REQ_METHOD_POST = "POST";

    // 请求头信息
    public static final String REQ_HEADER_HOST = "Host";

    public static final String REQ_HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEAD_VALUE_ACCEPT_CHARSET = "UTF-8";

    public static final String REQ_CONTENT_LENGHT = "Content-Length";

    public static final String REQ_CONTENT_TYPE = "Content-Type";
    public static final String HEAD_VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";


    // 响应码信息
    public static final int RESP_CODE_OK = 200;
    // 响应头信息
    public static final String RESP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String RESP_HEADER_CONTENT_LENGHT = "Content-Length";



    public static final String RESP_HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String HEAD_VALUE_TRANSFER_ENCODING = "chunked";

    // 公共字段

    public static final String REQ_RESP_CONNECTION = "Connection";
    public static final String HEAD_VALUE_CONNECTION1 = "Keep-Alive";  // 请求头用 或响应头用， 根据服务器返回的而定。
    public static final String HEAD_VALUE_CONNECTION2 = "keep-alive"; //  响应头用
}
