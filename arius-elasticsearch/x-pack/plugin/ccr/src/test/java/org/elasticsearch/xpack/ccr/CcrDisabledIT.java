/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.ccr;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.xpack.core.XPackClientPlugin;
import org.elasticsearch.xpack.core.XPackSettings;

import java.util.Collection;
import java.util.Collections;

public class CcrDisabledIT extends ESIntegTestCase {

    public void testClusterCanStartWithCcrInstalledButNotEnabled() throws Exception {
        // TODO: Assert that x-pack ccr feature is not enabled once feature functionality has been added
        ensureGreen();
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal)).put(XPackSettings.CCR_ENABLED_SETTING.getKey(), true)
            .put(XPackSettings.SECURITY_ENABLED.getKey(), false).build();
    }

    @Override
    protected Settings transportClientSettings() {
        return Settings.builder().put(super.transportClientSettings()).put(XPackSettings.SECURITY_ENABLED.getKey(), false).build();
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singletonList(LocalStateCcr.class);
    }

    @Override
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return Collections.singletonList(XPackClientPlugin.class);
    }
}
