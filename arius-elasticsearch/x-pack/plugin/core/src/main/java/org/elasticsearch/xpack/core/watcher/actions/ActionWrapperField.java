/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.watcher.actions;

import org.elasticsearch.common.ParseField;

public final class ActionWrapperField {
    public static final ParseField ID = new ParseField("id");
    public static final ParseField TYPE = new ParseField("type");
    public static final ParseField STATUS = new ParseField("status");

    private ActionWrapperField() {}
}
