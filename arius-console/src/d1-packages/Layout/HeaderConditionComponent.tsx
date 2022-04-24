import React, { useState } from "react";
import { Dropdown, Menu, Divider, Popover, Drawer, Select, Input } from "antd";
import _ from 'lodash';
import { FormattedMessage } from "react-intl";
import { prefixCls } from "./config";
import { MsgPanel } from "./MsgPanel";
import ProjectSelect from "./ProjectSelect";
import { CustomAppDropDown } from "./CustomProjectSelect";

const HeaderConditionComponent = (params: any, index: number, props: any) => {
  const cPrefixCls = `${prefixCls}-layout`;
  const { UserCenter } = props

  const [visible, setVisible] = useState(false);
  const [dispname, setDispname] = useState("");
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
    return <ProjectSelect key={index} value={props.currentProject} onProjectChange={props.setCurrentProject} projectList={props.projectList} />;
  };

  //项目drop
  const componentProjectDrop = () => {
    return (
      <CustomAppDropDown
        key={index}
        setCurrentProject={props.setCurrentProject}
        projectList={props.projectList}
        currentProject={props.currentProject}
      />
    );
  };

  // 文档中心
  const componentDocCenter = () => {
    return (
      <Dropdown
        key={index}
        placement="bottomRight"
        overlay={
          <Menu style={{ width: 100 }}>
            <Menu.Divider />
            {params.options.map((item, index) => {
              return (
                <Menu.Item key={index}>
                  <a href={`${item.link}`}>
                    <FormattedMessage id={`${item.id}`} />
                  </a>
                </Menu.Item>
              );
            })}
          </Menu>
        }
      >
        <span className={`${cPrefixCls}-doc`}>
          <span style={{ paddingRight: 5 }}>
            <FormattedMessage id="doc-center" />
          </span>
          <svg className={`${prefixCls}-layout-menus-icon small`} aria-hidden="true">
            <use xlinkHref="#iconxuanzekuangzhankai"></use>
          </svg>
        </span>
      </Dropdown>
    );
  };

  // 个人信息
  const componentUserCenter = () => {

    return (
      <>
        <Dropdown
          key={index}
          placement="bottomRight"
          overlay={
            <Menu style={{ width: 125 }}>
              <Menu.Item>
                <a onClick={() => props.logout()}>
                  <FormattedMessage id="logout" />
                </a>
              </Menu.Item>
            </Menu>
          }
        >
          <span className={`${cPrefixCls}-username`}>
            <img
              // src={_.get(props.feConf, 'header.userIconSrc')}
              src={require('../../assets/avatars.png')}
              alt=""
              onClick={() => params.showUserDrawer ? setVisible(true) : null}
            />
            <span style={{ paddingRight: 5 }}>{dispname}</span>
            <svg className={`${prefixCls}-layout-menus-icon`} aria-hidden="true">
              <use xlinkHref="#iconxuanzekuangzhankai"></use>
            </svg>
          </span>
        </Dropdown>
        <Drawer
          title={<FormattedMessage id="user-center" />}
          placement="right"
          onClose={() => {
            setVisible(false);
          }}
          visible={visible}
          mask={false}
          width={516}
          className={"user-center-drawer"}
        >
          <UserCenter />
        </Drawer>
      </>
    );
  };

  const renderInput = () => {
    return <Input key={index} {...params.componentProps} />;
  };

  const renderDivider = () => {
    return <Divider key={index} className={`${cPrefixCls}-header-right-divider`} type="vertical" />;
  };

  const renderDropdown = () => {
    if (params.scene === "doc") {
      return componentDocCenter();
    }
    if (params.scene === "user") {
      return componentUserCenter();
    }
    if (params.scene === "project") {
      return componentProjectDrop();
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
    return (
      <div className={`${cPrefixCls}-header-right-icons`} key={index}>
        {params.options.map((item: { icon: string; scene: string }, index: number) => {
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
