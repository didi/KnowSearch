package com.didichuxing.arius.admin.extend.fastindex.bean.entity;

import lombok.Data;

@Data
public class SpatialInfo {
    private String s2Data;
    private String geoName;
    private String cityIdName;
    private Integer maxCell = 10;
    private Integer maxLevel = 12;
}
