package com.nzh.simple_okhttp.net;


import com.nzh.simple_okhttp.net.chain.my_interceptor.MyInterceptor;
import com.nzh.simple_okhttp.net.core.ConnPool;
import com.nzh.simple_okhttp.net.dispatcher.Dispatcher;
import com.nzh.simple_okhttp.net.dispatcher.Task;
import com.nzh.simple_okhttp.net.http_entity.Request;

import java.util.ArrayList;
import java.util.LinkedList;


public class MyOkHttp {

    // 配置对象的属性
    int times;

    // 调度器
    Dispatcher dispatcher;

    //用户自定义的拦截器
    LinkedList<MyInterceptor> interceptors;

    // 连接池
    ConnPool pool;

    public MyOkHttp(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.times = builder.times;
        this.interceptors = builder.interceptors;
        this.pool = builder.pool;
    }


    public static class Builder {


        // 配置对象的属性
        int times;

        Dispatcher dispatcher;
        LinkedList<MyInterceptor> interceptors;
        ConnPool pool;

        /**
         * 构建MyOkHttp时 添加用户自定义的拦截器
         *
         * @param i
         * @return
         */
        public Builder addInterceptor(MyInterceptor i) {
            if (this.interceptors == null) {
                this.interceptors = new LinkedList<>();
            }

            interceptors.add(i);
            return this;
        }

        public Builder setTimes(int times) {
            this.times = times;
            return this;
        }

        public Builder setRetryCount(int times) {
            this.times = times;
            return this;
        }

        public Builder setKeepAliveTime(int time) {
            if (pool == null) {
                pool = new ConnPool();
            }
//            pool.set
            return this;
        }

        // 构建方法
        public MyOkHttp build() {
            if (dispatcher == null) {
                dispatcher = new Dispatcher();
            }

            if (pool == null) {
                pool = new ConnPool();
            }

            return new MyOkHttp(this);
        }


    }


    public Dispatcher getDispatch() {
        return dispatcher;
    }

    /**
     * 构建MyOkHttp后还可以 添加用户自定义的拦截器
     *
     * @param i
     * @return
     */
    public MyOkHttp addInterceptor(MyInterceptor i) {
        if (this.interceptors == null) {
            this.interceptors = new LinkedList<>();
        }

        interceptors.add(i);
        return this;
    }


    public void enqueue(Request request, ICallBack callBack) {

        if (dispatcher == null) {
            throw new IllegalArgumentException("请先初始化MyOkHttp对象");
        }

        // 创建网络任务
        Task task = new Task(request, callBack, this);
        // 添加自定义的拦截器
        // 重新拷贝一份list
        if (interceptors != null && interceptors.size() > 0) {
            ArrayList<MyInterceptor> newList = new ArrayList<>();
            newList.addAll(interceptors);
            interceptors.clear();
            task.addUserInterceptors(newList);
        }


        // 交给调度器 执行网络任务
        dispatcher.enqueue(task);
    }


    public void cancelAll() {
        dispatcher.cancelAll();
    }

    public void cancelTaskById(String id) {
        dispatcher.cancelTaskById(id);
    }

    public ConnPool getPool() {
        return pool;
    }
}
