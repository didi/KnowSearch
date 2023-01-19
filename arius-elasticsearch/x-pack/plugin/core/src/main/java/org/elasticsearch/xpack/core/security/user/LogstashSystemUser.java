/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.user;

import org.elasticsearch.xpack.core.security.support.MetadataUtils;

/**
 * Built in user for logstash internals. Currently used for Logstash monitoring.
 */
public class LogstashSystemUser extends User {

    public static final String NAME = UsernamesField.LOGSTASH_NAME;
    public static final String ROLE_NAME = UsernamesField.LOGSTASH_ROLE;

    public LogstashSystemUser(boolean enabled) {
        super(NAME, new String[]{ ROLE_NAME }, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, enabled);
    }
}
