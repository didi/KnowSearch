/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.rest;

import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestRequest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RemoteHostHeader {

    static final String KEY = "_rest_remote_address";

    /**
     * Extracts the remote address from the given rest request and puts in the request context. This will
     * then be copied to the subsequent action requests.
     */
    public static void process(RestRequest request, ThreadContext threadContext) {
        threadContext.putTransient(KEY, request.getHttpChannel().getRemoteAddress());
    }

    /**
     * Extracts the rest remote address from the message context. If not found, returns {@code null}. transport
     * messages that were created by rest handlers, should have this in their context.
     */
    public static InetSocketAddress restRemoteAddress(ThreadContext threadContext) {
        SocketAddress address = threadContext.getTransient(KEY);
        if (address != null && address instanceof InetSocketAddress) {
            return (InetSocketAddress) address;
        }
        return null;
    }

    public static void putRestRemoteAddress(ThreadContext threadContext, SocketAddress address) {
        threadContext.putTransient(KEY, address);
    }
}
