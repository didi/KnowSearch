import React, { useState, useEffect } from 'react';
import { Form, Table } from 'antd';
import { TableProps, ColumnsType } from 'antd/lib/table';
import { cloneDeep } from 'lodash';
import './index.less';

interface ITableData {
  id: number | string;
  edit?: boolean;
  [key: string]: any;
}

export interface IEditTableProps extends TableProps<ITableData> {
  dataSource: ITableData[];
  columns: ColumnsType<ITableData> | any;
  rowData: ITableData;
  form: any;
  formItemParams?: any;
  EditRow?: (rowData: ITableData) => void;
  deleteRow?: (data: ITableData[], rowData: ITableData) => void;
  okRow?: (rowData: ITableData, index: number) => void;
  setData?: () => void;
}

const Item = Form.Item;
const basicClass = 'table-form-edittable';

const EditableTable = (props) => {
  const [tabledata, SetTableData] = useState([]);
  const [columns, setColumns] = useState(props.columns);
  const EditRow = (rowData: ITableData) => {
    let cloneData = cloneDeep(tabledata);
    const index = cloneData.findIndex((item) => item.id === rowData.id);
    cloneData[index].edit = !cloneData[index].edit;
    if (cloneData[index].edit) {
      cloneData = cloneData.map((item) => {
        item.disabled = true;
        return item;
      });
    } else {
      cloneData = cloneData.map((item) => {
        item.disabled = false;
        return item;
      });
    }
    props?.form?.setFieldsValue({ ...rowData });
    SetTableData(cloneData);
  };

  const okRow = (rowData: ITableData) => {
    props
      .form!.validateFields()
      .then((result: ITableData) => {
        let cloneData = cloneDeep(tabledata);
        const index = cloneData.findIndex((item) => item.id === rowData.id);
        result.id = tabledata[index].id;
        result.edit = !tabledata[index].edit;
        cloneData[index] = { ...cloneData[index], ...result };
        cloneData = cloneData.map((item) => {
          item.disabled = false;
          return item;
        });
        props.setData && props.setData(cloneData);
        SetTableData(cloneData);
      })
      .catch((errs: any) => {
        console.log(errs);
      });
  };

  const deleteRow = (data: ITableData[], rowData: ITableData) => {
    const cloneData = cloneDeep(data).filter((row) => row.id !== rowData.id);
    props.setData && props.setData(cloneData);
    SetTableData(cloneData);
  };

  useEffect(() => {
    const cloneData = cloneDeep(tabledata);
    if (props.rowData?.id) {
      if (cloneData.filter((item) => item.edit).length) {
        cloneData.push({ ...props.rowData, disabled: true });
      } else {
        cloneData.push(props.rowData);
      }
      props.setData && props.setData(cloneData);
      SetTableData(cloneData);
    }
  }, [props.rowData]);

  useEffect(() => {
    SetTableData(props.dataSource);
  }, [props.dataSource]);

  // 劫持columns增加操作行
  useEffect(() => {
    const editColumns: ColumnsType<ITableData> = [
      {
        dataIndex: '',
        title: '操作',
        render: (_, record: ITableData) => {
          if (record.edit) {
            return (
              <>
                <a
                  style={{ marginRight: 10 }}
                  onClick={() => {
                    props.okRow ? props.okRow(record) : okRow(record);
                  }}
                >
                  确认
                </a>
                <span style={{ color: '#ccc', marginRight: 10 }}>|</span>
                <a
                  onClick={() => {
                    props.EditRow ? props.EditRow(record) : EditRow(record);
                  }}
                >
                  取消
                </a>
              </>
            );
          }
          if (record.disabled) {
            return (
              <div className={record.disabled ? 'ant-typography-disabled' : ''}>
                <span style={{ color: '#ccc', marginRight: 10 }}>编辑</span>
                <span style={{ color: '#ccc', marginRight: 10 }}>|</span>
                <span style={{ color: '#ccc' }}>删除</span>
              </div>
            );
          }
          return (
            <>
              <a
                style={{ marginRight: 10 }}
                onClick={() => {
                  props.EditRow ? props.EditRow(record) : EditRow(record);
                }}
              >
                编辑
              </a>
              <span style={{ color: '#ccc', marginRight: 10 }}>|</span>
              <a
                onClick={() => {
                  props.deleteRow ? props.deleteRow(tabledata, record) : deleteRow(tabledata, record);
                }}
              >
                删除
              </a>
            </>
          );
        },
      },
    ];
    setColumns([...props.columns, ...editColumns]);
  }, [props.columns, tabledata]);
  return (
    <div className={basicClass}>
      <Item className={`${basicClass}-formitem`} name={props.name} {...props.formItemParams}>
        <Table pagination={false} rowKey="id" {...props} dataSource={tabledata} columns={columns} />
      </Item>
    </div>
  );
};

export default EditableTable;
