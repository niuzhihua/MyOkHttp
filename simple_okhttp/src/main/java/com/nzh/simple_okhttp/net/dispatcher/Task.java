package com.nzh.simple_okhttp.net.dispatcher;


import android.util.Log;

import com.nzh.simple_okhttp.net.ICallBack;
import com.nzh.simple_okhttp.net.MyOkHttp;
import com.nzh.simple_okhttp.net.chain.CancelInterceptor;
import com.nzh.simple_okhttp.net.chain.ConnPoolInterceptor;
import com.nzh.simple_okhttp.net.chain.HttpHeaderInterceptor;
import com.nzh.simple_okhttp.net.chain.IInterceptor;
import com.nzh.simple_okhttp.net.chain.InterceptorRequest;
import com.nzh.simple_okhttp.net.chain.RealConnInterceptor;
import com.nzh.simple_okhttp.net.chain.my_interceptor.MyInterceptor;
import com.nzh.simple_okhttp.net.core.HttpInfo;
import com.nzh.simple_okhttp.net.exception.MyCancelException;
import com.nzh.simple_okhttp.net.http_entity.Request;
import com.nzh.simple_okhttp.net.http_entity.Response;

import java.util.ArrayList;
import java.util.Map;


/**
 * 网络任务 ：
 * <p>
 * 1： 负责构建并执行责任链
 * 2： 负责回调网络请求结果
 */
public class Task implements Runnable {

    // http 请求
    Request request;

    // 请求执行完后的回调
    ICallBack callBack;

    public MyOkHttp client;

    // 是否取消当前任务
    boolean isCancel = false;

    // 保存用户自定义的拦截器
    ArrayList<MyInterceptor> myInterceptors;

    public Task(Request request, ICallBack callBack, MyOkHttp dispatcher) {
        this.request = request;
        this.callBack = callBack;
        this.client = dispatcher;
    }

    /**
     * 添加用户自定义的拦截器
     *
     * @param myInterceptors
     */
    public void addUserInterceptors(ArrayList<MyInterceptor> myInterceptors) {
        this.myInterceptors = myInterceptors;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public boolean isCancel() {
        return isCancel;
    }

    @Override
    public void run() {

        // 构建 责任链

        ArrayList<IInterceptor> chainList = new ArrayList<>();

        // 添加用户自定义拦截器
        if (myInterceptors != null && myInterceptors.size() > 0) {
//            for (MyInterceptor i : myInterceptors) {
//            }
            chainList.addAll(myInterceptors);
        }

        chainList.add(new CancelInterceptor());

        chainList.add(new HttpHeaderInterceptor());

        chainList.add(new ConnPoolInterceptor());

        chainList.add(new RealConnInterceptor());
        // 构建请求
        int processIndex = 0;
        InterceptorRequest request = new InterceptorRequest(chainList, processIndex, this, null);

        //执行请求
        Response response = null;
        try {
            response = request.process();

            // 先判断是否取消了请求
            if (isCancel()) {
                callBack.onFailed(this, new MyCancelException("task canceled"));
            } else {
                if (response.respCode == HttpInfo.RESP_CODE_OK) {
                    // 打印测试
                    print(response);
                    callBack.onSuccess(this, response);
                } else {
                    callBack.onFailed(this, new IllegalArgumentException("网络异常"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            callBack.onFailed(this, e);
            Log.e("请求响应异常：", e.getMessage());
        } finally {
            System.out.println("-");
            // 从等待队列中删除 已经执行过的 网络任务。
            client.getDispatch().dequeue(this);

            // 删除当次请求设置d拦截器
            if (chainList != null)
                chainList.clear();
        }


    }

    public void print(Response response) {
        for (Map.Entry<String, String> entry : response.respHeaders.entrySet()) {
            Log.e("\t " + entry.getKey(), entry.getValue());
        }
        Log.e("response.body", response.body);
    }

    public Request getRequest() {
        return request;
    }
}
