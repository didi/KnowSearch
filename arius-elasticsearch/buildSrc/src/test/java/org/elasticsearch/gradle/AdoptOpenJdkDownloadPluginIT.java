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

package org.elasticsearch.gradle;

import java.io.IOException;
import java.io.InputStream;

public class AdoptOpenJdkDownloadPluginIT extends JdkDownloadPluginIT {

    @Override
    public String oldJdkVersion() {
        return "1+99";
    }

    @Override
    public String jdkVersion() {
        return "12.0.2+10";
    }

    @Override
    public String jdkVendor() {
        return "adoptopenjdk";
    }

    @Override
    protected String urlPath(final boolean isOld, final String platform, final String extension) {
        final String module = platform.equals("osx") ? "mac" : platform;
        if (isOld) {
            return "/adoptopenjdk/OpenJDK1U-jdk_x64_" + module + "_hotspot_1_99." + extension;
        } else {
            return "/adoptopenjdk/OpenJDK12U-jdk_x64_" + module + "_hotspot_12.0.2_10." + extension;
        }
    }

    @Override
    protected byte[] filebytes(final String platform, final String extension) throws IOException {
        try (InputStream stream = JdkDownloadPluginIT.class.getResourceAsStream("fake_adoptopenjdk_" + platform + "." + extension)) {
            return stream.readAllBytes();
        }
    }

}
