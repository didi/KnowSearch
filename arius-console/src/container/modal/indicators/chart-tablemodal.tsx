import React, { memo, useEffect, useState } from "react";
import { Table, Modal, Spin, Tooltip, TimePicker, message, Input } from "antd";
import "./chart-modal.less";
import * as actions from "../../../actions";
import { useSelector, useDispatch, shallowEqual } from "react-redux";
import { getChartTableList } from "api/cluster-kanban";
import moment from "moment";
import { transTimeFormat } from "lib/utils";

export const ChartTableModal = memo((props) => {
  const dispatch = useDispatch();
  const { params, cb } = useSelector(
    (state) => ({
      params: (state as any).modal.params,
      cb: (state as any).modal.cb,
    }),
    shallowEqual
  );

  const { clusterPhyName, node, time } = params;
  const [data, setData] = useState<any>([]);
  const [listData, setListData] = useState<any>([]);
  const [dataLoading, setDataLoading] = useState(false);
  const columns = [
    {
      title: "操作类型",
      dataIndex: "action",
      width: 200,
      render: (text) => {
        return (
          <div style={{ overflow: "hidden", textOverflow: "ellipsis" }}>
            <Tooltip title={text}>{text || "-"}</Tooltip>
          </div>
        );
      },
    },
    {
      title: "开始时间",
      dataIndex: "startTime",
      render: (text) => {
        return transTimeFormat(text);
      },
    },
    {
      title: "运行时间",
      dataIndex: "runningTimeString",
      render: (text) => {
        return text || "-";
      },
    },
    {
      title: "描述",
      dataIndex: "description",
      width: 200,
      render: (text) => {
        return (
          <div style={{ overflow: "hidden", textOverflow: "ellipsis" }}>
            <Tooltip title={text}>{text || "-"}</Tooltip>
          </div>
        );
      },
    },
  ];

  const onInputIndexChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const searchKey = e.target.value.trim();
    setListData(searchKey ? data.filter((row) => row.action.includes(searchKey)) : data);
  };

  const getData = async (times?: any) => {
    if (times && times[1] - times[0] > 60 * 1000 * 60) {
      message.info("时间范围仅支持一小时");
      return;
    }
    setDataLoading(true);
    let t = times ? times : [time, time];
    try {
      const res = await getChartTableList(clusterPhyName, node, t);
      if (res && res.length) {
        setData(res);
        setListData(res);
      }
    } catch (error) {
      console.error(error);
      setData([]);
      setListData([]);
    } finally {
      setDataLoading(false);
    }
  };

  useEffect(() => {
    getData();
  }, []);

  return (
    <Modal
      visible={true}
      maskClosable={false}
      width={750}
      onOk={() => {
        dispatch(actions.setModalId(""));
      }}
      onCancel={() => {
        dispatch(actions.setModalId(""));
      }}
      title="查询Task详情"
      footer={null}
      bodyStyle={{
        display: "block",
        padding: "20px",
      }}
    >
      <Spin spinning={dataLoading}>
        <div style={{ overflow: "hidden" }}>
          <TimePicker.RangePicker
            format="HH:mm:ss"
            style={{ float: "right", marginBottom: 10, marginLeft: 20 }}
            onChange={(e) => getData([moment(e[0]).valueOf(), moment(e[1]).valueOf()])}
            defaultValue={[moment(Number(time)), moment(Number(time))]}
          />
          <Input
            allowClear
            onChange={onInputIndexChange}
            placeholder="请输入操作类型关键字"
            style={{ float: "right", marginBottom: 10, width: 200 }}
          />
        </div>
        <Table pagination={false} dataSource={listData} rowKey="taskId" columns={columns} style={{ width: "100%" }} scroll={{ y: 500 }} />
      </Spin>
    </Modal>
  );
});
