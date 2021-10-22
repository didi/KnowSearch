package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.QueryContext;
import org.elasticsearch.rest.RestStatus;

import java.util.List;

public class CommonUtil {
    public static boolean isIndexType(QueryContext queryContext) {
        if (!isSearchKibana(queryContext.getUri(), queryContext.getIndices()) && queryContext.getAppDetail().getSearchType() == AppDetail.RequestType.Index) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isIndexType(QueryContext queryContext, List<String> indices) {
        if (!isSearchKibana(queryContext.getUri(), indices) && queryContext.getAppDetail().getSearchType() == AppDetail.RequestType.Index) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSearchKibana(String uri, List<String> indices) {
        if (uri != null && (uri.startsWith("/."))) {
            return true;
        } else if (indices != null && indices.size() > 0) {
            for (String index : indices) {
                if (index.startsWith(".")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static RestStatus fromCode(int code){
       for(RestStatus restStatus : RestStatus.values()){
           if(code == restStatus.getStatus()){
               return restStatus;
           }
       }

        return null;
    }
}
