/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.analysis.index;

import org.elasticsearch.xpack.sql.type.EsField;

import java.util.Map;

public class EsIndex {

    private final String name;
    private final Map<String, EsField> mapping;

    public EsIndex(String name, Map<String, EsField> mapping) {
        assert name != null;
        assert mapping != null;
        this.name = name;
        this.mapping = mapping;
    }

    public String name() {
        return name;
    }

    public Map<String, EsField> mapping() {
        return mapping;
    }

    @Override
    public String toString() {
        return name;
    }
}
