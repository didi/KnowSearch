import React, { useState, useEffect } from "react";
import { BASE_INFO } from "./config";
import { Descriptions, Spin, Divider } from "antd";
import { IBaseInfo } from "typesPath/base-types";
import { connect } from "react-redux";
import { getPackageTypeDescVersion } from "api/cluster-api";
import { getTaskType } from "api/task-api";

const mapStateToProps = (state) => ({
  task: state.task,
});

const BaseInfo = (props) => {
  const { task } = props;
  const [typeList, setTypeList] = useState([]);

  useEffect(() => {
    _getTaskType();
  }, [task]);

  const _getTaskType = async () => {
    let typeList = await getTaskType();
    setTypeList(typeList);
  };

  return (
    <Spin spinning={task.loading}>
      <Descriptions title="基础信息" className="base-info" size="middle" column={3}>
        {BASE_INFO.map((item: IBaseInfo, index: number) => (
          <Descriptions.Item key={index} label={item.label}>
            {item.render ? item.render({ baseInfo: task.taskBaseInfo?.[item.key], task, typeList }) : task.taskBaseInfo?.[item.key]}
          </Descriptions.Item>
        ))}
      </Descriptions>
      <Divider />
    </Spin>
  );
};

export default connect(mapStateToProps)(BaseInfo);
