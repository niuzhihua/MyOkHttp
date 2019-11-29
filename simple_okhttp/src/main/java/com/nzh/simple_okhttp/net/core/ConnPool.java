package com.nzh.simple_okhttp.net.core;

import android.text.TextUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * 网络连接池： 功能是复用网络连接，减少Socket 对象的创建。相当于缓存
 * <p>
 * 实现 机制：
 * <p>
 * 1：既然是缓存，就提供了 put, get 方法用来 存储 和 获取 连接对象。
 * 2：每次存入连接对象前，检测连接池中过期的连接，删除掉即可。
 * <p>
 * <br/>
 * 检测过期连接机制：
 * 为什么这里也用线程池：不断的new thread 不如 线程池好，可以复用线程。
 */

public class ConnPool {

    // 连接池 ：存放 socket 连接。这里存放封装socket的MyHttpUrlConnction。
    private Deque<MyHttpUrlConnction> connctions = new ArrayDeque<>();

    // http 协议返回的响应头中，connctions为keep-alive 时，客户端规定的过期时间。
    // 超过这个时间就可以 从 连接池 清理 连接了。
    long keepAliveTime = 40 * 1000;  // 单位毫秒

    // 清理过期连接也用线程来完成。这里用线程池可以优化 new thread . 达到复用线程作用。
    ExecutorService pool;

    // 判断是否正在执行清理。 true : 是
    boolean isCleanning = false;

    public ConnPool() {

        //corePoolSize:核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true。
        //maximumPoolSize: 最大线程数。超过这个数的线程将被阻塞
        //keepAliveTime :  非核心线程的闲置超时时间，超过这个时间就会被回收。
        //timeUnit :       keepAliveTime的单位
        //workQueue:       线程池中的任务队列 ,常用的有 SynchronousQueue,LinkedBlockingDeque,ArrayBlockingQueue。
        //threadFactory:   给线程池提供创建新线程的功能 。
        int coreThreadSize = 0;
        int maxPoolSize = Integer.MAX_VALUE;
        long keepAliveTime = 30;
        TimeUnit unit = TimeUnit.SECONDS;
        SynchronousQueue queue = new SynchronousQueue<Runnable>();
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                // 清理过期 连接的 线程 。设置为守护线程。守护 网络任务线程。
                Thread t = new Thread(r, "my_demaon_thread");
                t.setDaemon(true);
                return t;
            }
        };
        pool = new ThreadPoolExecutor(coreThreadSize, maxPoolSize, keepAliveTime, unit, queue, factory);
    }


    /**
     * 根据host,port 从缓存池中获取一个连接。
     * 1：get 方法是在多线程情况下执行的。并且多个线程操作的是同一个ConnPool对象。
     * 2：由于获取连接后需要从连接池中删除，所以需要同步操作。
     *
     * @param host
     * @param port
     * @return
     */
    public synchronized MyHttpUrlConnction get(String host, int port) {
        if (TextUtils.isEmpty(host) || port <= 0) {
            return null;
        }
        Iterator<MyHttpUrlConnction> iterator = connctions.iterator();

        for (; iterator.hasNext(); ) {
            MyHttpUrlConnction c = iterator.next();
            if (host.equals(c.request.getUrl().getHost()) && c.request.getUrl().getPort() == port) {
                iterator.remove();
                // ?
                return c;
            }
        }
        return null;
    }


    /**
     * 将连接对象 存入连接池，并开启另外一个线程清理过期的连接。
     * 由于开启的线程 和 网络任务线程 操作的都是同一个 ConnPool对象的成员。所以这里需要用对象锁。
     *
     * @param connction
     */
    public synchronized void put(MyHttpUrlConnction connction) {

        // 步骤1： 执行情理过期连接。若不是正在执行，就执行清理。
        //  这个清理线程 是 网络任务线程的一个守护线程。 并且 清理线程和网络任务线程 是业务上做不同事情。
        if (!isCleanning) {
            isCleanning = true;
            // 再次开启线程来 操作 ConnPool 对象，和 网络任务线程操作的 ConnPool 对象 是同一个对象。多线程访问同一个对象，需要同步处理。
            pool.execute(cleanOverdueConnRunable);
        }

        // 步骤2：存放可复用的连接。
        connctions.add(connction);
    }


    // 清理过期连接 ： 注意是 while 不停的检查过期的连接。
    private Runnable cleanOverdueConnRunable = new Runnable() {
        @Override
        public void run() {
            synchronized (ConnPool.this) {

                while (true) {
                    // 若找到过期对象 ，则 从连接池中删除，
                    // 否则 （若 连接池为空，就return,不检测了。）计算 最近的一个链接的过期时间，等待一下。等待完毕后再走while就清理了。

                    long maxIdleTime = -1; //最大闲置时间： 小于0 表示 连接池没有连接。
                    long now = System.currentTimeMillis();  // 当前时间
                    Iterator<MyHttpUrlConnction> iterator = connctions.iterator();
                    while (iterator.hasNext()) {
                        MyHttpUrlConnction c = iterator.next();

                        long idleTime = now - c.getLastUseTime(); // 当前连接的闲置时间 ： 大于过期时间了就清理掉 。
                        if (idleTime > keepAliveTime) {
                            // 清理，并关闭连接，释放资源
                            c.close();
                            iterator.remove();
                            return;
                        }

                        // 统计最大 限制时间
                        if (maxIdleTime < idleTime) {
                            maxIdleTime = idleTime;
                        }
                    }

                    if (maxIdleTime <= 0) {  //小于0 表示 连接池没有连接。
                        return;
                    }

                    // 下面要开始等待清理了。
                    isCleanning = true;

                    // 计算等待while循环下次清理的时间。 例如 闲置10，过期定位30秒，那么 等待就是 30 - 10 = 20 秒。
                    long waitTime = keepAliveTime - maxIdleTime;
                    try {
                        ConnPool.this.wait(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };


    public int getPoolSize() {
        return connctions.size();
    }

}
