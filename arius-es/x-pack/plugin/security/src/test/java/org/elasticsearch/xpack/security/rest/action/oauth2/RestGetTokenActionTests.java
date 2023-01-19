/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.rest.action.oauth2;

import org.apache.lucene.util.SetOnce;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.AbstractRestChannel;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.SecuritySettingsSourceField;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.xpack.core.security.action.token.CreateTokenRequest;
import org.elasticsearch.xpack.core.security.action.token.CreateTokenResponse;
import org.elasticsearch.xpack.core.security.support.NoOpLogger;
import org.elasticsearch.xpack.security.authc.kerberos.KerberosAuthenticationToken;
import org.elasticsearch.xpack.security.rest.action.oauth2.RestGetTokenAction.CreateTokenResponseActionListener;

import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;

public class RestGetTokenActionTests extends ESTestCase {

    public void testListenerHandlesExceptionProperly() {
        FakeRestRequest restRequest = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY)
                .build();
        final SetOnce<RestResponse> responseSetOnce = new SetOnce<>();
        RestChannel restChannel = new AbstractRestChannel(restRequest, randomBoolean()) {
            @Override
            public void sendResponse(RestResponse restResponse) {
                responseSetOnce.set(restResponse);
            }
        };
        CreateTokenResponseActionListener listener = new CreateTokenResponseActionListener(restChannel, restRequest, NoOpLogger.INSTANCE);

        ActionRequestValidationException ve = new CreateTokenRequest(null, null, null, null, null, null).validate();
        listener.onFailure(ve);
        RestResponse response = responseSetOnce.get();
        assertNotNull(response);

        Map<String, Object> map = XContentHelper.convertToMap(response.content(), false,
                XContentType.fromMediaType(response.contentType())).v2();
        assertThat(map, hasEntry("error", "unsupported_grant_type"));
        assertThat(map, hasEntry("error_description", ve.getMessage()));
        assertEquals(2, map.size());
        assertEquals(RestStatus.BAD_REQUEST, response.status());
    }

    public void testSendResponse() {
        FakeRestRequest restRequest = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY).build();
        final SetOnce<RestResponse> responseSetOnce = new SetOnce<>();
        RestChannel restChannel = new AbstractRestChannel(restRequest, randomBoolean()) {
            @Override
            public void sendResponse(RestResponse restResponse) {
                responseSetOnce.set(restResponse);
            }
        };
        CreateTokenResponseActionListener listener = new CreateTokenResponseActionListener(restChannel, restRequest, NoOpLogger.INSTANCE);
        CreateTokenResponse createTokenResponse =
                new CreateTokenResponse(randomAlphaOfLengthBetween(1, 256), TimeValue.timeValueHours(1L), null, randomAlphaOfLength(4),
                        randomAlphaOfLength(5));
        listener.onResponse(createTokenResponse);

        RestResponse response = responseSetOnce.get();
        assertNotNull(response);

        Map<String, Object> map = XContentHelper.convertToMap(response.content(), false,
                XContentType.fromMediaType(response.contentType())).v2();
        assertEquals(RestStatus.OK, response.status());
        assertThat(map, hasEntry("type", "Bearer"));
        assertThat(map, hasEntry("access_token", createTokenResponse.getTokenString()));
        assertThat(map, hasEntry("expires_in", Math.toIntExact(createTokenResponse.getExpiresIn().seconds())));
        assertThat(map, hasEntry("refresh_token", createTokenResponse.getRefreshToken()));
        assertThat(map, hasEntry("kerberos_authentication_response_token", createTokenResponse.getKerberosAuthenticationResponseToken()));
        assertEquals(5, map.size());
    }

    public void testSendResponseKerberosError() {
        FakeRestRequest restRequest = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY).build();
        final SetOnce<RestResponse> responseSetOnce = new SetOnce<>();
        RestChannel restChannel = new AbstractRestChannel(restRequest, randomBoolean()) {
            @Override
            public void sendResponse(RestResponse restResponse) {
                responseSetOnce.set(restResponse);
            }
        };
        CreateTokenResponseActionListener listener = new CreateTokenResponseActionListener(restChannel, restRequest, NoOpLogger.INSTANCE);
        String errorMessage = "failed to authenticate user, gss context negotiation not complete";
        ElasticsearchSecurityException ese = new ElasticsearchSecurityException(errorMessage, RestStatus.UNAUTHORIZED);
        boolean addBase64EncodedToken = randomBoolean();
        ese.addHeader(KerberosAuthenticationToken.WWW_AUTHENTICATE, "Negotiate" + ((addBase64EncodedToken) ? " FAIL" : ""));
        listener.onFailure(ese);

        RestResponse response = responseSetOnce.get();
        assertNotNull(response);

        Map<String, Object> map = XContentHelper.convertToMap(response.content(), false,
                XContentType.fromMediaType(response.contentType())).v2();
        assertThat(map, hasEntry("error", RestGetTokenAction.TokenRequestError._UNAUTHORIZED.name().toLowerCase(Locale.ROOT)));
        if (addBase64EncodedToken) {
            assertThat(map, hasEntry("error_description", "FAIL"));
        } else {
            assertThat(map, hasEntry("error_description", null));
        }
        assertEquals(2, map.size());
        assertEquals(RestStatus.BAD_REQUEST, response.status());
    }

    public void testParser() throws Exception {
        final String request = "{" +
                "\"grant_type\": \"password\"," +
                "\"username\": \"user1\"," +
                "\"password\": \"" + SecuritySettingsSourceField.TEST_PASSWORD + "\"," +
                "\"scope\": \"FULL\"" +
                "}";
        try (XContentParser parser = XContentType.JSON.xContent()
                .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, request)) {
            CreateTokenRequest createTokenRequest = RestGetTokenAction.PARSER.parse(parser, null);
            assertEquals("password", createTokenRequest.getGrantType());
            assertEquals("user1", createTokenRequest.getUsername());
            assertEquals("FULL", createTokenRequest.getScope());
            assertTrue(SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING.equals(createTokenRequest.getPassword()));
        }
    }

    public void testParserRefreshRequest() throws Exception {
        final String token = randomAlphaOfLengthBetween(4, 32);
        final String request = "{" +
                "\"grant_type\": \"refresh_token\"," +
                "\"refresh_token\": \"" + token + "\"," +
                "\"scope\": \"FULL\"" +
                "}";
        try (XContentParser parser = XContentType.JSON.xContent()
                .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, request)) {
            CreateTokenRequest createTokenRequest = RestGetTokenAction.PARSER.parse(parser, null);
            assertEquals("refresh_token", createTokenRequest.getGrantType());
            assertEquals(token, createTokenRequest.getRefreshToken());
            assertEquals("FULL", createTokenRequest.getScope());
            assertNull(createTokenRequest.getUsername());
            assertNull(createTokenRequest.getPassword());
        }
    }
}
