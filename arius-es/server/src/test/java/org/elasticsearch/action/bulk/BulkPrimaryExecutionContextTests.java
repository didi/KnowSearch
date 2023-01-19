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

package org.elasticsearch.action.bulk;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.TransportShardBulkActionTests.FakeDeleteResult;
import org.elasticsearch.action.bulk.TransportShardBulkActionTests.FakeIndexResult;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.test.ESTestCase;

import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkPrimaryExecutionContextTests extends ESTestCase {

    public void testAbortedSkipped() {
        BulkShardRequest shardRequest = generateRandomRequest();

        ArrayList<DocWriteRequest<?>> nonAbortedRequests = new ArrayList<>();
        for (BulkItemRequest request : shardRequest.items()) {
            if (randomBoolean()) {
                request.abort("index", new ElasticsearchException("bla"));
            } else {
                nonAbortedRequests.add(request.request());
            }
        }

        ArrayList<DocWriteRequest<?>> visitedRequests = new ArrayList<>();
        for (BulkPrimaryExecutionContext context = new BulkPrimaryExecutionContext(shardRequest, null);
             context.hasMoreOperationsToExecute();
             ) {
            visitedRequests.add(context.getCurrent());
            context.setRequestToExecute(context.getCurrent());
            // using failures prevents caring about types
            context.markOperationAsExecuted(new Engine.IndexResult(new ElasticsearchException("bla"), 1));
            context.markAsCompleted(context.getExecutionResult());
        }

        assertThat(visitedRequests, equalTo(nonAbortedRequests));
    }

    private BulkShardRequest generateRandomRequest() {
        BulkItemRequest[] items = new BulkItemRequest[randomInt(20)];
        for (int i = 0; i < items.length; i++) {
            final DocWriteRequest request;
            switch (randomFrom(DocWriteRequest.OpType.values())) {
                case INDEX:
                    request = new IndexRequest("index", "_doc", "id_" + i);
                    break;
                case CREATE:
                    request = new IndexRequest("index", "_doc", "id_" + i).create(true);
                    break;
                case UPDATE:
                    request = new UpdateRequest("index", "_doc", "id_" + i);
                    break;
                case DELETE:
                    request = new DeleteRequest("index", "_doc", "id_" + i);
                    break;
                default:
                    throw new AssertionError("unknown type");
            }
            items[i] = new BulkItemRequest(i, request);
        }
        return new BulkShardRequest(new ShardId("index", "_na_", 0),
            randomFrom(WriteRequest.RefreshPolicy.values()), items);
    }

    public void testTranslogLocation() {

        BulkShardRequest shardRequest = generateRandomRequest();

        Translog.Location expectedLocation = null;
        final IndexShard primary = mock(IndexShard.class);
        when(primary.shardId()).thenReturn(shardRequest.shardId());

        long translogGen = 0;
        long translogOffset = 0;

        BulkPrimaryExecutionContext context = new BulkPrimaryExecutionContext(shardRequest, primary);
        while (context.hasMoreOperationsToExecute()) {
            final Engine.Result result;
            final DocWriteRequest<?> current = context.getCurrent();
            final boolean failure = rarely();
            if (frequently()) {
                translogGen += randomIntBetween(1, 4);
                translogOffset = 0;
            } else {
                translogOffset += randomIntBetween(200, 400);
            }

            Translog.Location location = new Translog.Location(translogGen, translogOffset, randomInt(200));
            switch (current.opType()) {
                case INDEX:
                case CREATE:
                    context.setRequestToExecute(current);
                    if (failure) {
                        result = new Engine.IndexResult(new ElasticsearchException("bla"), 1);
                    } else {
                        result = new FakeIndexResult(1, 1, randomLongBetween(0, 200), randomBoolean(), location);
                    }
                    break;
                case UPDATE:
                    context.setRequestToExecute(new IndexRequest(current.index(), current.type(), current.id()));
                    if (failure) {
                        result = new Engine.IndexResult(new ElasticsearchException("bla"), 1, 1, 1);
                    } else {
                        result = new FakeIndexResult(1, 1, randomLongBetween(0, 200), randomBoolean(), location);
                    }
                    break;
                case DELETE:
                    context.setRequestToExecute(current);
                    if (failure) {
                        result = new Engine.DeleteResult(new ElasticsearchException("bla"), 1, 1);
                    } else {
                        result = new FakeDeleteResult(1, 1, randomLongBetween(0, 200), randomBoolean(), location);
                    }
                    break;
                default:
                    throw new AssertionError("unknown type:" + current.opType());
            }
            if (failure == false) {
                expectedLocation = location;
            }
            context.markOperationAsExecuted(result);
            context.markAsCompleted(context.getExecutionResult());
        }

        assertThat(context.getLocationToSync(), equalTo(expectedLocation));
    }
}
