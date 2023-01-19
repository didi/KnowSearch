package org.elasticsearch.tasks;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.Map;

public abstract class AutoCancellableTask extends CancellableTask implements Runnable {

    private volatile Scheduler.Cancellable cancellable;
    private Runnable timeoutRunnable = null;

    public AutoCancellableTask(long id, String type, String action, String description, TaskId parentTaskId, Map<String, String> headers) {
        super(id, type, action, description, parentTaskId, headers);
    }

    @Override
    public void run() {
        cancellable = null;
        if (timeoutRunnable != null) {
            timeoutRunnable.run();
        }
        cancel("auto cancellable task time out");
    }

    public void cancel() {
        if (cancellable != null) {
            timeoutRunnable = null;
            try {
                cancellable.cancel();
            } catch (Exception e) {
            }
            cancellable = null;
        }
    }

    public void scheduleTimeout(ThreadPool threadPool, TimeValue timeout) {
        if (cancellable != null || timeout.millis() <= 0L) {
            return;
        }
        cancellable = threadPool.schedule(this, timeout, ThreadPool.Names.SEARCH_CHECKER);
    }

    public void setTimeoutRunnable(Runnable timeoutRunnable) {
        this.timeoutRunnable = timeoutRunnable;
    }

}
