package com.nzh.simple_okhttp.net.chain;

import android.util.Log;

import com.nzh.simple_okhttp.net.core.HttpDecoder;
import com.nzh.simple_okhttp.net.core.MyHttpUrlConnction;
import com.nzh.simple_okhttp.net.http_entity.Request;
import com.nzh.simple_okhttp.net.http_entity.Response;

import java.io.InputStream;

/**
 * 最终执行网络任务的 拦截器  : 不再传递请求 到下一个拦截器了。开始返回结果了。
 */

public class RealConnInterceptor implements IInterceptor {
    @Override
    public Response doInterceptor(InterceptorRequest interceptorRequest) throws Exception{

        Request request = interceptorRequest.task.getRequest();

        // 通过socket发送
        MyHttpUrlConnction urlConnction = interceptorRequest.getUrlConnction();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 组装http协议数据,并 发送
        InputStream inputStream = urlConnction.doRequestBySocket(request);

        // 解析服务器返回的数据
        HttpDecoder decoder = new HttpDecoder();
        long start = System.currentTimeMillis();
        Response response = null;

            response = decoder.decode(inputStream);

        urlConnction.setLastUseTime(System.currentTimeMillis());
        long t = System.currentTimeMillis() - start;
        Log.e("time:", String.valueOf(t));


        return response;
    }


}
