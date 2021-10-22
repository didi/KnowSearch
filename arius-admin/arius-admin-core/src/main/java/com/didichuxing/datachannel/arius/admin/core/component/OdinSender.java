package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
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
public class OdinSender {

    private static final ILog             LOGGER = LogFactory.getLog(OdinSender.class);

    private LinkedBlockingQueue<OdinData> queue  = new LinkedBlockingQueue<>();

    @Autowired
    private AriusTaskThreadPool           ariusTaskThreadPool;

    @Autowired
    private RemoteMonitorService          remoteMonitorService;

    public void send(OdinData data) {
        queue.add(data);
    }

    public void batchSend(List<OdinData> datas) {
        queue.addAll(datas);
    }

    @PostConstruct
    public void start() {
        LOGGER.info("class=OdinSender||method=init||OdinSender init start.");
        ariusTaskThreadPool.run(new SendTask());
        LOGGER.info("class=OdinSender||method=init||OdinSender init finished, sender thread start");
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    List<OdinData> toSend = Lists.newArrayList();
                    OdinData odinData = null;
                    for (int size = 0; size < 2000; size++) {
                        if (toSend.size() == 0) {
                            odinData = queue.take();
                        } else {
                            odinData = queue.poll();
                            if (odinData == null) {
                                break;
                            }
                        }
                        toSend.add(odinData);
                    }
                    send(toSend);
                } catch (Exception e) {
                    LOGGER.error("class=OdinSender||method=SendTask.run||errMsg={}", e.getMessage(), e);
                }
            }
        }

        private void send(List<OdinData> toSend) {
            if (EnvUtil.isOnline() || EnvUtil.isPre()) {
                if (remoteMonitorService.sendData(toSend)) {
                    LOGGER.info("method=send||msg=send odin succ {}", toSend.size());
                } else {
                    LOGGER.warn("method=send||msg=send odin fail {}", toSend.size());
                }
            }
        }
    }
}
