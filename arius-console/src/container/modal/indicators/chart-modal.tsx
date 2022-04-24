import React, { memo, useEffect, useState } from 'react'
import { useSelector, useDispatch, shallowEqual } from 'react-redux';
import { Descriptions, Modal, Spin, Tooltip } from 'antd';

import { Line } from 'container/indicators-kanban/components';
import { getOption } from 'container/indicators-kanban/config';
import { getQueryTemplateData } from 'api/gateway-kanban';
import { indexConfigData } from 'container/indicators-kanban/gateway-kanban/query-template-config';

import { getDslDetail } from 'api/search-query';
import * as actions from "../../../actions";

import './chart-modal.less';
import { CopyOutlined } from '@ant-design/icons';
import { copyString } from 'lib/utils';

const DescriptionsItem = Descriptions.Item;

const contentStyle: any = { whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', fontFamily: "PingFangSC-Regular", fontSize: 12, color: "#374053" };
const labelStyle = { fontFamily: "PingFangSC-Regular", fontSize: 12, color: "#697687" };

export const ChartModal = memo((props) => {
  const dispatch = useDispatch();

  const { params, cb } = useSelector(state => ({
    params: (state as any).modal.params,
    cb: (state as any).modal.cb
  }), shallowEqual);

  const { md5, metricsType } = params;

  const { startTime, endTime, isMoreDay } = useSelector(
    (state) => ({
      startTime: (state as any).gatewayKanban.startTime,
      endTime: (state as any).gatewayKanban.endTime,
      isMoreDay: (state as any).gatewayKanban.isMoreDay,
    }),
    shallowEqual
  );

  const [data, setData] = useState<any>({});
  const [viewData, setViewData] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [dataLoading, setDataLoading] = useState(false);

  const typeMap = [
    {
      type: '查询模板',
      value: <div className="dsl-drawer-box" title={data?.dslTemplate}>{data?.dslTemplate} <CopyOutlined className="dsl-drawer-icon" onClick={() => copyString(data?.dslTemplate)} /></div>,
    },
    {
      type: '查询语句示例',
      value: <div className="dsl-drawer-box" title={data?.dsl} >{data?.dsl} <CopyOutlined className="dsl-drawer-icon" onClick={() => copyString(`GET ${data?.indices}/_search\n${data?.dsl}`)} /></div>,
    }
  ]

  const detailMap = [
    {
      type: '请求数',
      value: data?.searchCount,
    },
    {
      type: '查询语句长度',
      value: data?.dslLenAvg,
    },
    {
      type: '响应长度',
      value: data?.responseLenAvg,
    },
    {
      type: '耗时（ms）',
      value: data?.totalCostAvg,
    },
    {
      type: 'es响应时间（ms）',
      value: data?.esCostAvg,
    },
    {
      type: '预处理时间（ms）',
      value: data?.beforeCostAvg,
    },
    {
      type: '总Shard个数',
      value: data?.totalShardsAvg,
    },
    {
      type: '失败Shard个数',
      value: data?.failedShardsAvg,
    },
    {
      type: '总命中数',
      value: data?.totalHitsAvg,
    }
  ]

  const getAsyncViewData = async () => {
    setIsLoading(true);
    try {
      const res = await getQueryTemplateData(
        [metricsType],
        startTime,
        endTime,
        0,
        md5
      );
      const viewData = res?.length ? res.map((item) => getOption(item, indexConfigData, isMoreDay, false, false, false))[0] : [];
      setViewData(viewData);
    } catch (error) {
      console.error(error);
      setViewData([]);
    } finally {
      setIsLoading(false);
    }
  }


  const getTemplate = async (md5) => {
    setDataLoading(true);
    try {
      const data = await getDslDetail(md5);
      setData(data);
    } catch (error) {
      console.error(error);
      setData({});
    } finally {
      setDataLoading(false);
    }
  }

  useEffect(() => {
    getAsyncViewData();
    getTemplate(md5);
  }, []);

  return (
    <Modal
      visible={true}
      maskClosable={false}
      width={1080}
      onOk={() => {
        dispatch(actions.setModalId(""));
      }}
      onCancel={() => {
        dispatch(actions.setModalId(""));
      }}
      title='查询模版详情'
      className={"chartModal-container"}
      footer={null}
      bodyStyle={{
        display: "block",
        padding: "20px"
      }}
    >
      <div className={"qsl-template-box"}>
        <div className={"qsl-template-box-chart"}>
          <Line
            key={`${md5}`}
            index={`${md5}`}
            option={viewData || {}}
            isLoading={isLoading}
            height={260}
          />
        </div>
        <Spin spinning={dataLoading}>
          <div className={"qsl-template-box-description"}>
            <Descriptions contentStyle={contentStyle} style={{ paddingLeft: 20 }} labelStyle={labelStyle} column={4}>
              {typeMap.map((item, index) => (<DescriptionsItem key={index} label={item.type} span={4} >{item.value}</DescriptionsItem>))}
              {detailMap.map((item, index) => (<DescriptionsItem key={index} label={item.type} span={2} >
                <Tooltip title={item.value}>
                  <span className="description-cell">
                    {item.value}
                  </span>
                </Tooltip>
              </DescriptionsItem>))}
            </Descriptions>
          </div>
        </Spin>
      </div>
    </Modal>)
});
