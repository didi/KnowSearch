package com.didi.cloud.fastdump.common.bean.taskcontext;

import com.didi.cloud.fastdump.common.action.BaseActionContext;
import com.didi.cloud.fastdump.common.bean.reader.BaseReader;
import com.didi.cloud.fastdump.common.bean.sinker.BaseDataSinker;
import com.didi.cloud.fastdump.common.bean.source.BaseSource;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/11
 */
@Data
public abstract class BaseTaskActionContext<Source extends BaseSource, Reader extends BaseReader, Sinker extends BaseDataSinker>
                                           extends BaseActionContext {
    /**
     * 任务id
     */
    private String taskId;
    /**
     * @see com.didi.cloud.fastdump.common.enums.TaskStatusEnum
     */
    private String status;

    /**
     * ES索引source端信息
     */
    private Source source;

    /**
     * ES索引reader端信息
     */
    private Reader reader;

    /**
     * ES索引sinker端信息
     */
    private Sinker sinker;

}
