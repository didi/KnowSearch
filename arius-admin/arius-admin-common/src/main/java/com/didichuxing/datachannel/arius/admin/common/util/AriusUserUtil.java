package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Sets;

/**
 * 是一些arius用户id的工具类
 * Created by d06679 on 2018/9/8.
 */
public class AriusUserUtil {

    public static String userIds2Str(Collection<Long> userIds) {

        if (CollectionUtils.isEmpty(userIds)) {
            return "";
        }

        List<String> userIdStrs = userIds.stream().map(String::valueOf).collect(Collectors.toList());

        return String.join(",", userIdStrs);
    }

    public static Set<Long> getUserIdsByStr(String userIdStr) {
        Set<Long> userIds = Sets.newHashSet();
        for (String userId : userIdStr.split(",")) {
            userIds.add(Long.valueOf(userId));
        }
        return userIds;
    }


    public static boolean userIdsIsSame(Set<Long> newUserIds, Set<Long> oldUserIds) {

        if (CollectionUtils.isEmpty(newUserIds) && CollectionUtils.isEmpty(oldUserIds)) {
            return true;
        }

        if (CollectionUtils.isEmpty(newUserIds) || CollectionUtils.isEmpty(oldUserIds)) {
            return false;
        }

        for (Long userId : newUserIds) {
            if (!oldUserIds.contains(userId)) {
                return false;
            }
        }

        for (Long userId : oldUserIds) {
            if (!newUserIds.contains(userId)) {
                return false;
            }
        }

        return true;
    }
}
