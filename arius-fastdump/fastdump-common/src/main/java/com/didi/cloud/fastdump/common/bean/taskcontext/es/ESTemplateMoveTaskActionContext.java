package com.didi.cloud.fastdump.common.bean.taskcontext.es;

import com.didi.cloud.fastdump.common.bean.reader.es.ESTemplateReader;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESTemplateDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESTemplateSource;
import com.didi.cloud.fastdump.common.bean.taskcontext.BaseTaskActionContext;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class ESTemplateMoveTaskActionContext extends BaseTaskActionContext<ESTemplateSource, ESTemplateReader, ESTemplateDataSinker> {
}
