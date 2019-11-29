package com.nzh.simple_okhttp.net.chain;


import com.nzh.simple_okhttp.net.http_entity.Response;

public interface IInterceptor {

    /**
     * 1：负责执行 责任链中 当前 节点（拦截器）。
     * 2：负责驱动 责任链中 下一个节点 。
     *
     * @param request4Next 责任链模式中的请求
     * @return 当前链的执行结果
     */
    Response doInterceptor(InterceptorRequest request4Next) throws Exception ;
}
