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

package org.elasticsearch.index.store.smbmmapfs;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.MMapDirectory;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.store.FsDirectoryFactory;
import org.elasticsearch.index.store.SmbDirectoryWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

public final class SmbMmapFsDirectoryFactory extends FsDirectoryFactory {

    @Override
    protected Directory newFSDirectory(Path location, LockFactory lockFactory, IndexSettings indexSettings) throws IOException {
        return new SmbDirectoryWrapper(setPreload(new MMapDirectory(location, lockFactory), lockFactory, new HashSet<>(
            indexSettings.getValue(IndexModule.INDEX_STORE_PRE_LOAD_SETTING))));
    }
}
