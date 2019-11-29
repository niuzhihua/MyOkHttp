package com.nzh.simple_okhttp.net.chain.my_interceptor;


import com.nzh.simple_okhttp.net.chain.IInterceptor;
import com.nzh.simple_okhttp.net.chain.InterceptorRequest;
import com.nzh.simple_okhttp.net.http_entity.Response;

/**
 * 自定义拦截器 的 父类，所有自定义拦截 需要继承此类。
 */
public abstract class MyInterceptor implements IInterceptor {


    /**
     * 拦截请求前 的处理
     *
     * @param request4Next
     */
    public abstract void beforeDoInterceptor(InterceptorRequest request4Next);

    /**
     * 请求执行返回后的处理
     *
     * @param response
     */
    public abstract void afterDoInterceptor(Response response);

    @Override
    public Response doInterceptor(InterceptorRequest request4Next) throws Exception {

        beforeDoInterceptor(request4Next);

        Response response = request4Next.process();

        afterDoInterceptor(response);

        return response;
    }
}
