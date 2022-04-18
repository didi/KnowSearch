package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.N9eData;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author d06679
 * @date 2019-07-23
 */
@Component
public class MonitorDataSender {

    private static final ILog               LOGGER = LogFactory.getLog( MonitorDataSender.class);

    private LinkedBlockingQueue<N9eData>    queue  = new LinkedBlockingQueue<>();

    @Autowired
    private AriusOpThreadPool               ariusOpThreadPool;

    @Autowired
    private RemoteMonitorService            remoteMonitorService;

    /**
     * 表示SendTask是否处于运行中，true：运行 false：停止
     */
    private volatile boolean sendTaskIsOpen = true;

    public void send(N9eData data) {
        queue.add(data);
    }

    public void batchSend(List<N9eData> datas) {
        queue.addAll(datas);
    }

    @PostConstruct
    public void start() {
        LOGGER.info("class=MonitorDataSender||method=init||MonitorDataSender init start.");
        ariusOpThreadPool.run(new SendTask());
        LOGGER.info("class=MonitorDataSender||method=init||MonitorDataSender init finished, sender thread start");
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            while (sendTaskIsOpen) {
                try {
                    List<N9eData> toSend = Lists.newArrayList();
                    N9eData n9eData = null;
                    for (int size = 0; size < 2000; size++) {
                        if (toSend.size() == 0) {
                            n9eData = queue.take();
                        } else {
                            n9eData = queue.poll();
                            if (n9eData == null) {
                                break;
                            }
                        }
                        toSend.add(n9eData);
                    }
                    send(toSend);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("class=MonitorDataSender||method=SendTask.run||warnMsg={}", e.getMessage(), e);
                } catch (Exception e) {
                    LOGGER.error("class=MonitorDataSender||method=SendTask.run||errMsg={}", e.getMessage(), e);
                }
            }
        }

        private void send(List<N9eData> toSend) {
            if (EnvUtil.isOnline() || EnvUtil.isPre()) {
                if (remoteMonitorService.sendData(toSend)) {
                    LOGGER.info("class=MonitorDataSender||method=send||msg=send succ {}", toSend.size());
                } else {
                    LOGGER.warn("class=MonitorDataSender||method=send||msg=send fail {}", toSend.size());
                }
            }
        }
    }
}
