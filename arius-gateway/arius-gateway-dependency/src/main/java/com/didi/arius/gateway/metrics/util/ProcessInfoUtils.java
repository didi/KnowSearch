package com.didi.arius.gateway.metrics.util;

import org.apache.commons.lang.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.UnknownHostException;

public class ProcessInfoUtils {

    private static String hostname  = "";

    private static String processId = "";

    public static String getHostname() {
        if (!StringUtils.isEmpty(hostname)) {
            return hostname;
        }
        try {
            hostname = DNS.getDefaultHost("default", "default");
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
        return hostname;
    }

    /**
     * 获取进程的ID
     * 
     * @return 进程ID
     */
    public static String getProcessId() {
        if (StringUtils.isNotBlank(processId)) {
            return processId;
        }

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String processName = runtimeMXBean.getName();
        processId = processName.substring(0, processName.indexOf('@'));
        return processId;

    }
}
