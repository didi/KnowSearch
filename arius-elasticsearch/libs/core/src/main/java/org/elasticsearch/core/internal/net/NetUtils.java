/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.core.internal.net;

import java.lang.reflect.Field;
import java.net.SocketOption;

/**
 * Utilities for network-related methods.
 */
public class NetUtils {

    /**
     * Returns the extended TCP_KEEPIDLE socket option, if available on this JDK
     */
    public static SocketOption<Integer> getTcpKeepIdleSocketOptionOrNull() {
        return getExtendedSocketOptionOrNull("TCP_KEEPIDLE");
    }

    /**
     * Returns the extended TCP_KEEPINTERVAL socket option, if available on this JDK
     */
    public static SocketOption<Integer> getTcpKeepIntervalSocketOptionOrNull() {
        return getExtendedSocketOptionOrNull("TCP_KEEPINTERVAL");
    }

    /**
     * Returns the extended TCP_KEEPCOUNT socket option, if available on this JDK
     */
    public static SocketOption<Integer> getTcpKeepCountSocketOptionOrNull() {
        return getExtendedSocketOptionOrNull("TCP_KEEPCOUNT");
    }

    @SuppressWarnings("unchecked")
    private static <T> SocketOption<T> getExtendedSocketOptionOrNull(String fieldName) {
        try {
            final Class<?> extendedSocketOptionsClass = Class.forName("jdk.net.ExtendedSocketOptions");
            final Field field = extendedSocketOptionsClass.getField(fieldName);
            return (SocketOption<T>) field.get(null);
        } catch (Exception t) {
            // ignore
            return null;
        }
    }
}
