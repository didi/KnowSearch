import * as React from "react";
import { Badge, Menu, message, Popconfirm, Tabs } from "antd";
import {
  CloseOutlined,
  CloseCircleOutlined,
  LeftOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { withRouter, Link } from "react-router-dom";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import "./index.less";
import { dealPathname } from "lib/utils";
import _ from "lodash";
import { pageEventList } from './pageEvent';

interface ITab {
  label: string;
  href: string;
  key: string;
  show: boolean;
}

export const RouterTabs = withRouter<any, any>((props: { tabList: ITab[], removePaths: string[], permissions, department}) => {
  const { sliderMenuCollapsed } = useSelector(
    (state) => ({
      sliderMenuCollapsed: (state as any).sliderMenu.sliderMenuCollapsed,
    }),
    shallowEqual
  );

  const dispatch = useDispatch();
  const tabs = props.tabList || (window as any).currentOpenRouterList;
  const pathname = dealPathname(location.pathname);
  const [currentKey, setCurrentKey] = React.useState(
    `menu${pathname.split("/")?.join(".")}`
  );
  const [tabList, setTabList] = React.useState(tabs);
  const [isShowLeft, setIsShowLeft] = React.useState(false);
  const [isShowRight, setIsShowRight] = React.useState(false);
  const isFirstRender = React.useRef(true);
  const { removePaths } = props;

  React.useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }

    // 过滤掉现有 tab 中的详情页
    const newTabList = (window as any).currentOpenRouterList.filter(item => !props.permissions[item.key]);

    (window as any).currentOpenRouterList = newTabList;

    // 判断当前页面被选中页面是否是详情页
    const currentHref = props.permissions[currentKey];
    
    if(currentHref) {
      // 当前页面是详情页，跳转到相应的列表页
      (props as any).history.push(currentHref);
    }
  }, [props.department]);

  React.useEffect(() => {
    setCurrentKey(`menu${pathname.split("/")?.join(".")}`);
  }, [location.pathname]);

  React.useEffect(() => {
    setTabList(tabs);
  }, [tabs]);

  const onClickArrow = _.throttle((type = '') => {
    const navWrap = document.querySelector(
      "#tabs-list .ant-tabs-nav-wrap"
    ) as HTMLElement;
    const navList = document.querySelector(
      "#tabs-list .ant-tabs-nav-list"
    ) as HTMLElement;
    const navTab = document.querySelector(
      "#tabs-list .ant-tabs-tab"
    ) as HTMLElement;

    const navWrapWidth = Number(navWrap.offsetWidth);
    const navListWidth = Number(navList.offsetWidth);
    const tagWidth = navTab ? Number(navTab.offsetWidth) + 12 : 106;
    const translateLen = Number(
      (navList as any).style.transform
        .split("(")[1]
        .split(",")[0]
        .replace("px", "")
    );
    const navListScrollLeft = Number(translateLen * -1);
    const navListScrollRight = Number(
      navListWidth - navWrapWidth - navListScrollLeft
    );

    const tag3 = tagWidth * 3;

    if (type) {
      // 判断点击的是左箭头还是右箭头
      if (type === "left") {
        // 判断盒子是否被隐藏
        if (navListScrollLeft <= 0) {
          return;
        }
        // 如果隐藏长度大于等于 3 个 tab，大于移动 3 个 tab 的长度，否则移动剩余长度
        if (navListScrollLeft >= tag3) {
          navList.style.transform = `translate(${translateLen + tag3}px, 0)`;
        } else {
          navList.style.transform = `translate(0, 0)`;
          // 左边没有隐藏部分删除阴影
          navWrap.classList.remove('ant-tabs-nav-wrap-ping-left');
        }
        // 右边有隐藏部分添加阴影
        navWrap.classList.add('ant-tabs-nav-wrap-ping-right');
      } else if (type === "right") {
        if (navListScrollRight <= 0) {
          return;
        }
        if (navListScrollRight >= tag3) {
          navList.style.transform = `translate(${translateLen - tag3}px, 0)`;
        } else {
          navList.style.transform = `translate(${navWrapWidth - navListWidth
            }px, 0)`;
          navWrap.classList.remove('ant-tabs-nav-wrap-ping-right');
        }
        navWrap.classList.add('ant-tabs-nav-wrap-ping-left');
      }
      onClickArrow();
    } else {
      // 判断是否显示和禁用页面的左右箭头
      navListScrollLeft <= 0 ? setIsShowLeft(false) : setIsShowLeft(true);
      navListScrollRight <= 0 ? setIsShowRight(false) : setIsShowRight(true);
    }
  }, 300);

  React.useEffect(() => {
    onClickArrow();
  }, [tabList]);

  // 关闭页面逻辑
  React.useEffect(() => {
    try {
      // 做判断防止是空值
      if (!Array.isArray(removePaths) || !removePaths.length || !removePaths[0]) return;
      let newTabs = Array.from(tabList);
      for (const path of removePaths) {
        // 去空逻辑 
        newTabs = newTabs.filter((tab) => tab.key !== `menu${path.split("/")?.join(".")}`);
      }
      // 如果没有页面了返回首页
      if (newTabs && !newTabs.length) {
        newTabs = [
          {
            "key": "menu.es.cluster.physics",
            "label": "物理集群",
            "href": "/cluster/physics",
            "show": true
          }
        ]
      }
      setTabList(newTabs);
      (window as any).currentOpenRouterList = newTabs;
      removePaths.indexOf(window.location.pathname) > -1 ? (props as any)?.history?.push('/cluster/physics') : null;
      // 恢复值
      window.localStorage.setItem('removePath', '');
    } catch (err) {
      console.log(err)
    }
  }, [removePaths]);

  const onChange = (key) => {
    setCurrentKey(key);
  };

  // 为了支持点击关闭时对特定页面做提示拆分关闭方法
  const OneRemoveIcon = (key: string) => {
    const changeTabList = tabList.filter((item) => item.key !== key);
    setTabList(changeTabList);

    if (key === currentKey) {
      setCurrentKey(changeTabList[0].key);
      (props as any).history.push(changeTabList[0].href);
    }

    (window as any).currentOpenRouterList = changeTabList;
  }

  const onClickOneRemoveIcon = (e, key: string) => {
    e.stopPropagation();
    if (pageEventList && pageEventList[key]) {
      pageEventList[key](OneRemoveIcon, key, dispatch);
    } else {
      OneRemoveIcon(key)
    }
  };

  const onClickRemoveIcon = () => {
    const changeTabList = tabList.filter((item) => item.key === currentKey);
    setTabList(changeTabList);
    (window as any).currentOpenRouterList = changeTabList;
  };

  return (
    <>
      <div
        className={
          sliderMenuCollapsed ? "router-tabs collapsed" : "router-tabs"
        }
      >
        <Tabs
          activeKey={currentKey}
          onChange={onChange}
          type="card"
          id="tabs-list"
          onTabScroll={onClickArrow}
          onTabClick={(key) => {
            const index = tabList.findIndex(item => item.key === key);
            (props as any).history.push(tabList[index].href);
          }}
        >
          {tabList.map((tab) => (
            <Tabs.TabPane
              tab={
                <>
                  <Link to={tab.href}>
                    <Badge
                      status={currentKey === tab.key ? "processing" : "default"}
                    />
                    {tab.label}
                  </Link>
                  {tabList.length > 1 ? (
                    <CloseOutlined
                      className="ml-10"
                      onClick={(e) => onClickOneRemoveIcon(e, tab.key)}
                    />
                  ) : (
                    false
                  )}
                </>
              }
              key={tab.key}
            />
          ))}
        </Tabs>

        {(isShowLeft || isShowRight) ? (
          <>
            <div
              className="router-tabs-icon router-tabs-icon-left"
              onClick={() => {
                if (!isShowLeft) {
                  return;
                }
                onClickArrow("left");
              }}
              style={{ color: isShowLeft ? '#000' : 'rgba(0,0,0,.25)' }}
            >
              <LeftOutlined />
            </div>
            <div
              className="router-tabs-icon router-tabs-icon-right"
              onClick={() => {
                if (!isShowRight) {
                  return;
                }
                onClickArrow("right");
              }}
              style={{ color: isShowRight ? '#000' : 'rgba(0,0,0,.25)' }}
            >
              <RightOutlined />
            </div>
          </>
        ) : (
          ""
        )}
      </div>

      {
        tabList && tabList.length > 1 ?
          <div className="remove-all-icon">
            <Popconfirm
              placement="bottomRight"
              title={"确定要关闭所有标签吗？"}
              onConfirm={onClickRemoveIcon}
              okText="确定"
              cancelText="取消"
            >
              <CloseCircleOutlined />
            </Popconfirm>
          </div>
          : ""
      }
    </>
  );
});
