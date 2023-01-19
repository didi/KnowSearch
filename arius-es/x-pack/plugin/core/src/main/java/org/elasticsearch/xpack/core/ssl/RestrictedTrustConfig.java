/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ssl;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.xpack.core.ssl.cert.CertificateInfo;

import javax.net.ssl.X509ExtendedTrustManager;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@link TrustConfig} that constructs a {@link RestrictedTrustManager}.
 * This implementation always wraps another <code>TrustConfig</code> to perform the
 * underlying certificate validation.
 */
public final class RestrictedTrustConfig extends TrustConfig {

    private static final String RESTRICTIONS_KEY_SUBJECT_NAME = "trust.subject_name";
    private final String groupConfigPath;
    private final TrustConfig delegate;

    RestrictedTrustConfig(String groupConfigPath, TrustConfig delegate) {
        this.groupConfigPath = Objects.requireNonNull(groupConfigPath);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    RestrictedTrustManager createTrustManager(@Nullable Environment environment) {
        try {
            final X509ExtendedTrustManager delegateTrustManager = delegate.createTrustManager(environment);
            final CertificateTrustRestrictions trustGroupConfig = readTrustGroup(resolveGroupConfigPath(environment));
            return new RestrictedTrustManager(delegateTrustManager, trustGroupConfig);
        } catch (IOException e) {
            throw new ElasticsearchException("failed to initialize TrustManager for {}", e, toString());
        }
    }

    @Override
    Collection<CertificateInfo> certificates(Environment environment) throws GeneralSecurityException, IOException {
        return delegate.certificates(environment);
    }

    @Override
    List<Path> filesToMonitor(@Nullable Environment environment) {
        List<Path> files = new ArrayList<>(delegate.filesToMonitor(environment));
        files.add(resolveGroupConfigPath(environment));
        return Collections.unmodifiableList(files);
    }

    @Override
    public String toString() {
        return "restrictedTrust=[" + groupConfigPath + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestrictedTrustConfig that = (RestrictedTrustConfig) o;
        return this.groupConfigPath.equals(that.groupConfigPath) && this.delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        int result = groupConfigPath.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }

    private Path resolveGroupConfigPath(@Nullable Environment environment) {
        return CertParsingUtils.resolvePath(groupConfigPath, environment);
    }

    private CertificateTrustRestrictions readTrustGroup(Path path) throws IOException {
        Settings settings = Settings.builder().loadFromPath(path).build();
        final List<String> trustNodeNames = settings.getAsList(RESTRICTIONS_KEY_SUBJECT_NAME);
        return new CertificateTrustRestrictions(trustNodeNames);
    }
}
