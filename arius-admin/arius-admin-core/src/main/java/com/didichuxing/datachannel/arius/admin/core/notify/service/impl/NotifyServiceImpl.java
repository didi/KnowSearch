package com.didichuxing.datachannel.arius.admin.core.notify.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.core.notify.*;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务公司飞鸽有流控，每分钟100条
 * 目前admin分布式部署 没有分布式锁的实现方案；目前邮件通知都是在tts任务中实现的，在规划tts任务执行时间时需要考虑到这点
 * @author didi
 */
@Service
public class NotifyServiceImpl implements NotifyService {

    private static final ILog      LOGGER = LogFactory.getLog(NotifyServiceImpl.class);

    @Autowired
    private NotifyTool             notifyTool;

    private AriusTaskThreadPool    ariusTaskThreadPool;

    @PostConstruct
    public void init(){
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(3, "NotifyServiceImpl", 100);
    }

    @Override
    public Result send(NotifyTaskTypeEnum type, NotifyInfo notifyInfo, List<String> receivers) {

        if (type == null || notifyInfo == null || CollectionUtils.isEmpty(receivers)) {
            return Result.buildParamIllegal("参数非法");
        }

        // 构建通知任务
        NotifyTaskInfo taskInfo = getNotifyTaskInfo(type);

        // 获取通知通道
        Set<NotifyChannelEnum> channelEnums = taskInfo.getChannels().stream().map(NotifyChannelEnum::valueByDesc)
            .collect(Collectors.toSet());

        // 记录错误信息
        List<String> failedMessages = Lists.newArrayList();
        // 遍历通道，发送通知
        for (NotifyChannelEnum channelEnum : channelEnums) {
            try {
                // 尝试次数
                int tryCount = 0;
                // 最大尝试次数
                int maxTryCount = 100;
                Result<Void> sendResult = Result.buildSucc();

                while (tryCount++ <= maxTryCount) {
                    // 发送通知
                    sendResult = sendPerChannel(taskInfo, channelEnum, notifyInfo, receivers);
                    if (sendResult.success()) {
                        break;
                    }

                    LOGGER.warn("class=NotifyServiceImpl||method=send||failMsg={}||tryCount={}", sendResult.getMessage(), tryCount);

                    // 间隔1s
                    sleepOneSecond();
                }

                if (sendResult.success()) {
                    LOGGER.info("class=NotifyServiceImpl||method=send||type={}||channel={}||msg=sendSucc", taskInfo.getType(),
                        channelEnum.getName());
                } else {
                    String errMsg = sendResult.getMessage();
                    LOGGER.warn("class=NotifyServiceImpl||method=send||type={}||channel={}||failMsg={}", taskInfo.getType(),
                        channelEnum.getName(), errMsg);
                    failedMessages.add(errMsg);
                }
            } catch (Exception e) {
                String errMsg = String.format("exception while send info by channel %s because %s",
                    channelEnum.getName(), e.getMessage());
                failedMessages.add(errMsg);
                LOGGER.error("class=NotifyServiceImpl||method=send||type={}||channel={}||e->", taskInfo.getType(), channelEnum.getName(), e);
            }
        }

        //简单的sleep是为了应对飞鸽的流控，
        sleepOneSecond();

        if (CollectionUtils.isEmpty(failedMessages)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", failedMessages));
    }

    @Override
    public void sendAsync(NotifyTaskTypeEnum type, NotifyInfo data, List<String> receivers) {
        ariusTaskThreadPool.run(() -> send(type, data, receivers));
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.warn("class=NotifyServiceImpl||method=sleepOneSecond||errMsg=InterruptedException", e);
            Thread.currentThread().interrupt();
        }
    }

    private NotifyTaskInfo getNotifyTaskInfo(NotifyTaskTypeEnum type) {
        // 构建默认NotifyTaskInfo
        NotifyTaskInfo taskInfo = new NotifyTaskInfo();
        taskInfo.setType(type.getName());
        taskInfo.setChannels(type.getChannels());
        taskInfo.setMaxSendCountPerDay(type.getMaxSendCountPerDay());
        taskInfo.setSendIntervalMinutes(type.getSendIntervalMinutes());

        JSONObject tgtTaskInfo = JSON.parseObject(JSON.toJSONString(taskInfo));

        return JSON.parseObject(tgtTaskInfo.toJSONString(), NotifyTaskInfo.class);
    }

    /**
     * 单独渠道的通知
     * @param taskInfo    任务信息
     * @param channelEnum 通道
     * @param notifyInfo  消息内容
     * @param receivers   接收人
     * @return Result
     */
    private Result<Void> sendPerChannel(NotifyTaskInfo taskInfo, NotifyChannelEnum channelEnum, NotifyInfo notifyInfo,
                                  List<String> receivers) {
        // 若有mockReceiver则替换通知人
        if (StringUtils.isNotBlank(taskInfo.getMockReceiver())) {
            receivers = Lists.newArrayList(taskInfo.getMockReceiver());

            LOGGER.info("class=NotifyServiceImpl||method=sendPerChannel||srcReceiver={}||mockReceiver={}", receivers,
                    taskInfo.getMockReceiver());
        }

        Result<Void> sendResult;

        // 发送通知
        switch (channelEnum) {
            case EMAIL:
                sendResult = notifyTool.byEmail(notifyInfo.getTitle(), String.join(",", receivers), null,
                    notifyInfo.getMailContent());
                break;
            case SMS:
                sendResult = notifyTool.bySms(String.join(",", receivers), notifyInfo.getSmsContent());
                break;
            case VOICE:
                sendResult = notifyTool.byVoice(String.join(",", receivers), notifyInfo.getVoiceContent());
                break;
            default:
                sendResult = Result.buildFail(String.format("非法的通知渠道：%s", channelEnum.getName()));
        }

        return sendResult;
    }
}
