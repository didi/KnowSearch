package com.didi.cloud.fastdump.core.service.source;

import com.didi.cloud.fastdump.common.bean.source.BaseSource;

/**
 * Created by linyunan on 2022/8/10
 */
public interface FileSourceService<Source extends BaseSource> {
      /**
       * 定位文件相关信息
       */
      void parseSource(Source Source) throws Exception;
}
