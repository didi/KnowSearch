/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.datetime;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.datetime.NamedDateTimeProcessor.NameExtractor;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo.NodeCtor2;

import java.time.ZoneId;

/**
 * Extract the day of the week from a datetime in text format (Monday, Tuesday etc.)
 */
public class DayName extends NamedDateTimeFunction {
    
    public DayName(Source source, Expression field, ZoneId zoneId) {
        super(source, field, zoneId, NameExtractor.DAY_NAME);
    }

    @Override
    protected NodeCtor2<Expression, ZoneId, BaseDateTimeFunction> ctorForInfo() {
        return DayName::new;
    }

    @Override
    protected DayName replaceChild(Expression newChild) {
        return new DayName(source(), newChild, zoneId());
    }
}