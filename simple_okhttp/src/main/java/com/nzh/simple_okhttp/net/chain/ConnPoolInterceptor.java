package com.nzh.simple_okhttp.net.chain;

import android.util.Log;

import com.nzh.simple_okhttp.net.core.MyHttpUrlConnction;
import com.nzh.simple_okhttp.net.http_entity.Response;


/**
 * 使用连接池 来复用连接。
 */

public class ConnPoolInterceptor implements IInterceptor {
    @Override
    public Response doInterceptor(InterceptorRequest request4Next) throws Exception{
        // 从连接池 中 查找 可复用连接

        String host = request4Next.task.getRequest().getUrl().getHost();
        int port = request4Next.task.getRequest().getUrl().getPort();
        if (request4Next.getUrlConnction() == null) {

            MyHttpUrlConnction c = request4Next.task.client.getPool().get(host, port);
            if (c != null) {
                request4Next.urlConnction = c;
                Log.e("ConnPoolInterceptor", "--复用连接MyHttpUrlConnction--");
            } else {
                request4Next.urlConnction = new MyHttpUrlConnction();
                Log.e("ConnPoolInterceptor", "--新建连接MyHttpUrlConnction--");
            }

        }
        Response response = request4Next.process();

        // 保存可复用的连接

        if (response.isKeepAlive) {
            request4Next.task.client.getPool().put(request4Next.getUrlConnction());
            Log.e("ConnPoolInterceptor", "--保存连接MyHttpUrlConnction--");
            Log.e("连接池有连接:", request4Next.task.client.getPool().getPoolSize() + "");
        }
        return response;
    }
}
