package com.didi.arius.gateway.common.event;

import com.didi.arius.gateway.common.metadata.QueryContext;

public class QueryPostResponseEvent extends PostResponseEvent {

    private QueryContext queryContext;

    public QueryPostResponseEvent(Object source, QueryContext queryContext) {
        super( source );
        this.queryContext = queryContext;
    }

    public QueryContext getQueryContext(){
        return queryContext;
    }
}
