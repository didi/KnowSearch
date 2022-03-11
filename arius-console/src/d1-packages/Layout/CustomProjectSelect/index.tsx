import { Dropdown, Input, Tooltip } from 'antd';
import * as React from 'react';
import { CaretDownOutlined } from '@ant-design/icons';
import './index.less';

export interface IProject {
  id: number;
  memo: string;
  name: string;
}

interface IProjectDropDown {
  list: IProject[];
  currentProject: IProject;
  setCurrentProject: (newProject: IProject) => void;
  setVisible?: (visible: boolean) => void;
}

export const CustomAppDropDown = (props: {
  currentProject: IProject;
  projectList: IProject[];
  setCurrentProject: (newProject: IProject) => void;
}) => {
  const { currentProject, projectList = [], setCurrentProject } = props;

  return (
    <>
      {
        currentProject?.name ?
          <>
            <Dropdown
              key="2"
              overlayClassName="app-wrapper"
              overlay={<ProjectSelect setCurrentProject={setCurrentProject} list={projectList} currentProject={currentProject} />}
              trigger={['click', 'hover']}
              placement="bottomCenter"
            >
              <span className="dropdown-content">
                <a className="dropdown-text">
                  <span>
                    {currentProject?.name?.length > 20 ?
                      <Tooltip title={currentProject?.name}>
                        {currentProject?.name?.slice(0, 18) + '...'}
                      </Tooltip>
                      : currentProject?.name}
                  </span>
                  <CaretDownOutlined className="dropdown-icon" />
                </a>
              </span>
            </Dropdown>
          </>
          : ''
      }
    </>
  );
}

class ProjectSelect extends React.Component<IProjectDropDown> {
  public state = {
    list: this.props.list || [] as IProject[],
  };

  public onHandleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { value } = e.target;
    const originList = this.props.list || [];
    const list = value?.trim() ? originList.filter(item => item.name.includes(value?.trim()) || (item.id + '').includes(value?.trim())) : originList;
    this.setState({
      list,
    });
  }

  public switchAppId = (item: IProject) => {
    this.props.setCurrentProject && this.props.setCurrentProject(item);
  }

  public render() {
    const { list } = this.state;
    const { currentProject } = this.props;
    return (
      <>
        <>
          <Input onChange={this.onHandleInputChange} placeholder="请输入项目名称或项目ID" />
        </>
        <ul>
          {
            list.map((item, index) =>
            (<li key={index}>
              <a className={currentProject?.id === item.id ? 'active' : ''} onClick={() => this.switchAppId(item)}>
                <span>{item.name}({item.id})</span>
                {/* <span className="right-text">{item.id}</span> */}
              </a>
            </li>),
            )
          }
        </ul>
      </>
    );
  }
}
