/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.gen.processor;

import org.elasticsearch.common.io.stream.NamedWriteable;

/**
 * A {@code Processor} evaluates locally an expression. For instance, ABS(foo).
 * Aggregate functions are handled by ES but scalars are not.
 *
 * This is an opaque class, the computed/compiled result gets saved on the client during scrolling.
 */
public interface Processor extends NamedWriteable {

    Object process(Object input);
}
