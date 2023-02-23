import React, { useEffect, useState } from "react";
import { Descriptions, Button, Tooltip, message, Modal, Drawer, Divider } from "antd";
import { copyString, transTimeFormat, formatDecimalPoint } from "lib/utils";
import { changeStatus, getDslDetail } from "api/search-query";
import EditLimit from "../search-query/components/editLimit";
import store from "store";

const confirm = Modal.confirm;
const DescriptionsItem = Descriptions.Item;

const appInfo = {
  app: store.getState().app.appInfo,
  user: store.getState().user.getName,
};
interface IProps {
  visible: boolean;
  detailData: any;
  onCancel: () => void;
  cb: () => void;
  showEditLimit: (detailData: any) => void;
  history?: any;
}

const DslDetail: React.FC<IProps> = (props: IProps) => {
  const { detailData } = props;
  const [data, setData]: any = useState({});
  const [records, setRecords]: any = useState([]);
  const [editVisible, setEditVisible] = useState(false);

  const getWidth = () => {
    return 600;
  };

  const getDetail = () => {
    getDslDetail(detailData.dslTemplateMd5, detailData.projectId).then((res = {}) => {
      setData(res);
      setRecords([res]);
    });
  };

  const editCancel = () => {
    setEditVisible(false);
  };

  const showEditLimit = () => {
    setEditVisible(true);
  };

  useEffect(() => {
    if (props.visible) {
      getDetail();
    }
    return () => {
      setData({});
      setRecords([]);
    };
  }, [props.visible, detailData, detailData.dslTemplateMd5]);

  const typeMap = [
    {
      type: "查询索引示例",
      value: (
        <div className="dsl-drawer-box" title={data?.indiceSample}>
          <span className="icon iconfont iconfuzhi dsl-drawer-icon" onClick={() => copyString(data?.indiceSample)}></span>
          {data?.indiceSample}
        </div>
      ),
    },
    {
      type: "所属应用",
      value: `${detailData?.projectName}(${detailData?.projectId})`,
    },
    {
      type: "查询模板",
      value: (
        <div className="dsl-drawer-box" title={data?.dslTemplate}>
          <span className="icon iconfont iconfuzhi dsl-drawer-icon" onClick={() => copyString(data?.dslTemplate)}></span>
          {data?.dslTemplate}
        </div>
      ),
    },
    {
      type: "查询语句示例",
      value: (
        <div className="dsl-drawer-box" title={data?.dsl}>
          <span
            className="icon iconfont iconfuzhi dsl-drawer-icon"
            onClick={() => copyString(`GET ${data?.indices}/_search\n${data?.dsl}`)}
          ></span>
          {data?.dsl}
        </div>
      ),
    },
    {
      type: "创建时间",
      value: transTimeFormat(data?.ariusCreateTime),
    },
    {
      type: "修改时间",
      value: transTimeFormat(data?.ariusModifyTime),
    },
    {
      type: "最近使用时间",
      value: transTimeFormat(data?.flinkTime),
    },
    {
      type: "MD5信息",
      value: (
        <div className="dsl-drawer-box" title={data?.dslTemplateMd5}>
          {data?.dslTemplateMd5}
        </div>
      ),
    },
  ];
  const detailMap = [
    {
      type: "请求数",
      value: data?.searchCount,
    },
    {
      type: "查询语句长度",
      value: formatDecimalPoint(data?.dslLenAvg),
    },
    {
      type: "单次响应长度",
      value: formatDecimalPoint(data?.responseLenAvg),
    },
    {
      type: "耗时(ms)",
      value: formatDecimalPoint(data?.totalCostAvg),
    },
    {
      type: "es响应时间(ms)",
      value: formatDecimalPoint(data?.esCostAvg),
    },
    {
      type: "预处理时间(ms)",
      value: formatDecimalPoint(data?.beforeCostAvg),
    },
    {
      type: "总Shard个数",
      value: formatDecimalPoint(data?.totalShardsAvg),
    },
    {
      type: "失败Shard个数",
      value: formatDecimalPoint(data?.failedShardsAvg),
    },
    {
      type: "单次命中数",
      value: formatDecimalPoint(data?.totalHitsAvg),
    },
    {
      type: "查询限流值",
      value: formatDecimalPoint(data?.queryLimit),
    },
    {
      type: "语句类型",
      value: data?.dslType,
    },
    {
      type: "请求方式",
      value: data?.requestType,
    },
    {
      type: "查询方式",
      value: data?.searchType,
    },
  ];

  const handleChangeStatus = () => {
    confirm({
      title: "提示",
      content: `确定${data?.enable || data?.enable === null ? "禁用" : "启用"}查询模板${data?.dslTemplateMd5}？`,
      width: 500,
      okText: "确认",
      cancelText: "取消",
      onOk() {
        changeStatus(data?.dslTemplateMd5, data?.projectId).then((res) => {
          getDetail();
          props.cb();
          message.success("操作成功");
        });
      },
    });
  };

  const renderFooter = () => {
    return (
      <div>
        <Button onClick={handleChangeStatus} className={"dsl-mr-20"}>
          {data?.enable || data?.enable === null ? "禁用" : "启用"}
        </Button>
        <Button type="primary" onClick={showEditLimit}>
          修改限流值
        </Button>
        <Divider
          type="vertical"
          style={{
            height: "16px",
            margin: "0 16px",
          }}
        ></Divider>
      </div>
    );
  };

  const title = (text) => {
    return (
      <div className="dsl-drawer-title">
        <Divider
          type="vertical"
          style={{
            width: "3px",
            height: "12px",
            margin: "0 4px 0 -2px",
            background: "#1473FF",
          }}
        ></Divider>{" "}
        {text}
      </div>
    );
  };

  const contentStyle: any = {
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    color: "rgba(0,0,0,0.87)",
    alignItems: "center",
    fontSize: "14px",
  };
  const labelStyle: any = { width: "127px", justifyContent: "flex-end", fontSize: "14px", color: "rgba(0,0,0,0.6)" };

  return (
    <Drawer
      title={"查询模板详情"}
      visible={props.visible}
      width={getWidth()}
      maskClosable={true}
      extra={renderFooter()}
      onClose={props.onCancel}
      bodyStyle={{ padding: "0px" }}
    >
      <div className="dsl-drawer">
        <EditLimit history={props?.history} appInfo={appInfo} visible={editVisible} record={records} cancel={editCancel} cb={getDetail} />
        {title("基本信息")}
        <Descriptions column={1} labelStyle={labelStyle} contentStyle={contentStyle} style={{ paddingLeft: 24 }}>
          {typeMap.map((item, index) => (
            <DescriptionsItem key={index} label={item.type}>
              {item.value}
            </DescriptionsItem>
          ))}
        </Descriptions>
      </div>
      <div className="dsl-drawer">
        {title("业务信息")}
        <Descriptions column={1} labelStyle={labelStyle} contentStyle={contentStyle} style={{ paddingLeft: 24 }}>
          {detailMap.map((item, index) => (
            <DescriptionsItem key={index} label={item.type}>
              <Tooltip title={item.value}>{item.value}</Tooltip>
            </DescriptionsItem>
          ))}
        </Descriptions>
      </div>
    </Drawer>
  );
};
export default DslDetail;
