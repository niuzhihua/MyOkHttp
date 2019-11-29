package com.nzh.simple_okhttp.net.dispatcher;

import android.util.Log;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;


/**
 * 调度器：
 * <p>
 * 包含 2 请求队列  和 1 个线程池。
 * <p>
 * 请求队列1：保存正在执行的task.
 * 请求队列2：保存等待执行的task.
 * <p>
 * 线程池： 负责异步执行task.
 */

public class Dispatcher {

    private Deque<Task> runningQueue;  //正在执行task的队列 。

    private Deque<Task> waittingQueue;  // 等待执行task的队列。

    private ExecutorService executorService; // 执行任务线程池
    private ExecutorService cleanService; // 清理任务线程池

    //当前正在执行的最大请求数目
    int maxRequestCount = 5;

    // 同一个host的最大请求数目
    int maxRequestToHost = 3;

    Lock lock = new ReentrantLock();

    public Dispatcher() {


        if (executorService == null) {

            ThreadFactory factory = new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread t = new Thread(r, "thread_for_pool");
                    // 给线程池 创建线程。
                    return t;
                }
            };

            //corePoolSize:核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true。
            //maximumPoolSize: 最大线程数。超过这个数的线程将被阻塞
            //keepAliveTime :  非核心线程的闲置超时时间，超过这个时间就会被回收。
            //timeUnit :       keepAliveTime的单位
            //workQueue:       线程池中的任务队列 ,常用的有 SynchronousQueue,LinkedBlockingDeque,ArrayBlockingQueue。
            //threadFactory:   给线程池提供创建新线程的功能 。

            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), factory);
        }
        runningQueue = new LinkedBlockingDeque<>();
        waittingQueue = new LinkedBlockingDeque<>();


        // 清理任务线程池
        if (cleanService == null) {


            ThreadFactory f = new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread t = new Thread(r, "clean_thread");
                    // 给线程池 创建线程。
                    return t;
                }
            };

            cleanService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), f);

        }
    }


    /**
     * 将要执行的 任务 加入队列。
     * <p>
     * 判断是否 满足 加入 runningQueue 队列的条件， 若满足 就加入，否则加入等待队列 waittingQueue。
     * <p>
     * 条件 1 ： 当前正在执行的 任务数量 不能超过 设定的 限值 。
     * 条件 2 ： 当前正在执行的 任务数量 不能超过 设定的 同一个host的 最多请求数量。
     *
     * @param task 网络任务
     */
    public void enqueue(Task task) {

        lock.lock();

        if (runningQueue.size() < maxRequestCount && getCountToOneHost(task) < maxRequestToHost) {
            runningQueue.add(task);
            executorService.execute(task);
            Log.e("dispatcher:", "加入了执行队列");
        } else {
            waittingQueue.add(task);
            Log.e("dispatcher:", "加入了等待队列");
        }

        lock.unlock();
    }

    /**
     * 在当前任务执行队列中，找到 和 task 同一个host 的请求，并返回总数目。
     *
     * @return
     */
    private int getCountToOneHost(Task task) {

        int count = 0;
        for (Task t : runningQueue) {
            if (task.request.getUrl().getHost().equals(t.request.getUrl().getHost())) {
                count++;
            }
        }
        return count;
    }


    /**
     * 做两件事情：
     * 1：将任务移除队列 runningQueue。
     * 2：把等待队列中的数据加入 runningQueue，并执行。
     *
     * @param task
     */
    public void dequeue(Task task) {

        lock.lock();
        removeTask(task);
        lock.unlock();

    }

    private void removeTask(Task task) {

        Log.e("dispatcher:", "start to remove task from runningQueue");
        runningQueue.remove(task);
        Log.e("dispatcher-end:", "task was removed from runningQueue");

        // 如果等待队列为空 ，直接返回。
        if (waittingQueue.size() == 0) {
            return;
        }

        // 遍历等待队列
        Iterator<Task> iterator = waittingQueue.iterator();
        for (; iterator.hasNext(); ) {
            Task t = iterator.next();
            // 判断是否符合加入 执行队列条件
            if (runningQueue.size() < maxRequestCount && getCountToOneHost(task) < maxRequestToHost) {

                // 添加到执行队列
                runningQueue.add(t);
                //从等待队列删除
                iterator.remove();

                // 执行
                executorService.execute(t);

                Log.e("dispatcher:", "remove task from waittingQueue,add new task into runningQueue");

            } else {
                break;
            }

        }
    }


    /**
     * 取消全部网络请求。
     */
    public void cancelAll() {

        cleanService.execute(new Runnable() {
            @Override
            public void run() {

                lock.lock();
                realCancelAll();
                lock.unlock();
            }
        });

    }

    private void realCancelAll() {

        if (waittingQueue.isEmpty() && runningQueue.isEmpty()) {
            return;
        }

        Log.e("dispatcher-cancel:", "等待队列任务数:" + waittingQueue.size() + "--取消:" + waittingQueue.size());
        waittingQueue.clear();
        if (!waittingQueue.isEmpty()) {
            throw new IllegalArgumentException("取消全部请求异常！");
        }

        Iterator<Task> it = runningQueue.iterator();
        for (; it.hasNext(); ) {
            Task task = it.next();
            task.setCancel(true);
        }
        Log.e("dispatcher-cancel:", "等待队列任务数:" + runningQueue.size() + "--取消:" + runningQueue.size());

    }

    /**
     * 取消执行队列中 （包含请求id 为id） 的任务
     *
     * @param id
     */
    public void cancelTaskById(final String id) {

        cleanService.execute(new Runnable() {
            @Override
            public void run() {

                lock.lock();
                realCancelById(id);
                lock.unlock();

            }
        });

    }

    private void realCancelById(String id) {
        Iterator<Task> it = runningQueue.iterator();
        for (; it.hasNext(); ) {
            Task task = it.next();
            if (id.equals(task.getRequest().getId())) {
                task.setCancel(true);
                Log.e("dispatcher-cancel:", "取消了等待队列中 id为 " + id + "的任务。");
                break;
            }
        }

    }


}
