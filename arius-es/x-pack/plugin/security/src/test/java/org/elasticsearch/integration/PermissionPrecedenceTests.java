/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.integration;

import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesAction;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.test.SecurityIntegTestCase;
import org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.test.SecurityTestsUtils.assertThrowsAuthorizationException;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken.basicAuthHeaderValue;
import static org.hamcrest.Matchers.hasSize;

/**
 * This test makes sure that if an action is a cluster action (according to our
 * internal categorization in security), then we apply the cluster priv checks and don't
 * fallback on the indices privs at all. In particular, this is useful when we want to treat
 * actions that are normally categorized as index actions as cluster actions - for example,
 * index template actions.
 */
public class PermissionPrecedenceTests extends SecurityIntegTestCase {

    @Override
    protected String configRoles() {
        return "admin:\n" +
                "  cluster: [ all ] \n" +
                "  indices:\n" +
                "    - names: '*'\n" +
                "      privileges: [ all ]" +
                "\n" +
                "user:\n" +
                "  indices:\n" +
                "    - names: 'test_*'\n" +
                "      privileges: [ all ]";
    }

    @Override
    protected String configUsers() {
        final String usersPasswdHashed = new String(getFastStoredHashAlgoForTests().hash(new SecureString("test123".toCharArray())));
        return "admin:" + usersPasswdHashed + "\n" +
            "client:" + usersPasswdHashed + "\n" +
            "user:" + usersPasswdHashed + "\n";
    }

    @Override
    protected String configUsersRoles() {
        return "admin:admin\n" +
                "transport_client:client\n" +
                "user:user\n";
    }

    @Override
    protected String nodeClientUsername() {
        return "admin";
    }

    @Override
    protected SecureString nodeClientPassword() {
        return new SecureString("test123".toCharArray());
    }

    @Override
    protected String transportClientUsername() {
        return "admin";
    }

    @Override
    protected SecureString transportClientPassword() {
        return new SecureString("test123".toCharArray());
    }

    public void testDifferentCombinationsOfIndices() throws Exception {
        Client client = internalCluster().transportClient();

        // first lets try with "admin"... all should work

        AcknowledgedResponse putResponse = client
            .filterWithHeader(Collections.singletonMap(UsernamePasswordToken.BASIC_AUTH_HEADER,
                    basicAuthHeaderValue(transportClientUsername(), transportClientPassword())))
            .admin().indices().preparePutTemplate("template1")
            .setPatterns(Collections.singletonList("test_*"))
            .get();
        assertAcked(putResponse);

        GetIndexTemplatesResponse getResponse = client.admin().indices().prepareGetTemplates("template1")
                .get();
        List<IndexTemplateMetaData> templates = getResponse.getIndexTemplates();
        assertThat(templates, hasSize(1));

        // now lets try with "user"

        Map<String, String> auth = Collections.singletonMap(UsernamePasswordToken.BASIC_AUTH_HEADER, basicAuthHeaderValue("user",
                transportClientPassword()));
        assertThrowsAuthorizationException(client.filterWithHeader(auth).admin().indices().preparePutTemplate("template1")
                .setPatterns(Collections.singletonList("test_*"))::get, PutIndexTemplateAction.NAME, "user");

        Map<String, String> headers = Collections.singletonMap(UsernamePasswordToken.BASIC_AUTH_HEADER, basicAuthHeaderValue("user",
                new SecureString("test123")));
        assertThrowsAuthorizationException(client.filterWithHeader(headers).admin().indices().prepareGetTemplates("template1")::get,
                GetIndexTemplatesAction.NAME, "user");
    }
}
