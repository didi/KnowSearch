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

package org.elasticsearch;

import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.monitor.jvm.JvmInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Version implements Comparable<Version>, ToXContentFragment {

    public static final String INNER_VERSION = "1404.20210624";

    /*
     * The logic for ID is: XXYYZZAA, where XX is major version, YY is minor version, ZZ is revision, and AA is alpha/beta/rc indicator AA
     * values below 25 are for alpha builder (since 5.0), and above 25 and below 50 are beta builds, and below 99 are RC builds, with 99
     * indicating a release the (internal) format of the id is there so we can easily do after/before checks on the id
     *
     * IMPORTANT: Unreleased vs. Released Versions
     *
     * All listed versions MUST be released versions, except the last major, the last minor and the last revison. ONLY those are required
     * as unreleased versions.
     *
     * Example: assume the last release is 7.3.0
     * The unreleased last major is the next major release, e.g. _8_.0.0
     * The unreleased last minor is the current major with a upped minor: 7._4_.0
     * The unreleased revision is the very release with a upped revision 7.3._1_
     */
    public static final int V_EMPTY_ID = 0;
    public static final Version V_EMPTY = new Version(V_EMPTY_ID, org.apache.lucene.util.Version.LATEST);

    public static final Version V_6_0_0_alpha1 =
            new Version(6000001, org.apache.lucene.util.Version.LUCENE_7_0_0);
    public static final Version V_6_0_0_alpha2 =
            new Version(6000002, org.apache.lucene.util.Version.LUCENE_7_0_0);
    public static final Version V_6_0_0_beta1 =
        new Version(6000026, org.apache.lucene.util.Version.LUCENE_7_0_0);
    public static final Version V_6_0_0_beta2 =
        new Version(6000027, org.apache.lucene.util.Version.LUCENE_7_0_0);
    public static final Version V_6_0_0_rc1 =
        new Version(6000051, org.apache.lucene.util.Version.LUCENE_7_0_0);
    public static final Version V_6_0_0_rc2 =
        new Version(6000052, org.apache.lucene.util.Version.LUCENE_7_0_1);
    public static final Version V_6_0_0 =
        new Version(6000099, org.apache.lucene.util.Version.LUCENE_7_0_1);
    public static final Version V_6_0_1 =
        new Version(6000199, org.apache.lucene.util.Version.LUCENE_7_0_1);
    public static final Version V_6_1_0 = new Version(6010099, org.apache.lucene.util.Version.LUCENE_7_1_0);
    public static final Version V_6_1_1 = new Version(6010199, org.apache.lucene.util.Version.LUCENE_7_1_0);
    public static final Version V_6_1_2 = new Version(6010299, org.apache.lucene.util.Version.LUCENE_7_1_0);
    public static final Version V_6_1_3 = new Version(6010399, org.apache.lucene.util.Version.LUCENE_7_1_0);
    public static final Version V_6_1_4 = new Version(6010499, org.apache.lucene.util.Version.LUCENE_7_1_0);
    // The below version is missing from the 7.3 JAR
    private static final org.apache.lucene.util.Version LUCENE_7_2_1 = org.apache.lucene.util.Version.fromBits(7, 2, 1);
    public static final Version V_6_2_0 = new Version(6020099, LUCENE_7_2_1);
    public static final Version V_6_2_1 = new Version(6020199, LUCENE_7_2_1);
    public static final Version V_6_2_2 = new Version(6020299, LUCENE_7_2_1);
    public static final Version V_6_2_3 = new Version(6020399, LUCENE_7_2_1);
    public static final Version V_6_2_4 = new Version(6020499, LUCENE_7_2_1);
    public static final Version V_6_3_0 = new Version(6030099, org.apache.lucene.util.Version.LUCENE_7_3_1);
    public static final Version V_6_3_1 = new Version(6030199, org.apache.lucene.util.Version.LUCENE_7_3_1);
    public static final Version V_6_3_2 = new Version(6030299, org.apache.lucene.util.Version.LUCENE_7_3_1);
    public static final Version V_6_4_0 = new Version(6040099, org.apache.lucene.util.Version.LUCENE_7_4_0);
    public static final Version V_6_4_1 = new Version(6040199, org.apache.lucene.util.Version.LUCENE_7_4_0);
    public static final Version V_6_4_2 = new Version(6040299, org.apache.lucene.util.Version.LUCENE_7_4_0);
    public static final Version V_6_4_3 = new Version(6040399, org.apache.lucene.util.Version.LUCENE_7_4_0);
    public static final Version V_6_5_0 = new Version(6050099, org.apache.lucene.util.Version.LUCENE_7_5_0);
    public static final Version V_6_5_1 = new Version(6050199, org.apache.lucene.util.Version.LUCENE_7_5_0);
    public static final Version V_6_5_2 = new Version(6050299, org.apache.lucene.util.Version.LUCENE_7_5_0);
    public static final Version V_6_5_3 = new Version(6050399, org.apache.lucene.util.Version.LUCENE_7_5_0);
    public static final Version V_6_5_4 = new Version(6050499, org.apache.lucene.util.Version.LUCENE_7_5_0);
    public static final Version V_6_6_0 = new Version(6060099, org.apache.lucene.util.Version.LUCENE_7_6_0);
    public static final Version V_6_6_1 = new Version(6060199, org.apache.lucene.util.Version.LUCENE_7_6_0);
    public static final Version V_6_6_2 = new Version(6060299, org.apache.lucene.util.Version.LUCENE_7_6_0);
    public static final Version V_6_7_0 = new Version(6070099, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_7_1 = new Version(6070199, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_7_2 = new Version(6070299, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_8_0 = new Version(6080099, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_8_1 = new Version(6080199, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_8_2 = new Version(6080299, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_8_3 = new Version(6080399, org.apache.lucene.util.Version.LUCENE_7_7_0);
    public static final Version V_6_8_4 = new Version(6080499, org.apache.lucene.util.Version.LUCENE_7_7_2);
    public static final Version V_6_8_5 = new Version(6080599, org.apache.lucene.util.Version.LUCENE_7_7_2);
    public static final Version V_6_8_6 = new Version(6080699, org.apache.lucene.util.Version.LUCENE_7_7_2);
    public static final Version V_6_8_7 = new Version(6080799, org.apache.lucene.util.Version.LUCENE_7_7_2);
    public static final Version V_7_0_0 = new Version(7000099, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_0_1 = new Version(7000199, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_1_0 = new Version(7010099, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_1_1 = new Version(7010199, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_2_0 = new Version(7020099, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_2_1 = new Version(7020199, org.apache.lucene.util.Version.LUCENE_8_0_0);
    public static final Version V_7_3_0 = new Version(7030099, org.apache.lucene.util.Version.LUCENE_8_1_0);
    public static final Version V_7_3_1 = new Version(7030199, org.apache.lucene.util.Version.LUCENE_8_1_0);
    public static final Version V_7_3_2 = new Version(7030299, org.apache.lucene.util.Version.LUCENE_8_1_0);
    public static final Version V_7_4_0 = new Version(7040099, org.apache.lucene.util.Version.LUCENE_8_2_0);
    public static final Version V_7_4_1 = new Version(7040199, org.apache.lucene.util.Version.LUCENE_8_2_0);
    public static final Version V_7_4_2 = new Version(7040299, org.apache.lucene.util.Version.LUCENE_8_2_0);
    public static final Version V_7_5_0 = new Version(7050099, org.apache.lucene.util.Version.LUCENE_8_3_0);
    public static final Version V_7_5_1 = new Version(7050199, org.apache.lucene.util.Version.LUCENE_8_3_0);
    public static final Version V_7_5_2 = new Version(7050299, org.apache.lucene.util.Version.LUCENE_8_3_0);
    public static final Version V_7_5_3 = new Version(7050399, org.apache.lucene.util.Version.LUCENE_8_3_0);
    public static final Version V_7_6_0 = new Version(7060099, org.apache.lucene.util.Version.LUCENE_8_4_0);

    public static final Version CURRENT = V_7_6_0;

    private static final ImmutableOpenIntMap<Version> idToVersion;

    static {
        final ImmutableOpenIntMap.Builder<Version> builder = ImmutableOpenIntMap.builder();

        for (final Field declaredField : Version.class.getFields()) {
            if (declaredField.getType().equals(Version.class)) {
                final String fieldName = declaredField.getName();
                if (fieldName.equals("CURRENT") || fieldName.equals("V_EMPTY")) {
                    continue;
                }
                assert fieldName.matches("V_\\d+_\\d+_\\d+(_alpha[1,2]|_beta[1,2]|_rc[1,2])?")
                        : "expected Version field [" + fieldName + "] to match V_\\d+_\\d+_\\d+";
                try {
                    final Version version = (Version) declaredField.get(null);
                    if (Assertions.ENABLED) {
                        final String[] fields = fieldName.split("_");
                        if (fields.length == 5) {
                            assert fields[1].equals("6") && fields[2].equals("0") :
                                "field " + fieldName + " should not have a build qualifier";
                        } else {
                            final int major = Integer.valueOf(fields[1]) * 1000000;
                            final int minor = Integer.valueOf(fields[2]) * 10000;
                            final int revision = Integer.valueOf(fields[3]) * 100;
                            final int expectedId = major + minor + revision + 99;
                            assert version.id == expectedId :
                                "expected version [" + fieldName + "] to have id [" + expectedId + "] but was [" + version.id + "]";
                        }
                    }
                    final Version maybePrevious = builder.put(version.id, version);
                    assert maybePrevious == null :
                            "expected [" + version.id + "] to be uniquely mapped but saw [" + maybePrevious + "] and [" + version + "]";
                } catch (final IllegalAccessException e) {
                    assert false : "Version field [" + fieldName + "] should be public";
                }
            }
        }
        assert CURRENT.luceneVersion.equals(org.apache.lucene.util.Version.LATEST) : "Version must be upgraded to ["
                + org.apache.lucene.util.Version.LATEST + "] is still set to [" + CURRENT.luceneVersion + "]";

        idToVersion = builder.build();
    }

    public static Version readVersion(StreamInput in) throws IOException {
        return fromId(in.readVInt());
    }

    public static Version fromId(int id) {
        if (idToVersion.containsKey(id)) {
            return idToVersion.get(id);
        }
        switch (id) {

            case V_EMPTY_ID:
                return V_EMPTY;
            default:
                // We need at least the major of the Lucene version to be correct.
                // Our best guess is to use the same Lucene version as the previous
                // version in the list, assuming that it didn't change. This is at
                // least correct for patch versions of known minors since we never
                // update the Lucene dependency for patch versions.
                List<Version> versions = DeclaredVersionsHolder.DECLARED_VERSIONS;
                Version tmp = new Version(id, org.apache.lucene.util.Version.LATEST);
                int index = Collections.binarySearch(versions, tmp);
                if (index < 0) {
                    index = -2 - index;
                } else {
                    assert false : "Version [" + tmp + "] is declared but absent from the switch statement in Version#fromId";
                }
                final org.apache.lucene.util.Version luceneVersion;
                if (index == -1) {
                    // this version is older than any supported version, so we
                    // assume it is the previous major to the oldest Lucene version
                    // that we know about
                    luceneVersion = org.apache.lucene.util.Version.fromBits(
                            versions.get(0).luceneVersion.major - 1, 0, 0);
                } else {
                    luceneVersion = versions.get(index).luceneVersion;
                }
                return new Version(id, luceneVersion);
        }
    }

    /**
     * Return the {@link Version} of Elasticsearch that has been used to create an index given its settings.
     *
     * @throws IllegalStateException if the given index settings doesn't contain a value for the key
     *         {@value IndexMetaData#SETTING_VERSION_CREATED}
     */
    public static Version indexCreated(Settings indexSettings) {
        final Version indexVersion = IndexMetaData.SETTING_INDEX_VERSION_CREATED.get(indexSettings);
        if (indexVersion == V_EMPTY) {
            final String message = String.format(
                    Locale.ROOT,
                    "[%s] is not present in the index settings for index with UUID [%s]",
                    IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(),
                    indexSettings.get(IndexMetaData.SETTING_INDEX_UUID));
            throw new IllegalStateException(message);
        }
        return indexVersion;
    }

    public static void writeVersion(Version version, StreamOutput out) throws IOException {
        out.writeVInt(version.id);
    }

    /**
     * Returns the minimum version between the 2.
     */
    public static Version min(Version version1, Version version2) {
        return version1.id < version2.id ? version1 : version2;
    }

    /**
     * Returns the maximum version between the 2
     */
    public static Version max(Version version1, Version version2) { return version1.id > version2.id ? version1 : version2; }

    /**
     * Returns the version given its string representation, current version if the argument is null or empty
     */
    public static Version fromString(String version) {
        if (!Strings.hasLength(version)) {
            return Version.CURRENT;
        }
        final boolean snapshot; // this is some BWC for 2.x and before indices
        if (snapshot = version.endsWith("-SNAPSHOT")) {
            version = version.substring(0, version.length() - 9);
        }
        String[] parts = version.split("[.-]");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException(
                    "the version needs to contain major, minor, and revision, and optionally the build: " + version);
        }

        try {
            final int rawMajor = Integer.parseInt(parts[0]);
            if (rawMajor >= 5 && snapshot) { // we don't support snapshot as part of the version here anymore
                throw new IllegalArgumentException("illegal version format - snapshots are only supported until version 2.x");
            }
            if (rawMajor >=7 && parts.length == 4) { // we don't support qualifier as part of the version anymore
                throw new IllegalArgumentException("illegal version format - qualifiers are only supported until version 6.x");
            }
            final int betaOffset = rawMajor < 5 ? 0 : 25;
            //we reverse the version id calculation based on some assumption as we can't reliably reverse the modulo
            final int major = rawMajor * 1000000;
            final int minor = Integer.parseInt(parts[1]) * 10000;
            final int revision = Integer.parseInt(parts[2]) * 100;


            int build = 99;
            if (parts.length == 4) {
                String buildStr = parts[3];
                if (buildStr.startsWith("alpha")) {
                    assert rawMajor >= 5 : "major must be >= 5 but was " + major;
                    build = Integer.parseInt(buildStr.substring(5));
                    assert build < 25 : "expected a alpha build but " + build + " >= 25";
                } else if (buildStr.startsWith("Beta") || buildStr.startsWith("beta")) {
                    build = betaOffset + Integer.parseInt(buildStr.substring(4));
                    assert build < 50 : "expected a beta build but " + build + " >= 50";
                } else if (buildStr.startsWith("RC") || buildStr.startsWith("rc")) {
                    build = Integer.parseInt(buildStr.substring(2)) + 50;
                } else {
                    throw new IllegalArgumentException("unable to parse version " + version);
                }
            }

            return fromId(major + minor + revision + build);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("unable to parse version " + version, e);
        }
    }

    public final int id;
    public final byte major;
    public final byte minor;
    public final byte revision;
    public final byte build;
    public final org.apache.lucene.util.Version luceneVersion;

    Version(int id, org.apache.lucene.util.Version luceneVersion) {
        this.id = id;
        this.major = (byte) ((id / 1000000) % 100);
        this.minor = (byte) ((id / 10000) % 100);
        this.revision = (byte) ((id / 100) % 100);
        this.build = (byte) (id % 100);
        this.luceneVersion = Objects.requireNonNull(luceneVersion);
    }

    public boolean after(Version version) {
        return version.id < id;
    }

    public boolean onOrAfter(Version version) {
        return version.id <= id;
    }

    public boolean before(Version version) {
        return version.id > id;
    }

    public boolean onOrBefore(Version version) {
        return version.id >= id;
    }

    @Override
    public int compareTo(Version other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder.value(toString());
    }

    /*
     * We need the declared versions when computing the minimum compatibility version. As computing the declared versions uses reflection it
     * is not cheap. Since computing the minimum compatibility version can occur often, we use this holder to compute the declared versions
     * lazily once.
     */
    private static class DeclaredVersionsHolder {
        static final List<Version> DECLARED_VERSIONS = Collections.unmodifiableList(getDeclaredVersions(Version.class));
    }

    /**
     * Returns the minimum compatible version based on the current
     * version. Ie a node needs to have at least the return version in order
     * to communicate with a node running the current version. The returned version
     * is in most of the cases the smallest major version release unless the current version
     * is a beta or RC release then the version itself is returned.
     */
    public Version minimumCompatibilityVersion() {
        if (major == 6) {
            // force the minimum compatibility for version 6 to 5.6 since we don't reference version 5 anymore
            return Version.fromId(5060099);
        } else if (major >= 7) {
            // all major versions from 7 onwards are compatible with last minor series of the previous major
            Version bwcVersion = null;

            for (int i = DeclaredVersionsHolder.DECLARED_VERSIONS.size() - 1; i >= 0; i--) {
                final Version candidateVersion = DeclaredVersionsHolder.DECLARED_VERSIONS.get(i);
                if (candidateVersion.major == major - 1 && candidateVersion.isRelease() && after(candidateVersion)) {
                    if (bwcVersion != null && candidateVersion.minor < 6) {
                        break;
                    }
                    bwcVersion = candidateVersion;
                }
            }
            return bwcVersion == null ? this : bwcVersion;
        }

        return Version.min(this, fromId((int) major * 1000000 + 0 * 10000 + 99));
    }

    /**
     * Returns the minimum created index version that this version supports. Indices created with lower versions
     * can't be used with this version. This should also be used for file based serialization backwards compatibility ie. on serialization
     * code that is used to read / write file formats like transaction logs, cluster state, and index metadata.
     */
    public Version minimumIndexCompatibilityVersion() {
        final int bwcMajor;
        if (major == 5) {
            bwcMajor = 2; // we jumped from 2 to 5
        } else if (major == 7) {
            return V_6_0_0_beta1;
        } else {
            bwcMajor = major - 1;
        }
        final int bwcMinor = 0;
        return Version.min(this, fromId(bwcMajor * 1000000 + bwcMinor * 10000 + 99));
    }

    /**
     * Returns <code>true</code> iff both version are compatible. Otherwise <code>false</code>
     */
    public boolean isCompatible(Version version) {
        boolean compatible = onOrAfter(version.minimumCompatibilityVersion())
            && version.onOrAfter(minimumCompatibilityVersion());

        assert compatible == false || Math.max(major, version.major) - Math.min(major, version.major) <= 1;
        return compatible;
    }

    @SuppressForbidden(reason = "System.out.*")
    public static void main(String[] args) {
        final String versionOutput = String.format(
                Locale.ROOT,
                "Version: %s, Build: %s/%s/%s/%s, JVM: %s",
            Build.CURRENT.getQualifiedVersion(),
                Build.CURRENT.flavor().displayName(),
                Build.CURRENT.type().displayName(),
                Build.CURRENT.hash(),
                Build.CURRENT.date(),
                JvmInfo.jvmInfo().version());
        System.out.println(versionOutput);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor).append('.').append(revision);
        if (isAlpha()) {
            sb.append("-alpha");
            sb.append(build);
        } else if (isBeta()) {
            if (major >= 2) {
                sb.append("-beta");
            } else {
                sb.append(".Beta");
            }
            sb.append(major < 5 ? build : build-25);
        } else if (build < 99) {
            if (major >= 2) {
                sb.append("-rc");
            } else {
                sb.append(".RC");
            }
            sb.append(build - 50);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        if (id != version.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isBeta() {
        return major < 5 ? build < 50 : build >= 25 && build < 50;
    }

    /**
     * Returns true iff this version is an alpha version
     * Note: This has been introduced in elasticsearch version 5. Previous versions will never
     * have an alpha version.
     */
    public boolean isAlpha() {
        return major < 5 ? false :  build < 25;
    }

    public boolean isRC() {
        return build > 50 && build < 99;
    }

    public boolean isRelease() {
        return build == 99;
    }

    /**
     * Extracts a sorted list of declared version constants from a class.
     * The argument would normally be Version.class but is exposed for
     * testing with other classes-containing-version-constants.
     */
    public static List<Version> getDeclaredVersions(final Class<?> versionClass) {
        final Field[] fields = versionClass.getFields();
        final List<Version> versions = new ArrayList<>(fields.length);
        for (final Field field : fields) {
            final int mod = field.getModifiers();
            if (false == Modifier.isStatic(mod) && Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
                continue;
            }
            if (field.getType() != Version.class) {
                continue;
            }
            switch (field.getName()) {
                case "CURRENT":
                case "V_EMPTY":
                    continue;
            }
            assert field.getName().matches("V(_\\d+)+(_(alpha|beta|rc)\\d+)?") : field.getName();
            try {
                versions.add(((Version) field.get(null)));
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Collections.sort(versions);
        return versions;
    }
}
