import { toBytesFun, transTimeFormat, formatDecimalPoint } from "lib/utils";
import { bytesUnitFormatter } from "lib/utils";

export const node_state = () => {
  const columns = [
    {
      title: "node_name",
      dataIndex: "nodeName",
      key: "nodeName",
      width: 120,
      sorter: (a, b) => {
        const aa = a.nodeName || "0";
        const bb = b.nodeName || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "segments_memory",
      dataIndex: "segmentsMemory",
      key: "segmentsMemory",
      width: 170,
      sorter: (a, b) => {
        const aa = a.segmentsMemory || -1;
        const bb = b.segmentsMemory || -1;
        return aa - bb;
      },
    },
    {
      title: "os_cpu",
      dataIndex: "osCpu",
      key: "osCpu",
      width: 100,
      sorter: (a, b) => {
        const aa = a.osCpu || -1;
        const bb = b.osCpu || -1;
        return aa - bb;
      },
    },
    {
      title: "os.load_average",
      dataIndex: "loadAverage5m",
      key: "loadAverage5m",
      sorter: (a, b) => {
        const aa = a.loadAverage5m || -1;
        const bb = b.loadAverage5m || -1;
        return aa - bb;
      },
      width: 150,
    },
    {
      title: "jvm_heap_used_percent",
      dataIndex: "jvmHeapUsedPercent",
      width: 200,
      key: "jvmHeapUsedPercent",
      sorter: (a, b) => {
        const aa = a.jvmHeapUsedPercent || -1;
        const bb = b.jvmHeapUsedPercent || -1;
        return aa - bb;
      },
      render: (text: number) => (text + "" && text + "%") || "-",
    },
    {
      title: "thread.count",
      dataIndex: "threadsCount",
      width: 140,
      key: "threadsCount",
      sorter: (a, b) => {
        const aa = a.threadsCount || -1;
        const bb = b.threadsCount || -1;
        return aa - bb;
      },
    },
    {
      title: "http_current_open",
      dataIndex: "currentOpen",
      key: "currentOpen",
      width: 160,
      sorter: (a, b) => {
        const aa = a.currentOpen || -1;
        const bb = b.currentOpen || -1;
        return aa - bb;
      },
    },
    {
      title: "thread_pool_write_active",
      dataIndex: "threadPoolWriteActive",
      width: 200,
      sorter: (a, b) => {
        const aa = a.threadPoolWriteActive || -1;
        const bb = b.threadPoolWriteActive || -1;
        return aa - bb;
      },
      key: "threadPoolWriteActive",
    },
    {
      title: "thread_pool_write_queue",
      dataIndex: "threadPoolWriteQueue",
      key: "threadPoolWriteQueue",
      width: 200,
      sorter: (a, b) => {
        const aa = a.threadPoolWriteQueue || -1;
        const bb = b.threadPoolWriteQueue || -1;
        return aa - bb;
      },
    },
    {
      title: "thread_pool_write_reject",
      dataIndex: "threadPoolWriteReject",
      key: "threadPoolWriteReject",
      sorter: (a, b) => {
        const aa = a.threadPoolWriteReject || -1;
        const bb = b.threadPoolWriteReject || -1;
        return aa - bb;
      },
      width: 200,
    },
    {
      title: "thread_pool_search_active",
      dataIndex: "threadPoolSearchActive",
      key: "threadPoolSearchActive",
      width: 210,
      sorter: (a, b) => {
        const aa = a.threadPoolSearchActive || -1;
        const bb = b.threadPoolSearchActive || -1;
        return aa - bb;
      },
    },
    {
      title: "thread_pool_search_queue",
      dataIndex: "threadPoolSearchQueue",
      key: "threadPoolSearchQueue",
      sorter: (a, b) => {
        const aa = a.threadPoolSearchQueue || -1;
        const bb = b.threadPoolSearchQueue || -1;
        return aa - bb;
      },
      width: 210,
    },
    {
      title: "thread_pool_search_reject",
      dataIndex: "threadPoolSearchReject",
      key: "threadPoolSearchReject",
      sorter: (a, b) => {
        const aa = a.threadPoolSearchReject || -1;
        const bb = b.threadPoolSearchReject || -1;
        return aa - bb;
      },
      width: 210,
    },
    {
      title: "thread_pool_management_active",
      dataIndex: "threadPoolManagementActive",
      key: "threadPoolManagementActive",
      sorter: (a, b) => {
        const aa = a.threadPoolManagementActive || -1;
        const bb = b.threadPoolManagementActive || -1;
        return aa - bb;
      },
      width: 250,
    },
    {
      title: "thread_pool_management_queue",
      dataIndex: "threadPoolManagementQueue",
      key: "threadPoolManagementQueue",
      sorter: (a, b) => {
        const aa = a.threadPoolManagementQueue || -1;
        const bb = b.threadPoolManagementQueue || -1;
        return aa - bb;
      },
      width: 255,
    },
    {
      title: "thread_pool_management_reject",
      dataIndex: "threadPoolManagementReject",
      key: "threadPoolManagementReject",
      sorter: (a, b) => {
        const aa = a.threadPoolManagementReject || -1;
        const bb = b.threadPoolManagementReject || -1;
        return aa - bb;
      },
      width: 250,
    },
  ];
  return columns;
};

export const getPhysicsColumns = () => {
  const columns = [
    {
      title: "health",
      dataIndex: "health",
      key: "health",
      width: 100,
      sorter: (a, b) => {
        const aa = a.health || "0";
        const bb = b.health || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "status",
      dataIndex: "status",
      key: "status",
      width: 100,
      sorter: (a, b) => {
        const aa = a.status || "0";
        const bb = b.status || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "index",
      dataIndex: "index",
      width: 115,
      key: "index",
    },
    {
      title: "pri",
      dataIndex: "pri",
      key: "pri",
      width: 80,
      sorter: (a, b) => {
        const aa = a.pri || -1;
        const bb = b.pri || -1;
        return aa - bb;
      },
    },
    {
      title: "rep",
      dataIndex: "rep",
      key: "rep",
      width: 80,
      sorter: (a, b) => {
        const aa = a.rep || -1;
        const bb = b.rep || -1;
        return aa - bb;
      },
    },
    {
      title: "docs.Deleted",
      dataIndex: "docsDeleted",
      key: "docsDeleted",
      width: 120,
      sorter: (a, b) => {
        const aa = a.docsDeleted || -1;
        const bb = b.docsDeleted || -1;
        return aa - bb;
      },
    },
    {
      title: "store.size",
      dataIndex: "storeSize",
      key: "storeSize",
      width: 100,
      sorter: (a, b) => {
        const aa = a.storeSize || -1;
        const bb = b.storeSize || -1;
        return aa - bb;
      },
      render: (val: number) => bytesUnitFormatter(val),
    },
    {
      title: "pri.store.size",
      dataIndex: "priStoreSize",
      key: "priStoreSize",
      width: 120,
      sorter: (a, b) => {
        const aa = a.priStoreSize || -1;
        const bb = b.priStoreSize || -1;
        return aa - bb;
      },
      render: (val: number) => bytesUnitFormatter(val),
    },
  ];
  return columns;
};

export const shard_distribution = () => {
  const columns = [
    {
      title: "index",
      dataIndex: "index",
      key: "index",
      width: 200,
      sorter: (a, b) => {
        const aa = a.index || "0";
        const bb = b.index || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "shard",
      dataIndex: "shard",
      key: "shard",
      width: 100,
      sorter: (a, b) => {
        const aa = a.shard || -1;
        const bb = b.shard || -1;
        return aa - bb;
      },
    },
    {
      title: "p/r",
      dataIndex: "prirep",
      key: "prirep",
      width: 80,
      sorter: (a, b) => {
        const aa = a.prirep || "0";
        const bb = b.prirep || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "state",
      dataIndex: "state",
      key: "state",
      width: 108,
      sorter: (a, b) => {
        const aa = a.state || "0";
        const bb = b.state || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "docs",
      dataIndex: "docs",
      key: "docs",
      width: 113,
      sorter: (a, b) => {
        const aa = a.docs || -1;
        const bb = b.docs || -1;
        return aa - bb;
      },
    },
    {
      title: "store",
      dataIndex: "store",
      key: "store",
      width: 109,
      sorter: (a, b) => {
        const aa = a.store || -1;
        const bb = b.store || -1;
        return aa - bb;
      },
      render: (val: number) => bytesUnitFormatter(val),
    },
    {
      title: "ip",
      dataIndex: "ip",
      key: "ip",
      width: 120,
      sorter: (a, b) => {
        const aa = a.state || "0";
        const bb = b.state || "0";
        return aa.localeCompare(bb);
      },
    },
    {
      title: "node",
      dataIndex: "node",
      key: "node",
      width: 100,
      sorter: (a, b) => {
        const aa = a.node || "0";
        const bb = b.node || "0";
        return aa.localeCompare(bb);
      },
    },
  ];
  return columns;
};

export const pending_TaskAnalysis = () => {
  const columns = [
    {
      title: "insert_order",
      dataIndex: "insertOrder",
      key: "insertOrder",
      width: 200,
    },
    {
      title: "priority",
      dataIndex: "priority",
      key: "priority",
      width: 200,
    },
    {
      title: "source",
      dataIndex: "source",
      key: "source",
      width: 200,
    },
    {
      title: "time_in_queue_millis",
      dataIndex: "timeInQueueMillis",
      key: "timeInQueueMillis",
      width: 200,
      sorter: (a, b) => {
        return a.timeInQueueMillis - b.timeInQueueMillis;
      },
    },
    {
      title: "time_in_queue",
      dataIndex: "timeInQueue",
      key: "timeInQueue",
      width: 200,
      sorter: (a, b) => {
        return toBytesFun(a.timeInQueue) - toBytesFun(b.timeInQueue);
      },
      render: (text: string) => formatDecimalPoint(text),
    },
  ];
  return columns;
};

export const task_Analysis = () => {
  const columns = [
    {
      title: "nodeName",
      dataIndex: "node",
      key: "node",
      width: 210,
    },
    {
      title: "action",
      dataIndex: "action",
      key: "action",
      width: 190,
    },
    {
      title: "description",
      dataIndex: "description",
      key: "description",
      width: 150,
    },
    {
      title: "start_time_in_millis",
      dataIndex: "startTimeInMillis",
      key: "startTimeInMillis",
      width: 200,
      sorter: (a, b) => {
        const aa = a.startTimeInMillis || -1;
        const bb = b.startTimeInMillis || -1;
        return parseFloat(aa) - parseFloat(bb);
      },
      render: (text: number) => transTimeFormat(text),
    },
    {
      title: "running_time_in_nanos",
      dataIndex: "runningTimeInNanos",
      key: "runningTimeInNanos",
      sorter: (a, b) => {
        const aa = a.runningTimeInNanos || -1;
        const bb = b.runningTimeInNanos || -1;
        return parseFloat(aa) - parseFloat(bb);
      },
      render: (text: string) => formatDecimalPoint(text),
    },
  ];
  return columns;
};

export const shard_DistributionExplain = () => {
  const columns = [
    {
      title: "index",
      dataIndex: "index",
      key: "index",
      width: 122,
    },
    {
      title: "shard",
      dataIndex: "shard",
      key: "shard",
      width: 112,
    },
    {
      title: "primary",
      dataIndex: "primary",
      key: "primary",
      width: 120,
    },
    {
      title: "current_state",
      dataIndex: "currentState",
      key: "currentState",
      width: 148,
    },
    {
      title: "node_name",
      dataIndex: "nodeName",
      key: "nodeName",
      width: 170,
    },
    {
      title: "nodeDecide",
      dataIndex: "nodeDecide",
      key: "nodeDecide",
      width: 138,
    },
    {
      title: "explanation",
      dataIndex: "explanation",
      key: "explanation",
    },
  ];
  return columns;
};
