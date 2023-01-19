package org.elasticsearch.plugin.spatial.router;

import java.util.HashSet;
import java.util.Set;

public class RouterParam {
    public Double lat = null;
    public Double lng = null;
    public Double radius = null;

    public Set<Integer> cityIds =  new HashSet<>();
}
