package com.didichuxing.datachannel.arius.admin.common.util;

import com.google.common.collect.Lists;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.lucene.util.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FutureUtil<T> {
    private static final ILog LOGGER = LogFactory.getLog(FutureUtil.class);

    private ThreadPoolExecutor  executor;
    private List<Future<T>>     futures;
    private String              name;

    public void setExecutor(ThreadPoolExecutor executor){ this.executor = executor; }

    public ThreadPoolExecutor getExecutor() {return executor;}

    public void setFutures(List<Future<T>> futures) {
        this.futures = futures;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static FutureUtil init(String name, int corePoolSize, int maxPoolSize, int queueSize){
        FutureUtil futureUtil = new FutureUtil();

        ThreadPoolExecutor exe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 3000, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(queueSize),
                new NamedThreadFactory("Arius-admin-FutureUtil-" + name),
                (r, executor) -> LOGGER.error("FutureUtil.ThreadPoolExecutor RejectedExecution!"));

        futureUtil.setExecutor(exe);
        futureUtil.setName(name);
        futureUtil.setFutures(new ArrayList<>());
        return futureUtil;
    }

    public static FutureUtil init(String name){
        return FutureUtil.init(name, 10, 20, 500);
    }

    public FutureUtil callableTask(Callable<T> callable){
        this.futures.add(executor.submit(callable));
        return this;
    }

    public FutureUtil runnableTask(Runnable runnable){
        this.futures.add((Future<T>) executor.submit(runnable));
        return this;
    }

    public void waitExecute(){
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        });

        if(!EnvUtil.isOnline()){
            LOGGER.info("class=FutureUtil||method={}||template={}||futuresSize={}||msg=all future excu done!",
                    name, futures.size());
        }
    }

    public List<T> waitResult(){
        List<T> list = Lists.newArrayList();
        futures.forEach(f -> {
            try {
                list.add(f.get());
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        });

        if(!EnvUtil.isOnline()){
            LOGGER.info("class=FutureUtil||method={}||template={}||futuresSize={}||msg=all future excu done!",
                    name, futures.size());
        }
        return list;
    }
}
