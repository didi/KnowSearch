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

package com.didichuxing.datachannel.arius.plugin.appendlucene;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Request to reindex some documents from one index to another. This implements CompositeIndicesRequest but in a misleading way. Rather than
 * returning all the subrequests that it will make it tries to return a representative set of subrequests. This is best-effort for a bunch
 * of reasons, not least of which that scripts are allowed to change the destination request in drastic ways, including changing the index
 * to which documents are written.
 */
public class AppendLuceneRequest extends ActionRequest {

    /* 索引名 */
    public String indexName;

    /* 索引uuid */
    public String uuid;

    /* shardId */
    public int shardId;

    /* 待加载的lucene数据所在的目录 */
    public String appendSegmentDirs;

    /* 索引的主键 */
    public String primeKey;

    public AppendLuceneRequest() { }

    public void check() throws Exception {
        check(indexName, "indexName is empty");
        check(uuid, "uuid is empty");

        if(shardId<0) {
            throw new Exception("shardId <0, shardId:" + shardId);
        }

        check(appendSegmentDirs, "appendSegmentDirs is empty");
        for(String dir : appendSegmentDirs.split(",")) {
            check(dir, "dir is empty");
        }
    }

    public List<String> getAppendDirs() {
        List<String> dirs = new ArrayList<>();

        for(String dir : appendSegmentDirs.split(",")) {
            dirs.add(dir);
        }

        return dirs;
    }



    private void check(String str, String errMsg) throws Exception {
        if(isBlank(str)) {
            throw new Exception(errMsg);
        }
    }


    private boolean isBlank(String str) {
        if(str==null) {
            return true;
        }

        if(str.trim().length()==0) {
            return true;
        }

        return false;
    }



    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
