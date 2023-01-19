/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.expression.function.scalar;

import org.elasticsearch.xpack.sql.session.Configuration;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.type.DataType;

public class User extends ConfigurationFunction {

    public User(Source source, Configuration configuration) {
        super(source, configuration, DataType.KEYWORD);
    }

    @Override
    public Object fold() {
        return configuration().username();
    }

    @Override
    protected NodeInfo<User> info() {
        return NodeInfo.create(this, User::new, configuration());
    }
}
