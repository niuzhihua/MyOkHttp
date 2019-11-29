package com.nzh.demo;

import android.app.Activity;
import android.icu.util.Measure;
import android.os.Bundle;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.nzh.simple_okhttp.net.ICallBack;
import com.nzh.simple_okhttp.net.MyOkHttp;
import com.nzh.simple_okhttp.net.chain.my_interceptor.TestInterceptor1;
import com.nzh.simple_okhttp.net.chain.my_interceptor.TestInterceptor2;
import com.nzh.simple_okhttp.net.dispatcher.Task;
import com.nzh.simple_okhttp.net.exception.MyCancelException;
import com.nzh.simple_okhttp.net.http_entity.Request;
import com.nzh.simple_okhttp.net.http_entity.RequestBody;
import com.nzh.simple_okhttp.net.http_entity.Response;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    //0: ViewStub Merge
    //1: Measure/layout优化 ：
    //      ViewStub/Merge   :  减少布局层次
    //      ConstraintLayout ： 减少布局层次
    //      PrecomputedText(异步进行Measure/layout) + StaticLayout ：

    //2: View 重用：自己研发，可以在不同activity/fragment 间重用view

    // 3: 卡顿监控：Choreographer帧率检测方案
    // https://mp.weixin.qq.com/s?__biz=MzAxMzYyNDkyNA==&mid=2651332439&idx=1&sn=ba542ffeb494d827b9009d4e2128ed5c&scene=1&srcid=0818mFLqkkIMoS7vzjxTOEq2&key=8dcebf9e179c9f3a440c75502cc03ba23f09db161f5a3a4bd15acbdaa6b2ac0e641552d3b40a09b753739924009c9bfb&ascene=0&uin=Mjc3OTU3Nzk1&devicetype=iMac+MacBookPro10%2C1+OSX+OSX+10.10.5+build%2814F1909%29&version=11020201&pass_ticket=jzkG1H7kXvY27npmMnFUaa1eMShQT4c0PYd9r8wDanabk59MHU0ynkDd6oxen8jH
    // https://github.com/fangyuxiong/AndroidPerformance/blob/master/aplib/src/main/java/com/xfy/androidperformance/FrameCallbackImpl.java

    //4： RenderThread
//    https://cloud.tencent.com/developer/article/1418980
    MyOkHttp client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new MyOkHttp.Builder().setRetryCount(3).build();


        // 遇到的问题：
        // 注意httpurl 的端口获取
        // 注意线程池创建时的参数 threadFactory 是否传入了runnable
        // 连接复用后报错。

        System.out.println(this.getClass().getClassLoader().toString()); // PathClassLoader
        System.out.println(Activity.class.getClassLoader().toString()); // BootClassLoader
        System.out.println(AppCompatActivity.class.getClassLoader().toString()); //PathClassLoader
    }

    String id;

    public void doGet(String url) {

        Request request = new Request.Builder().setUrl(url).build();
        id = request.getId();


        client.addInterceptor(new TestInterceptor1())
                .addInterceptor(new TestInterceptor2())
                .enqueue(request, new ICallBack() {
                    @Override
                    public void onSuccess(Task task, Response response) {

                        Log.e("onSuccess:", Thread.currentThread().getName());
                    }

                    @Override
                    public void onFailed(Task task, Exception e) {

                        if (e instanceof MyCancelException) {
                            Log.e("cancel", "取消了请求");
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

    }

    public void testGet(View view) {
        String url = "http://www.kuaidi100.com/query?type=yuantong&postid=11111111111";
        doGet(url);
    }


    public void testPost(View view) {
        doPost();
    }

    private void doPost() {
        String url = "http://restapi.amap.com/v3/weather/weatherInfo";

        RequestBody body = new RequestBody.Builder()
                .addValueWithUTF_8("city", "长沙")
                .addValueWithUTF_8("key", "13cb58f5884f9749287abbead9c658f2")
                .build();

        Request request = new Request.Builder().setUrl(url).setRequestBody(body).build();

        client.enqueue(request, new ICallBack() {
            @Override
            public void onSuccess(Task task, Response response) {

            }

            @Override
            public void onFailed(Task task, Exception e) {

            }
        });


    }

    // 取消所有的网络任务
    public void testCancelAll(View view) {

        // 全部取消   # 已实现

        // 取消全部排队的
        // 取消全部正在执行的
        // 取消等待队列中某一个任务

        // 取消执行队列中某一个任务  # 已实现

        client.cancelAll();

    }

    // 取消 某一个网络任务
    public void testCancelById(View view) {
        if (!TextUtils.isEmpty(id)) {
            client.cancelTaskById(id);
        }
    }
}
