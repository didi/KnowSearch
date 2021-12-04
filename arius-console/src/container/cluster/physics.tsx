import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhysicsColumns, getPhyClusterQueryXForm } from "./config";
import {
  getNodeList,
  getOpPhysicsClusterList,
  IClusterList,
  getPackageList,
} from "api/cluster-api";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { INodeListObjet } from "container/modal/physics-cluster/apply-cluster";
import { IVersions } from "@types/cluster/physics-type";
import { INode } from "@types/cluster/cluster-types";
import { notification } from "antd";
import { RenderTitle } from "component/render-title";
import { queryFormText } from "constants/status-map";
import QueryForm from "component/dantd/query-form";
import { isOpenUp } from "constants/common";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setDrawerId(modalId, params)),
});

const PhysicsClusterBox = (props) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject]:any = useState({
    from: 0,
    size: 10,
  });
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);

  React.useEffect(() => {
    reloadData();
  }, [department, queryFromObject]);

  // const getData = () => {
  //   // 查询项的key 要与 数据源的key  对应
  //   if (!queryFromObject) return data;
  //   const keys = Object.keys(queryFromObject);
  //   const filterData = data.filter((d) => {
  //     let b = true;
  //     keys.forEach((k: string) => {
  //       if (k === "currentAppAuth") {
  //         d[k] === queryFromObject[k] ? "" : (b = false);
  //       } else {
  //         (d[k] + "")?.toLowerCase().includes(queryFromObject[k])
  //           ? ""
  //           : (b = false);
  //       }
  //     });
  //     return b;
  //   });
    // return filterData.map((item) => ({
    //   ...item,
    //   diskInfo: {
    //     diskTotal: item.diskTotal,
    //     diskUsage: item.diskUsage,
    //     diskUsagePercent: item.diskUsagePercent,
    //   },
    // }));
  // };

  const reloadData = () => {
    const app = JSON.parse(localStorage.getItem("current-project"));
    if (!app?.name) {
      return;
    }
    setloading(true);
    const Params: IClusterList = {
      from: queryFromObject.from,
      size: queryFromObject.size,
      authType: queryFromObject.currentAppAuth,
      health: queryFromObject.health,
      cluster: queryFromObject.cluster,
      esVersion: queryFromObject.esVersion,
    }
    getOpPhysicsClusterList(Params)
      .then((res) => {
        if (res) {
          setData(res?.bizData?.map((item) => ({
            ...item,
            diskInfo: {
              diskTotal: item.diskTotal,
              diskUsage: item.diskUsage,
              diskUsagePercent: item.diskUsagePercent,
            },
          })));
          setTotal(res?.pagination?.total)
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const renderTitleContent = () => {
    return {
      title: "物理集群",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFromObject({...result, from: 0, size: 10});
  };

  const getModalData = () => {
    props.setModalId("applyPhyCluster", { loading: true }, reloadData);
    const nodeList = {} as INodeListObjet;
    let packageDockerList = [] as IVersions[];
    let packageHostList = [] as IVersions[];
    const setNodeList = (data: INode[]) => {
      const list = data.map((item: INode, index: number) => {
        return {
          ...item,
          key: index,
          value: item.spec,
        };
      });
      nodeList.masternode = list.filter((ele) => ele.role === "masternode");
      nodeList.clientnode = list.filter((ele) => ele.role === "clientnode");
      nodeList.datanode = list.filter((ele) => ele.role === "datanode");
      nodeList.datanodeceph = list.filter(
        (ele) => ele.role === "datanode-ceph"
      );
    };

    const setPackageList = (data: IVersions[]) => {
      const list = data.filter((data, indx, self) => {
        return (
          self.findIndex((ele) => ele.esVersion === data.esVersion) === indx
        );
      });
      const packageList = list.map((ele, index) => {
        return {
          ...ele,
          key: index,
          value: ele.esVersion,
        };
      });
      packageDockerList = packageList.filter((ele) => ele.manifest === 3);
      packageHostList = packageList.filter((ele) => ele.manifest === 4);
    };

    Promise.all([
      getNodeList().then(setNodeList),
      getPackageList().then(setPackageList),
    ])
      .then((values) => {
        props.setModalId(
          "applyPhyCluster",
          { loading: false, nodeList, packageDockerList, packageHostList },
          reloadData
        );
      })
      .catch((err) => {
        notification.error({ message: `网络错误！` });
        props.setModalId("");
      });
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "接入集群",
        className: "ant-btn-primary",
        clickFunc: () => props.setModalId("accessCluster", {}, reloadData),
      },
      {
        className: "ant-btn-primary",
        label: "新建集群",
        isOpenUp: isOpenUp,
        clickFunc: () => getModalData(),
      },
    ];
  };
  const handleChange = (pagination) => {
    setqueryFromObject((state) => ({
      ...state,
      from: (pagination.current - 1) * pagination.pageSize,
      size: pagination.pageSize,
    }));
  }

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm
          {...queryFormText}
          defaultCollapse
          columns={getPhyClusterQueryXForm(data)}
          onChange={() => null}
          onReset={handleSubmit}
          onSearch={handleSubmit}
          initialValues={{}}
          isResetClearAll
        />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={data}
            attrs={{
              onChange: handleChange
            }}
            key={JSON.stringify({
              authType: queryFromObject.authType,
              health: queryFromObject.health,
              cluster: queryFromObject.cluster,
              esVersion: queryFromObject.esVersion,
            })}
            columns={getPhysicsColumns(
              props.setModalId,
              props.setDrawerId,
              reloadData
            )}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
            paginationProps={{
              position: 'bottomRight',
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
              showTotal: (total) => `共 ${total} 条`,
            }}
          />
        </div>
      </div>
    </>
  );
};
export const PhysicsCluster = connect(
  null,
  mapDispatchToProps
)(PhysicsClusterBox);
