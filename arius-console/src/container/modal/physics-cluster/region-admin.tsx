import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import RegionTransfer from "container/custom-form/region-transfer";
import { getRegionNode, getRegionList, getRackRegionList, editRegion, deleteRegion } from "api/cluster-api";
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
    logicClusterAndRegionList?: any;
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
  const [divideType, setDivideType] = useState("");

  useEffect(() => {
    getDivideType();
  }, []);

  useEffect(() => {
    _getRegionNode();
  }, [divideType]);

  const getDivideType = async () => {
    let cluster = props.params.cluster;
    if (!props.params.logicClusterAndRegionList) {
      let res = await getRegionList(cluster);
      let divideAttributeKey = res?.[0].divideAttributeKey;
      setDivideType(divideAttributeKey || "");
    }
  };

  const _getRegionNode = async () => {
    let clusterId = props.params.id;
    setLoading(true);
    let res = await getRegionNode(clusterId, divideType);
    let tableData = await getTableList(res, divideType);
    let nodeList = (res || []).map((item) => {
      return {
        ...item,
        key: item.id,
        selected: item.regionName ? true : false,
        ids: [item.id],
      };
    });
    setNodeList(nodeList);
    setTableList(tableData);
    setLoading(false);
    return nodeList;
  };

  const getTransferList = (data, ids) => {
    let list = data.filter((item) => !item.selected || ids?.includes(item.id));
    // rack 划分需要过滤掉 attributeValue 为 null 的节点
    if (divideType) {
      list = list.filter((item) => item.attributeValue);
      // rack 划分方式需要拼接 attributeValue 相同的 ip、nodeSet
      if (list.length) {
        let rackList = [list[0]];
        let attributeValueList = [list[0].attributeValue];
        (list || []).forEach((item) => {
          for (let i = 0; i < rackList.length; i++) {
            let rack = rackList[i];
            if (item.attributeValue === rack.attributeValue && rack.ip !== item.ip && rack.nodeSet !== item.nodeSet) {
              rack.ip = rack.ip + "，" + item.ip;
              rack.nodeSet = rack.nodeSet + "，" + item.nodeSet;
              rack.ids = [...rack.ids, item.id];
            } else {
              !attributeValueList.includes(item.attributeValue) && rackList.push(item);
              attributeValueList.push(item.attributeValue);
            }
          }
        });
        return rackList;
      }
    }
    return list;
  };

  const getHostData = (list, res) => {
    let tableList = [];
    // 通过 name 判断 data 中是否有对应的数据
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
        tableList[index].selectKey.push(item.id);
      } else if (regionIndex !== -1) {
        let data = {
          ...item,
          selectKey: [item.id],
          ...res[regionIndex],
        };
        tableList.push(data);
      } else {
        let data = {
          ...item,
          selectKey: [item.id],
        };
        tableList.push(data);
      }
    });
    return tableList;
  };

  const getRackData = (list, res) => {
    let tableList = (res || []).map((item) => ({ ...item, key: [item.id], regionId: item.id, selectKey: [] }));
    const hasAttribute = (data: any, value: string) => {
      let index = -1;
      for (let i = 0; i < data?.length; i++) {
        if (data[i]?.attributeValues?.includes(value)) {
          index = i;
          break;
        }
      }
      return index;
    };
    list.forEach((item) => {
      let attributeValue = item.attributeValue;
      let index = hasAttribute(tableList, attributeValue);
      // 将region列表数据和节点数据进行合并
      if (index !== -1) {
        tableList[index].selectKey.push(item.id);
      } else {
        let data = {
          ...item,
          key: [item.id],
        };
        tableList.push(data);
      }
    });
    return tableList;
  };

  const getTableList = async (data, type: string) => {
    let cluster = props.params.cluster;
    // 获取region列表
    let res = type ? await getRackRegionList(cluster, type) : await getRegionList(cluster);
    // 过滤掉未绑定的节点
    let list = (data || []).filter((item) => item.regionId !== -1);
    // rack 划分需要过滤掉 attributeValue 为 null 的节点
    if (type) {
      list = list.filter((item) => item.attributeValue);
    }
    let tableList = type ? getRackData(list, res) : getHostData(list, res);
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
    let params: any = [];
    let unBindingNodeIds = [];
    let transferSelectIds = transferList.map((item) => item.id);
    let orginSelectKeys = currentRegion?.selectKey.filter((item) => transferSelectIds?.includes(item));
    (orginSelectKeys || []).forEach((item) => {
      if (!selectKeys.includes(item)) {
        unBindingNodeIds.push(item);
      }
    });
    let lastRegion: any = {
      bindingNodeIds: selectKeys,
      id: currentRegion.regionId,
      logicClusterIds: props.params.id,
      name: currentRegion.regionName,
      phyClusterName: props.params.cluster,
      unBindingNodeIds,
    };
    // 根据 rack 划分，需要新增 divideAttributeKey 入参
    if (divideType) {
      lastRegion.divideAttributeKey = divideType;
      let bindingNodes = lastRegion.bindingNodeIds;
      transferList.forEach((item) => {
        let ids = item.ids.slice(1);
        if (selectKeys.includes(item.id)) {
          bindingNodes.push(...ids);
        }
        if (unBindingNodeIds.includes(item.id) && item.id.length !== item.ids.length) {
          unBindingNodeIds.push(...ids);
        }
      });
      lastRegion.bindingNodeIds = bindingNodes;
      lastRegion.unBindingNodeIds = unBindingNodeIds;
    }
    params.push(lastRegion);
    await editRegion(params);
    XNotification({ type: "success", message: "编辑成功" });
    props.dispatch(actions.setModalId(""));
  };

  const clickEdit = (record, list?: any) => {
    if (transferVisible) {
      let unBindingNodeIds = [];
      (record?.key || []).forEach((item) => {
        if (!selectKeys.includes(item)) {
          unBindingNodeIds.push(item);
        }
      });
      let region = {
        bindingNodeIds: selectKeys,
        id: record.regionId,
        logicClusterIds: props.params.id,
        name: record.regionName,
        phyClusterName: props.params.cluster,
        unBindingNodeIds,
      };
      setEditList([...editList, region]);
    } else {
      setTransferVisible(true);
    }
    let transferData = getTransferList(list || nodeList, record?.selectKey);
    let transferSelectIds = transferData.map((item) => item.id);
    let selectKey = record?.selectKey.filter((item) => transferSelectIds?.includes(item));
    setTransferList(transferData);
    setSelectKeys(selectKey);
    setCurrentRegion(record);
  };

  const getColumns = () => {
    let columns = [
      {
        title: "Region名称",
        dataIndex: `${divideType ? "name" : "regionName"}`,
        key: `${divideType ? "name" : "regionName"}`,
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
        title: "节点名称",
        dataIndex: `${divideType ? "nodeNames" : "nodeSet"}`,
        key: `${divideType ? "nodeNames" : "nodeSet"}`,
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
    if (divideType) {
      let attribute = {
        title: "attribute",
        dataIndex: `${divideType ? "attributeValues" : "attributes"}`,
        key: `${divideType ? "attributeValues" : "attributes"}`,
      };
      columns.splice(1, 0, attribute);
    }
    return columns;
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
          attribute={divideType}
        />
      </>
    );
  };

  const xFormModalConfig = {
    width: 1080,
    title: "Region管理",
    type: "drawer",
    className: "region-container",
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
        <div className="warning-container">
          <span className="icon iconfont iconbiaogejieshi"></span>
          <span>Region中节点规格需要保持严格一致，请谨慎操作。</span>
        </div>
        <div className="region-title">Region列表</div>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={tableList}
          columns={getColumns()}
          attrs={{ size: "small", style: { marginTop: -15 }, pagination: false }}
        />
        {transferVisible && renderTransfer()}
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps)(RegionAdmin);
