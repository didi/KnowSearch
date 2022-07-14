package com.didichuxing.datachannel.arius.admin.common.util;

import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.*;

/**
 * Created by linyunan on 2021-06-29
 */
public class ClusterUtils {

    private ClusterUtils() {
    }

    public static String getDuplicateIp(List<String> ips) {
        if (CollectionUtils.isEmpty(ips)) {
            return null;
        }

        Set<String> tempSet = Sets.newHashSet();
        for (String ip : ips) {
            if (tempSet.contains(ip)) {
                return ip;
            }

            tempSet.add(ip);
        }

        return null;
    }

    public static Integer getClusterLogicHealthByClusterHealth(Set<Integer> clusterHealthSet) {
        if (CollectionUtils.isEmpty(clusterHealthSet) || clusterHealthSet.contains(UNKNOWN.getCode())) {
            return UNKNOWN.getCode();
        }

        if (clusterHealthSet.contains(RED.getCode())) {
            return RED.getCode();
        }

        if (clusterHealthSet.contains(YELLOW.getCode())) {
            return YELLOW.getCode();
        }

        return GREEN.getCode();
    }
}
