package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FastDumpTaskLogVO {
    private String logType;
    private String traceId;
    private String hostName;
    private String fileName;
    private String logThread;
    private String ip;
    private String methodName;
    private String className;
    private String message;
    private String spanId;
    private String logName;
    private String logLevel;
    private Long logMills;
    private Integer lineNumber;
    private String applicationName;
}
