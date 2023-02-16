import React from "react";
import "styles/custom-component.less";
import "styles/table-filter.less";
import { QuestionCircleOutlined } from "@ant-design/icons";
import { Tooltip, Modal, Button, Popconfirm, Dropdown, Spin, Menu, Tag, Popover, Progress } from "antd";
import { withRouter, Link } from "react-router-dom";
import { ITableBtn } from "component/dantd/dtable";
import { openSourceTip } from "constants/status-map";
import { refreshByCacheKey } from "react-router-cache-route";
import { bytesUnitFormatter } from "../lib/utils";
import { TooltipPlacement } from "antd/es/tooltip";

interface INavRouterLinkProps {
  element: JSX.Element | string;
  href: string;
  needToolTip?: boolean;
  onClick?: any;
  removeCacheKey?: any;
  maxShowLength?: number;
  style?: any;
}

const hiddenTooltipElement = (className: string) => {
  const navLinkTooltips = document.getElementsByClassName(className);
  for (let i = 0; i < navLinkTooltips.length; i++) {
    (navLinkTooltips[i] as any).style = "top: -1000px; left: -1000px";
  }
};

export const NavRouterLink = withRouter<any, any>((props: INavRouterLinkProps) => {
  const overlayClassName = "nav-link-tooltip";
  // 限制展示固定字符
  const renderElement = () => {
    if (props.maxShowLength && typeof props.element === "string") {
      if (props.element.length <= props.maxShowLength) {
        return props.element;
      }
      return props.element.slice(0, props.maxShowLength) + "...";
    } else {
      return props.element;
    }
  };
  return (
    <Link
      onClick={() => {
        props.onClick && props.onClick();
        props.needToolTip && hiddenTooltipElement(overlayClassName);
        props.removeCacheKey && refreshByCacheKey(props.removeCacheKey);
      }}
      to={props.href}
      style={props?.style}
    >
      {props.needToolTip ? (
        <Tooltip overlayClassName={overlayClassName} placement="bottomLeft" title={props.element}>
          {renderElement()}
        </Tooltip>
      ) : (
        renderElement()
      )}
    </Link>
  );
});

export const showSubmitOrderSuccessModal = (params: { title: string; id: number; onOk?: any; width?: number }, history?: any) => {
  Modal.success({
    onOk: params.onOk && params.onOk,
    okText: "确定",
    title: "提交成功！",
    width: params.width,
    content: (
      <>
        <div className="order-success">
          <span>{params.title}已提交！可至“工单任务” &gt; “我的申请”中查看工单详情</span>
          <br />
          <span>
            工单标题（ID）：
            <Button
              type="link"
              style={{ padding: 0 }}
              onClick={() => {
                Modal.destroyAll();
                history.push(`/work-order/my-application/detail?orderId=${params.id}`);
              }}
            >
              {params.title}（{params.id}）
            </Button>
          </span>
        </div>
      </>
    ),
  });
};

export const showSubmitTaskSuccessModal = (
  params: {
    title: string;
    id: number;
    onOk?: any;
    businessKey: string;
    taskType: number;
    status: string;
    expandData: string;
  },
  history?: any
) => {
  Modal.success({
    onOk: params.onOk && params.onOk,
    okText: "确定",
    title: "提交成功！",
    content: (
      <>
        <div className="order-success">
          <span>{params.title}已提交！可至“任务中心”查看任务详情</span>
          <br />
          <span>
            任务标题（ID）：
            <Button
              type="link"
              style={{ padding: 0 }}
              onClick={() => {
                Modal.destroyAll();
                history.push(
                  `/work-order/task/detail?title=${params.title}&taskid=${params.id}&id=${Number(params.businessKey)}&type=${
                    params.taskType
                  }&status=${params.status}&dcdr_info=${encodeURI(`${params.expandData}`)}`
                );
              }}
            >
              {params.title}（{params.id}）
            </Button>
          </span>
        </div>
      </>
    ),
  });
};

export const CancelActionModal = (props: { routeHref: string; history?: any; cb?: Function }) => {
  const onHandleCancel = (routeHref: string, history?: any, cb?: Function) => {
    Modal.confirm({
      title: "确定取消？",
      content: "取消后当前填写内容将失效，请谨慎操作",
      okText: "确定",
      cancelText: "取消",
      icon: <QuestionCircleOutlined className="question-icon" />,
      onOk: () => {
        const url = window?.location?.pathname;
        setTimeout(() => {
          if (history) {
            history.push(routeHref.replace("/es", ""));
          } else {
            window.location.href = routeHref;
          }
          if (cb) {
            cb(url);
          }
        }, 500);
      },
    });
  };

  return <Button onClick={() => onHandleCancel(props.routeHref, props.history, props.cb)}>取消</Button>;
};

export const renderOperationBtns = (btns: ITableBtn[], record: any, limit = 4) => {
  btns = btns.filter((item) => !item.invisible);
  const freeBtns = btns.length < limit ? btns : [].concat(btns).splice(0, 2);
  const leftBtns = [].concat(btns).splice(2);

  if (!freeBtns.length) {
    return <>-</>;
  }

  return (
    <>
      <span className="table-operation">
        {freeBtns.map((item, index) => {
          if (item.isOpenUp)
            return (
              <Tooltip key={index} title={item.tip || openSourceTip}>
                <a key={index} style={{ color: "#bfbfbf" }}>
                  {item.label}
                </a>
              </Tooltip>
            );

          if (item.isRouterNav) {
            return <span key={index}>{item.label}</span>;
          }

          if (item.needConfirm) {
            return (
              <Popconfirm
                key={index}
                title={`确认${item.confirmText}?`}
                onConfirm={() => item.clickFunc(record)}
                okText={item.confirmText}
                cancelText="取消"
              >
                <a type="javascript;">{item.label}</a>
              </Popconfirm>
            );
          }

          if (item.clickFunc) {
            return (
              <a type="javascript;" key={index} onClick={() => item.clickFunc(record)}>
                {item.label}
              </a>
            );
          }
          return (
            <span key={index}>
              <a>{item.label}</a>
            </span>
          );
        })}
        {btns.length >= limit ? <MoreBtns btns={leftBtns} data={record} /> : null}
      </span>
    </>
  );
};

export const renderMoreBtns = (btns: ITableBtn[], record: any) => {
  const freeBtns = btns.length < 3 ? btns : [].concat(btns).splice(0, 3);
  const leftBtns = [].concat(btns).splice(3);

  return (
    <>
      <span className="table-header-operation">
        {freeBtns.map((item, index) => {
          if (item.invisible) return null;
          if (item.isOpenUp)
            return (
              <Tooltip key={index} title={openSourceTip}>
                <Button type={item.type || "primary"} key={index} disabled={true} onClick={item.clickFunc}>
                  {item.label}
                </Button>
              </Tooltip>
            );

          if (item.isRouterNav) {
            return (
              <Button type={item.type || "primary"} key={index}>
                {item.label}
              </Button>
            );
          }

          if (item.clickFunc) {
            return (
              <Button type={item.type || "primary"} key={index} onClick={item.clickFunc}>
                {item.label}
              </Button>
            );
          }
        })}
        {btns.length > 3 ? <DetailMoreBtns btns={leftBtns} data={record} /> : null}
      </span>
    </>
  );
};

interface IMoreBtnsProps {
  btns: ITableBtn[];
  data: object;
}

export const MoreBtns = (props: IMoreBtnsProps) => {
  const { btns, data } = props;

  // 当下拉框中选项都禁用时，将 更多 文字置灰
  const flag = btns.length === btns.filter((item) => item.isOpenUp).length;

  const btnsMenu = (
    <ul className="dropdown-menu">
      {btns.map((v, index) => {
        if (v.invisible) return null;

        if (v.isOpenUp)
          return (
            <li key={index} className="es-open-theme">
              <Tooltip title={v.tip || openSourceTip}>
                <a style={{ color: "#bfbfbf" }}> {v.label} </a>
              </Tooltip>
            </li>
          );

        if (v.isRouterNav) {
          return (
            <li key={index} className="es-open-theme">
              {v.label}
            </li>
          );
        }
        if (v.clickFunc) {
          return (
            <li key={index} onClick={() => v.clickFunc(data)} className="es-open-theme">
              <a>{v.label}</a>
            </li>
          );
        }
        return (
          <li key={index} className="es-open-theme">
            <a>{v.label}</a>
          </li>
        );
      })}
    </ul>
  );
  return (
    <Dropdown key="2" overlay={btnsMenu} trigger={["hover"]} placement="bottomLeft">
      <span className="es-open-theme ml-10">
        <a style={{ color: flag ? "#bfbfbf" : "none" }}>更多</a>
      </span>
    </Dropdown>
  );
};

export const DetailMoreBtns = (props: IMoreBtnsProps) => {
  const { btns, data } = props;

  // 当下拉框中选项都禁用时，将 更多 文字置灰
  const flag = btns.length === btns.filter((item) => item.isOpenUp).length;

  const btnsMenu = (
    <Menu>
      {btns.map((v, index) => {
        if (v.invisible) return null;

        if (v.isOpenUp) {
          return (
            <Menu.Item key={index}>
              <Tooltip title={openSourceTip}>
                <a style={{ color: "#bfbfbf" }}> {v.label} </a>
              </Tooltip>
            </Menu.Item>
          );
        }

        if (v.clickFunc) {
          return (
            <Menu.Item key={index}>
              <a style={{ color: "#526ecc" }} onClick={() => v.clickFunc()}>
                {v.label}
              </a>
            </Menu.Item>
          );
        }
        return (
          <Menu.Item key={index}>
            <a>{v.label}</a>
          </Menu.Item>
        );
      })}
    </Menu>
  );
  return (
    <Dropdown key="2" overlay={btnsMenu} trigger={["click", "hover"]} placement="bottomLeft">
      <Button className={flag ? "disabled-button" : ""}>更多</Button>
    </Dropdown>
  );
};

export const LoadingBlock = (props: { loading: boolean; height?: number }) => {
  return <Spin style={{ height: props.height, lineHeight: props.height }} spinning={props.loading} className="loading-content" />;
};

export const renderAttributes = (params: {
  data: any;
  type?: string;
  limit?: number;
  splitType?: string;
  placement?: TooltipPlacement;
}) => {
  const { data, type = ",", limit = 2, splitType = "；", placement } = params;
  let attrArray = data;
  if (!Array.isArray(data) && data) {
    attrArray = data.split(type);
  }
  const showItems = attrArray.slice(0, limit) || [];
  const hideItems = attrArray.slice(limit, attrArray.length) || [];
  let content = hideItems.map((item, index) => (
    <Tag key={index} className="tag-blue">
      {item}
    </Tag>
  ));

  return (
    <div className="attribute-content">
      {showItems.length > 0 ? showItems.join(splitType) : "-"}
      {hideItems.length > 0 && (
        <Popover placement={placement || "bottomRight"} content={content} overlayClassName="attribute-tag">
          <Tag className="tag-blue tag-num">共{attrArray.length}个</Tag>
        </Popover>
      )}
    </div>
  );
};

export const renderDiskRate = (diskInfo: { diskUsagePercent: number; diskUsage: number; diskTotal: number }) => {
  const num = Number((diskInfo.diskUsagePercent * 100).toFixed(2));
  let strokeColor: string;
  let yellow = "#F4A838";
  let red = "#df6d62";
  if (num > 90) {
    strokeColor = red;
  } else if (num > 70) {
    strokeColor = yellow;
  } else {
    strokeColor = "#1473FF";
  }

  return (
    <div style={{ position: "relative" }} className="process-box">
      <Progress className="process-box-" percent={num} size="small" strokeColor={strokeColor} style={{ width: "100%" }} showInfo={false} />
      <div className="process-box-foot">
        <span className="process-box-foot-byte">
          {bytesUnitFormatter(diskInfo.diskUsage || 0)}/{bytesUnitFormatter(diskInfo.diskTotal || 0)}
        </span>
        <span className="process-box-foot-per">{num + "%"}</span>
      </div>
    </div>
  );
};

export const renderMoreText = (text, maxShowLength) => {
  if (text?.length <= maxShowLength) {
    return text || "-";
  } else {
    return <Tooltip title={text}>{text?.slice(0, maxShowLength) + "..."}</Tooltip>;
  }
};
