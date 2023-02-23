import { Dropdown, Input, Tooltip } from "antd";
import React, { useState } from "react";
import "./index.less";

export interface IProject {
  id: number;
  memo: string;
  name: string;
}

interface IProjectDropDown {
  list: IProject[];
  currentProject: IProject;
  onChange: (newProject: IProject) => void;
  setVisible?: (visible: boolean) => void;
}

export const CustomAppDropDown = (props: {
  currentProject: IProject;
  projectList: IProject[];
  setCurrentProject: (newProject: IProject) => void;
  setLeftIndex: (index: number) => void;
}) => {
  const [visible, setVisible] = useState(false);

  const { currentProject, projectList = [], setCurrentProject, setLeftIndex } = props;
  const handleVisibleChange = (newVisible: boolean) => {
    setVisible(newVisible);
  };

  return (
    <>
      {currentProject?.name ? (
        <>
          <Dropdown
            visible={visible}
            key="2"
            arrow
            overlayClassName="app-wrapper"
            onVisibleChange={handleVisibleChange}
            overlay={
              <ProjectSelect
                onChange={(val) => {
                  setLeftIndex(0);
                  setCurrentProject(val);
                  setVisible(false);
                }}
                list={projectList}
                currentProject={currentProject}
              />
            }
            trigger={["click"]}
            placement="bottomCenter"
          >
            <span className="dropdown-content">
              <a className="dropdown-text">
                <span className={`icon iconfont iconyingyong`}></span>
                <span className="text">
                  {currentProject?.name?.length > 20 ? (
                    <Tooltip title={currentProject?.name}>{currentProject?.name?.slice(0, 18) + "..."}</Tooltip>
                  ) : (
                    currentProject?.name
                  )}
                </span>
                <div className={`select-icon`}>
                  <span className={`icon iconfont iconRight`}></span>
                </div>
              </a>
            </span>
          </Dropdown>
        </>
      ) : (
        ""
      )}
    </>
  );
};

class ProjectSelect extends React.Component<IProjectDropDown> {
  public state = {
    list: this.props.list || ([] as IProject[]),
    isSearch: false,
  };

  public onHandleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { value } = e.target;
    const originList = this.props.list || [];
    const list = value?.trim()
      ? originList.filter((item) => item.name.includes(value?.trim()) || (item.id + "").includes(value?.trim()))
      : originList;
    this.setState({
      list,
      isSearch: value ? true : false,
    });
  };

  public switchProjectId = (item: IProject) => {
    this.props.onChange && this.props.onChange(item);
  };

  public render() {
    const { list, isSearch } = this.state;
    const { currentProject } = this.props;
    return (
      <div className="project-select">
        <Input
          className={isSearch ? "hasclear" : ""}
          allowClear
          onChange={this.onHandleInputChange}
          placeholder="请输入应用名称或应用ID"
          prefix={
            <svg className="icon svg-icon" aria-hidden="true">
              <use xlinkHref="#icontubiao-sousuo"></use>
            </svg>
          }
        />
        <ul className="project-list">
          {isSearch && !list.length ? <li>无匹配结果</li> : null}
          {!isSearch && !list.length ? <li>无应用</li> : null}
          {list.map((item, index) => (
            <li key={index} className={currentProject?.id === item.id ? "active" : ""} onClick={() => this.switchProjectId(item)}>
              <span>
                {item.name}({item.id})
              </span>
            </li>
          ))}
        </ul>
      </div>
    );
  }
}
