package com.didichuxing.datachannel.arius.admin.common.util;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

/**
 * Created by linyunan on 2021-06-29
 */
public class ClusterUtils {
	public static String getDuplicateIp(List<String> ips) {
		Set<String> tempSet = Sets.newHashSet();
		for (String ip : ips) {
			if (tempSet.contains(ip)) {
				return ip;
			}

			tempSet.add(ip);
		}

		return null;
	}
}
