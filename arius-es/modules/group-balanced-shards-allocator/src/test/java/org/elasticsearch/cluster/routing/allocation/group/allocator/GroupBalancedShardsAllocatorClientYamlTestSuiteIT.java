package org.elasticsearch.cluster.routing.allocation.group.allocator;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import org.elasticsearch.test.rest.yaml.ClientYamlTestCandidate;
import org.elasticsearch.test.rest.yaml.ESClientYamlSuiteTestCase;

/**
 * author weizijun
 * date：2020-02-24
 */
public class GroupBalancedShardsAllocatorClientYamlTestSuiteIT extends ESClientYamlSuiteTestCase {

    public GroupBalancedShardsAllocatorClientYamlTestSuiteIT(@Name("yaml") ClientYamlTestCandidate testCandidate) {
        super(testCandidate);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() throws Exception {
        return ESClientYamlSuiteTestCase.createParameters();
    }
}
