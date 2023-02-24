package com.didi.cloud.fastdump.common.bean.taskcontext.es;

import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReader;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.bean.taskcontext.BaseTaskActionContext;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class ESIndexMoveTaskActionContext extends BaseTaskActionContext<ESIndexSource, LuceneReader, ESIndexDataSinker> {
}
