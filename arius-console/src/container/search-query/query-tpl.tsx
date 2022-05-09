import React, { useState, useCallback, useRef } from 'react';
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
  { label: '今天', value: 1 },
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
  const [columns, setColumns] = useState([]);
  const error = useRef(false)

  const [pagination, setPagination] = useState({
    position: 'bottomRight',
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
    showTotal: (total) => `共 ${total} 条`,
    current: 1,
    pageSize: 10
  });
  const [page, setPage] = useState({
    page: 1,
    size: 10,
    sortInfo: '',
    orderByDesc: true,
  })

  React.useEffect(() => {
    let params: any = sessionStorage.getItem('query-tpl');
    if (params) {
      params = JSON.parse(params);
      setDslTemplateMd5(params?.dslTemplateMd5);
      setQueryIndex(params?.queryIndex);
      setStartTime(moment(Number(params?.startTime)));
      setEndTime(moment(Number(params?.endTime)));
      form.setFieldsValue({ MD5: params?.dslTemplateMd5, queryIndex: params?.queryIndex })
      setloading(true);
      const param = {
        ...page,
        ...params,
      }
      getDslList(param).then((res: any) => {
        if (res) {
          setData(res?.bizData);
          sessionStorage.setItem('query-tpl', '')
          const { pageNo = 0, pageSize = 10 } = res.pagination
          setPagination({
            position: 'bottomRight',
            total: res?.pagination?.total,
            showQuickJumper: true,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
            showTotal: (total) => `共 ${total} 条`,
            current: pageNo,
            pageSize: pageSize
          });
        }
      }).finally(() => {
        setloading(false)
      })
    }
  }, []);

  React.useEffect(() => {
    // console.log(sessionStorage.getItem('query-tpl'), !!sessionStorage.getItem('query-tpl'))
    if (sessionStorage.getItem('query-tpl')) {
      sessionStorage.setItem('query-tpl', '')
    } else {
      reloadData();
    }
  }, [department, page]);

  React.useEffect(() => {
    return () => {
      sessionStorage.setItem('query-tpl', JSON.stringify({
        dslTemplateMd5,
        queryIndex,
        startTime: startTime.valueOf(),
        endTime: endTime.valueOf(),
      }))
    }
  }, [dslTemplateMd5, queryIndex, startTime, endTime])

  const onSelectChange = (selectedRowKeys, records) => {
    setSelectItem(records);
    setSelectedRowKeys(selectedRowKeys);
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: onSelectChange,
  };

  const reloadData = () => {
    // 校验不通过时不发送请求
    if (error.current) {
      return
    }
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
        const { pageNo = 1, pageSize = 10 } = res.pagination
        setPagination({
          position: 'bottomRight',
          total: res?.pagination?.total,
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
          showTotal: (total) => `共 ${total} 条`,
          current: pageNo,
          pageSize: pageSize
        });
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const handleChange = (pagination, _, tableParams) => {
    setPage({
      page: pagination.current,
      size: pagination.pageSize,
      sortInfo: (tableParams.order === "ascend" || tableParams.order === "descend") ? tableParams.field : null,
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
    sessionStorage.setItem('query-tpl', '')
    setPage({
      page: 1,
      size: page.size,
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
              <Item label="查询模板MD5" name="MD5" rules={[{
                required: false,
                validator: (rule, value) => {
                  if (value?.length > 128) {
                    error.current = true;
                    return Promise.reject('上限128字符');
                  }
                  error.current = false;
                  return Promise.resolve();
                }
              }]}>
                <Input placeholder="请输入" onChange={(e) => {
                  setDslTemplateMd5(e.target.value.trim());
                }}
                  onPressEnter={onSearch}
                />
              </Item>
            </Col>
            <Col span={5}>
              <Item label="查询索引" name="queryIndex" rules={[{
                required: false,
                validator: (rule, value) => {
                  if (value?.length > 128) {
                    error.current = true;
                    return Promise.reject('上限128字符');
                  }
                  error.current = false;
                  return Promise.resolve();
                }
              }]}>
                <Input placeholder="请输入" onChange={(e) => {
                  setQueryIndex(e.target.value.trim())
                }}
                  onPressEnter={onSearch}
                />
              </Item>
            </Col>
            <Col span={14}>
              <Radio.Group
                onChange={timeChange}
                value={''}
              >
                {
                  tiemOptions.map(option => (
                    <Radio.Button value={option.value} key={option.value}>{option.label}</Radio.Button>
                  ))
                }
              </Radio.Group>
              <RangePicker
                className="dsl-ml-20"
                style={{ minWidth: 310, marginLeft: 20 }}
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
    const checkListStr: string = await window.localStorage.getItem('dslTemplate');
    if (checkListStr) {
      try {
        return JSON.parse(checkListStr);
      } catch (err) {
        console.log(err);
      }
    } else {
      return []
    }
  }

  const saveCheckFn = (list: string[]) => {
    window.localStorage.setItem('dslTemplate', JSON.stringify(list))
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
    )
  }, [selectItem])

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
      <DslDetail visible={visible} detailData={record} onCancel={onCancel} cb={reloadData} showEditLimit={showEditLimit} />
      <EditLimit visible={editVisible} record={records} cancel={editCancel} cb={reloadData} />
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
            scroll: { x: 1700 - ((13 - columns.length) * 130) },
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
