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

package org.elasticsearch.indices.settings;

import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.common.ValidationException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;

public class PrivateSettingsIT extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singletonList(InternalOrPrivateSettingsPlugin.class);
    }

    @Override
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return Collections.singletonList(InternalOrPrivateSettingsPlugin.class);
    }

    public void testSetPrivateIndexSettingOnCreate() {
        final Settings settings = Settings.builder().put("index.private", "private").build();
        final Exception e = expectThrows(Exception.class, () -> createIndex("index", settings));
        assertThat(e, anyOf(instanceOf(IllegalArgumentException.class), instanceOf(ValidationException.class)));
        assertThat(e, hasToString(containsString("private index setting [index.private] can not be set explicitly")));
    }

    public void testUpdatePrivateIndexSettingViaSettingsAPI() {
        createIndex("test");
        // we can not update the setting via the update settings API
        final IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
                () -> client().admin()
                        .indices()
                        .prepareUpdateSettings("test")
                        .setSettings(Settings.builder().put("index.private", "private-update"))
                        .get());
        final String message = "can not update private setting [index.private]; this setting is managed by Elasticsearch";
        assertThat(e, hasToString(containsString(message)));
        final GetSettingsResponse responseAfterAttemptedUpdate = client().admin().indices().prepareGetSettings("test").get();
        assertNull(responseAfterAttemptedUpdate.getSetting("test", "index.private"));
    }

    public void testUpdatePrivatelIndexSettingViaDedicatedAPI() {
        createIndex("test");
        client().execute(
                InternalOrPrivateSettingsPlugin.UpdateInternalOrPrivateAction.INSTANCE,
                new InternalOrPrivateSettingsPlugin.UpdateInternalOrPrivateAction.Request("test", "index.private", "private-update"))
                .actionGet();
        final GetSettingsResponse responseAfterUpdate = client().admin().indices().prepareGetSettings("test").get();
        assertThat(responseAfterUpdate.getSetting("test", "index.private"), equalTo("private-update"));
    }

}
