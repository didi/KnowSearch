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

package org.elasticsearch.common.lucene.search.morelikethis;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.lucene.search.MoreLikeThisQuery;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class MoreLikeThisQueryTests extends ESTestCase {
    public void testSimple() throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(dir, new IndexWriterConfig(Lucene.STANDARD_ANALYZER));
        indexWriter.commit();

        Document document = new Document();
        document.add(new TextField("_id", "1", Field.Store.YES));
        document.add(new TextField("text", "lucene", Field.Store.YES));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new TextField("_id", "2", Field.Store.YES));
        document.add(new TextField("text", "lucene release", Field.Store.YES));
        indexWriter.addDocument(document);

        IndexReader reader = DirectoryReader.open(indexWriter);
        IndexSearcher searcher = new IndexSearcher(reader);

        MoreLikeThisQuery mltQuery = new MoreLikeThisQuery("lucene", new String[]{"text"}, Lucene.STANDARD_ANALYZER);
        mltQuery.setLikeText("lucene");
        mltQuery.setMinTermFrequency(1);
        mltQuery.setMinDocFreq(1);
        long count = searcher.count(mltQuery);
        assertThat(count, equalTo(2L));

        reader.close();
        indexWriter.close();
    }

    public void testValidateMaxQueryTerms() {
        IllegalArgumentException e1 = expectThrows(IllegalArgumentException.class,
            () ->  new MoreLikeThisQuery("lucene", new String[]{"text"}, Lucene.STANDARD_ANALYZER).setMaxQueryTerms(0));
        assertThat(e1.getMessage(), containsString("requires 'maxQueryTerms' to be greater than 0"));

        IllegalArgumentException e2 = expectThrows(IllegalArgumentException.class,
            () -> new MoreLikeThisQuery("lucene", new String[]{"text"}, Lucene.STANDARD_ANALYZER).setMaxQueryTerms(-3));
        assertThat(e2.getMessage(), containsString("requires 'maxQueryTerms' to be greater than 0"));
    }

}
