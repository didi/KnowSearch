/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authc.ldap.support;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.security.authc.RealmConfig;
import org.elasticsearch.xpack.core.security.authc.ldap.LdapSessionFactorySettings;
import org.elasticsearch.xpack.core.security.authc.ldap.SearchGroupsResolverSettings;
import org.elasticsearch.xpack.core.security.authc.ldap.support.LdapLoadBalancingSettings;
import org.elasticsearch.xpack.core.security.authc.ldap.support.LdapSearchScope;
import org.elasticsearch.xpack.core.security.authc.ldap.support.SessionFactorySettings;
import org.elasticsearch.xpack.core.security.authc.support.DnRoleMapperSettings;
import org.elasticsearch.xpack.core.ssl.CertParsingUtils;
import org.elasticsearch.xpack.core.ssl.SSLConfigurationSettings;
import org.elasticsearch.xpack.core.ssl.VerificationMode;
import org.elasticsearch.xpack.security.authc.support.DnRoleMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import java.net.InetAddress;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.xpack.core.security.authc.RealmSettings.getFullSettingKey;
import static org.elasticsearch.xpack.core.security.authc.ldap.support.SessionFactorySettings.HOSTNAME_VERIFICATION_SETTING;
import static org.elasticsearch.xpack.core.security.authc.ldap.support.SessionFactorySettings.URLS_SETTING;

public abstract class LdapTestCase extends ESTestCase {

    protected static final RealmConfig.RealmIdentifier REALM_IDENTIFIER = new RealmConfig.RealmIdentifier("ldap", "ldap1");

    static int numberOfLdapServers;
    protected InMemoryDirectoryServer[] ldapServers;

    @BeforeClass
    public static void setNumberOfLdapServers() {
        numberOfLdapServers = randomIntBetween(1, 4);
    }

    @Before
    public void startLdap() throws Exception {
        ldapServers = new InMemoryDirectoryServer[numberOfLdapServers];
        for (int i = 0; i < numberOfLdapServers; i++) {
            InMemoryDirectoryServerConfig serverConfig = new InMemoryDirectoryServerConfig("o=sevenSeas");
            List<InMemoryListenerConfig> listeners = new ArrayList<>(2);
            listeners.add(InMemoryListenerConfig.createLDAPConfig("ldap", null, 0, null));
            if (openLdapsPort()) {
                final char[] ldapPassword = "ldap-password".toCharArray();
                final KeyStore ks = CertParsingUtils.getKeyStoreFromPEM(
                    getDataPath("/org/elasticsearch/xpack/security/authc/ldap/support/ldap-test-case.crt"),
                    getDataPath("/org/elasticsearch/xpack/security/authc/ldap/support/ldap-test-case.key"),
                    ldapPassword
                );
                X509ExtendedKeyManager keyManager = CertParsingUtils.keyManager(ks, ldapPassword, KeyManagerFactory.getDefaultAlgorithm());
                final SSLContext context = SSLContext.getInstance(XPackSettings.DEFAULT_SUPPORTED_PROTOCOLS.get(0));
                context.init(new KeyManager[] { keyManager }, null, null);
                SSLServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
                SSLSocketFactory clientSocketFactory = context.getSocketFactory();
                listeners.add(InMemoryListenerConfig.createLDAPSConfig("ldaps", null, 0, serverSocketFactory, clientSocketFactory));
            }
            serverConfig.setListenerConfigs(listeners);
            InMemoryDirectoryServer ldapServer = new InMemoryDirectoryServer(serverConfig);
            ldapServer.add("o=sevenSeas", new Attribute("dc", "UnboundID"),
                    new Attribute("objectClass", "top", "domain", "extensibleObject"));
            ldapServer.importFromLDIF(false,
                    getDataPath("/org/elasticsearch/xpack/security/authc/ldap/support/seven-seas.ldif").toString());
            // Must have privileged access because underlying server will accept socket connections
            AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                ldapServer.startListening();
                return null;
            });
            ldapServers[i] = ldapServer;
        }
    }

    protected boolean openLdapsPort() {
        return false;
    }

    @After
    public void stopLdap() {
        for (int i = 0; i < numberOfLdapServers; i++) {
            ldapServers[i].shutDown(true);
        }
    }

    protected String[] ldapUrls() throws LDAPException {
        List<String> urls = new ArrayList<>(numberOfLdapServers);
        for (int i = 0; i < numberOfLdapServers; i++) {
            InetAddress listenAddress = ldapServers[i].getListenAddress();
            if (listenAddress == null) {
                listenAddress = InetAddress.getLoopbackAddress();
            }
            LDAPURL url = new LDAPURL("ldap", NetworkAddress.format(listenAddress), ldapServers[i].getListenPort(), null, null, null, null);
            urls.add(url.toString());
        }
        return urls.toArray(Strings.EMPTY_ARRAY);
    }

    public static Settings buildLdapSettings(String ldapUrl, String userTemplate, String groupSearchBase, LdapSearchScope scope) {
        return buildLdapSettings(new String[]{ldapUrl}, new String[]{userTemplate}, groupSearchBase, scope);
    }

    public static Settings buildLdapSettings(String[] ldapUrl, String userTemplate, String groupSearchBase, LdapSearchScope scope) {
        return buildLdapSettings(ldapUrl, new String[]{userTemplate}, groupSearchBase, scope);
    }

    public static Settings buildLdapSettings(String[] ldapUrl, String[] userTemplate, String groupSearchBase, LdapSearchScope scope) {
        return buildLdapSettings(ldapUrl, userTemplate, groupSearchBase, scope, null);
    }

    public static Settings buildLdapSettings(String[] ldapUrl, String[] userTemplate,
                                             String groupSearchBase, LdapSearchScope scope,
                                             LdapLoadBalancing serverSetType) {
        return buildLdapSettings(ldapUrl, userTemplate, groupSearchBase, scope,
                serverSetType, false);
    }

    public static Settings buildLdapSettings(String[] ldapUrl, String[] userTemplate,
                                             String groupSearchBase, LdapSearchScope scope,
                                             LdapLoadBalancing serverSetType,
                                             boolean ignoreReferralErrors) {
        return buildLdapSettings(REALM_IDENTIFIER, ldapUrl, userTemplate, groupSearchBase, scope, serverSetType, ignoreReferralErrors);
    }

    public static Settings buildLdapSettings(RealmConfig.RealmIdentifier realmId, String[] ldapUrl, String[] userTemplate,
                                             String groupSearchBase, LdapSearchScope scope, LdapLoadBalancing serverSetType,
                                             boolean ignoreReferralErrors) {
        Settings.Builder builder = Settings.builder()
                .putList(getFullSettingKey(realmId, URLS_SETTING), ldapUrl)
                .putList(getFullSettingKey(realmId.getName(), LdapSessionFactorySettings.USER_DN_TEMPLATES_SETTING), userTemplate)
                .put(getFullSettingKey(realmId, SessionFactorySettings.TIMEOUT_TCP_CONNECTION_SETTING), TimeValue.timeValueSeconds(1L))
                .put(getFullSettingKey(realmId, SessionFactorySettings.IGNORE_REFERRAL_ERRORS_SETTING), ignoreReferralErrors)
                .put(getFullSettingKey(realmId, SearchGroupsResolverSettings.BASE_DN), groupSearchBase)
                .put(getFullSettingKey(realmId, SearchGroupsResolverSettings.SCOPE), scope);
        if (serverSetType != null) {
            builder.put(getFullSettingKey(realmId, LdapLoadBalancingSettings.LOAD_BALANCE_TYPE_SETTING), serverSetType.toString());
        }
        if (inFipsJvm()) {
            builder.put(XPackSettings.DIAGNOSE_TRUST_EXCEPTIONS_SETTING.getKey(), false);
        }
        return builder.build();
    }

    public static Settings buildLdapSettings(String[] ldapUrl, String userTemplate, boolean hostnameVerification) {
        Settings.Builder builder = Settings.builder()
                .putList(getFullSettingKey(REALM_IDENTIFIER, URLS_SETTING), ldapUrl)
                .putList(getFullSettingKey(REALM_IDENTIFIER.getName(), LdapSessionFactorySettings.USER_DN_TEMPLATES_SETTING), userTemplate);
        if (randomBoolean()) {
            builder.put(getFullSettingKey(REALM_IDENTIFIER, SSLConfigurationSettings.VERIFICATION_MODE_SETTING_REALM),
                    hostnameVerification ? VerificationMode.FULL : VerificationMode.CERTIFICATE);
        } else {
            builder.put(getFullSettingKey(REALM_IDENTIFIER, HOSTNAME_VERIFICATION_SETTING), hostnameVerification);
        }
        return builder.build();
    }

    protected DnRoleMapper buildGroupAsRoleMapper(ResourceWatcherService resourceWatcherService) {
        Settings settings = Settings.builder()
                .put(getFullSettingKey(REALM_IDENTIFIER, DnRoleMapperSettings.USE_UNMAPPED_GROUPS_AS_ROLES_SETTING), true)
                .put("path.home", createTempDir())
                .build();
        RealmConfig config = new RealmConfig(REALM_IDENTIFIER, settings,
                TestEnvironment.newEnvironment(settings), new ThreadContext(Settings.EMPTY));

        return new DnRoleMapper(config, resourceWatcherService);
    }

    protected LdapSession session(SessionFactory factory, String username, SecureString password) {
        PlainActionFuture<LdapSession> future = new PlainActionFuture<>();
        factory.session(username, password, future);
        return future.actionGet();
    }

    protected List<String> groups(LdapSession ldapSession) {
        Objects.requireNonNull(ldapSession);
        PlainActionFuture<List<String>> future = new PlainActionFuture<>();
        ldapSession.groups(future);
        return future.actionGet();
    }

    protected LdapSession unauthenticatedSession(SessionFactory factory, String username) {
        PlainActionFuture<LdapSession> future = new PlainActionFuture<>();
        factory.unauthenticatedSession(username, future);
        return future.actionGet();
    }

    protected static void assertConnectionValid(LDAPInterface conn, SimpleBindRequest bindRequest) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    if (conn instanceof LDAPConnection) {
                        assertTrue(((LDAPConnection) conn).isConnected());
                        assertEquals(bindRequest.getBindDN(),
                                ((SimpleBindRequest) ((LDAPConnection) conn).getLastBindRequest()).getBindDN());
                        ((LDAPConnection) conn).reconnect();
                    } else if (conn instanceof LDAPConnectionPool) {
                        try (LDAPConnection c = ((LDAPConnectionPool) conn).getConnection()) {
                            assertTrue(c.isConnected());
                            assertEquals(bindRequest.getBindDN(), ((SimpleBindRequest) c.getLastBindRequest()).getBindDN());
                            c.reconnect();
                        }
                    }
                } catch (LDAPException e) {
                    fail("Connection is not valid. It will not work on follow referral flow." +
                            System.lineSeparator() + ExceptionsHelper.stackTrace(e));
                }
                return null;
            }
        });
    }
}
