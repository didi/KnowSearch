import React, { memo, useState } from "react";
import { useDispatch } from "react-redux";
import { SettingOutlined } from "@ant-design/icons";
import { Button, Modal } from "antd";
import * as actions from "actions";

interface optionListType {
  title: string;
  plainOptions: { label: string; value: string | number }[];
}
interface propsType {
  title: string;
  optionList: optionListType[];
  checkedData?: object;
  setCheckedData: (defaultCheckedList) => void;
}

export const IndexConfig: React.FC<propsType> = memo(
  ({ title, optionList, checkedData, setCheckedData }) => {
    const { btnTitle } = {
      btnTitle: "指标配置",
    };
    const dispatch = useDispatch();
    const modalId = "IndexConfig";

    const params = {
      title: title,
      optionList: optionList,
      defaultCheckedData: checkedData || {},
    };

    return (
      <Button
        icon={<SettingOutlined />}
        onClick={() => {
          dispatch(actions.setModalId(modalId, params, setCheckedData));
        }}
      >
        {btnTitle}
      </Button>
    );
  }
);
