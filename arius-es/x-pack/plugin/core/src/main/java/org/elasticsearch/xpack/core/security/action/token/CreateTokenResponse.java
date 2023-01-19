/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.token;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Objects;

/**
 * Response containing the token string that was generated from a token creation request. This
 * object also contains the scope and expiration date. If the scope was not provided or if the
 * provided scope matches the scope of the token, then the scope value is <code>null</code>
 */
public final class CreateTokenResponse extends ActionResponse implements ToXContentObject {

    private String tokenString;
    private TimeValue expiresIn;
    private String scope;
    private String refreshToken;
    private String kerberosAuthenticationResponseToken;

    CreateTokenResponse() {}

    public CreateTokenResponse(StreamInput in) throws IOException {
        super(in);
        tokenString = in.readString();
        expiresIn = in.readTimeValue();
        scope = in.readOptionalString();
        if (in.getVersion().onOrAfter(Version.V_6_5_0)) {
            refreshToken = in.readOptionalString();
        } else if (in.getVersion().onOrAfter(Version.V_6_2_0)) {
            refreshToken = in.readString();
        }
        kerberosAuthenticationResponseToken = in.readOptionalString();
    }

    public CreateTokenResponse(String tokenString, TimeValue expiresIn, String scope, String refreshToken,
                               String kerberosAuthenticationResponseToken) {
        this.tokenString = Objects.requireNonNull(tokenString);
        this.expiresIn = Objects.requireNonNull(expiresIn);
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.kerberosAuthenticationResponseToken = kerberosAuthenticationResponseToken;
    }

    public String getTokenString() {
        return tokenString;
    }

    public String getScope() {
        return scope;
    }

    public TimeValue getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getKerberosAuthenticationResponseToken() {
        return kerberosAuthenticationResponseToken;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(tokenString);
        out.writeTimeValue(expiresIn);
        out.writeOptionalString(scope);
        if (out.getVersion().onOrAfter(Version.V_6_5_0)) {
            out.writeOptionalString(refreshToken);
        } else if (out.getVersion().onOrAfter(Version.V_6_2_0)) {
            if (refreshToken == null) {
                out.writeString("");
            } else {
                out.writeString(refreshToken);
            }
        }
        out.writeOptionalString(kerberosAuthenticationResponseToken);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject()
            .field("access_token", tokenString)
            .field("type", "Bearer")
            .field("expires_in", expiresIn.seconds());
        if (refreshToken != null) {
            builder.field("refresh_token", refreshToken);
        }
        // only show the scope if it is not null
        if (scope != null) {
            builder.field("scope", scope);
        }
        if (kerberosAuthenticationResponseToken != null) {
            builder.field("kerberos_authentication_response_token", kerberosAuthenticationResponseToken);
        }
        return builder.endObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateTokenResponse that = (CreateTokenResponse) o;
        return Objects.equals(tokenString, that.tokenString) &&
            Objects.equals(expiresIn, that.expiresIn) &&
            Objects.equals(scope, that.scope) &&
            Objects.equals(refreshToken, that.refreshToken) &&
            Objects.equals(kerberosAuthenticationResponseToken,  that.kerberosAuthenticationResponseToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenString, expiresIn, scope, refreshToken, kerberosAuthenticationResponseToken);
    }
}
