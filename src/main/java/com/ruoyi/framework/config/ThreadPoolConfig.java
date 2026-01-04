package com.ruoyi.framework.config;

import com.ruoyi.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author ruoyi
 **/
@Configuration
public class ThreadPoolConfig
{
    // 核心线程池大小
    private int corePoolSize = 50;

    // 最大可创建的线程数
    // private int maxPoolSize = 200;

    // 队列最大长度
    // private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    // private int keepAliveSeconds = 300;

    /**
     * 传统平台线程池 - 备用
     *
     * 注意：项目已启用虚拟线程(spring.threads.virtual.enabled=true)，
     *
     * @Async 任务统一使用 virtualTaskExecutor 执行。
     *
     *        此线程池目前未被使用，仅作为以下场景的回退方案保留：
     *        1. CPU密集型计算任务（虚拟线程在此场景无优势）
     *        2. 需要严格控制并发数的场景
     *
     *        使用方式：@Async("threadPoolTaskExecutor")
     */
    // @Bean(name = "threadPoolTaskExecutor")
    // public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    // {
    //     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //     executor.setMaxPoolSize(maxPoolSize);
    //     executor.setCorePoolSize(corePoolSize);
    //     executor.setQueueCapacity(queueCapacity);
    //     executor.setKeepAliveSeconds(keepAliveSeconds);
    //     // 线程池对拒绝任务(无线程可用)的处理策略
    //     executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    //     return executor;
    // }

    /**
     * 虚拟线程执行器 (Java 21+)
     * 适用于I/O密集型任务，如HTTP请求、数据库查询、文件操作等
     * 虚拟线程非常轻量，可以创建数百万个而不会耗尽系统资源
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor()
    {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Spring TaskExecutor适配器，用于@Async注解
     * 当需要使用虚拟线程执行异步任务时，可以指定此执行器：
     * @Async("virtualTaskExecutor")
     */
    @Bean(name = "virtualTaskExecutor")
    public TaskExecutor virtualTaskExecutor(ExecutorService virtualThreadExecutor)
    {
        return new TaskExecutorAdapter(virtualThreadExecutor);
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService()
    {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy())
        {
            @Override
            protected void afterExecute(Runnable r, Throwable t)
            {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
