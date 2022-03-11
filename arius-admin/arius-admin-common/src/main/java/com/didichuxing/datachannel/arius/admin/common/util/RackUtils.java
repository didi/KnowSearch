package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author d06679
 * @date 2019/4/3
 */
public class RackUtils {

    private RackUtils(){}

    public static String merge(String srcRack, String tgtRack) {
        if (StringUtils.isEmpty(srcRack) && StringUtils.isEmpty(tgtRack)) {
            return "";
        }

        if (StringUtils.isEmpty(srcRack)) {
            return tgtRack;
        }

        if (StringUtils.isEmpty(tgtRack)) {
            return srcRack;
        }

        Set<String> srcRackSet = Sets.newHashSet(srcRack.split(AdminConstant.RACK_COMMA));
        Set<String> tgtRackSet = Sets.newHashSet(tgtRack.split(AdminConstant.RACK_COMMA));
        srcRackSet.addAll(tgtRackSet);

        List<String> rackList = Lists.newArrayList(srcRackSet);
        rackList.sort(RackUtils::compareByName);

        return String.join(AdminConstant.RACK_COMMA, rackList);
    }

    public static String append(String srcRack, Collection<String> appendRacks) {
        if(StringUtils.isEmpty(srcRack)){
            return String.join(AdminConstant.RACK_COMMA, appendRacks);
        }

        if (CollectionUtils.isEmpty(appendRacks)) {
            return srcRack;
        }

        Set<String> srcRackSet = Sets.newHashSet(srcRack.split(AdminConstant.RACK_COMMA));
        Set<String> appendRackSet = Sets.newHashSet(appendRacks);
        appendRackSet.removeAll(srcRackSet);

        if (CollectionUtils.isEmpty(appendRackSet)) {
            return srcRack;
        }

        List<String> appendRackList = Lists.newLinkedList(appendRackSet);
        appendRackList.sort(RackUtils::compareByName);

        return srcRack + AdminConstant.RACK_COMMA + String.join(AdminConstant.RACK_COMMA, appendRackList);
    }

    public static int compareByName(String rack1, String rack2) {
        if (rack1.length() == rack2.length()) {
            return rack1.compareTo(rack2);
        } else {
            return rack1.length() - rack2.length();
        }
    }

    /**
     * 从racks中去掉racksToRemove
     * @param racks racks，逗号分隔
     * @param racksToRemove 需要去掉的racks
     * @return 去掉racksToRemove后的racks（即racks对racksToRemove的差集）
     */
    public static String removeRacks(String racks, Collection<String> racksToRemove) {
        List<String> rackList = racks2List(racks);
        rackList.removeAll(racksToRemove);
        return collection2Racks(rackList);
    }

    /**
     * 从racks中去掉racksToRemove
     * @param racks racks，逗号分隔
     * @param racksToRemove 需要去掉的racks，逗号分隔
     * @return 去掉racksToRemove后的racks（即racks对racksToRemove的差集）
     */
    public static String removeRacks(String racks, String racksToRemove) {
        return removeRacks(racks, racks2Set(racksToRemove));
    }

    /**
     * 判断racks中是否使用到了rackPool中的资源
     * @param racks racks
     * @param rackPool rack池
     * @return true/false
     */
    public static boolean hasIntersect(String racks, Collection<String> rackPool) {
        if (CollectionUtils.isEmpty(rackPool)) {
            return false;
        }

        Set<String> rackSet = Sets.newHashSet(rackPool);
        for (String rack : racks.split(AdminConstant.RACK_COMMA)) {
            if (rackSet.contains(rack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断racks中是否使用到了rackPool中的资源
     * @param racks racks
     * @param rackPool rack池
     * @return true/false
     */
    public static boolean hasIntersect(String racks, String rackPool) {
        if (StringUtils.isEmpty(rackPool)) {
            return false;
        }

       return hasIntersect(racks, racks2Set(rackPool));
    }

    public static boolean same(String rack1, String rack2) {

        if (rack1 == null && rack2 == null) {
            return true;
        }

        if (rack1 == null || rack2 == null) {
            return false;
        }

        Set<String> rackSet1 = Sets.newHashSet(rack1.split(AdminConstant.RACK_COMMA));
        Set<String> rackSet2 = Sets.newHashSet(rack2.split(AdminConstant.RACK_COMMA));

        return rackSet1.containsAll(rackSet2) && rackSet2.containsAll(rackSet1);
    }

    /**
     * 计算racks相似度
     * @param racks1 racks1
     * @param racks2 racks2
     */
    public static double similarityDegree(String racks1, String racks2) {

        if (racks1 == null && racks2 == null) {
            return 1.0;
        }

        if (racks1 == null || racks2 == null) {
            return 0.0;
        }

        Set<String> rackSet1 = RackUtils.racks2Set(racks1);
        Set<String> rackSet2 = RackUtils.racks2Set(racks2);

        double countAvg = (rackSet1.size() + rackSet2.size()) * 1.0 / 2;

        // 取交集
        rackSet1.retainAll(rackSet2);

        return rackSet1.size() / countAvg;
    }

    public static boolean belong(String srcRack, String tgtRack) {
        if (similarityDegree(srcRack, tgtRack) > 0.49) {
            return true;
        }

        if(srcRack == null || tgtRack == null){
            return false;
        }

        Set<String> srcRackSet = RackUtils.racks2Set(srcRack);
        Set<String> tgtRackSet = RackUtils.racks2Set(tgtRack);

        return tgtRackSet.containsAll(srcRackSet);
    }

    public static Set<String> racks2Set(String racks) {
        if (StringUtils.isEmpty(racks)) {
            return new HashSet<>();
        }

        return Sets.newHashSet(racks.split(AdminConstant.RACK_COMMA));
    }

    public static List<String> racks2List(String racks) {
        if (StringUtils.isEmpty(racks)) {
            return new ArrayList<>();
        }

        return Lists.newArrayList(racks.split(AdminConstant.RACK_COMMA));
    }

    public static String list2Racks(List<String> racks) {
        return collection2Racks(racks);
    }


    public static String collection2Racks(Collection<String> racks) {
        if (CollectionUtils.isEmpty(racks)){
            return "";
        }

        return String.join(AdminConstant.RACK_COMMA, racks);
    }
}
