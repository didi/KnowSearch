package com.didi.cloud.fastdump.core.action.movetask;

import static com.didi.cloud.fastdump.common.utils.BaseHttpUtil.buildHeader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpOperateException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.utils.BaseHttpUtil;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class PauseIndexMoveAction implements Action<String, Boolean> {
    @Value("${fastdump.httpTransport.port:8300}")
    private int                           httpPort;

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("RestPauseIndexMoveAction-FutureUtil", 5, 5, 1000);

    @Autowired
    private IndexMoveTaskMetadata         indexMoveTaskMetadata;

    @Override
    public Boolean doAction(String taskId) throws Exception {
        if (indexMoveTaskMetadata.isTaskSucc(taskId)) {
            throw new BaseException(String.format("index move task[%s] is succ", taskId), ResultType.ILLEGAL_PARAMS);
        }

        List<String> ipList = indexMoveTaskMetadata.getTaskIpList(taskId);
        if (CollectionUtils.isEmpty(ipList)) {
            throw new BaseException(String.format("index move task[%s] is not exist", taskId), ResultType.ILLEGAL_PARAMS);
        }

        List<Result> resultList = new CopyOnWriteArrayList<>();
        for (String ip : ipList) {
            FUTURE_UTIL.runnableTask(() -> {
                Result result;
                try {
                    result = RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                            "PauseIndexMoveAction",
                            RetryUtil.DEFAULT_TIME,
                            RetryUtil.DEFAULT_INTERVAL_MILLS,
                            () -> {
                                String res = BaseHttpUtil.putForString(buildHttpUrl(new String[]{ip, taskId}),
                                        null, buildHeader());
                                return ConvertUtil.str2ObjByJson(res, Result.class);
                            }
                    );
                } catch (Exception e) {
                    result = Result.buildFail(String.format("ip:%s,", ip) + "err:" + e.getMessage());
                }
                resultList.add(result);
            });
        }
        FUTURE_UTIL.waitExecute();

        StringBuilder err = new StringBuilder();
        for (Result result : resultList) {
            if (result.failed()) { err.append(result.getMessage()).append("\n");}
        }

        if (err.length() > 0) {
            throw new FastDumpOperateException(String.format("触发成功, 部分节点上的索引shard失败, detail:%s", err));
        }

        return true;
    }

    protected String buildHttpUrl(String[] args) {
        String ip = args[0], taskId = args[1];
        return "http://" + ip + ":" + httpPort + "/index-node-move/" + taskId + "/stop";
    }
}
