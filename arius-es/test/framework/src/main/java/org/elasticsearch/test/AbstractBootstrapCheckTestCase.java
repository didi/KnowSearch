/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test;

import org.elasticsearch.Version;
import org.elasticsearch.bootstrap.BootstrapContext;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import java.nio.file.Path;

public abstract class AbstractBootstrapCheckTestCase extends ESTestCase {
    protected final BootstrapContext emptyContext;

    public AbstractBootstrapCheckTestCase() {
        emptyContext = createTestContext(Settings.EMPTY, MetaData.EMPTY_META_DATA);
    }

    protected BootstrapContext createTestContext(Settings settings, MetaData metaData) {
        Path homePath = createTempDir();
        Environment environment = new Environment(settings(Version.CURRENT)
            .put(settings)
            .put(Environment.PATH_HOME_SETTING.getKey(), homePath.toString()).build(), null);
        return new BootstrapContext(environment, metaData);
    }
}
