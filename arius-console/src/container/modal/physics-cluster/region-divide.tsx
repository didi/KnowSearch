import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import RegionTransfer from "container/custom-form/region-transfer";
import { getRegionNode, divideRegionCheck, divideRegion, getRegionList } from "api/cluster-api";
import { Input, Button, Drawer, Radio, Tag, Tooltip } from "antd";
import { INodeDivide } from "typesPath/index-types";
import { DTable } from "component/dantd/dtable";
import { renderMoreText } from "container/custom-component";
import { regRegionName } from "../../../constants/reg";
import { XNotification } from "component/x-notification";
import { uuid } from "lib/utils";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const RegionDivide = (props: {
  dispatch: any;
  cb: Function;
  params: {
    cluster: string;
    nodeDivideList: INodeDivide[];
    record?: INodeDivide;
    id: number;
  };
}) => {
  const [transferList, setTransferList] = useState([]);
  const [selectKeys, setSelectKeys] = useState([]);
  const [tableList, setTableList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [name, setName] = useState("");
  const [error, setError] = useState(false);
  const [value, setValue] = useState(false);
  const [divideList, setDivideList] = useState([]);

  useEffect(() => {
    _getRegionNode();
  }, []);

  const _getRegionNode = async () => {
    let clusterId = props.params.id;
    setLoading(true);
    let res = await getRegionNode(clusterId);
    let tableData = await _getRegionList();
    let transferData = getTransferList(res);
    setTransferList(transferData);
    setTableList(tableData);
    setLoading(false);
  };

  const onChange = (e) => {
    setValue(e.target.value);
  };

  const _getRegionList = async () => {
    let cluster = props.params.cluster;
    let res = await getRegionList(cluster);
    let data = (res || []).map((item) => {
      return {
        ...item,
        regionName: item.name,
        nodeSet: item.nodeNames || "-",
      };
    });
    return data;
  };

  const getTransferList = (data) => {
    let list = data.filter((item) => item.regionId === -1);
    let res = list.map((item) => {
      return {
        ...item,
        key: item.id,
      };
    });
    return res;
  };

  const addRegion = async () => {
    if (error) {
      XNotification({ type: "error", message: "请输入正确输入Region名称" });
      return;
    }
    if (!name) {
      XNotification({ type: "error", message: "请输入Region名称" });
      return;
    }
    if (!selectKeys.length) {
      XNotification({ type: "error", message: "请选择加入Region的节点" });
      return;
    }
    let config = { cold: value };
    let params = [
      {
        bindingNodeIds: selectKeys,
        logicClusterIds: props.params.id,
        name,
        phyClusterName: props.params.cluster,
        config: JSON.stringify(config),
      },
    ] as any;
    if (!value) {
      delete params[0].config;
    }
    // 校验region名称是否重复
    await divideRegionCheck(params);
    setName("");
    setDivideList([...divideList, ...params]);
    // transfer 数据中过滤掉已选中的节点
    let transferData = transferList?.filter((item) => !selectKeys.includes(item?.id));
    setTransferList(transferData);
    // table 展示添加的节点
    let selectList = transferList?.filter((item) => selectKeys.includes(item?.id));
    let nodeSet = selectList.map((item) => item.nodeSet).join(",");
    setTableList([...tableList, { regionName: name, nodeSet, config: JSON.stringify(config), id: uuid() }]);
    setSelectKeys([]);
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
        title: "节点名称",
        dataIndex: "nodeSet",
        key: "nodeSet",
      },
    ];
  };

  const xFormModalConfig = {
    visible: true,
    width: 1080,
    title: "Region划分",
    className: "regin-contain",
    onClose: () => {
      props.dispatch(actions.setModalId(""));
    },
    footer: (
      <div>
        <Button
          type="primary"
          style={{ marginRight: 10 }}
          onClick={async () => {
            if (divideList.length) {
              await divideRegion(divideList);
              XNotification({ type: "success", message: "添加成功" });
            }
            props.dispatch(actions.setModalId(""));
          }}
        >
          确定
        </Button>
        <Button onClick={() => props.dispatch(actions.setModalId(""))}>取消</Button>
      </div>
    ),
  };

  const renderDefineRegion = () => {
    return (
      <div>
        <div className="region-title defined">Region定义</div>
        <div className="define-region">
          <div className="region-input">
            <div className="region-name-title">Region名称</div>
            <Input
              allowClear
              value={name}
              className={`region-name-input ${error && "region-name-input-error"}`}
              placeholder="请输入Region名称"
              onChange={(e) => {
                let value = e.target.value;
                if (regRegionName.test(value) && value.length <= 32) {
                  setError(false);
                } else {
                  setError(true);
                }
                setName(value);
              }}
            />
            <Button className="region-name-add" type="primary" onClick={addRegion}>
              添加
            </Button>
            {error && <div className="region-name-error">支持文字，字母，数字，下划线，中划线，32字以内</div>}
          </div>
          <div className="cold-region">
            <div className="cold-region-title">是否定义为cold属性Region？</div>
            <Radio.Group className="cold-group" onChange={onChange} value={value}>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </Radio.Group>
          </div>
        </div>
      </div>
    );
  };

  return (
    <>
      <Drawer {...xFormModalConfig}>
        <div className="node-list">
          <div className="region-title">节点列表</div>
          <RegionTransfer
            dataSource={transferList}
            selectKeys={(keys) => {
              setSelectKeys(keys);
            }}
            style={{ marginTop: -6 }}
          />
        </div>
        {renderDefineRegion()}
        <div className="region-title nodelist">Region列表</div>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={tableList}
          columns={getColumns()}
          attrs={{ size: "small", style: { marginTop: -15 } }}
        />
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps)(RegionDivide);
