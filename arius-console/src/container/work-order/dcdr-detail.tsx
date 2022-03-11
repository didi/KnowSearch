import React, { useState, useEffect, useRef } from 'react';
import { SPIT_STYLE_MAP, DCDR_STATE_MAP } from 'constants/status-map';
import { cancelDcdr, getDcdrDetail, canceltemplateDcdr } from 'api/dcdr-api'
import './dcdr.less';
import './index.less';
import  { Button, Progress, Popconfirm, Tooltip, Collapse, Modal, message } from 'antd';
import { DTable } from 'component/dantd/dtable';
import { DcdrDrawer } from './dcdr-drawer';
import Url from 'lib/url-parser';
import moment from 'moment';


export const DcdrTaskList: React.FC = () => {
  const [data, setData]: any = useState({});
  // 依赖data 利用ref避过effect依赖监测
  const ref: any = useRef();
  const [visible, setVisible] = useState(false);
  const [params, setParams] = useState({
    taskId: 0,
    templateId: 0
  });
  const urlParam: any = Url().search;

  const reloadData = () => {
    getDcdrDetail(urlParam.taskid).then(res => {
      ref.current = res;
      setData(res);
    })
  }

  // 开启定时器定时刷新
  React.useEffect(() => {
    reloadData();
    const time = setInterval(() => {
      if (ref.current && (ref.current?.state !== 0 && ref.current?.state !== 3 && ref.current?.state !== 1 )) {
        reloadData();
      }
    }, 10 * 1000);
    return () => clearInterval(time);
  }, [urlParam.taskid, ref])



  let pattern = null as any;
  SPIT_STYLE_MAP.forEach((ele, index) => {
    if (ele.type === DCDR_STATE_MAP[data?.state]) {
      pattern = ele;
    }
  });

  const getColumns = (reloadData: Function) => {
    return [
      {
        title: "任务标题",
        dataIndex: "taskTitle",
        key: "taskTitle",
      },
      {
        title: "任务状态",
        dataIndex: "taskStatus",
        key: "taskStatus",
        render: (text, record) => {
          let pattern = null as any;
          SPIT_STYLE_MAP.forEach((ele, index) => {
            if (ele.type === DCDR_STATE_MAP[text]) {
              pattern = ele;
            }
          });
          const colorMap = {
            0: '#bfbfbf',
            1: '#10c038',
            2: '#ff931d',
            3: '#f04134',
            4: '#bfbfbf',
          }
          const spotStyle ={
            width: 6,
            height: 6,
            borderRadius: '50%',
            background: colorMap[text],
            display: 'inline-block',
            marginRight: 4,
          }
          return (
            <span>
              <div style={spotStyle}/>
              {pattern.text}
            </span>
          )
        }
      },
      {
        title: "任务类型",
        dataIndex: "switchType",
        key: "switchType",
        render: (text) => {
          return text == 1 ? '平滑切换' : '强制切换'
        },
        sorter: (a, b) => {
          return a?.switchType - b?.switchType
        }
      },
      {
        title: "创建时间",
        dataIndex: "createTime",
        key: "createTime",
        render: (text) => {
          return text ? moment(text).format('YYYY-MM-DD hh:mm:ss') : '';
        },
        sorter: (a, b) => {
          return moment(a?.createTime).valueOf() - moment(b?.createTime).valueOf()
        }
      },
      {
        title: "更新时间",
        dataIndex: "updateTime",
        key: "updateTime",
        render: (text) => {
          return text ? moment(text).format('YYYY-MM-DD hh:mm:ss') : '';
        },
        sorter: (a, b) => {
          return moment(a?.updateTime).valueOf() - moment(b?.updateTime).valueOf()
        }
      },
      {
        title: "操作",
        dataIndex: "operation",
        key: "operation",
        render: (text: any, record: any) => {
          return (
            <>
              <a href="javaScript:;" onClick={() => {
                setVisible(true);
                setParams({
                  taskId: urlParam.taskid,
                  templateId: record.templateId,
                })
              }}>查看详情</a>
              {
                record.taskStatus === 2 || record.taskStatus === 4 ? <a href="javaScript:;" style={{ marginLeft: 20 }} onClick={() => {
                  Modal.confirm({
                    title: `提示`,
                    content: `确定取消任务${decodeURI(record?.taskTitle)}?`,
                    // icon: <DeleteOutlined style={{ color: "red" }} />,
                    // width: 500,
                    okText: "确定",
                    cancelText: "取消",
                    onOk() {
                      canceltemplateDcdr(urlParam?.taskid, record?.templateId).then(() => {
                        message.success('操作成功');
                        reloadData();
                      });
                    },
                  });
                }}>取消</a> : null
              }
            </>
          )
        },
      },
    ]
  }
  
  // 关闭抽屉
  const onCancel = () => {
    setVisible(false);
    setParams({
      taskId: 0,
      templateId: 0
    });
  }
  return (
    <div className="dcdr-taskdetail">
      <div className="dcdr-taskdetail-header">任务详情</div>
      <div className="plan-speed-head" style={{ borderBottom: 'solid 1px #DBE0E4' }}>
        <div className="speed-head-left">
          <span className="head-left-top">
            <Progress percent={data?.percent} strokeColor={pattern?.color.replace('333', 'BFBFBF')} className="left-top-pro" />
            <i className={`left-top-text ${pattern?.back}`}>{pattern?.text}</i>
          </span>
          <ul className="head-left-ul">
            <li>
              <span>
                <div className="spot running" />
                总数：{data?.total}
              </span>
              <span>
                <div className="spot success" />
                成功：{data?.successNum}
              </span>
              <span>
                <div className="spot failed" />
                失败：{data?.failedNum}
              </span>
              <span>
                <div className="spot creating" />
                执行中：{data?.runningNum}
              </span>
              <span>
                <div className="spot waiting" />
                待执行：{data?.waitNum}
              </span>
              <span>
                <div className="spot waiting" />
                已取消：{data?.cancelNum}
              </span>
            </li>
          </ul>
        </div>
        <div>
          <Button type="primary" disabled={ref.current?.state !== 2 } onClick={() => {
            Modal.confirm({
              title: `提示`,
              content: `确定取消任务${decodeURI(urlParam?.title)}?`,
              // icon: <DeleteOutlined style={{ color: "red" }} />,
              // width: 500,
              okText: "确定",
              cancelText: "取消",
              onOk() {
                cancelDcdr(urlParam?.taskid).then(() => {
                  reloadData();
                });
              },
            });
          }}>全部取消</Button>
        </div>
      </div>
      <DTable 
        columns={getColumns(reloadData)}
        rowKey="templateId"
        dataSource={data?.dcdrSingleTemplateMasterSlaveSwitchDetailList || []}
        reloadData={reloadData}
      />
      <DcdrDrawer visible={visible} onCancel={onCancel} {...params} parentReload={reloadData}/>
    </div>
  )
}