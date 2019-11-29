package com.nzh.simple_okhttp.net.chain;


import com.nzh.simple_okhttp.net.core.HttpInfo;
import com.nzh.simple_okhttp.net.http_entity.Request;
import com.nzh.simple_okhttp.net.http_entity.Response;

/**
 * 处理http请求协议中  的请求头 。
 * <p>
 * get 和 post  方式是请求头 有所不同，这里增加一个 拦截器来 做区分处理。
 */

public class HttpHeaderInterceptor implements IInterceptor {
    @Override
    public Response doInterceptor(InterceptorRequest request4Next) throws Exception{

        Request request = request4Next.task.getRequest();

        // get ，post 请求方式的通用处理 ：给请求头添加Host
        addHeaderField(request);


        if (HttpInfo.REQ_METHOD_GET.equals(request.getMethod())) {  //GET


        } else if (HttpInfo.REQ_METHOD_POST.equals(request.getMethod())) {     //POST

            addHeader4PostReq(request);
        }

        return request4Next.process();
    }

    public void addHeaderField(Request request) {
        request.getHeaders().put(HttpInfo.REQ_HEADER_HOST, request.getUrl().getHost());
        // http1.1协议的请求头中， 默认就是keep-alive，所以不写也可以。 默认情况下支持长链接。
        request.getHeaders().put("Connection", "keep-alive");

    }

    private void addHeader4PostReq(Request request) {
        if (request.getRequestBody().content == null) {
            request.getRequestBody().caluContentLength();
        }
        request.getHeaders().put(HttpInfo.REQ_CONTENT_LENGHT, String.valueOf(request.getRequestBody().content.getBytes().length));
        request.getHeaders().put(HttpInfo.REQ_CONTENT_TYPE, HttpInfo.HEAD_VALUE_CONTENT_TYPE);
        request.getHeaders().put(HttpInfo.REQ_HEADER_ACCEPT_CHARSET, HttpInfo.HEAD_VALUE_ACCEPT_CHARSET);
        request.getHeaders().put(HttpInfo.REQ_RESP_CONNECTION, HttpInfo.HEAD_VALUE_CONNECTION1);
    }


}
