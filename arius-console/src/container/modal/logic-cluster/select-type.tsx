import React, { FC, memo, useContext, useEffect, useState } from 'react';
import { Select, Tooltip } from 'antd'
import { useDispatch, useSelector } from 'react-redux';
import { getvailablePhysicsClusterListLogic } from 'api/cluster-api';

import * as actions from "actions";

const { Option } = Select;
interface propsType {
  onChange?: (result: any) => any;
  optionList: { value: any, label: any }[];
}

export const SelectType: FC<propsType> = memo(({ onChange, optionList }) => {
  const { tableData, type } = useSelector(state => ({
    tableData: (state as any).region.tableData,
    type: (state as any).region.type
  }));

  const dispatch = useDispatch();

  const [isDisabled, setIsDisabled] = useState(false);
  const [showTooltip, setShowTooltip] = useState(false);

  const getPhyClusterList = (type: number) => {
    getvailablePhysicsClusterListLogic(type).then((res) => {
      if (res) {
        res = res.map((item) => {
          return {
            value: item,
            label: item,
          };
        });
        dispatch(actions.setPhyClusterList(res, type + ""));
      }
    });
  };

  useEffect(() => {
    if(isDisabled && tableData?.length < 1) {
      getPhyClusterList(type);
    }
    
    setIsDisabled(tableData?.length > 0);
  }, [tableData]);

  return (
    <Tooltip title={'请先删除的关联region'} visible={showTooltip} >
      <Select style={{ width: "60%" }}
        onChange={(e) => {
          onChange(e);
        }}
        disabled={isDisabled}
        onMouseEnter={() => {
          if (!isDisabled) return;

          setShowTooltip(true);
        }}
        onMouseLeave={() => {
          setShowTooltip(false);
        }}
        placeholder="请选择集群类型"
      >
        {
          optionList.map(item => <Option value={item.value}>{item.label}</Option>)
        }
      </Select>
    </Tooltip>
  )
})
