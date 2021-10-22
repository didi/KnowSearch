package com.didi.arius.gateway.dsl.dsl.parser;

public enum ParserType {
    COMMON("root"),
    QUERY("query"),
    AGGR("aggr");

    private String value;

    ParserType(String value) {
        this.value = value;
    }
}
