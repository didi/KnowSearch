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
package org.elasticsearch.client.indices;

import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class GetIndexTemplatesResponse  {

    @Override
    public String toString() {
        List<IndexTemplateMetaData> thisList = new ArrayList<>(this.indexTemplates);
        thisList.sort(Comparator.comparing(IndexTemplateMetaData::name));
        return "GetIndexTemplatesResponse [indexTemplates=" + thisList + "]";
    }

    private final List<IndexTemplateMetaData> indexTemplates;

    GetIndexTemplatesResponse() {
        indexTemplates = new ArrayList<>();
    }

    GetIndexTemplatesResponse(List<IndexTemplateMetaData> indexTemplates) {
        this.indexTemplates = indexTemplates;
    }

    public List<IndexTemplateMetaData> getIndexTemplates() {
        return indexTemplates;
    }


    public static GetIndexTemplatesResponse fromXContent(XContentParser parser) throws IOException {
        final List<IndexTemplateMetaData> templates = new ArrayList<>();
        for (XContentParser.Token token = parser.nextToken(); token != XContentParser.Token.END_OBJECT; token = parser.nextToken()) {
            if (token == XContentParser.Token.FIELD_NAME) {
                final IndexTemplateMetaData templateMetaData = IndexTemplateMetaData.Builder.fromXContent(parser, parser.currentName());
                templates.add(templateMetaData);
            }
        }
        return new GetIndexTemplatesResponse(templates);
    }

    @Override
    public int hashCode() {
        List<IndexTemplateMetaData> sortedList = new ArrayList<>(this.indexTemplates);
        sortedList.sort(Comparator.comparing(IndexTemplateMetaData::name));
        return Objects.hash(sortedList);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        // To compare results we need to make sure the templates are listed in the same order
        GetIndexTemplatesResponse other = (GetIndexTemplatesResponse) obj;
        List<IndexTemplateMetaData> thisList = new ArrayList<>(this.indexTemplates);
        List<IndexTemplateMetaData> otherList = new ArrayList<>(other.indexTemplates);
        thisList.sort(Comparator.comparing(IndexTemplateMetaData::name));
        otherList.sort(Comparator.comparing(IndexTemplateMetaData::name));
        return Objects.equals(thisList, otherList);
    }
    
    
}
