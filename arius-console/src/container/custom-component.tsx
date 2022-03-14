import React from "react";
import "styles/custom-component.less";
import "styles/table-filter.less";
import { QuestionCircleOutlined } from "@ant-design/icons";
import { Tooltip, Modal, Button, Popconfirm, Dropdown, Spin, Menu } from "antd";
import { withRouter, Link } from "react-router-dom";
import { urlPrefix } from "constants/menu";
import { ITableBtn } from "component/dantd/dtable";
import { openSourceTip } from "constants/status-map";
import { refreshByCacheKey } from 'react-router-cache-route';

interface INavRouterLinkProps {
  element: JSX.Element | string;
  href: string;
  needToolTip?: boolean;
  onClick?: any;
  removeCacheKey?: any;
}

const hiddenTooltipElement = (className: string) => {
  const navLinkTooltips = document.getElementsByClassName(className);
  for (let i = 0; i < navLinkTooltips.length; i++) {
    (navLinkTooltips[i] as any).style = 'top: -1000px; left: -1000px';
  }
};

export const NavRouterLink = withRouter<any, any>(
  (props: INavRouterLinkProps) => {
    const overlayClassName = 'nav-link-tooltip';

    return (
      <Link
        onClick={() => {
          props.onClick && props.onClick();
          props.needToolTip && hiddenTooltipElement(overlayClassName);
          props.removeCacheKey && refreshByCacheKey(props.removeCacheKey);
        }}
        to={props.href}
      >
        {props.needToolTip ? (
          <Tooltip overlayClassName={overlayClassName} placement="bottomLeft" title={props.element}>
            {props.element}
          </Tooltip>
        ) : (
          props.element
        )}
      </Link>
    );
  }
);

export const showSubmitOrderSuccessModal = (params: {
  title: string;
  id: number;
  onOk?: any;
}) => {
  Modal.success({
    onOk: params.onOk
      ? params.onOk
      : () => {
        // history.pushState(null, '', window.location.pathname);
        // goToTargetPage(window.location.pathname);
        window.location.reload();
      },
    okText: "确定",
    title: "提交成功！",
    content: (
      <>
        <div className="order-success">
          <span>
            {params.title}已提交！可至“工单任务” &gt; “我的申请”中查看工单详情
          </span>
          <br />
          <span>
            工单标题（ID）：
            <a href={`/work-order/my-approval/detail?orderId=${params.id}`}>
              {params.title}（{params.id}）
            </a>
          </span>
        </div>
      </>
    ),
  });
};

export const showSubmitTaskSuccessModal = (params: {
  title: string;
  id: number;
  onOk?: any;
}) => {
  Modal.success({
    onOk: params.onOk
      ? params.onOk
      : () => {
        window.location.reload();
      },
    okText: "确定",
    title: "提交成功！",
    content: (
      <>
        <div className="order-success">
          <span>
            xxxx逻辑索引主从切换(任务ID：1001)创建成功！可至“工单任务” &gt;
            “任务列表”模块查看详情。
          </span>
          <br />
          <span>
            任务（ID）：
            <a href={`${urlPrefix}/user/task/detail?id=359`}>
              {params.title}（{params.id}）
            </a>
          </span>
        </div>
      </>
    ),
  });
};

export const CancelActionModal = (props: { routeHref: string, history?: any, cb?: Function }) => {
  const onHandleCancel = (routeHref: string, history?: any, cb?: Function) => {
    Modal.confirm({
      title: "确定取消？",
      content: "取消后当前填写内容将失效，请谨慎操作",
      okText: "确定",
      cancelText: "取消",
      icon: <QuestionCircleOutlined className="question-icon" />,
      onOk: () => {
        const url = window?.location?.pathname
        setTimeout(() => {
          if (history) {
            history.push(routeHref.replace('/es', ''));
          } else {
            window.location.href = routeHref;
          }
          if (cb) {
            cb(url);
          }
        }, 500)
      },
    });
  };

  return <Button onClick={() => onHandleCancel(props.routeHref, props.history, props.cb)}>取消</Button>;
};

export const renderOperationBtns = (btns: ITableBtn[], record: any) => {
  const freeBtns = btns.length <= 3 ? btns : [].concat(btns).splice(0, 2);
  const leftBtns = [].concat(btns).splice(2);

  if (!freeBtns.length) {
    return <a>{"无"}</a>;
  }

  return (
    <>
      <span className="table-operation">
        {freeBtns.map((item, index) => {
          if (item.invisible) return null;

          if (item.isOpenUp)
            return (
              <Tooltip key={index} title={item.tip || openSourceTip}>
                <a key={index} style={{ color: "#bfbfbf" }}>
                  {" "}
                  {item.label}{" "}
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
              <a
                type="javascript;"
                key={index}
                onClick={() => item.clickFunc(record)}
              >
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
        {btns.length > 3 ? <MoreBtns btns={leftBtns} data={record} /> : null}
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
                <Button
                  type={item.type || "primary"}
                  key={index}
                  disabled={true}
                  onClick={item.clickFunc}
                >
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
              <Button
                type={item.type || "primary"}
                key={index}
                onClick={item.clickFunc}
              >
                {item.label}
              </Button>
            );
          }
        })}
        {btns.length > 3 ? (
          <DetailMoreBtns btns={leftBtns} data={record} />
        ) : null}
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
  const flag = btns.length === btns.filter(item => item.isOpenUp).length;

  const btnsMenu = (
    <ul className="dropdown-menu">
      {btns.map((v, index) => {
        if (v.invisible) return null;

        if (v.isOpenUp)
          return (
            <li key={index} className="es-open-theme">
              <Tooltip title={openSourceTip}>
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
            <li
              key={index}
              onClick={() => v.clickFunc(data)}
              className="es-open-theme"
            >
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
    <Dropdown
      key="2"
      overlay={btnsMenu}
      trigger={["hover"]}
      placement="bottomLeft"
    >
      <span className="es-open-theme ml-10">
        <a style={{ color: flag ? "#bfbfbf" : "none" }}>更多</a>
      </span>
    </Dropdown>
  );
};

export const DetailMoreBtns = (props: IMoreBtnsProps) => {
  const { btns, data } = props;

  // 当下拉框中选项都禁用时，将 更多 文字置灰
  const flag = btns.length === btns.filter(item => item.isOpenUp).length;

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
    <Dropdown
      key="2"
      overlay={btnsMenu}
      trigger={["click", "hover"]}
      placement="bottomLeft"
    >
      <Button className={flag ? "disabled-button" : ""}>更多</Button>
    </Dropdown>
  );
};

export const LoadingBlock = (props: { loading: boolean; height?: number }) => {
  return (
    <Spin
      style={{ height: props.height, lineHeight: props.height }}
      spinning={props.loading}
      className="loading-content"
    />
  );
};
