package com.didi.cloud.fastdump.core.service.reader;

import com.didi.cloud.fastdump.common.bean.reader.BaseReader;

/**
 * Created by linyunan on 2022/8/10
 */
public interface FileReaderService<Reader extends BaseReader> {
    void parseReader(Reader BaseESReader) throws Exception;
}
