import React, { useEffect, useState } from 'react';
import { Drawer, Descriptions, Button, Tooltip, message, Modal } from 'antd';
import { CopyOutlined } from '@ant-design/icons';
import { copyString } from '../../lib/utils';
import { changeStatus, getDslDetail  } from 'api/search-query';
import EditLimit from '../search-query/components/editLimit';
import moment from 'moment';

const confirm = Modal.confirm;
const DescriptionsItem = Descriptions.Item;

interface IProps {
  visible: boolean;
  detailData: any;
  onCancel: () => void;
  cb: () => void;
  showEditLimit: (detailData: any) => void;
}

const DslDetail: React.FC<IProps> = (props: IProps) => {
  const { detailData } = props;
  const [data, setData]: any = useState({});
  const [records, setRecords]: any = useState([]);
  const [editVisible, setEditVisible] = useState(false);

  const getWidth = () => {
    return document.querySelector('.ecmc-layout-container')?.clientWidth || 600;
  }

  const getDetail = () => {
    getDslDetail(detailData.dslTemplateMd5)
      .then((res) => {
        setData(res);
        setRecords([res]);
      })
  }
  
  const editCancel = () => {
    setEditVisible(false);
  }

  const showEditLimit = () => {
    setEditVisible(true)
  }

  useEffect(() => {
    if(props.visible) {
      getDetail();
    }
    return () => {
      setData({});
      setRecords([]);
    }
  }, [props.visible, detailData, detailData.dslTemplateMd5])

  const typeMap = [
    {
      type: '查询索引示例',
      value: <div className="dsl-drawer-box" title={data?.indiceSample}>{data?.indiceSample} <CopyOutlined className="dsl-drawer-icon" onClick={() => copyString(data?.indiceSample)}/></div>,
    },
    {
      type: '查询模板',
      value: <div className="dsl-drawer-box" title={data?.dslTemplate}>{data?.dslTemplate} <CopyOutlined className="dsl-drawer-icon" onClick={() => copyString(data?.dslTemplate)}/></div>,
    },
    {
      type: '查询语句示例',
      value: <div className="dsl-drawer-box" title={data?.dsl}>{data?.dsl} <CopyOutlined className="dsl-drawer-icon" onClick={() => copyString(`GET ${data?.indices}/_search\n${data?.dsl}`)}/></div>,
    },
    {
      type: '创建时间',
      value: moment(data?.ariusCreateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      type: '修改时间',
      value: moment(data?.ariusModifyTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      type: '最近使用时间',
      value: moment(data?.flinkTime).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]
  const detailMap = [
    {
      type: '请求数',
      value: data?.searchCount,
    },
    {
      type: '查询语句长度',
      value:  Number(data?.dslLenAvg).toFixed(2),
    },
    {
      type: '响应长度',
      value:  Number(data?.responseLenAvg).toFixed(2),
    },
    {
      type: '耗时（ms）',
      value:  Number(data?.totalCostAvg).toFixed(2),
    },
    {
      type: 'es响应时间（ms）',
      value:  Number(data?.esCostAvg).toFixed(2),
    },
    {
      type: '预处理时间（ms）',
      value:  Number(data?.beforeCostAvg).toFixed(2),
    },
    {
      type: '总Shard个数',
      value:  Number(data?.totalShardsAvg).toFixed(2),
    },
    {
      type: '失败Shard个数',
      value:  Number(data?.failedShardsAvg).toFixed(2),
    },
    {
      type: '总命中数',
      value:  Number(data?.totalHitsAvg).toFixed(2),
    },
    {
      type: '查询限流值',
      value:  Number(data?.queryLimit).toFixed(2),
    },
    {
      type: '语句类型',
      value: data?.dslType,
    },
    {
      type: '请求方式',
      value: data?.requestType,
    },
    {
      type: '查询方式',
      value: data?.searchType,
    },
  ]

  const handleChangeStatus = () => {
    confirm({
      title: "提示",
      content: `确定${data?.enable || data?.enable === null ? '禁用' : '启用'}查询模板${data?.dslTemplateMd5}？`,
      width: 500,
      okText: "确认",
      cancelText: "取消",
      onOk() {
        changeStatus(data?.dslTemplateMd5)
          .then((res) => {
            getDetail();
            props.cb();
            if (res) {
              message.success('操作成功');
            }
          })
      },
    });
  }

  const renderFooter = () => {
    return <div>
      <Button type="primary" className={'dsl-mr-20'} onClick={showEditLimit}>修改限流值</Button>
      <Button onClick={handleChangeStatus}>
        {data.enable || data.enable === null ? '禁用' : '启用'}
      </Button>
    </div>
  }

  const title = (text) => {
    return <div className="dsl-drawer-title">{text}</div>
  }

  const contentStyle: any = {  whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', }

  return (
    <Drawer
      title={'查询模板详情'}
      visible={props.visible}
      width={getWidth()}
      maskClosable={true}
      footer={renderFooter()}
      onClose={props.onCancel}
      bodyStyle={{ padding: '0px' }}
    >
      <div className="dsl-drawer">
        <EditLimit visible={editVisible} record={records} cancel={editCancel} cb={getDetail}/>
        {title('基本信息')}
        <Descriptions column={3} contentStyle={contentStyle} style={{ paddingLeft: 24 }}>
          {typeMap.map((item, index) => (<DescriptionsItem key={index} label={item.type} >{item.value}</DescriptionsItem>))}
        </Descriptions>
      </div>
      <div className="dsl-drawer">
        {title('业务信息')}
        <Descriptions column={3} contentStyle={contentStyle}  style={{ paddingLeft: 24 }}>
          {detailMap.map((item, index) => (<DescriptionsItem key={index} label={item.type} ><Tooltip title={item.value}>{item.value}</Tooltip></DescriptionsItem>))}
        </Descriptions>
      </div>
    </Drawer>
  );
}
export default DslDetail;


