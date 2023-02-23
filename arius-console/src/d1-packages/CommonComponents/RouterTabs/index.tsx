import * as React from "react";
import { Badge, Popconfirm, Tabs, Modal } from "antd";
import { CloseOutlined, CloseCircleOutlined, LeftOutlined, RightOutlined, QuestionCircleOutlined } from "@ant-design/icons";
import { withRouter, Link, RouteComponentProps } from "react-router-dom";
import "./index.less";
import { getCookie } from "../../Utils/tools";
import _ from "lodash";
import { asyncMicroTasks, isSuperApp, resize } from "lib/utils";
interface ITab {
  label: string;
  href: string;
  key: string;
  show: boolean;
  leftIndex?: number;
}

interface Props {
  tabList: ITab[];
  dealPathname?: (pathname: string) => string;
  siderMenuCollapsed?: boolean;
  intlZhCN?: any;
  systemKey?: any;
  mulityPage?: { [key: string]: (path: string) => string };
  removePaths?: any[];
  defaultTab?: any;
  pageEventList?: any[];
  permissions?: { [key: string]: string };
  currentProject?: any;
  leftIndex?: number;
  setLeftIndex?: (index: number) => void;
  headerClick?: boolean;
  setHeaderClick?: (bool: boolean) => void;
  permissionPoints?: any[];
  dropByCacheKey: any;
  redirectPath?: (permissionPoints: any, history?: any, leftIndex?: number, changeProject?: boolean) => string;
}

const RouterTabs = (props: Props & RouteComponentProps) => {
  const {
    dealPathname,
    tabList: tabs,
    history,
    siderMenuCollapsed,
    removePaths,
    defaultTab,
    pageEventList,
    permissions,
    currentProject,
    leftIndex,
    setLeftIndex,
    headerClick,
    setHeaderClick,
    permissionPoints,
    redirectPath,
    systemKey,
    dropByCacheKey,
    mulityPage,
  } = props;
  const currentPathname = typeof dealPathname === "function" ? dealPathname(window.location.pathname) : window.location.pathname;
  const [currentKey, setCurrentKey] = React.useState(`menu${currentPathname.split("/")?.join(".")}`);
  const [tabList, setTabList] = React.useState(tabs || []);
  const initCollapsed = getCookie("siderMenuCollapsed");
  const [collapsed, setCollapsed] = React.useState(siderMenuCollapsed || initCollapsed === "true");
  const [isShowIcon, setIsShowIcon] = React.useState(false);
  const [isShowLeft, setIsShowLeft] = React.useState(false);
  const [isShowRight, setIsShowRight] = React.useState(false);

  const lastProject = React.useRef({} as any);

  React.useEffect(() => {
    if (headerClick) {
      redirectPath(permissionPoints, history, leftIndex);
    }
  }, [leftIndex]);

  React.useEffect(() => {
    if (permissionPoints?.length) {
      let targetPath: string;
      if (lastProject?.current.id && lastProject?.current.id !== currentProject?.id) {
        targetPath = redirectPath(permissionPoints, history, 0, true);
      } else {
        targetPath = redirectPath(permissionPoints, history);
      }
      lastProject.current = currentProject;
      let key = `menu.${props.systemKey}${targetPath?.split("/")?.join(".")}`;
      const search = window.location.search;
      const hash = window.location.hash;
      let label: string;
      const mulityPageName = Object.keys(mulityPage).find((path) => path == targetPath);
      if (mulityPageName) {
        label = mulityPage[mulityPageName].call(null, props.intlZhCN[key]);
        key += search;
      }
      const newTabList = [
        {
          key,
          label: label || props.intlZhCN[key],
          href: `${currentPathname}` === `/${systemKey}${targetPath}` ? `${targetPath}${search ?? ""}${hash ?? ""}` : targetPath,
          show: true,
          leftIndex,
        },
      ];
      setTabList(newTabList);
    }
  }, [permissionPoints]);

  const onChange = (key: string) => {
    setCurrentKey(key);
  };

  const onClosePage = ({ key, onCloseCallback }) => {
    Modal.confirm({
      title: "确定取消？",
      content: "取消后当前填写内容将失效，请谨慎操作",
      okText: "确定",
      cancelText: "取消",
      icon: <QuestionCircleOutlined className="question-icon" />,
      onOk: () => {
        if (onCloseCallback) {
          onCloseCallback();
        }
        onRemoveOneIcon(key);
      },
    });
  };

  // 为了支持点击关闭时对特定页面做提示拆分关闭方法
  const onRemoveOneIcon = (key: string) => {
    const index = tabList.findIndex((item) => item.key === key);
    const changeTabList = tabList.filter((item) => item.key !== key);
    setTabList(changeTabList);

    if (key === currentKey) {
      const currentIndex = index > 0 ? index - 1 : 0;
      setCurrentKey(changeTabList[currentIndex].key);
      (props as any).history.push(changeTabList[currentIndex].href);
    }
  };

  const onClickOneRemoveIcon = (e, key: string) => {
    e.stopPropagation();
    dropByCacheKey && dropByCacheKey(key);

    if (pageEventList && pageEventList.find((item) => item.key === key)) {
      const pageEventItem = pageEventList.find((item) => item.key === key);
      onClosePage(pageEventItem);
    } else {
      onRemoveOneIcon(key);
    }
  };

  const onClickRemoveIcon = () => {
    // TODO 要保留一个兜底页面, 保留当前选中页面
    const changeTabList = tabList.filter((item) => item.key === currentKey);
    setTabList(changeTabList);
  };

  const onClickArrow = _.throttle((type = "") => {
    const navWrap = document.querySelector("#tabs-list .ant-tabs-nav-wrap") as HTMLElement;
    const navList = document.querySelector("#tabs-list .ant-tabs-nav-list") as HTMLElement;
    const navTab = document.querySelector("#tabs-list .ant-tabs-tab") as HTMLElement;

    const navWrapWidth = Number(navWrap.offsetWidth);
    const navListWidth = Number(navList.offsetWidth);
    const tagWidth = navTab ? Number(navTab.offsetWidth) + 12 : 96 + 12;
    const translateLen = Number((navList as any).style.transform.split("(")[1].split(",")[0].replace("px", ""));
    const navListScrollLeft = Number(translateLen * -1);
    const navListScrollRight = Number(navListWidth - navWrapWidth - navListScrollLeft);

    const tag3 = tagWidth * 3;

    if (type) {
      if (type === "left") {
        if (navListScrollRight <= 0) {
          return;
        }
        if (navListScrollRight >= tag3) {
          navList.style.transform = `translate(${translateLen - tag3}px, 0)`;
        } else {
          navList.style.transform = `translate(${navWrapWidth - navListWidth}px, 0)`;
          navWrap.classList.remove("ant-tabs-nav-wrap-ping-right");
        }
        navWrap.classList.add("ant-tabs-nav-wrap-ping-left");
      } else if (type === "right") {
        if (navListScrollLeft <= 0) {
          return;
        }
        if (navListScrollLeft > tag3) {
          navList.style.transform = `translate(${translateLen + tag3}px, 0)`;
        } else {
          navList.style.transform = `translate(0, 0)`;
          navWrap.classList.remove("ant-tabs-nav-wrap-ping-left");
        }
        navWrap.classList.add("ant-tabs-nav-wrap-ping-right");
      }
      onClickArrow();
    } else {
      navListScrollRight <= 0 ? setIsShowLeft(false) : setIsShowLeft(true);
      navListScrollLeft <= 0 ? setIsShowRight(false) : setIsShowRight(true);
    }
  }, 300);

  React.useEffect(() => {
    // redirect情况 通过dealpathname来手动关联处理
    let key = `menu${currentPathname.split("/")?.join(".")}`;
    let label;

    // 翻译不存在表示非左侧菜单栏页面
    if (!props.intlZhCN[key]) {
      return;
    }
    const search = window.location.search;
    const hash = window.location.hash;
    const pathname = currentPathname.replace(`/${props.systemKey}`, "");
    const href = pathname + search + hash;
    const mulityPageName = Object.keys(mulityPage).find((path) => path == pathname);
    if (mulityPageName) {
      label = mulityPage[mulityPageName].call(null, props.intlZhCN[key]);
      key += search;
    }

    const hasTab = tabList.filter((item) => item.key === key).length;
    let newTabs = tabList.map((row) => {
      row.show = row.key === key;
      if (row.key === key) {
        row.href = href;
      }
      return row;
    });

    if (!hasTab && permissionPoints.length) {
      newTabs.push({
        key,
        label: label || props.intlZhCN[key],
        href,
        show: true,
        leftIndex,
      });
    }

    setTabList(newTabs);
    asyncMicroTasks(resize);

    // 判断当前过滤后的tabs有没有
    const hasTabKey = newTabs.filter((item) => item.key === key).length;
    if (hasTabKey) {
      setCurrentKey(key);
      return;
    }

    const newShowTab = newTabs[newTabs.length - 1 || 0];
    if (newShowTab?.key) {
      setCurrentKey(newShowTab.key);
      history.push(newShowTab.href);
    }
  }, [window.location.href]);

  React.useEffect(() => {
    setCollapsed(siderMenuCollapsed);
  }, [siderMenuCollapsed]);

  React.useEffect(() => {
    const navWrap = document.querySelector("#tabs-list .ant-tabs-nav-wrap") as HTMLElement;
    const navList = document.querySelector("#tabs-list .ant-tabs-nav-list") as HTMLElement;
    const navWrapWidth = Number(navWrap.offsetWidth);
    const navListWidth = Number(navList.offsetWidth);
    if (navListWidth >= navWrapWidth) {
      setIsShowIcon(true);
    } else {
      setIsShowIcon(false);
      navList.style.transform = `translate(0, 0)`;
    }
    onClickArrow();
  }, [tabList]);

  // 关闭页面逻辑
  React.useEffect(() => {
    try {
      // 做判断防止是空值
      if (!Array.isArray(removePaths) || !removePaths.length || !removePaths[0]) return;
      let newTabs = Array.from(tabList);
      for (let path of removePaths) {
        if (Array.isArray(path)) {
          path = path[0];
        }
        // 去空逻辑
        newTabs = newTabs.filter((tab: any) => tab.key !== `menu${path?.split("/")?.join(".")}`);
      }
      // 如果没有页面了返回首页
      if (newTabs && !newTabs.length) {
        newTabs = [defaultTab];
      }
      setTabList(newTabs);
      // 恢复值
      window.localStorage.setItem("removePath", "");
    } catch (err) {
      console.log(err);
    }
  }, [removePaths]);

  const onLinkClick = () => {
    asyncMicroTasks(resize);
  };

  return (
    <>
      <div className={collapsed ? "router-tabs collapsed" : "router-tabs"}>
        <Tabs
          activeKey={currentKey}
          onChange={onChange}
          type="card"
          id="tabs-list"
          onTabClick={(key) => {
            if (key === currentKey) return;
            const index = tabList.findIndex((item) => item.key === key);
            (props as any).history.push(tabList[index].href);
          }}
        >
          {tabList.map((tab) => (
            <Tabs.TabPane
              tab={
                <>
                  <Link
                    onClick={() => {
                      onLinkClick();
                      setLeftIndex(tab?.leftIndex);
                      setHeaderClick(false);
                    }}
                    to={tab.href}
                  >
                    <Badge status={currentKey === tab.key ? "processing" : "default"} />
                    {tab.label}
                  </Link>
                  {tabList.length > 1 ? <CloseOutlined className="ml-10" onClick={(e: any) => onClickOneRemoveIcon(e, tab.key)} /> : null}
                </>
              }
              key={tab.key}
            />
          ))}
        </Tabs>
        {isShowIcon ? (
          <>
            <div
              className="router-tabs-icon router-tabs-icon-left"
              onClick={() => {
                onClickArrow("left");
              }}
              style={{ color: isShowLeft ? "#000" : "rgba(0,0,0,.25)" }}
            >
              <LeftOutlined />
            </div>
            <div
              className="router-tabs-icon router-tabs-icon-right"
              onClick={() => {
                onClickArrow("right");
              }}
              style={{ color: isShowRight ? "#000" : "rgba(0,0,0,.25)" }}
            >
              <RightOutlined />
            </div>
          </>
        ) : (
          ""
        )}
      </div>
      {tabList && tabList.length > 1 ? (
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
      ) : (
        ""
      )}
    </>
  );
};

export default withRouter(RouterTabs);
