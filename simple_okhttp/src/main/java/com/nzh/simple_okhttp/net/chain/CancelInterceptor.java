package com.nzh.simple_okhttp.net.chain;


import com.nzh.simple_okhttp.net.dispatcher.Task;
import com.nzh.simple_okhttp.net.http_entity.Response;

/**
 * 取消网络 拦截器
 */
public class CancelInterceptor implements IInterceptor{
    @Override
    public Response doInterceptor(InterceptorRequest request4Next) throws Exception{

        // 执行当前 拦截器
        Task task = request4Next.task;

        // 传递下一个拦截器
        Response response = request4Next.process();


        return response;
    }
}
