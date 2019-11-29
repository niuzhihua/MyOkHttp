package com.nzh.simple_okhttp.net.chain;

import com.nzh.simple_okhttp.net.core.MyHttpUrlConnction;
import com.nzh.simple_okhttp.net.dispatcher.Task;
import com.nzh.simple_okhttp.net.http_entity.Response;

import java.util.List;


public class InterceptorRequest {

    // 标记 当前责任链中的 那一条链
    int currentIndex = 0;
    // 责任链数据结构：线性结构

    List<IInterceptor> chainList;

    Task task;

    MyHttpUrlConnction urlConnction;

    public InterceptorRequest(List<IInterceptor> chainList, int currentIndex, Task t, MyHttpUrlConnction urlConnction) {
        this.chainList = chainList;
        this.currentIndex = currentIndex;
        this.task = t;
        this.urlConnction = urlConnction;
    }

    /**
     * 执行当前节点，驱动下一个节点。
     * 执行当前节点，驱动下一个节点。
     * 执行当前节点，驱动下一个节点。
     * 执行当前节点，驱动下一个节点。
     *
     * @return
     */
    public Response process() throws Exception{

        // 获取当前链节点
        IInterceptor current = chainList.get(currentIndex);

        // 产生一个请求 ，用来执行下一个拦截器。
        InterceptorRequest request = new InterceptorRequest(chainList, currentIndex + 1, task, urlConnction);

        //doInterceptor 方法功能： 1：执行当前拦截器。  2：传递下一个拦截器
        Response response = current.doInterceptor(request);

        return response;
    }

    public MyHttpUrlConnction getUrlConnction() {
        return urlConnction;
    }


}
