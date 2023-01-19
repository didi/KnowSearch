package org.elasticsearch.plugin.spatial.util;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.plugin.spatial.config.IndexSpatialConfig;
import org.elasticsearch.plugin.spatial.router.RouterParam;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.List;

public class SpatialUtils {

    public static RouterParam getSpatialInfo(IndexSpatialConfig indexConfig, SearchSourceBuilder searchSourceBuilder) {
        RouterParam rp = new RouterParam();

        QueryBuilder queryBuilder = searchSourceBuilder.query();
        getSpaitalInfo(queryBuilder, rp, indexConfig);

        if(queryBuilder instanceof FunctionScoreQueryBuilder) {
            QueryBuilder innerQueryBuild = ((FunctionScoreQueryBuilder)queryBuilder).query();
            getSpaitalInfo(innerQueryBuild, rp, indexConfig);
        }

        return rp;
    }

    private static void getSpaitalInfo(QueryBuilder queryBuilder, RouterParam rp, IndexSpatialConfig indexConfig) {
        if (!(queryBuilder instanceof BoolQueryBuilder)) {
            return ;
        }

        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) queryBuilder;
        if (boolQueryBuilder.filter() == null) {
            return ;
        }

        List<QueryBuilder> filterList = boolQueryBuilder.filter();
        for (QueryBuilder fqb : filterList) {

            if (fqb instanceof GeoDistanceQueryBuilder) {
                GeoDistanceQueryBuilder geo = (GeoDistanceQueryBuilder) fqb;
                if (indexConfig.getGeoField().equals(geo.fieldName())) {
                    // TODO 处理单位
                    rp.radius = geo.distance();
                    rp.lat = geo.point().lat();
                    rp.lng = geo.point().lon();
                }
            }

            if (fqb instanceof TermQueryBuilder) {
                TermQueryBuilder termQueryBuilder = (TermQueryBuilder) fqb;
                if (termQueryBuilder.fieldName().equals(indexConfig.getCityField())) {
                    rp.cityIds.add(getCityIdValue(termQueryBuilder.value()));
                }
            }

            if (fqb instanceof TermsQueryBuilder) {
                TermsQueryBuilder termsQueryBuilder = (TermsQueryBuilder) fqb;
                if (termsQueryBuilder.fieldName().equals(indexConfig.getCityField())) {
                    for (Object value : termsQueryBuilder.values()) {
                        rp.cityIds.add(getCityIdValue(value));
                    }
                }
            }
        }
    }

    private static int getCityIdValue(Object value) {
        if(value==null) {
            throw new RuntimeException("city id value is null");
        }

        if(value instanceof BytesRef) {
            value = ((BytesRef)value).utf8ToString();
        }

        return Integer.valueOf(value.toString());
    }

//    /**
//     * 将ES中distance字符串转成double，单位为米
//     * @param distanceStr 距离字符串
//     * @return 距离
//     */
//    private double convertDistance(String distanceStr) {
//        double distance;
//        if (distanceStr.endsWith("km")) {
//            distance = Double.parseDouble(distanceStr.substring(0, distanceStr.length() - 2)) * 1000.0;
//        } else if (distanceStr.endsWith("m")) {
//            distance = Double.parseDouble(distanceStr.substring(0, distanceStr.length() - 1));
//        } else {
//            distance = Double.parseDouble(distanceStr);
//        }
//
//        return distance;
//    }


}
