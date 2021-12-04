import React, { useEffect } from 'react';
import { connect, useDispatch } from "react-redux";
import  { Button, Form, Popconfirm, Select, Table } from 'antd';
import { getPhyClusterRegionList, getPhyClusterList } from 'api/op-cluster-region-api';
import * as actions from "actions";

import './index.less'


const mapStateToProps = (state: any) => ({
  clusterList: state.region.phyClusterList,
  type: state.region.type,
});

export const RelevanceRegion: React.FC<any> = connect(mapStateToProps)((props: {onChange?: (result: any) => any, type: string, clusterList: {value: string, label: string}[]}) => {
  const [tableData, setTableData] = React.useState([]);
  const [regionList, setRegionList] = React.useState([]);
  const dispatch = useDispatch();
  const [form] = Form.useForm();

  const onSubmit = (valus) => {
    valus.regionObject = valus.region?.map(item => JSON.parse(item));
    let racks = '';
    let regionId = '';
    valus.regionObject.forEach(element => {
      if (racks) {
        racks = racks + ',' + element.racks;
        regionId = regionId + ',' + element.regionId;
      } else {
        racks = racks + element.racks;
        regionId = regionId + element.regionId;
      }
    });
    valus.racks = racks;
    valus.regionId = regionId;
    let dataSource = [...tableData];
    dataSource.push({key: dataSource.length, ...valus});
    setTableData(dataSource);
    setRegionList(regionList.map(item => {
      valus.region?.map(d => {
        if (item.value === d) {
          item.disabled = true
        }
      });
      return item;
    }));
    const { onChange } = props;
    onChange && onChange(dataSource);
    form.resetFields();
  }

  const handleDelete = (key) => {
    setRegionList(regionList.map(item => {
      tableData[key].region?.map(d => {
        if (item.value === d) {
          item.disabled = false
        }
      });
      return item;
    }));
    const dataSource = [...tableData];
    dataSource.splice(key, 1);
    setTableData(dataSource);
    const { onChange } = props;
    onChange && onChange(dataSource);
  }

  const columns = [
    {
      title: '物理集群',
      dataIndex: 'name',
    },
    {
      title: 'RegionId',
      dataIndex: 'regionId',
    },
    {
      title: 'racks',
      dataIndex: 'racks',
    },
    {
      title: '操作',
      dataIndex: 'operation',
      render: (_, record: { key: React.Key }) => (
        <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.key)}>
          <a>删除</a>
        </Popconfirm>
      )
    },
  ];

  const getAsyncPhyClusterList = async () => {
    if(tableData?.length !== 1) {
      dispatch(actions.setPhyClusterList(props.clusterList, props.type, tableData));
      return;
    }

    const res = await getPhyClusterList(props.type, tableData[0].name);
    
    const clusterList = res.map((item) => ({
      value: item,
      label: item,
    }));

    dispatch(actions.setPhyClusterList(clusterList, props.type, tableData));
  }

  useEffect(() => {
    getAsyncPhyClusterList();
  }, [tableData]);

  const getRegionList = async (e: string, type) => {
    const arr = tableData.filter(item => item.name === e);
    getPhyClusterRegionList(e).then((res) => {
      res = res.map(item => {
        return {
          label: `${item.id}(racks: ${item.racks})`,
          value: JSON.stringify({regionId: item.id, racks: item.racks}),
          disabled: (item.logicClusterId === -1) ? false : true,
        };
      });
      if(arr && arr.length > 0){ //重复选同个集群，禁用已选
        res = res.map(item => {
          arr.forEach(i => {
            if(item.label.indexOf(i.racks) > -1 ) {
              item.disabled = true;
            }
          });
          return item;
        });
      }
      setRegionList(res);
    });
  }

  return (
    <>
      <Form layout='inline' form={form} name="control-hooks1" onFinish={onSubmit}>
          <Form.Item
             name="name"
             rules={[{ required: true, message: '请选择物理集群' }]}
          >
            <Select 
              showSearch 
              placeholder="请选择物理集群" 
              style={{ width: 250}} 
              options={props.clusterList} 
              filterOption={(input, option) => JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >= 0}
              onChange={getRegionList}
            />
           </Form.Item>
           <Form.Item
             name="region"
             rules={[{ required: true, message: '请选择region' }]}
          >
            <Select
                mode="multiple"
                allowClear
                style={{ width: 250}}
                placeholder="请选择region"
                options={regionList}
              >
              </Select>
           </Form.Item>
           <Form.Item>
              <Button type="primary" htmlType="submit" >
                添加
              </Button>
           </Form.Item>
      </Form>
      <div style={{paddingTop: 15}}>
          {
            tableData.length ?
            <Table
              dataSource={tableData}
              columns={columns}
              pagination={false}
            />
            :
            null
          }
      </div>
    </>
  );
});