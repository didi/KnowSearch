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
  goldConfig?: any;
}

export const IndexConfig: React.FC<propsType> = memo(
  ({ title, optionList, checkedData, setCheckedData, goldConfig }) => {
    const { btnTitle } = {
      btnTitle: "指标配置",
    };
    const dispatch = useDispatch();
    const modalId = "IndexConfig";

    const openWindow = () => {
      for (const key in checkedData) {
        optionList.forEach(item => {
          if (item.title === key) {
            const before = [];
            const after = [];
            item.plainOptions.forEach(option => {
              const index = checkedData[key].indexOf(option.value);
              if (index !== -1) {
                before[index] = option;
              } else {
                after.push(option);
              }
            })
            item.plainOptions = [...before, ...after];
          }
        })
      }
      const params = {
        title: title,
        optionList: optionList,
        defaultCheckedData: checkedData || {},
        goldConfig,
      };
      dispatch(actions.setModalId(modalId, params, setCheckedData));
    }

    return (
      <Button
        icon={<SettingOutlined />}
        onClick={() => {
          openWindow()
        }}
      >
        {btnTitle}
      </Button>
    );
  }
);
