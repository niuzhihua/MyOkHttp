package com.nzh.simple_okhttp.net;


import com.nzh.simple_okhttp.net.dispatcher.Task;
import com.nzh.simple_okhttp.net.http_entity.Response;

public interface ICallBack {

    void onSuccess(Task task, Response response);

    void onFailed(Task task, Exception e);


}
