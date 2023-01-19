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

package org.elasticsearch.ingest;

import java.util.function.Consumer;

class WrappingProcessorImpl extends FakeProcessor implements WrappingProcessor {

    WrappingProcessorImpl(String type, String tag, Consumer<IngestDocument> executor) {
        super(type, tag, executor);
    }

    @Override
    public Processor getInnerProcessor() {
        String theType = getType();
        String theTag = getTag();
        return new Processor() {
            @Override
            public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
                return ingestDocument;
            }

            @Override
            public String getType() {
                return theType;
            }

            @Override
            public String getTag() {
                return theTag;
            }
        };
    }
}
