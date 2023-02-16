import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import RegionTransfer from "container/custom-form/region-transfer";
import { getRegionNode, getRegionList, editRegion, deleteRegion } from "api/cluster-api";
import { Button, Drawer, Tag, Tooltip } from "antd";
import { INodeDivide } from "typesPath/index-types";
import { DTable } from "component/dantd/dtable";
import { renderOperationBtns, renderMoreText } from "container/custom-component";
import { XNotification } from "component/x-notification";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const RegionAdmin = (props: {
  dispatch: any;
  cb: Function;
  params: {
    cluster: string;
    nodeDivideList: INodeDivide[];
    record?: INodeDivide;
    id: number;
  };
}) => {
  const [nodeList, setNodeList] = useState([]);
  const [transferList, setTransferList] = useState([]);
  const [selectKeys, setSelectKeys] = useState([]);
  const [tableList, setTableList] = useState([]);
  const [currentRegion, setCurrentRegion] = useState({} as any);
  const [loading, setLoading] = useState(false);
  const [transferVisible, setTransferVisible] = useState(false);
  const [editList, setEditList] = useState([]);

  useEffect(() => {
    _getRegionNode();
  }, []);

  const _getRegionNode = async () => {
    let clusterId = props.params.id;
    setLoading(true);
    let res = await getRegionNode(clusterId);
    let tableData = await getTableList(res);
    let nodeList = (res || []).map((item) => {
      return {
        ...item,
        key: item.id,
        selected: item.regionName ? true : false,
      };
    });
    setNodeList(nodeList);
    setTableList(tableData);
    setLoading(false);
    return nodeList;
  };

  const getTransferList = (data, keys) => {
    let list = data.filter((item) => !item.selected || keys?.includes(item.id));
    let res = list.map((item) => {
      return {
        ...item,
      };
    });
    return res;
  };

  const getTableList = async (data) => {
    let cluster = props.params.cluster;
    // 获取region列表
    let res = await getRegionList(cluster);
    // 过滤掉未绑定的节点
    let list = (data || []).filter((item) => item.regionId !== -1);
    let tableList = [];
    // 通过name判断data中是否有对应的数据
    const hasNode = (data: any, name: string, key: string) => {
      let index = -1;
      for (let i = 0; i < data?.length; i++) {
        if (data[i]?.[key] === name) {
          index = i;
          break;
        }
      }
      return index;
    };
    list.forEach((item) => {
      let name = item.regionName;
      let index = hasNode(tableList, name, "regionName");
      let regionIndex = hasNode(res, name, "name");
      // 将region列表数据和节点数据进行合并
      if (index !== -1) {
        tableList[index].nodeSet = tableList[index].nodeSet + ", " + item.nodeSet;
        tableList[index].key.push(item.id);
      } else if (regionIndex !== -1) {
        let data = {
          ...item,
          key: [item.id],
          ...res[regionIndex],
        };
        tableList.push(data);
      } else {
        let data = {
          ...item,
          key: [item.id],
        };
        tableList.push(data);
      }
    });
    // 若接口返回列表长度和拼接region后的列表长度不一致，说明有region为空
    if (res?.length !== tableList?.length) {
      let nameList = (tableList || []).map((item) => item.name);
      let emptyNodeList = (res || []).filter((item) => {
        return !nameList.includes(item.name);
      });
      emptyNodeList = emptyNodeList.map((item) => {
        return {
          ...item,
          regionName: item.name,
          regionId: item.id,
        };
      });
      tableList = [...tableList, ...emptyNodeList];
    }
    return tableList;
  };

  const _editRegion = async () => {
    // 未进行操作，点击确定直接关闭弹窗
    if (!editList.length && !currentRegion.regionId) {
      props.dispatch(actions.setModalId(""));
      return;
    }
    if (!selectKeys.length) {
      XNotification({ type: "error", message: "region不能为空" });
      return;
    }
    let params = editList;
    let unBindingNodeIds = [];
    (currentRegion?.key || []).forEach((item) => {
      if (!selectKeys.includes(item)) {
        unBindingNodeIds.push(item);
      }
    });
    let lastRegion = {
      bindingNodeIds: selectKeys,
      id: currentRegion.regionId,
      logicClusterIds: props.params.id,
      name: currentRegion.regionName,
      phyClusterName: props.params.cluster,
      unBindingNodeIds,
    };
    params.push(lastRegion);
    await editRegion(params);
    XNotification({ type: "success", message: "编辑成功" });
    props.dispatch(actions.setModalId(""));
  };

  const clickEdit = (record, list?: any) => {
    setCurrentRegion(record);
    if (transferVisible) {
      let unBindingNodeIds = [];
      (currentRegion?.key || []).forEach((item) => {
        if (!selectKeys.includes(item)) {
          unBindingNodeIds.push(item);
        }
      });
      let region = {
        bindingNodeIds: selectKeys,
        id: currentRegion.regionId,
        logicClusterIds: props.params.id,
        name: currentRegion.regionName,
        phyClusterName: props.params.cluster,
        unBindingNodeIds,
      };
      setEditList([...editList, region]);
    } else {
      setTransferVisible(true);
    }
    let transferData = getTransferList(list || nodeList, record.key);
    setTransferList(transferData);
    setSelectKeys(record.key);
    setCurrentRegion(record);
  };

  const getColumns = () => {
    return [
      {
        title: "Region名称",
        dataIndex: "regionName",
        key: "regionName",
        width: 264,
        render: (val: string, record: any) => {
          let cold = false;
          if (record?.config) {
            let config = JSON.parse(record?.config);
            cold = config?.cold;
          }
          return cold ? (
            <div className="cold-region-tag">
              {renderMoreText(val, 36)}
              <Tooltip title="该Region只用于冷热分离，不可用于划分逻辑集群" overlayStyle={{ maxWidth: 1080 }}>
                <Tag color="blue" className="tag-bule">
                  cold
                </Tag>
              </Tooltip>
            </div>
          ) : (
            renderMoreText(val, 36)
          );
        },
      },
      {
        title: "实例名称",
        dataIndex: "nodeSet",
        key: "nodeSet",
      },
      {
        title: "操作",
        dataIndex: "actions",
        key: "actions",
        render: (val, record) => {
          let btn = [
            {
              label: "编辑",
              type: "primary",
              clickFunc: (record) => {
                clickEdit(record);
              },
            },
            {
              label: "删除",
              type: "primary",
              needConfirm: true,
              confirmText: `删除`,
              clickFunc: async (record) => {
                await deleteRegion(record.regionId);
                XNotification({ type: "success", message: "删除成功" });
                let list = await _getRegionNode();
                if (currentRegion?.regionId === record.regionId) {
                  setCurrentRegion({});
                  setTransferVisible(false);
                  setEditList([]);
                } else {
                  transferVisible && clickEdit(currentRegion, list);
                }
              },
            },
          ];
          return renderOperationBtns(btn, record);
        },
      },
    ];
  };

  const renderTransfer = () => {
    return (
      <>
        <div className="region-title">节点列表</div>
        <RegionTransfer
          dataSource={transferList}
          selectKeys={(keys) => {
            setSelectKeys(keys);
            let list = nodeList.map((item) => {
              return {
                ...item,
                selected: keys.includes(item.key) || (item?.id !== currentRegion.id && item.selected),
              };
            });
            setNodeList(list);
          }}
          targetKeys={selectKeys}
        />
      </>
    );
  };

  const xFormModalConfig = {
    width: 1080,
    title: "Region管理",
    type: "drawer",
    onClose: () => {
      props.dispatch(actions.setModalId(""));
    },
    footer: (
      <div>
        <Button type="primary" style={{ marginRight: 10 }} onClick={_editRegion}>
          确定
        </Button>
        <Button onClick={() => props.dispatch(actions.setModalId(""))}>取消</Button>
      </div>
    ),
  };

  return (
    <>
      <Drawer visible={true} {...xFormModalConfig}>
        <div>
          <div className="region-title">Region列表</div>
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={tableList}
            columns={getColumns()}
            attrs={{ size: "small", style: { marginTop: -15 } }}
          />
        </div>
        {transferVisible && renderTransfer()}
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps)(RegionAdmin);
