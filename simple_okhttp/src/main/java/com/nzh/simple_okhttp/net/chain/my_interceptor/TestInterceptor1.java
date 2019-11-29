package com.nzh.simple_okhttp.net.chain.my_interceptor;

import android.util.Log;

import com.nzh.simple_okhttp.net.chain.InterceptorRequest;
import com.nzh.simple_okhttp.net.http_entity.Response;


/**
 * 自定义拦截器  测试类
 */
public class TestInterceptor1 extends MyInterceptor {
    @Override
    public void beforeDoInterceptor(InterceptorRequest request4Next) {
        Log.e("自定义拦截器TestInterceptor1", "--beforeDoInterceptor--");
    }

    @Override
    public void afterDoInterceptor(Response response) {
        Log.e("自定义拦截器TestInterceptor1", "--afterDoInterceptor--");
    }
}
