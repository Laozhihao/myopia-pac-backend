package com.wupol.myopia.business.common.utils.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 *  线程池配置
 * @author hang.yuan
 * @date 2022/5/10
 */
@Slf4j
@Configuration
public class ExecutorConfig {



    @Value("${async.executor.thread.core_pool_size:2}")
    private int corePoolSize;
    @Value("${async.executor.thread.max_pool_size:5}")
    private int maxPoolSize;
    @Value("${async.executor.thread.queue_capacity:100}")
    private int queueCapacity;
    @Value("${async.executor.thread.name.prefix:async-service-}")
    private String namePrefix;

    @Bean(name = "asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        executor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        return executor;
    }
}
