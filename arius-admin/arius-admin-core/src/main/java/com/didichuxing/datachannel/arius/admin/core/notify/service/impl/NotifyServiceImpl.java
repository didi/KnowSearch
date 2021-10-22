package com.didichuxing.datachannel.arius.admin.core.notify.service.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyChannelEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTool;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.po.notify.NotifyHistoryPO;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.notify.NotifyHistoryDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * 通知服务公司飞鸽有流控，每分钟100条
 * 目前admin分布式部署 没有分布式锁的实现方案；目前邮件通知都是在tts任务中实现的，在规划tts任务执行时间时需要考虑到这点
 * @author didi
 */
@Service
public class NotifyServiceImpl implements NotifyService {

    private static final ILog      LOGGER = LogFactory.getLog(NotifyServiceImpl.class);

    @Autowired
    private NotifyHistoryDAO       notifyHistoryDAO;

    @Autowired
    private NotifyTool             notifyTool;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private AriusTaskThreadPool    ariusTaskThreadPool;

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
                Result sendResult = Result.buildSucc();

                while (tryCount++ <= maxTryCount) {
                    // 发送通知
                    sendResult = sendPerChannel(taskInfo, channelEnum, notifyInfo, receivers);
                    if (sendResult.success()) {
                        break;
                    }

                    LOGGER.warn("method=send||failMsg={}||tryCount={}", sendResult.getMessage(), tryCount);

                    // 间隔1s
                    sleepOneSecond();
                }

                if (sendResult.success()) {
                    LOGGER.info("method=send||type={}||channel={}||msg=sendSucc", taskInfo.getType(),
                        channelEnum.getName());
                } else {
                    String errMsg = sendResult.getMessage();
                    LOGGER.warn("method=send||type={}||channel={}||failMsg={}", taskInfo.getType(),
                        channelEnum.getName(), errMsg);
                    failedMessages.add(errMsg);
                }
            } catch (Exception e) {
                String errMsg = String.format("exception while send info by channel %s because %s",
                    channelEnum.getName(), e.getMessage());
                failedMessages.add(errMsg);
                LOGGER.error("method=send||type={}||channel={}||e->", taskInfo.getType(), channelEnum.getName(), e);
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
            LOGGER.warn("errMsg=InterruptedException", e);
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

        // 获取动态设置的通知配置参数
        JSONObject configTaskInfo = ariusConfigInfoService
            .objectSetting(ARIUS_COMMON_GROUP, "notifyInfo.task.config", new JSONObject(), JSONObject.class)
            .getJSONObject(type.getName());
        if (configTaskInfo == null) {
            configTaskInfo = new JSONObject();
        }
        LOGGER.info("method=getNotifyTaskInfo||type={}||configTaskInfo={}", type.getName(),
            configTaskInfo.toJSONString());

        // 合并通知设置，以动态配置的为default？？
        XContentHelper.mergeDefaults(tgtTaskInfo, configTaskInfo);

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
    private Result sendPerChannel(NotifyTaskInfo taskInfo, NotifyChannelEnum channelEnum, NotifyInfo notifyInfo,
                                  List<String> receivers) {

        // 防疲劳处理
        List<String> receiversFinal = receivers.stream()
            .filter(receiver -> antiFatigue(taskInfo, notifyInfo.getBizId(), receiver, channelEnum))
            .collect(Collectors.toList());

        // 若有mockReceiver则替换通知人
        if (StringUtils.isNotBlank(taskInfo.getMockReceiver())) {
            LOGGER.info("method=sendPerChannel||srcReceiver={}||mockReceiver={}", receiversFinal,
                taskInfo.getMockReceiver());
            receiversFinal = Lists.newArrayList(taskInfo.getMockReceiver());
        }

        if (CollectionUtils.isEmpty(receiversFinal)) {
            return Result.buildSucWithTips("全部被防疲劳拦截");
        }

        Result sendResult;

        // 发送通知
        switch (channelEnum) {
            case EMAIL:
                sendResult = notifyTool.byEmail(notifyInfo.getTitle(), String.join(",", receiversFinal), null,
                    notifyInfo.getMailContent());
                break;
            case SMS:
                sendResult = notifyTool.bySms(String.join(",", receiversFinal), notifyInfo.getSmsContent());
                break;
            case VOICE:
                sendResult = notifyTool.byVoice(String.join(",", receiversFinal), notifyInfo.getVoiceContent());
                break;
            default:
                sendResult = Result.buildFail(String.format("非法的通知渠道：%s", channelEnum.getName()));
        }

        // 保存通知记录
        if (sendResult.success()) {
            saveNotifyRecord(taskInfo, notifyInfo.getBizId(), receiversFinal, channelEnum);
        }

        return sendResult;
    }

    /**
     * 保存消息记录
     * @param taskInfo       任务信息
     * @param bizId          业务ID
     * @param receiversFinal 接收人
     * @param channelEnum    通道
     */
    private void saveNotifyRecord(NotifyTaskInfo taskInfo, String bizId, List<String> receiversFinal,
                                  NotifyChannelEnum channelEnum) {
        for (String receiver : receiversFinal) {
            NotifyHistoryPO historyPo = new NotifyHistoryPO();
            historyPo.setTaskType(taskInfo.getType());
            historyPo.setBizId(bizId);
            historyPo.setReceiver(receiver);
            historyPo.setChannel(channelEnum.getName());
            notifyHistoryDAO.insert(historyPo);
        }
    }

    /**
     * 防疲劳控制
     * @param taskInfo    任务信息
     * @param bizId       业务ID
     * @param receiver    接收人
     * @return true 可以发  false 不可以发
     */
    private boolean antiFatigue(NotifyTaskInfo taskInfo, String bizId, String receiver, NotifyChannelEnum channelEnum) {

        // 明确指定不防疲劳的情况
        if (NotifyConstant.IGNORE_ANTI_FATIGUE_BIZ_ID.equals(bizId)) {
            return true;
        }

        int maxSendCountPerDay = taskInfo.getMaxSendCountPerDay();
        int sendIntervalMinutes = taskInfo.getSendIntervalMinutes();

        // 没有 每天最大发送次数控制 和 发送间隔控制
        if (maxSendCountPerDay <= 0 && sendIntervalMinutes <= 0) {
            return true;
        }

        Date now = new Date();
        Date startTime = AriusDateUtils.getZeroDate(now);

        // 获取今天的发送记录，按着发送时间降序排列
        List<NotifyHistoryPO> historyPos = notifyHistoryDAO.getByKeyAndTime(taskInfo.getType(), bizId, receiver,
            channelEnum.getName(), startTime, now);

        // 今天没有发送消息，可以发送
        if (CollectionUtils.isEmpty(historyPos)) {
            return true;
        }

        // 每天最大发送次数控制
        if (maxSendCountPerDay > 0 && historyPos.size() >= maxSendCountPerDay) {
            LOGGER.info("method=antiFatigue||type={}||bizId={}||receiver={}||sendCount={}", taskInfo.getType(), bizId,
                receiver, historyPos.size());
            return false;
        }

        // 发送间隔控制
        if (sendIntervalMinutes > 0) {
            long interval = now.getTime() - historyPos.get(0).getCreateTime().getTime();
            int millisecondUnit = 60 * 1000;
            if (interval < sendIntervalMinutes * millisecondUnit) {
                LOGGER.info("method=antiFatigue||type={}||bizId={}||receiver={}||interval={}", taskInfo.getType(),
                    bizId, receiver, interval);
                return false;
            }
        }

        return true;
    }
}
