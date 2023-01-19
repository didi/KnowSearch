package org.elasticsearch.plugin.spatial.config;

import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2RegionCoverer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class IndexSpatialConfig {
    private final String indexName;
    private final Map<Integer/*city*/, Set<Integer/*shardId*/>> cityShardIdMap = new ConcurrentHashMap<>();
    private final Long[] maxCellId;

    private final String geoField;
    private final String cityField;
    private final Integer maxCell;
    private final Integer maxLevel;

    private final ShardId2Routing shardId2Routing;
    private final int shardNum;


    public IndexSpatialConfig(String indexName, String s2data, String geoField,
                              String cityIdField, Integer maxCell, Integer maxLevel, int shardNum) {
        this.indexName = indexName;

        List<Long> maxCellList = new Vector<>();
        for (String line : s2data.split("#")) {
            if (line == null || line.length() == 0) {
                continue;
            }

            String[] split = line.split(" ");
            int shardId = Integer.parseInt(split[0]);

            long maxCellId = Long.parseLong(split[1]);
            maxCellList.add(maxCellId);

            if (split.length == 3) {
                Set<Integer> cityIds = parseIntSet(split[2]);
                for (int cityId : cityIds) {
                    if (!cityShardIdMap.containsKey(cityId)) {
                        cityShardIdMap.put(cityId, new HashSet<>());
                    }

                    cityShardIdMap.get(cityId).add(shardId);
                }
            }
        }

        maxCellId = maxCellList.toArray(new Long[0]);

        this.geoField = geoField;
        this.cityField = cityIdField;
        this.maxCell = maxCell;
        this.maxLevel = maxLevel;

        this.shardId2Routing = new ShardId2Routing(shardNum);
        this.shardNum = shardNum;
    }


    public String getIndexName() {
        return indexName;
    }

    public String getGeoField() {
        return geoField;
    }

    public String getCityField() {
        return cityField;
    }

    public String toRouting(int shardId) {
        return shardId2Routing.toRouting(shardId);
    }

    public int getShardNum() {
        return shardNum;
    }

    /**
     * 根据多个城市id计算shard集合
     *
     * @param cityIds 城市ID
     * @return shardId集合
     */
    public Set<Integer> getRoutingByCityId(Set<Integer> cityIds) throws Exception {
        Set<Integer> shardIds = new HashSet<>();

        if (cityIds == null) {
            return shardIds;
        }

        for (int cityId : cityIds) {
            shardIds.addAll(getShardIdByCityId(cityId));
        }

        return shardIds;
    }

    /**
     * 根据城市id计算shard集合
     *
     * @param cityId 城市ID
     * @return shardId集合
     */
    private Set<Integer> getShardIdByCityId(int cityId) throws Exception {
        if (!cityShardIdMap.containsKey(cityId)) {
            throw new Exception("unknown city id, id: " + cityId);
        }

        return cityShardIdMap.get(cityId);
    }

    public Set<Integer> getRoutingBySpatial(Double lng, Double lat, Double radius) {
        Set<Integer> shardIds = new HashSet<>();

        if (lng == null || lat == null) {
            return shardIds;
        }

        if (radius == null) {
            shardIds.addAll(getSpatialShardByPoint(maxCell, maxLevel, lng, lat));
        } else {
            shardIds.addAll(getSpatialShardByCap(maxCell, maxLevel, lng, lat, radius));
        }

        return shardIds;
    }

    /**
     * 根据经纬度计算shard
     *
     * @param lng 经度
     * @param lat 纬度
     * @return shards
     */
    private Set<Integer> getSpatialShardByPoint(int maxCell, int maxLevel, double lng, double lat) {
        return calculateShard(getCellId(maxLevel, lng, lat));
    }

    public static long getCellId(int maxLevel, double lng, double lat) {
        S2CellId s2CellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(lat, lng));
        return s2CellId.parent(maxLevel).id();
    }

    private static final double EARTH_RADIUS = 6371393.0;   //  地球半径近似值，单位（米）

    /**
     * 计算球帽覆盖的shard
     *
     * @param lng    经度
     * @param lat    纬度
     * @param radius 半径,单位米
     * @return shardId集合
     */
    private Set<Integer> getSpatialShardByCap(int maxCell, int maxLevel, double lng, double lat, double radius) {
        Set<Integer> shardSet = new HashSet<>();

        S2Point s2Point = S2LatLng.fromDegrees(lat, lng).normalized().toPoint();
        S1Angle s1Angle = S1Angle.radians(radius / EARTH_RADIUS);
        S2Cap s2Cap = S2Cap.fromAxisAngle(s2Point, s1Angle);

        S2RegionCoverer s2RegionCoverer = new S2RegionCoverer();
        s2RegionCoverer.setMaxCells(maxCell);
        s2RegionCoverer.setMaxLevel(maxLevel);
        S2CellUnion s2CellUnion = s2RegionCoverer.getCovering(s2Cap);

        for (S2CellId s2CellId : s2CellUnion) {
            if (s2CellId.level() == maxLevel) {
                shardSet.addAll(calculateShard(s2CellId.id()));
            } else {
                //  level更小时，可能存在跨多个shard的情况
                S2CellId begin = s2CellId.childBegin(maxLevel);
                S2CellId end = s2CellId.childEnd(maxLevel);

                int beginShard = calculateShard(begin.id()).stream().min(Comparator.comparingInt(t -> t)).get();
                //  不包含endShard
                int endShard = calculateShard(end.prev().id()).stream().max(Comparator.comparingInt(t -> t)).get();
                for (int i = beginShard; i <= endShard; i++) {
                    shardSet.add(i);
                }
            }
        }

        return shardSet;
    }

    /**
     * 通过二分查找在maxCellId的集合中查询对应Id所处位置
     *
     * @param cellId cellId
     * @return index
     */
    public Set<Integer> calculateShard(long cellId) {
        Set<Integer> results = new HashSet<>();
        results.add(calculateShardNew(cellId));
        return results;
    }

    public int calculateShardNew(long cellId) {
        return bsearch(maxCellId, maxCellId.length, cellId);
    }

    /**
     * 查找最后一个小于等于给定值的元素下标
     */
    public static int bsearch(Long[] a, int n, Long value) {
        int low = 0;
        int high = n - 1;
        while (low <= high) {
            int mid = low + ((high - low) >> 1);
            if (a[mid] > value) {
                high = mid - 1;
            } else {
                if ((mid == n - 1) || (a[mid + 1] > value)) {
                    return mid;
                } else {
                    low = mid + 1;
                }
            }
        }
        return 0;
    }


    /*
     * 解析
     */
    private Set<Integer> parseIntSet(String str) {
        Set<Integer> ret = new HashSet<>();

        String[] split = str.split(",");
        for (String s : split) {
            ret.add(Integer.parseInt(s));
        }

        return ret;
    }
}
