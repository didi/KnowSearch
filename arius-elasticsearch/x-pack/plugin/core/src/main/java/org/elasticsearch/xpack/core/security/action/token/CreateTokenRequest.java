/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.token;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.SecureString;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * Represents a request to create a token based on the provided information. This class accepts the
 * fields for an OAuth 2.0 access token request that uses the <code>password</code> grant type or the
 * <code>refresh_token</code> grant type.
 */
public final class CreateTokenRequest extends ActionRequest {

    public enum GrantType {
        PASSWORD("password"),
        KERBEROS("_kerberos"),
        REFRESH_TOKEN("refresh_token"),
        AUTHORIZATION_CODE("authorization_code"),
        CLIENT_CREDENTIALS("client_credentials");

        private final String value;

        GrantType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static GrantType fromString(String grantType) {
            if (grantType != null) {
                for (GrantType type : values()) {
                    if (type.getValue().equals(grantType)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    private static final Set<GrantType> SUPPORTED_GRANT_TYPES = Collections.unmodifiableSet(
        EnumSet.of(GrantType.PASSWORD, GrantType.KERBEROS, GrantType.REFRESH_TOKEN, GrantType.CLIENT_CREDENTIALS));

    private String grantType;
    private String username;
    private SecureString password;
    private SecureString kerberosTicket;
    private String scope;
    private String refreshToken;

    public CreateTokenRequest(StreamInput in) throws IOException {
        super(in);
        grantType = in.readString();
        if (in.getVersion().onOrAfter(Version.V_6_2_0)) {
            username = in.readOptionalString();
            password = in.readOptionalSecureString();
            refreshToken = in.readOptionalString();
            kerberosTicket = in.readOptionalSecureString();
        } else {
            username = in.readString();
            password = in.readSecureString();
        }
        scope = in.readOptionalString();
    }

    public CreateTokenRequest() {}

    public CreateTokenRequest(String grantType, @Nullable String username, @Nullable SecureString password,
                              @Nullable SecureString kerberosTicket, @Nullable String scope, @Nullable String refreshToken) {
        this.grantType = grantType;
        this.username = username;
        this.password = password;
        this.kerberosTicket = kerberosTicket;
        this.scope = scope;
        this.refreshToken = refreshToken;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        GrantType type = GrantType.fromString(grantType);
        if (type != null) {
            switch (type) {
                case PASSWORD:
                    validationException = validateUnsupportedField(type, "kerberos_ticket", kerberosTicket, validationException);
                    validationException = validateUnsupportedField(type, "refresh_token", refreshToken, validationException);
                    validationException = validateRequiredField("username", username, validationException);
                    validationException = validateRequiredField("password", password, validationException);
                    break;
                case KERBEROS:
                    validationException = validateUnsupportedField(type, "username", username, validationException);
                    validationException = validateUnsupportedField(type, "password", password, validationException);
                    validationException = validateUnsupportedField(type, "refresh_token", refreshToken, validationException);
                    validationException = validateRequiredField("kerberos_ticket", kerberosTicket, validationException);
                    break;
                case REFRESH_TOKEN:
                    validationException = validateUnsupportedField(type, "username", username, validationException);
                    validationException = validateUnsupportedField(type, "password", password, validationException);
                    validationException = validateUnsupportedField(type, "kerberos_ticket", kerberosTicket, validationException);
                    validationException = validateRequiredField("refresh_token", refreshToken, validationException);
                    break;
                case CLIENT_CREDENTIALS:
                    validationException = validateUnsupportedField(type, "username", username, validationException);
                    validationException = validateUnsupportedField(type, "password", password, validationException);
                    validationException = validateUnsupportedField(type, "kerberos_ticket", kerberosTicket, validationException);
                    validationException = validateUnsupportedField(type, "refresh_token", refreshToken, validationException);
                    break;
                default:
                    validationException = addValidationError("grant_type only supports the values: [" +
                            SUPPORTED_GRANT_TYPES.stream().map(GrantType::getValue).collect(Collectors.joining(", ")) + "]",
                        validationException);
            }
        } else {
            validationException = addValidationError("grant_type only supports the values: [" +
                    SUPPORTED_GRANT_TYPES.stream().map(GrantType::getValue).collect(Collectors.joining(", ")) + "]",
                validationException);
        }
        return validationException;
    }

    private static ActionRequestValidationException validateRequiredField(String field, String fieldValue,
                                                                          ActionRequestValidationException validationException) {
        if (Strings.isNullOrEmpty(fieldValue)) {
            validationException = addValidationError(String.format(Locale.ROOT, "%s is missing", field), validationException);
        }
        return validationException;
    }

    private static ActionRequestValidationException validateRequiredField(String field, SecureString fieldValue,
                                                                          ActionRequestValidationException validationException) {
        if (fieldValue == null || fieldValue.getChars() == null || fieldValue.length() == 0) {
            validationException = addValidationError(String.format(Locale.ROOT, "%s is missing", field), validationException);
        }
        return validationException;
    }

    private static ActionRequestValidationException validateUnsupportedField(GrantType grantType, String field, Object fieldValue,
                                                                               ActionRequestValidationException validationException) {
        if (fieldValue != null) {
            validationException = addValidationError(
                    String.format(Locale.ROOT, "%s is not supported with the %s grant_type", field, grantType.getValue()),
                    validationException);
        }
        return validationException;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public void setPassword(@Nullable SecureString password) {
        this.password = password;
    }

    public void setKerberosTicket(@Nullable SecureString kerberosTicket) {
        this.kerberosTicket = kerberosTicket;
    }

    public void setScope(@Nullable String scope) {
        this.scope = scope;
    }

    public void setRefreshToken(@Nullable String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getGrantType() {
        return grantType;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @Nullable
    public SecureString getPassword() {
        return password;
    }

    @Nullable
    public SecureString getKerberosTicket() {
        return kerberosTicket;
    }

    @Nullable
    public String getScope() {
        return scope;
    }

    @Nullable
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        if (out.getVersion().before(Version.V_6_5_0) && GrantType.CLIENT_CREDENTIALS.getValue().equals(grantType)) {
            throw new IllegalArgumentException("a request with the client_credentials grant_type cannot be sent to version [" +
                out.getVersion() + "]");
        }
        if (out.getVersion().before(Version.V_7_3_0) && GrantType.KERBEROS.getValue().equals(grantType)) {
            throw new IllegalArgumentException("a request with the _kerberos grant_type cannot be sent to version [" +
                out.getVersion() + "]");
        }

        out.writeString(grantType);
        if (out.getVersion().onOrAfter(Version.V_6_2_0)) {
            out.writeOptionalString(username);
            out.writeOptionalSecureString(password);
            out.writeOptionalString(refreshToken);
            out.writeOptionalSecureString(kerberosTicket);
        } else {
            if ("refresh_token".equals(grantType)) {
                throw new IllegalArgumentException("a refresh request cannot be sent to an older version");
            } else {
                out.writeString(username);
                out.writeSecureString(password);
            }
        }
        out.writeOptionalString(scope);
    }
}
