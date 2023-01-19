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

package org.elasticsearch.transport;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.network.InetAddresses;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.common.Booleans.parseBoolean;

public class TransportInfo implements Writeable, ToXContentFragment {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(LogManager.getLogger(TransportInfo.class));

    /** Whether to add hostname to publish host field when serializing. */
    private static final boolean CNAME_IN_PUBLISH_ADDRESS =
            parseBoolean(System.getProperty("es.transport.cname_in_publish_address"), false);

    private BoundTransportAddress address;
    private Map<String, BoundTransportAddress> profileAddresses;
    private final boolean cnameInPublishAddress;

    public TransportInfo(BoundTransportAddress address, @Nullable Map<String, BoundTransportAddress> profileAddresses) {
        this(address, profileAddresses, CNAME_IN_PUBLISH_ADDRESS);
    }

    public TransportInfo(BoundTransportAddress address, @Nullable Map<String, BoundTransportAddress> profileAddresses,
                         boolean cnameInPublishAddress) {
        this.address = address;
        this.profileAddresses = profileAddresses;
        this.cnameInPublishAddress = cnameInPublishAddress;
    }

    public TransportInfo(StreamInput in) throws IOException {
        address = new BoundTransportAddress(in);
        int size = in.readVInt();
        if (size > 0) {
            profileAddresses = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                BoundTransportAddress value = new BoundTransportAddress(in);
                profileAddresses.put(key, value);
            }
        }
        this.cnameInPublishAddress = CNAME_IN_PUBLISH_ADDRESS;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        address.writeTo(out);
        if (profileAddresses != null) {
            out.writeVInt(profileAddresses.size());
        } else {
            out.writeVInt(0);
        }
        if (profileAddresses != null && profileAddresses.size() > 0) {
            for (Map.Entry<String, BoundTransportAddress> entry : profileAddresses.entrySet()) {
                out.writeString(entry.getKey());
                entry.getValue().writeTo(out);
            }
        }
    }

    static final class Fields {
        static final String TRANSPORT = "transport";
        static final String BOUND_ADDRESS = "bound_address";
        static final String PUBLISH_ADDRESS = "publish_address";
        static final String PROFILES = "profiles";
    }

    private String formatPublishAddressString(String propertyName, TransportAddress publishAddress){
        String publishAddressString = publishAddress.toString();
        String hostString = publishAddress.address().getHostString();
        if (InetAddresses.isInetAddress(hostString) == false) {
            if (cnameInPublishAddress) {
                publishAddressString = hostString + '/' + publishAddress.toString();
            } else {
                deprecationLogger.deprecated(
                        propertyName + " was printed as [ip:port] instead of [hostname/ip:port]. "
                                + "This format is deprecated and will change to [hostname/ip:port] in a future version. "
                                + "Use -Des.transport.cname_in_publish_address=true to enforce non-deprecated formatting."
                );
            }
        }
        return publishAddressString;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.TRANSPORT);
        builder.array(Fields.BOUND_ADDRESS, (Object[]) address.boundAddresses());
        builder.field(Fields.PUBLISH_ADDRESS, formatPublishAddressString("transport.publish_address", address.publishAddress()));
        builder.startObject(Fields.PROFILES);
        if (profileAddresses != null && profileAddresses.size() > 0) {
            for (Map.Entry<String, BoundTransportAddress> entry : profileAddresses.entrySet()) {
                builder.startObject(entry.getKey());
                builder.array(Fields.BOUND_ADDRESS, (Object[]) entry.getValue().boundAddresses());
                String propertyName = "transport." + entry.getKey() + ".publish_address";
                builder.field(Fields.PUBLISH_ADDRESS, formatPublishAddressString(propertyName, entry.getValue().publishAddress()));
                builder.endObject();
            }
        }
        builder.endObject();
        builder.endObject();
        return builder;
    }

    public BoundTransportAddress address() {
        return address;
    }

    public BoundTransportAddress getAddress() {
        return address();
    }

    public Map<String, BoundTransportAddress> getProfileAddresses() {
        return profileAddresses();
    }

    public Map<String, BoundTransportAddress> profileAddresses() {
        return profileAddresses;
    }
}
