package com.didi.cloud.fastdump.common.bean.reader.es;

import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;

import lombok.Data;

/**
 * Created by linyunan on 2022/10/12
 */
@Data
public class LuceneReaderWithESIndexSinker extends LuceneReader{
    private ESIndexDataSinker esIndexDataSinker;
}
