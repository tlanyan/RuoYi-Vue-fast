package com.ruoyi.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.ruoyi.common.utils.Threads;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 异步任务管理器
 * 使用虚拟线程执行异步任务 (Java 21+)
 *
 * @author ruoyi
 */
public class AsyncManager
{
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池 (用于定时调度)
     */
    private ScheduledExecutorService scheduler = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 虚拟线程执行器 (用于实际任务执行, Java 21+)
     */
    private ExecutorService virtualExecutor = SpringUtils.getBean("virtualThreadExecutor");

    /**
     * 单例模式
     */
    private AsyncManager(){}

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 执行任务 (使用虚拟线程)
     * 调度由传统线程池处理，实际执行由虚拟线程执行器处理
     *
     * @param task 任务
     */
    public void execute(TimerTask task)
    {
        scheduler.schedule(() -> virtualExecutor.execute(task), OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 立即执行任务 (使用虚拟线程，无延迟)
     *
     * @param task 任务
     */
    public void executeNow(Runnable task)
    {
        virtualExecutor.execute(task);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown()
    {
        Threads.shutdownAndAwaitTermination(scheduler);
        Threads.shutdownAndAwaitTermination(virtualExecutor);
    }
}
