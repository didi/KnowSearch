/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security;

import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.TemplateUpgradeService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.elasticsearch.test.SecurityIntegTestCase;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * This test ensures, that the plugin template upgrader can add and remove
 * templates when started within security, as this requires certain
 * system privileges
 */
@ClusterScope(maxNumDataNodes = 1, scope = Scope.SUITE, numClientNodes = 0)
public class TemplateUpgraderTests extends SecurityIntegTestCase {

    public void testTemplatesWorkAsExpected() throws Exception {
        ClusterService clusterService = internalCluster().getInstance(ClusterService.class, internalCluster().getMasterName());
        ThreadPool threadPool = internalCluster().getInstance(ThreadPool.class, internalCluster().getMasterName());
        Client client = internalCluster().getInstance(Client.class, internalCluster().getMasterName());
        UnaryOperator<Map<String, IndexTemplateMetaData>> indexTemplateMetaDataUpgraders = map -> {
            map.remove("removed-template");
            map.put("added-template", IndexTemplateMetaData.builder("added-template")
                    .order(1)
                    .patterns(Collections.singletonList(randomAlphaOfLength(10))).build());
            return map;
        };

        AcknowledgedResponse putIndexTemplateResponse = client().admin().indices().preparePutTemplate("removed-template")
                .setOrder(1)
                .setPatterns(Collections.singletonList(randomAlphaOfLength(10)))
                .get();
        assertAcked(putIndexTemplateResponse);
        assertTemplates("removed-template", "added-template");

        TemplateUpgradeService templateUpgradeService = new TemplateUpgradeService(client, clusterService, threadPool,
                Collections.singleton(indexTemplateMetaDataUpgraders));

        // ensure the cluster listener gets triggered
        ClusterChangedEvent event = new ClusterChangedEvent("testing", clusterService.state(), clusterService.state());
        templateUpgradeService.clusterChanged(event);

        assertBusy(() -> assertTemplates("added-template", "removed-template"));
    }

    private void assertTemplates(String existingTemplate, String deletedTemplate) {
        GetIndexTemplatesResponse response = client().admin().indices().prepareGetTemplates().get();
        List<String> templateNames = response.getIndexTemplates().stream().map(IndexTemplateMetaData::name).collect(Collectors.toList());
        assertThat(templateNames, hasItem(existingTemplate));
        assertThat(templateNames, not(hasItem(deletedTemplate)));
    }
}
