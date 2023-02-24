package com.didi.cloud.fastdump.common.utils.lucene;

import java.util.ArrayList;
import java.util.List;

import com.didi.cloud.fastdump.common.content.Tuple;

/**
 * Created by linyunan on 2022/8/31
 */
public class LuceneReaderUtil {
    private LuceneReaderUtil() {
    } // no instance

    /**
     * Returns index of the searcher/reader for document <code>n</code> in the
     * array used to construct this searcher/reader.
     */
    public static int subIndex(int n, int[] docStarts) { // find
        // searcher/reader for doc n:
        int size = docStarts.length;
        int lo = 0; // search starts array
        int hi = size - 1; // for first element less than n, return its index
        while (hi >= lo) {
            int mid = (lo + hi) >>> 1;
            int midValue = docStarts[mid];
            if (n < midValue)
                hi = mid - 1;
            else if (n > midValue)
                lo = mid + 1;
            else { // found a match
                while (mid + 1 < size && docStarts[mid + 1] == midValue) {
                    mid++; // scan to last match
                }
                return mid;
            }
        }
        return hi;
    }

    /**
     * 构建bulk列表 :[[0,1000], [1001,2000], [2001,3000]....]
     * @param maxDoc                   总文档数
     * @param singleReadBulkSize       单个bulk长度
     * @return
     */
    public static List<Tuple<Integer, Integer>> getBulkList(Integer maxDoc, Integer singleReadBulkSize) {
        List<Tuple<Integer, Integer>> bulkList = new ArrayList<>();
        if (maxDoc < singleReadBulkSize) {
            bulkList.add(new Tuple<>(0, maxDoc));
            return bulkList;
        }

        for (int i = 0; i < maxDoc; i += singleReadBulkSize) {
            if (i + singleReadBulkSize > maxDoc) {
                bulkList.add(new Tuple<>(i, maxDoc));
                continue;
            }
            bulkList.add(new Tuple<>(i, i + singleReadBulkSize));
        }
        return bulkList;
    }
}
