import React, { useState, useEffect } from 'react';
import { Button, Checkbox, message, Modal, Spin } from 'antd';
import { SettingOutlined } from '@ant-design/icons';

const CheckboxGroup = Checkbox.Group;

interface IFilterColumnsProps {
  // 全量的列
  columns: object[];
  // 设置外面的列
  setColumns: any;
  // 可选择的列
  checkArr: string[];
  // 请求方法
  getCheckFn: () => Promise<string[]>;
  saveCheckFn: (list: string[]) => void;
}

const FilterColumns: React.FC<IFilterColumnsProps> = (props: IFilterColumnsProps) => {
  const list = props.checkArr;
  const [visible, setVisible] = useState(false);
  const [checkAll, setCheckAll] = useState(true);
  const [checkArr, setCheckArr] = useState([]);
  const [indeterminate, setIndeterminate] = useState(true)
  const [loading, setLoading] = useState(false)
  const onOk = () => {
    props.setColumns(props.columns.filter((item: any) => {
      if (list.includes(item.dataIndex) && !checkArr.includes(item.dataIndex)) {
        return false
      }
      return true
    }));
    props.saveCheckFn(checkArr)
    setVisible(false);
  }

  const cancel = () => {
    setVisible(false);
  }

  const onCheckAllChange = (e) => {
    setCheckArr(e.target.checked ? [...list] : []);
    setIndeterminate(false);
    setCheckAll(e.target.checked);
  }

  const onCheckboxGroupChange = (values) => {
    setCheckArr([...values]);
    setIndeterminate(!!values.length && values.length < list.length);
    setCheckAll(values.length === list.length);
  }

  const getCheck = async () => {
    setLoading(true);
    const cacheCheck = await props.getCheckFn();
    setLoading(false);
    if (cacheCheck && cacheCheck.length) {
      setCheckArr(list.filter(item => cacheCheck.includes(item)));
      // props.setColumns(props.columns.filter((item: any) => {
      //   if (list.includes(item.dataIndex) && !cacheCheck.includes(item.dataIndex)) {
      //     return false
      //   }
      //   return true
      // }));
    } else {
      setCheckArr([]);
    }
    setIndeterminate(!!cacheCheck.length && cacheCheck.length < list.length);
    setCheckAll(cacheCheck.length === list.length);
    props.setColumns(props.columns.filter((item: any) => {
      if (list.includes(item.dataIndex) && !cacheCheck.includes(item.dataIndex)) {
        return false
      }
      return true
    }));
  }

  useEffect(()=> {
      getCheck();
  }, [])

  useEffect(()=> {
    if (visible) {
      getCheck();
    }
    // onOk();
  }, [visible])

  return (
    <span>
      <Modal
        visible={visible}
        onOk={onOk}
        onCancel={cancel}
        width={600}
      >
        <Spin spinning={loading}>
          <Checkbox
            indeterminate={indeterminate}
            onChange={onCheckAllChange}
            checked={checkAll}
          >
            全选
          </Checkbox>
          <br />
          <CheckboxGroup
            className="checkboxGroup"
            options={props.columns.filter((item: any) => list.includes(item.dataIndex)).map((item: any) => ({
              label: item.title as string,
              value: item.dataIndex as string,
            }))}
            value={checkArr}
            onChange={onCheckboxGroupChange}
          />
        </Spin>
      </Modal>
      <Button
        icon={<SettingOutlined />}
        onClick={() => {
          setVisible(true);
        }}
      >
        字段配置
      </Button>
    </span>
  );
}

export default FilterColumns;