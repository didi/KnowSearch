package com.didi.arius.gateway.dsl.dsl.parser;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;

import java.util.HashMap;
import java.util.Map;

public class ParserRegister {
    // 各种解析器
    private static Map<String, DslParser> common = new HashMap<>();

    private static Map<String, DslParser> query = new HashMap<>();

    private static Map<String, DslParser> aggr = new HashMap<>();

    public static void registe(ParserType type, String name, DslParser parser) {
        Map<String, DslParser> m = getMap(type);
        assert m != null;
        m.put(name, parser);
    }

    public static KeyWord parse(ParserType type, String name, Object obj) throws Exception {
        Map<String, DslParser> m = getMap(type);

        assert m != null;
        DslParser parser  = m.get(name);

        if(parser==null) {
            // System.out.println("can't find parser " + name);
            return null;
        }

        return parser.parse(name, obj);
    }

    private static Map<String, DslParser> getMap(ParserType type) {
        switch (type) {
            case AGGR:
                return aggr;
            case QUERY:
                return query;
            case COMMON:
                return common;
        }

        return null;
    }
}
