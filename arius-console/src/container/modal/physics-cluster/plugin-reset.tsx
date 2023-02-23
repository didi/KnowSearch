import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { message } from "antd";
import { DTable } from "component/dantd/dtable";
import { getPluginStatus, opResetPlug } from "api/plug-api";
import { useResize } from "lib/utils";
import Url from "lib/url-parser";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const ResetPlugin = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const {
    params: { type, record },
    dispatch,
    cb,
  } = props;
  const [loading, setLoading] = useState(false);
  const [detailList, setDetailList] = useState([]);
  const [searchKey, setSearchKey] = useState("");
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRow, setSelectedRow] = useState([]);

  const clientSize = useResize("ant-drawer-body");

  useEffect(() => {
    getData();
  }, [record]);

  const getData = () => {
    setLoading(true);
    getPluginStatus(record.id)
      .then((res) => {
        let list = [];
        (res || []).forEach((item) => {
          list.push(...item?.componentHosts);
        });
        setDetailList(list);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const columns = [
    {
      title: "节点",
      dataIndex: "host",
      key: "host",
      render: (item, record) => <div className={record.status === 0 ? "offline-text" : ""}>{item}</div>,
    },
    {
      title: "运行状态",
      dataIndex: "status",
      key: "status",
      render: (item, record) => (
        <div className="node-status-box">
          <div className={record.status === 1 ? "spot online" : "spot offline"} />
          {item === 1 ? "在线" : "离线"}
        </div>
      ),
    },
  ];

  const filterData = (origin?: any[]) => {
    const searchKeys = (searchKey + "").trim().toLowerCase();
    const data = searchKeys ? origin.filter((d) => d.host?.toLowerCase().includes((searchKeys + "") as string)) : origin;
    return data;
  };

  const handleSearch = (value) => {
    setSearchKey(value);
  };

  const onSelectChange = (newSelectedRowKeys: any[], rows) => {
    setSelectedRowKeys(newSelectedRowKeys);
    setSelectedRow(rows);
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "instances",
        label: "",
        type: FormItemType.custom,
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        customFormItem: (
          <div className={type !== "reset" && "mt-24"}>
            {type === "reset" && (
              <div className="warning-container" style={{ margin: "0 -24px 16px" }}>
                <span className="icon iconfont iconbiaogejieshi"></span>
                <span>{`确定重启${record?.name}吗？请选择重启实例对象`}</span>
              </div>
            )}
            <DTable
              loading={loading}
              rowKey="host"
              dataSource={filterData(detailList)}
              columns={columns}
              tableHeaderSearchInput={{ submit: handleSearch }}
              attrs={{
                rowSelection:
                  type === "reset"
                    ? {
                        selectedRowKeys,
                        onChange: onSelectChange,
                      }
                    : null,
                pagination: false,
                scroll: { y: clientSize.height - 220 },
              }}
            />
          </div>
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: `${type === "reset" ? "插件重启" : "插件状态"}`,
    needBtnLoading: true,
    nofooter: type !== "reset",
    width: 600,
    onCancel: () => {
      dispatch(actions.setDrawerId(""));
    },
    onSubmit: (result: any) => {
      if (!selectedRowKeys.length) {
        message.error("请选择需要重启的实例对象");
        return new Promise((resolve) => {
          resolve("");
        });
      }
      let groupConfigList = [];
      selectedRow.forEach((item) => {
        let includes = false;
        groupConfigList.forEach((ele) => {
          if (ele.groupName === item.groupName) {
            ele.hosts = ele.hosts + "," + item.host;
            includes = true;
          }
        });
        if (!includes) {
          groupConfigList.push({ groupName: item?.groupName, hosts: item?.host });
        }
      });
      const data = {
        dependComponentId: +Url().search.componentId,
        componentId: record.componentId,
        groupConfigList,
      };
      let expandData = { expandData: JSON.stringify(data) };
      return opResetPlug(expandData).then((res) => {
        showSubmitTaskSuccessModal(res, props.params?.history);
        dispatch(actions.setDrawerId(""));
        cb && cb();
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
