import React, { useState, useEffect } from "react";
import { Button, Checkbox, message, Modal, Spin } from "antd";
import { SettingOutlined } from "@ant-design/icons";

const CheckboxGroup = Checkbox.Group;

interface IFilterColumnsProps {
  // 全量的列
  columns: object[];
  // 设置外面的列
  setColumns: any;
  // 可选择的列
  checkArr: string[];
  defaultCheckedArr?: string[];
  // 请求方法
  getCheckFn: () => Promise<string[]>;
  saveCheckFn: (list: string[]) => void;
  filterColumnsVisible: boolean;
  setFilterColumnsVisible: any;
  sortObj?: any;
}

const FilterColumns: React.FC<IFilterColumnsProps> = (props: IFilterColumnsProps) => {
  const { filterColumnsVisible, setFilterColumnsVisible, sortObj } = props;
  const list = props.checkArr;
  const [checkAll, setCheckAll] = useState(true);
  const [checkArr, setCheckArr] = useState(props.defaultCheckedArr || []);
  const [indeterminate, setIndeterminate] = useState(true);
  const [loading, setLoading] = useState(false);

  const onOk = () => {
    props.setColumns(
      props.columns.filter((item: any) => {
        if (list.includes(item.dataIndex) && !checkArr.includes(item.dataIndex)) {
          return false;
        }
        return true;
      })
    );
    props.saveCheckFn(checkArr);
    setFilterColumnsVisible(false);
  };

  const cancel = () => {
    setFilterColumnsVisible(false);
  };

  const onCheckAllChange = (e) => {
    setCheckArr(e.target.checked ? [...list] : []);
    setIndeterminate(false);
    setCheckAll(e.target.checked);
  };

  const onCheckboxGroupChange = (values) => {
    setCheckArr([...values]);
    setIndeterminate(!!values.length && values.length < list.length);
    setCheckAll(values.length === list.length);
  };

  const initCheckedItems = async () => {
    setLoading(true);
    const cacheCheck = await props.getCheckFn();
    setLoading(false);

    const checkedItems = cacheCheck?.length ? cacheCheck : props.defaultCheckedArr || [];
    setCheckArr(list.filter((item) => checkedItems.includes(item)));

    setIndeterminate(!!checkedItems.length && checkedItems.length < list.length);
    setCheckAll(checkedItems.length === list.length);
    props.setColumns(
      props.columns.filter((item: any) => {
        if (list.includes(item.dataIndex) && !checkedItems.includes(item.dataIndex)) {
          return false;
        }
        return true;
      })
    );
  };

  useEffect(() => {
    initCheckedItems();
  }, [sortObj]);

  useEffect(() => {
    if (filterColumnsVisible) {
      initCheckedItems();
    }
    // onOk();
  }, [filterColumnsVisible]);

  return (
    <Modal visible={filterColumnsVisible} onOk={onOk} onCancel={cancel} width={600}>
      <Spin spinning={loading}>
        <Checkbox indeterminate={indeterminate} onChange={onCheckAllChange} checked={checkAll}>
          全选
        </Checkbox>
        <br />
        <CheckboxGroup
          className="checkboxGroup"
          options={props.columns
            .filter((item: any) => list.includes(item.dataIndex))
            .map((item: any) => ({
              label: item.title as string,
              value: item.dataIndex as string,
            }))}
          value={checkArr}
          onChange={onCheckboxGroupChange}
        />
      </Spin>
    </Modal>
  );
};

export default FilterColumns;
