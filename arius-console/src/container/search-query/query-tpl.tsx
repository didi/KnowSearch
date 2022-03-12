import React, { useState, useCallback } from 'react';
import { Row, Col, Form, DatePicker, Input, Radio, Button, Tooltip } from 'antd';
import moment from 'moment';
import { getQueryTplColumns, cherryList } from './config';
import { getDslList, getCheckedList, setCheckedList } from 'api/search-query';
// todo 接口好后增加类型判断
// import { ITask } from '@types/task-types';
import { RadioChangeEvent } from 'antd/lib/radio'
import { DTable, ITableBtn } from 'component/dantd/dtable';
import DslDetail from '../drawer/dsl-detail';
import EditLimit from './components/editLimit';
import FilterColumns from 'component/filterColumns';

const CN = 'dsl-query-tpl';
const Item = Form.Item;
const tiemOptions = [
  { label: '今天', value: 1},
  { label: '近7天', value: 7 },
  { label: '近一月', value: 30 },
];

const RangePicker = DatePicker.RangePicker;

export const QueryTpl = () => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [data, setData] = useState([] as any[]);
  const [dslTemplateMd5, setDslTemplateMd5] = useState('');
  const [queryIndex, setQueryIndex] = useState('');
  const [startTime, setStartTime] = useState(moment().subtract(1, 'day'));
  const [endTime, setEndTime] = useState(moment());
  const [visible, setVisible] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [records, setRecords] = useState([]);
  const [selectItem, setSelectItem] = useState([]);
  const [editVisible, setEditVisible] = useState(false);
  const [form] = Form.useForm();
  const [record, setRecord] = useState({});
  const [columns, setColumns] = useState([])

  const [pagination, setPagination] = useState({
    position: 'bottomRight',
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
    showTotal: (total) => `共 ${total} 条`,
  });
  const [page, setPage] = useState({
    from: 0,
    size: 10,
    sortInfo: '',
    orderByDesc: true,
  })

  React.useEffect(() => {
    reloadData();
  }, [department, page]);

  const onSelectChange = (selectedRowKeys, records) => {
    setSelectItem(records);
    setSelectedRowKeys(selectedRowKeys);
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: onSelectChange,
  };

  const reloadData = () => {
    setloading(true);
    const params = {
      ...page,
      startTime: startTime.valueOf(),
      endTime: endTime.valueOf(),
      dslTemplateMd5,
      queryIndex,
    }
    getDslList(params).then((res: any) => {
      if (res) {
        setData(res?.bizData);
        setPagination({
          position: 'bottomRight',
          total: res?.pagination?.total,
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
          showTotal: (total) => `共 ${total} 条`,
        });
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const handleChange = (pagination, _, tableParams) => {
    setPage({
      from: (pagination.current - 1) * pagination.pageSize,
      size: pagination.pageSize,
      sortInfo: (tableParams.order === "ascend" || tableParams.order === "descend") ?  tableParams.field : null,
      orderByDesc: (tableParams.order === "ascend" || tableParams.order === "descend") ? tableParams.order !== "ascend" : null,
    })
  }

  const handleRangeChange = (dates: any) => {
    setStartTime(dates[0]);
    setEndTime(dates[1]);
    // setPage({
    //   from: 0,
    //   size: 10,
    //   sortInfo: '',
    //   orderByDesc: true,
    // });
    // this.props.refresh(dates);
  }

  const timeChange = (event: RadioChangeEvent) => {
    const value = event.target.value;
    setStartTime(moment().subtract(value, 'day'));
    setEndTime(moment());
    // setPage({
    //   from: 0,
    //   size: 10,
    //   sortInfo: '',
    //   orderByDesc: true,
    // });
  }

  const onCancel = () => {
    setVisible(false);
  }

  const showDrawer = (record: any) => {
    setRecord(record);
    setVisible(true);
  }

  const onSearch = () => {
    setPage({
      from: 0,
      size: 10,
      sortInfo: '',
      orderByDesc: true,
    });
  }

  const renderSearch = () => {
    return (
      <div className={`${CN}-search`}>
        <Form form={form}>
          <Row gutter={20}>
            <Col span={5}>
              <Item label="查询模板MD5" name="MD5">
                <Input placeholder="请输入" onChange={(e) => {
                  setDslTemplateMd5(e.target.value);
                }}/>
              </Item>
            </Col>
            <Col span={5}>
              <Item label="查询索引" name="queryIndex">
                <Input placeholder="请输入" onChange={(e) => {
                  setQueryIndex(e.target.value)
                }}/>
              </Item>
            </Col>
            <Col span={14}>
              <Radio.Group
                onChange={timeChange}
                defaultValue={1}
                value={parseInt(`${(endTime.valueOf() - startTime.valueOf()) / (60 * 1000 * 60 * 24)}`)}
              >
                {
                  tiemOptions.map(option => (
                    <Radio.Button value={option.value} key={option.value}>{option.label}</Radio.Button>
                  ))
                }
              </Radio.Group>
              <RangePicker
                className="dsl-ml-20"
                style={{ minWidth: 310 }}
                defaultValue={[startTime, endTime]}
                showTime
                value={[startTime, endTime]}
                onChange={handleRangeChange}
                allowClear={false}
                format={'YYYY-MM-DD HH:mm:ss'}
                onOk={handleRangeChange}
              />
              <Button type="primary" className={`${CN}-search-submit`} onClick={onSearch}>
                查询
              </Button>
            </Col>
          </Row>
        </Form>
      </div>
    )
  }

  const getCheckList = async () => {
    const checkList: string[] = await getCheckedList('dslTemplate');
    return checkList;
  }

  const saveCheckFn = (list: string[]) => {
    setCheckedList('dslTemplate', list)
  }
  
  const getOpBtns = useCallback(() => {
    return (
      <>
        <FilterColumns 
          columns={getQueryTplColumns(reloadData, showDrawer, showEditLimit)}
          setColumns={setColumns}
          checkArr={cherryList}
          getCheckFn={getCheckList}
          saveCheckFn={saveCheckFn}
        />
        <Tooltip title={(selectItem && selectItem.length) ? '' : '需要选中后批量修改'}>
          <Button onClick={() => showEditLimit()} type="primary" style={{ marginRight: 0 }} disabled={(selectItem && selectItem.length) ? false : true}>
            批量修改限流值
          </Button>
        </Tooltip>
      </>
    )}, [selectItem])
  
  const editCancel = () => {
    setEditVisible(false);
    setRecords([])
  }

  const showEditLimit = (record?: any) => {
    if (record) {
      setRecords([record]);
    } else {
      setRecords(selectItem);
    }
    setEditVisible(true)
  }
  return (
    <div>
      <DslDetail visible={visible} detailData={record} onCancel={onCancel} cb={reloadData} showEditLimit={showEditLimit}/>
      <EditLimit visible={editVisible} record={records} cancel={editCancel} cb={reloadData}/>
      {renderSearch()}
      <div className="table-content">
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={data}
          key={JSON.stringify({
            startTime: startTime.valueOf(),
            endTime: endTime.valueOf(),
            dslTemplateMd5,
            queryIndex
          })}
          paginationProps={pagination}
          attrs={{
            onChange: handleChange,
            scroll: {x: 1700 - ((13 - columns.length) * 130)},
            rowSelection: rowSelection,
            rowKey: 'dslTemplateMd5'
          }}
          columns={columns}
          reloadData={reloadData}
          renderInnerOperation={getOpBtns}
        />
      </div>
    </div>
  )
}
