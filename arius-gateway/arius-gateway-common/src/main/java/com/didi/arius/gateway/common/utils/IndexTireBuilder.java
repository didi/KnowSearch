package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexTireBuilder {
    private Map<String, IndexTemplate> templateMap;

    public IndexTireBuilder(Map<String, IndexTemplate> templateMap) {
        this.templateMap = templateMap;
    }

    public IndexTire build() {
        IndexTire indexTire = new IndexTire();
        for (Map.Entry<String, IndexTemplate> entry : templateMap.entrySet()) {
            String expression = entry.getKey();
            IndexTemplate indexTemplate = entry.getValue();

            List<IndexTire.Char> chars = getIndexChars(expression, indexTemplate.getDateFormat());
            indexTire.insert(chars, indexTemplate);

            if (false == StringUtils.isEmpty(indexTemplate.getDateFormat())) {
                List<IndexTire.Char> expChars = getIndexChars(expression, null);
                indexTire.insert(expChars, indexTemplate);
            }

            if (indexTemplate.getAliases() != null) {
                for (String alias : indexTemplate.getAliases()) {
                    List<IndexTire.Char> aliasChars = getIndexChars(alias, null);
                    indexTire.insert(aliasChars, indexTemplate);
                }
            }
        }

        return indexTire;
    }


    public static List<IndexTire.Char> getIndexChars(String expression, String dateFormat) {
        List<IndexTire.Char> chars = new ArrayList<>();
        for (int i = 0 ; i < expression.length(); ++i) {
            chars.add(new IndexTire.Char(expression.charAt(i), 0));
        }

        if (false == StringUtils.isEmpty(dateFormat)) {
            for (int i = 0 ; i < dateFormat.length(); ++i) {
                char c = dateFormat.charAt(i);
                switch (c) {
                    case 'Y':
                    case 'y':
                    case 'M':
                    case 'm':
                    case 'D':
                    case 'd':
                        chars.add(new IndexTire.Char(c, 1));
                        break;
                    default:
                        chars.add(new IndexTire.Char(c, 0));
                        break;
                }

            }
        }

        return chars;
    }
}
