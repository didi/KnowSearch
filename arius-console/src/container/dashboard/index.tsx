import React, { memo, useState, useEffect, useRef } from "react";
import { useSelector, useDispatch } from "react-redux";
import { TAB_LIST_KEY } from "./config";
import Operation from "./Operation";
import Url from "lib/url-parser";
import { setDashBoard } from "../../actions/dashBoard";
import { Dropdown, Button, Menu } from "antd";
import { DownOutlined } from "@ant-design/icons";

import "./index.less";
import "./../indicators-kanban/style";

export const DashBoard = () => {
  // const dispatch = useDispatch();
  // useEffect(() => {
  //   const url = Url().search;
  //   if (url && url.tabs) {
  //     dispatch(
  //       setDashBoard({
  //         tabs: url.tabs,
  //       })
  //     );
  //   }
  // }, [])
  // const { tabs } = useSelector(
  //   (state) => ({
  //     tabs: (state as any).dashBoard.tabs,
  //   }),
  // );

  const handleMenuClick = (target) => {
    // window.location.search = `tabs=${key}`
    // dispatch(
    //   setDashBoard({
    //     tabs: target.key,
    //   })
    // );
  };

  const menu = (
    <Menu onClick={handleMenuClick}>
      <Menu.Item key={TAB_LIST_KEY.Operation}>运维视角</Menu.Item>
      <Menu.Item key={TAB_LIST_KEY.operate}>运营视角</Menu.Item>
    </Menu>
  );
  return (
    <div className="hash-menu-container dashboard">
      {/* <Dropdown overlay={menu} disabled={true}>
        <div className="dashboard-changepage ant-btn-primary">
          <div className="dashboard-changepage-text">运维视角 <DownOutlined className="dashboard-changepage-icon" /></div>
        </div>
      </Dropdown> */}
      <Operation />
    </div>
  );
};
