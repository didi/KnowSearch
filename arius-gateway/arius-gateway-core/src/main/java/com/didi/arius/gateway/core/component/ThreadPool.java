package com.didi.arius.gateway.core.component;

import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component("threadPool")
public class ThreadPool {

	private ScheduledExecutorService scheduleThreadPool;
	private int scheduleThreadNum = 16;
	private int searchSize = 100;
	private int searchQueueSize = 1000;
	
	private Executor searchExecutor;
	
	@PostConstruct
	public void init() {
		scheduleThreadPool = Executors.newScheduledThreadPool(
				scheduleThreadNum, new DeamondThreadFactory(
						"scheduleThreadPool"));
		
		searchExecutor = EsExecutors.newFixed("search", searchSize, searchQueueSize, new DeamondThreadFactory(
				"searchThreadPool"));
	}
	
	public void submitScheduleAtFixTask(Runnable runnable, long initialDelay,
			long period) {
		scheduleThreadPool.scheduleAtFixedRate(runnable, initialDelay, period,
				TimeUnit.SECONDS);
	}
	
}

class DeamondThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	static {
		ThreadRenamingRunnable
				.setThreadNameDeterminer( (currentThreadName, proposedThreadName) -> currentThreadName );
	}

	public DeamondThreadFactory(String prefix) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		namePrefix = prefix + "-pool-" + poolNumber.getAndIncrement()
				+ "-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix
				+ threadNumber.getAndIncrement(), 0);
		if (!t.isDaemon())
			t.setDaemon(true);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	};
}