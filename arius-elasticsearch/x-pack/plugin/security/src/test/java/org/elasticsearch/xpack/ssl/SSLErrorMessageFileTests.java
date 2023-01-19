/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ssl;

import org.apache.lucene.util.Constants;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.ssl.SSLService;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessControlException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.elasticsearch.test.SecuritySettingsSource.addSecureSettings;
import static org.elasticsearch.test.TestMatchers.throwableWithMessage;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;

/**
 * This is a suite of tests to ensure that meaningful error messages are generated for defined SSL configuration problems due to file
 * problems.
 */
public class SSLErrorMessageFileTests extends ESTestCase {

    private Environment env;
    private Map<String, Path> paths;

    @Before
    public void setup() throws Exception {
        env = TestEnvironment.newEnvironment(getSettingsBuilder().put("path.home", createTempDir()).build());
        paths = new HashMap<>();

        requirePath("ca1.p12");
        requirePath("ca1.jks");
        requirePath("ca1.crt");

        requirePath("cert1a.p12");
        requirePath("cert1a.jks");
        requirePath("cert1a.crt");
        requirePath("cert1a.key");
    }

    public void testMessageForMissingKeystore() {
        checkMissingKeyManagerResource("keystore", "keystore.path", null);
    }

    public void testMessageForMissingPemCertificate() {
        checkMissingKeyManagerResource("certificate", "certificate", withKey("cert1a.key"));
    }

    public void testMessageForMissingPemKey() {
        checkMissingKeyManagerResource("key", "key", withCertificate("cert1a.crt"));
    }

    public void testMessageForMissingTruststore() {
        checkMissingTrustManagerResource("truststore", "truststore.path");
    }

    public void testMessageForMissingCertificateAuthorities() {
        checkMissingTrustManagerResource("certificate_authorities", "certificate_authorities");
    }

    public void testMessageForKeystoreWithoutReadAccess() throws Exception {
        checkUnreadableKeyManagerResource("cert1a.p12", "keystore", "keystore.path", null);
    }

    public void testMessageForPemCertificateWithoutReadAccess() throws Exception {
        checkUnreadableKeyManagerResource("cert1a.crt", "certificate", "certificate", withKey("cert1a.key"));
    }

    public void testMessageForPemKeyWithoutReadAccess() throws Exception {
        checkUnreadableKeyManagerResource("cert1a.key", "key", "key", withCertificate("cert1a.crt"));
    }

    public void testMessageForTruststoreWithoutReadAccess() throws Exception {
        checkUnreadableTrustManagerResource("cert1a.p12", "truststore", "truststore.path");
    }

    public void testMessageForCertificateAuthoritiesWithoutReadAccess() throws Exception {
        checkUnreadableTrustManagerResource("ca1.crt", "certificate_authorities", "certificate_authorities");
    }

    public void testMessageForKeyStoreOutsideConfigDir() throws Exception {
        checkBlockedKeyManagerResource("keystore", "keystore.path", null);
    }

    public void testMessageForPemCertificateOutsideConfigDir() throws Exception {
        checkBlockedKeyManagerResource("certificate", "certificate", withKey("cert1a.key"));
    }

    public void testMessageForPemKeyOutsideConfigDir() throws Exception {
        checkBlockedKeyManagerResource("key", "key", withCertificate("cert1a.crt"));
    }

    public void testMessageForTrustStoreOutsideConfigDir() throws Exception {
        checkBlockedTrustManagerResource("truststore", "truststore.path");
    }

    public void testMessageForCertificateAuthoritiesOutsideConfigDir() throws Exception {
        checkBlockedTrustManagerResource("certificate_authorities", "certificate_authorities");
    }

    public void testMessageForTransportSslEnabledWithoutKeys() throws Exception {
        final String prefix = "xpack.security.transport.ssl";
        final Settings.Builder settings = getSettingsBuilder();
        settings.put(prefix + ".enabled", true);
        configureWorkingTruststore(prefix, settings);

        expectSuccess(settings);
        assertWarnings("invalid SSL configuration for " + prefix +
            " - server ssl configuration requires a key and certificate, but these have not been configured;" +
            " you must set either [" + prefix + ".keystore.path], or both [" + prefix + ".key] and [" + prefix + ".certificate]");
    }

    public void testNoErrorIfTransportSslDisabledWithoutKeys() throws Exception {
        final String prefix = "xpack.security.transport.ssl";
        final Settings.Builder settings = getSettingsBuilder();
        settings.put(prefix + ".enabled", false);
        configureWorkingTruststore(prefix, settings);
        expectSuccess(settings);
    }

    public void testMessageForTransportNotEnabledButKeystoreConfigured() throws Exception {
        final String prefix = "xpack.security.transport.ssl";
        checkUnusedConfiguration(prefix, prefix + ".keystore.path," + prefix + ".keystore.secure_password",
            this::configureWorkingKeystore);
    }

    public void testMessageForTransportNotEnabledButTruststoreConfigured() throws Exception {
        final String prefix = "xpack.security.transport.ssl";
        checkUnusedConfiguration(prefix, prefix + ".truststore.path," + prefix + ".truststore.secure_password",
            this::configureWorkingTruststore);
    }

    public void testMessageForHttpsNotEnabledButKeystoreConfigured() throws Exception {
        final String prefix = "xpack.security.http.ssl";
        checkUnusedConfiguration(prefix, prefix + ".keystore.path," + prefix + ".keystore.secure_password",
            this::configureWorkingKeystore);
    }

    public void testMessageForHttpsNotEnabledButTruststoreConfigured() throws Exception {
        final String prefix = "xpack.security.http.ssl";
        checkUnusedConfiguration(prefix, prefix + ".truststore.path," + prefix + ".truststore.secure_password",
            this::configureWorkingTruststore);
    }

    private void checkMissingKeyManagerResource(String fileType, String configKey, @Nullable Settings.Builder additionalSettings) {
        checkMissingResource("KeyManager", fileType, configKey,
            (prefix, builder) -> buildKeyConfigSettings(additionalSettings, prefix, builder));
    }

    private void buildKeyConfigSettings(@Nullable Settings.Builder additionalSettings, String prefix, Settings.Builder builder) {
        configureWorkingTruststore(prefix, builder);
        if (additionalSettings != null) {
            builder.put(additionalSettings.normalizePrefix(prefix + ".").build());
        }
    }

    private void checkMissingTrustManagerResource(String fileType, String configKey) {
        checkMissingResource("TrustManager", fileType, configKey, this::configureWorkingKeystore);
    }

    private void checkUnreadableKeyManagerResource(String fromResource, String fileType, String configKey,
                                                   @Nullable Settings.Builder additionalSettings) throws Exception {
        checkUnreadableResource("KeyManager", fromResource, fileType, configKey,
            (prefix, builder) -> buildKeyConfigSettings(additionalSettings, prefix, builder));
    }

    private void checkUnreadableTrustManagerResource(String fromResource, String fileType, String configKey) throws Exception {
        checkUnreadableResource("TrustManager", fromResource, fileType, configKey, this::configureWorkingKeystore);
    }

    private void checkBlockedKeyManagerResource(String fileType, String configKey, Settings.Builder additionalSettings) throws Exception {
        checkBlockedResource("KeyManager", fileType, configKey,
            (prefix, builder) -> buildKeyConfigSettings(additionalSettings, prefix, builder));
    }

    private void checkBlockedTrustManagerResource(String fileType, String configKey) throws Exception {
        checkBlockedResource("TrustManager", fileType, configKey, this::configureWorkingKeystore);
    }

    private void checkMissingResource(String sslManagerType, String fileType, String configKey,
                                      BiConsumer<String, Settings.Builder> configure) {
        final String prefix = randomSslPrefix();
        final Settings.Builder settings = getSettingsBuilder();
        configure.accept(prefix, settings);

        final String fileName = missingFile();
        final String key = prefix + "." + configKey;
        settings.put(key, fileName);

        Throwable exception = expectFailure(settings);
        assertThat(exception, throwableWithMessage("failed to load SSL configuration [" + prefix + "]"));
        assertThat(exception, instanceOf(ElasticsearchSecurityException.class));

        exception = exception.getCause();
        assertThat(exception, throwableWithMessage(
            "failed to initialize SSL " + sslManagerType + " - " + fileType + " file [" + fileName + "] does not exist"));
        assertThat(exception, instanceOf(ElasticsearchException.class));

        exception = exception.getCause();
        assertThat(exception, instanceOf(NoSuchFileException.class));
        assertThat(exception, throwableWithMessage(fileName));
    }

    private void checkUnreadableResource(String sslManagerType, String fromResource, String fileType, String configKey,
                                         BiConsumer<String, Settings.Builder> configure) throws Exception {
        final String prefix = randomSslPrefix();
        final Settings.Builder settings =getSettingsBuilder();
        configure.accept(prefix, settings);

        final String fileName = unreadableFile(fromResource);
        final String key = prefix + "." + configKey;
        settings.put(key, fileName);

        Throwable exception = expectFailure(settings);
        assertThat(exception, throwableWithMessage("failed to load SSL configuration [" + prefix + "]"));
        assertThat(exception, instanceOf(ElasticsearchSecurityException.class));

        exception = exception.getCause();
        assertThat(exception, throwableWithMessage(
            "failed to initialize SSL " + sslManagerType + " - not permitted to read " + fileType + " file [" + fileName + "]"));
        assertThat(exception, instanceOf(ElasticsearchException.class));

        exception = exception.getCause();
        assertThat(exception, instanceOf(AccessDeniedException.class));
        assertThat(exception, throwableWithMessage(fileName));
    }

    private void checkBlockedResource(String sslManagerType, String fileType, String configKey,
                                      BiConsumer<String, Settings.Builder> configure) throws Exception {
        final String prefix = randomSslPrefix();
        final Settings.Builder settings = getSettingsBuilder();
        configure.accept(prefix, settings);

        final String fileName = blockedFile();
        final String key = prefix + "." + configKey;
        settings.put(key, fileName);

        Throwable exception = expectFailure(settings);
        assertThat(exception, throwableWithMessage("failed to load SSL configuration [" + prefix + "]"));
        assertThat(exception, instanceOf(ElasticsearchSecurityException.class));

        exception = exception.getCause();
        assertThat(exception.getMessage(),
            containsString("failed to initialize SSL " + sslManagerType + " - access to read " + fileType + " file"));
        assertThat(exception.getMessage(), containsString("file.error"));
        assertThat(exception, instanceOf(ElasticsearchException.class));

        exception = exception.getCause();
        assertThat(exception, instanceOf(AccessControlException.class));
        assertThat(exception, throwableWithMessage(containsString(fileName)));
    }

    private void checkUnusedConfiguration(String prefix, String settingsConfigured, BiConsumer<String, Settings.Builder> configure) {
        final Settings.Builder settings = getSettingsBuilder();
        configure.accept(prefix, settings);

        expectSuccess(settings);
        assertWarnings("invalid configuration for " + prefix + " - [" + prefix + ".enabled] is not set," +
            " but the following settings have been configured in elasticsearch.yml : [" + settingsConfigured + "]");
    }

    private String missingFile() {
        return resource("cert1a.p12").replace("cert1a.p12", "file.dne");
    }

    private String unreadableFile(String fromResource) throws IOException {
        assumeFalse("This behaviour uses POSIX file permissions", Constants.WINDOWS);
        final Path fromPath = this.paths.get(fromResource);
        if (fromPath == null) {
            throw new IllegalArgumentException("Test SSL resource " + fromResource + " has not been loaded");
        }
        return copy(fromPath, createTempFile(fromResource, "-no-read"), PosixFilePermissions.fromString("---------"));
    }

    private String blockedFile() throws IOException {
        return PathUtils.get("/this", "path", "is", "outside", "the", "config", "directory", "file.error").toString();
    }

    /**
     * This more-or-less replicates the functionality of {@link Files#copy(Path, Path, CopyOption...)} but doing it this way allows us to
     * set the file permissions when creating the file (which helps with security manager issues)
     */
    private String copy(Path fromPath, Path toPath, Set<PosixFilePermission> permissions) throws IOException {
        Files.deleteIfExists(toPath);
        final FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(permissions);
        final EnumSet<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        try (SeekableByteChannel channel = Files.newByteChannel(toPath, options, fileAttributes);
             OutputStream out = Channels.newOutputStream(channel)) {
            Files.copy(fromPath, out);
        }
        return toPath.toString();
    }

    private Settings.Builder withKey(String fileName) {
        assertThat(fileName, endsWith(".key"));
        return getSettingsBuilder().put("key", resource(fileName));
    }

    private Settings.Builder withCertificate(String fileName) {
        assertThat(fileName, endsWith(".crt"));
        return getSettingsBuilder().put("certificate", resource(fileName));
    }

    private Settings.Builder configureWorkingTruststore(String prefix, Settings.Builder settings) {
        settings.put(prefix + ".truststore.path", resource("cert1a.p12"));
        addSecureSettings(settings, secure -> secure.setString(prefix + ".truststore.secure_password", "cert1a-p12-password"));
        return settings;
    }

    private Settings.Builder configureWorkingKeystore(String prefix, Settings.Builder settings) {
        settings.put(prefix + ".keystore.path", resource("cert1a.p12"));
        addSecureSettings(settings, secure -> secure.setString(prefix + ".keystore.secure_password", "cert1a-p12-password"));
        return settings;
    }

    private ElasticsearchException expectFailure(Settings.Builder settings) {
        return expectThrows(ElasticsearchException.class, () -> new SSLService(settings.build(), env));
    }

    private SSLService expectSuccess(Settings.Builder settings) {
        return new SSLService(settings.build(), env);
    }

    private String resource(String fileName) {
        final Path path = this.paths.get(fileName);
        if (path == null) {
            throw new IllegalArgumentException("Test SSL resource " + fileName + " has not been loaded");
        }
        return path.toString();
    }

    private void requirePath(String fileName) throws FileNotFoundException {
        final Path path = getDataPath("/org/elasticsearch/xpack/ssl/SSLErrorMessageTests/" + fileName);
        if (Files.exists(path)) {
            paths.put(fileName, path);
        } else {
            throw new FileNotFoundException("File " + path + " does not exist");
        }
    }

    private String randomSslPrefix() {
        return randomFrom(
            "xpack.security.transport.ssl",
            "xpack.security.http.ssl",
            "xpack.http.ssl",
            "xpack.security.authc.realms.ldap.ldap1.ssl",
            "xpack.security.authc.realms.saml.saml1.ssl",
            "xpack.monitoring.exporters.http.ssl"
        );
    }

    private Settings.Builder getSettingsBuilder() {
        final Settings.Builder settings = Settings.builder();
        if (inFipsJvm()) {
            settings.put(XPackSettings.DIAGNOSE_TRUST_EXCEPTIONS_SETTING.getKey(), false);
        }
        return settings;
    }
}
