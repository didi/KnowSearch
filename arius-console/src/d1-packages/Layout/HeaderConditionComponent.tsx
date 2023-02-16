import React, { useState, useEffect } from "react";
import { Dropdown, Menu, Divider, Popover, Drawer, Select, Input } from "knowdesign";
import _ from "lodash";
import { FormattedMessage } from "react-intl";
import { prefixCls } from "./config";
import { MsgPanel } from "./MsgPanel";
import ProjectSelect from "./ProjectSelect";
import { CustomAppDropDown } from "./CustomProjectSelect";
import UseCenter from "./UseCenter";
import { isSuperApp, getCookie } from "lib/utils";
import { getUser } from "api/logi-security";
import "./index.less";

const HeaderConditionComponent = (params: any, index: number, props: any) => {
  const cPrefixCls = `${prefixCls}-layout`;
  const { setLeftIndex, leftIndex, setHeaderClick, history } = props;

  const [visible, setVisible] = useState(false);
  const [fullScreen, setFullScreen] = useState(false);
  const [userInfo, setUserInfo] = React.useState({} as any);
  const [trigger, setTrigger] = React.useState(["click"] as Array<"click" | "hover" | "contextMenu">);

  React.useEffect(() => {
    params.scene === "user" && getData();
  }, []);

  React.useEffect(() => {
    visible && getData();
  }, [visible]);

  const getData = () => {
    if (!getCookie("userId")) return;
    getUser(+getCookie("userId")).then((res) => {
      setUserInfo(res || {});
    });
  };

  const toggleFullscreen = () => {
    const docu = document as any;
    if (!docu.fullscreenElement && /* alternative standard method */ !docu.mozFullScreenElement && !docu.webkitFullscreenElement) {
      if (docu.documentElement.requestFullscreen) {
        docu.documentElement.requestFullscreen();
      } else if (docu.documentElement.mozRequestFullScreen) {
        docu.documentElement.mozRequestFullScreen();
      } else if (docu.documentElement.webkitRequestFullscreen) {
        docu.documentElement.webkitRequestFullscreen((Element as any).ALLOW_KEYBOARD_INPUT);
      }
    } else {
      if (docu.cancelFullScreen) {
        docu.cancelFullScreen();
      } else if (docu.mozCancelFullScreen) {
        docu.mozCancelFullScreen();
      } else if (docu.webkitCancelFullScreen) {
        docu.webkitCancelFullScreen();
      }
    }
  };
  function escFullScreen() {
    setFullScreen((fullScreen) => !fullScreen);
  }
  useEffect(() => {
    // 监听退出全屏事件 --- chrome 用 esc 退出全屏并不会触发 keyup 事件
    document.addEventListener("webkitfullscreenchange", escFullScreen); /* Chrome, Safari and Opera */
    document.addEventListener("mozfullscreenchange", escFullScreen); /* Firefox */
    document.addEventListener("fullscreenchange", escFullScreen); /* Standard syntax */
    document.addEventListener("msfullscreenchange", escFullScreen); /* IE / Edge */
    return () => {
      //销毁时清除监听
      document.removeEventListener("webkitfullscreenchange", escFullScreen);
      document.removeEventListener("mozfullscreenchange", escFullScreen);
      document.removeEventListener("fullscreenchange", escFullScreen);
      document.removeEventListener("MSFullscreenChange", escFullScreen);
    };
  }, []);

  // 消息通知
  const componentMsg = (item) => {
    return (
      <Dropdown placement="bottomRight" overlay={<MsgPanel />} trigger={["hover"]} key={index}>
        <svg className={`${prefixCls}-layout-menus-icon`} aria-hidden="true">
          <use xlinkHref={item.icon}></use>
        </svg>
      </Dropdown>
    );
  };

  // 项目下拉
  const componentProjectSelect = () => {
    return (
      <ProjectSelect key={index} value={props.currentProject} onProjectChange={(value)=> props.setCurrentProject(value, { history })} projectList={props.projectList} />
    );
  };

  //项目drop
  const componentProjectDrop = () => {
    return (
      <CustomAppDropDown
        key={index}
        setCurrentProject={(value)=>props.setCurrentProject(value, { history })}
        projectList={props.projectList}
        currentProject={props.currentProject}
      />
    );
  };

  // 全屏显示
  const componentFullscreen = () => {
    return (
      <span key={index} className={`icon iconfont ${fullScreen ? "icon-tuichuquanju" : "icon-quanju1"}`} onClick={toggleFullscreen}></span>
    );
  };

  // 个人信息
  const componentUserCenter = () => {
    const handleVisibleChange = (newVisible: boolean) => {
      setVisible(newVisible);
    };
    const close = () => {
      setVisible(false);
    };

    return (
      <span className={`${cPrefixCls}-username`} key={index}>
        <Dropdown
          visible={visible}
          onVisibleChange={handleVisibleChange}
          arrow
          placement="bottomRight"
          overlay={<UseCenter userInfo={userInfo} getData={getData} logout={props.logout} close={close} />}
          trigger={trigger}
          overlayClassName="user-wrapper dcloud-dropdown dcloud-dropdown-show-arrow dcloud-dropdown-placement-bottomRight"
        >
          <div className="user-trigger">
            <div className="user-icon">
              <span className={`icon iconfont icontouxiang`}></span>
            </div>
            <span>Hi,{userInfo?.userName || "-"}</span>
          </div>
        </Dropdown>
      </span>
    );
  };

  const renderInput = () => {
    return <Input allowClear key={index} {...params.componentProps} />;
  };

  const renderDivider = () => {
    return <Divider key={index} className={`${cPrefixCls}-header-right-divider`} type="vertical" />;
  };

  const renderDropdown = () => {
    if (params.scene === "user") {
      return componentUserCenter();
    }
    if (params.scene === "project") {
      return componentProjectDrop();
    }
    if (params.scene === "admin") {
      return renderAdmin();
    }
    return (
      <Dropdown placement="bottomRight" overlay={params.overlay} key="other">
        {params.content}
      </Dropdown>
    );
  };

  const renderSelect = () => {
    if (params.scene === "project") {
      return componentProjectSelect;
    }
    return (
      <Select allowClear={true} placeholder="请选择" key={index}>
        {params.options.map((item, index) => {
          return (
            <Select.Option key={item.value} value={item.value}>
              {item.name}
            </Select.Option>
          );
        })}
      </Select>
    );
  };

  const renderLink = () => {
    return (
      <div className={`${cPrefixCls}-header-right-links`} key={index}>
        {params.options.map((item: { icon: string | undefined; href: string; text: string }, index: number) => {
          return item.icon ? (
            <a href={item.href} key={index}>
              <Popover content={item.text}>
                <svg className={`${cPrefixCls}-header-menus-icon`} aria-hidden="true">
                  <use xlinkHref={item.icon}></use>
                </svg>
              </Popover>
            </a>
          ) : (
            <a href={item.href} key={index}>
              {item.text}
            </a>
          );
        })}
      </div>
    );
  };

  const renderIcon = () => {
    if (params.scene === "fullscreen") {
      return componentFullscreen();
    }
    return (
      <div className={`${cPrefixCls}-header-right-icons`} key={index}>
        {params.options?.map((item: { icon: string; scene: string }, index: number) => {
          return item.scene == "msg" ? (
            componentMsg(item)
          ) : (
            <svg className={`${prefixCls}-layout-menus-icon`} aria-hidden="true" key={index}>
              <use xlinkHref={item.icon}></use>
            </svg>
          );
        })}
      </div>
    );
  };

  const renderAdmin = () => {
    // if (!isSuperApp()) return;
    let menu = (
      <ul className="admin-select">
        {params?.children.map((item, index) => (
          <li
            key={index}
            className={item.key === leftIndex ? "active" : ""}
            onClick={() => {
              setLeftIndex(item.key);
              setHeaderClick(item.key, { history });
            }}
          >
            <span className={`icon iconfont ${item.icon}`}></span>
            <span>{item.label}</span>
          </li>
        ))}
      </ul>
    );
    return (
      <Dropdown key="admin-dropdown" arrow placement="bottomCenter" overlay={menu} trigger={["click"]} overlayClassName="admin-wrapper">
        <div className="admin-container">
          <span className={`icon iconfont ${params?.children?.[leftIndex]?.icon}`}></span>
          <span>{params?.children?.[leftIndex]?.label}</span>
          <div className={`select-icon`}>
            <span className={`icon iconfont iconRight`}></span>
          </div>
        </div>
      </Dropdown>
    );
  };

  const headerConditionMap = {
    input: renderInput,
    select: renderSelect,
    dropdown: renderDropdown,
    link: renderLink,
    icon: renderIcon,
    divider: renderDivider,
  };

  return headerConditionMap[params.type]();
};

export default HeaderConditionComponent;
