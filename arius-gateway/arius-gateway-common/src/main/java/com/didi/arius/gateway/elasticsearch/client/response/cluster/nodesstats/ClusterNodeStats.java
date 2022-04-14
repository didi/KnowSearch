package com.didi.arius.gateway.elasticsearch.client.response.cluster.nodesstats;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.response.model.breakers.Breakers;
import com.didi.arius.gateway.elasticsearch.client.response.model.fs.FSNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.http.HttpNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.indices.CommonStat;
import com.didi.arius.gateway.elasticsearch.client.response.model.jvm.JvmNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.node.NodeAttributes;
import com.didi.arius.gateway.elasticsearch.client.response.model.os.OsNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.process.ProcessNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.script.ScriptNode;
import com.didi.arius.gateway.elasticsearch.client.response.model.threadpool.ThreadPoolNodes;
import com.didi.arius.gateway.elasticsearch.client.response.model.transport.TransportNode;

import java.util.List;

public class ClusterNodeStats {
       @JSONField(name = "timestamp")
       private long timestamp;

       @JSONField(name = "name")
       private String name;

       @JSONField(name = "transport_address")
       private String transportAddress;

       @JSONField(name = "host")
       private String host;

       @JSONField(name = "ip")
       private List<String> ip;

       @JSONField(name = "attributes")
       private NodeAttributes attributes;

       @JSONField(name = "indices")
       private CommonStat indices;

       @JSONField(name = "os")
       private OsNode os;

       @JSONField(name = "process")
       private ProcessNode process;

       @JSONField(name = "jvm")
       private JvmNode jvm;

       @JSONField(name = "thread_pool")
       private ThreadPoolNodes threadPool;

       @JSONField(name = "fs")
       private FSNode fs;

       @JSONField(name = "transport")
       private TransportNode transport;

       @JSONField(name = "http")
       private HttpNode http;

       @JSONField(name = "breakers")
       private Breakers breakers;

       @JSONField(name = "script")
       private ScriptNode script;

       public ClusterNodeStats() {
              // pass
       }

       public long getTimestamp() {
              return timestamp;
       }

       public void setTimestamp(long timestamp) {
              this.timestamp = timestamp;
       }

       public String getName() {
              return name;
       }

       public void setName(String name) {
              this.name = name;
       }

       public String getTransportAddress() {
              return transportAddress;
       }

       public void setTransportAddress(String transportAddress) {
              this.transportAddress = transportAddress;
       }

       public String getHost() {
              return host;
       }

       public void setHost(String host) {
              this.host = host;
       }

       public List<String> getIp() {
              return ip;
       }

       public void setIp(List<String> ip) {
              this.ip = ip;
       }

       public NodeAttributes getAttributes() {
              return attributes;
       }

       public void setAttributes(NodeAttributes attributes) {
              this.attributes = attributes;
       }

       public CommonStat getIndices() {
              return indices;
       }

       public void setIndices(CommonStat indices) {
              this.indices = indices;
       }

       public OsNode getOs() {
              return os;
       }

       public void setOs(OsNode os) {
              this.os = os;
       }

       public ProcessNode getProcess() {
              return process;
       }

       public void setProcess(ProcessNode process) {
              this.process = process;
       }

       public JvmNode getJvm() {
              return jvm;
       }

       public void setJvm(JvmNode jvm) {
              this.jvm = jvm;
       }

       public ThreadPoolNodes getThreadPool() {
              return threadPool;
       }

       public void setThreadPool(ThreadPoolNodes threadPool) {
              this.threadPool = threadPool;
       }

       public FSNode getFs() {
              return fs;
       }

       public void setFs(FSNode fs) {
              this.fs = fs;
       }

       public TransportNode getTransport() {
              return transport;
       }

       public void setTransport(TransportNode transport) {
              this.transport = transport;
       }

       public HttpNode getHttp() {
              return http;
       }

       public void setHttp(HttpNode http) {
              this.http = http;
       }

       public Breakers getBreakers() {
              return breakers;
       }

       public void setBreakers(Breakers breakers) {
              this.breakers = breakers;
       }

       public ScriptNode getScript() {
              return script;
       }

       public void setScript(ScriptNode script) {
              this.script = script;
       }


}
