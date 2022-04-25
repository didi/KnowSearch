import { Breadcrumb, notification, TablePaginationConfig, Tree, TreeNodeProps } from "antd";
import { DTable, ITableBtn, pagination } from "../../dantd/DTable";
import React, { forwardRef, useContext, useImperativeHandle } from "react";
import { getResourcesTabColumns } from "./config";
import "./index.less";
import { FolderOpenTwoTone, FolderTwoTone, FundTwoTone } from "@ant-design/icons";
import { AssetDrawer } from "./drawer";
import { getProjectList, getResourceTypeList, getResourceList } from "./api";
import { Project, Resource, ResourceObj, TreeData } from "./type";
import { debounce, cloneDeep } from "lodash";
import GlobalState from "../../GlobalStore";
import Progress from '../../CommonComponents/ProgressBar'

const placeholderArr = ["请输入项目名称", "请输入资源类型名称", "请输入资源名称"];
export const ResourcesTab: React.FC<any> = forwardRef((props: {}, ref) => {
  const { project } = useContext(GlobalState) as any;
  const [treeData, setTreeData] = React.useState([] as TreeData[]);
  const [dataSource, setDataSource] = React.useState([] as ResourceObj[]);
  const [loading, setloading] = React.useState(false);
  const [selectedKeys, setSelectedKeys] = React.useState<string[]>([`0-0`]);
  const [breadcrumbItem, setBreadcrumbItem] = React.useState([] as TreeData[]);
  const [selectedRowKeys, setSelectedRowKeys] = React.useState([]);
  const [dawerInfo, setDawerInfo] = React.useState({
    visible: false,
    drawerKey: "AssignUsers",
    data: {} as any,
  });
  const [paginationProps, setPaginationProps] = React.useState(pagination as unknown as TablePaginationConfig);
  const [searchPlaceholder, setSearchPlaceholder] = React.useState(placeholderArr[0]);

  React.useEffect(() => {
    reloadData();
  }, []);

  React.useEffect(() => {
    getResourceData();
    if (breadcrumbItem.length === 1) {
      setSearchPlaceholder(placeholderArr[0]);
    } else if (breadcrumbItem.length === 2) {
      setSearchPlaceholder(placeholderArr[1]);
    } else if (breadcrumbItem.length === 3) {
      setSearchPlaceholder(placeholderArr[2]);
    } else {
      setSearchPlaceholder(placeholderArr[0]);
    }
  }, [breadcrumbItem]);

  React.useEffect(() => {
    if (paginationProps.current || paginationProps.pageSize) {
      getResourceData();
    }
  }, [paginationProps, project]);

  const reloadData = () => {
    Promise.all([getProjectList(), getResourceTypeList()])
      .then((values) => {
        setTerrStructure(values);
      })
      .catch((err) => {
        notification.error({ message: `网络错误！: ${err}` });
      });
  };

  const getResourceData = (value?: string) => {
    setloading(true);
    Progress.start();
    const req = {
      name: value || null, // 项目展示级别，则name表示项目名称、资源类别展示级别，则name表示资源类别名称、具体资源展示级别，则name表示具体资源名称）
      projectId: breadcrumbItem[1]?.key || null, // 项目id（2，3展示级别不可为null）
      resourceTypeId: getResourceTypeId(), //资源类别id（3展示级别不可为null）
      showLevel: breadcrumbItem.length || 1, // 按资源管理列表展示级别：1 项目展示级别、2 资源类别展示级别、3 具体资源展示级别
      page: paginationProps.current || 1,
      size: paginationProps.pageSize || 10,
    };
    getResourceList(req)
      .then((res) => {
        setDataSource(
          res.bizData.map((item, i) => {
            item.id = i + "";
            return item;
          })
        );
        paginationProps.total = res.pagination.total;
        setPaginationProps(paginationProps);
        setloading(false);
        Progress.done();
      })
      .finally(() => {
        setloading(false);
        Progress.done();
      });
  };

  useImperativeHandle(ref, () => ({
    getResourceData,
  }));

  const getResourceTypeId = () => {
    if (breadcrumbItem.length === 3) {
      return breadcrumbItem[2].key?.split("-")[1];
    }
    return null;
  };

  const setTerrStructure = (values: [Project[], Resource[]]) => {
    const projectArr: TreeData[] = values[0].map((item) => {
      return {
        key: item.id + "",
        title: item.projectName,
        icon: ({ expanded }: TreeNodeProps) => (expanded ? <FolderOpenTwoTone /> : <FolderTwoTone />),
        children: values[1].map((i) => {
          return {
            key: item.id + "-" + i.id,
            title: i.typeName,
            icon: <FundTwoTone />,
          };
        }),
      };
    });
    const allProject = [
      {
        key: "0-0",
        title: "全部项目",
        switcherIcon: ({ expanded }: TreeNodeProps) => (expanded ? <FolderOpenTwoTone /> : <FolderTwoTone />),
        children: projectArr,
      },
    ];
    setTreeData(allProject);
    setBreadcrumbItem(allProject);
  };

  const onSelect = (selectedKeys: string[]) => {
    setSelectedKeys(selectedKeys);
    fullParentPath(selectedKeys[0], treeData, []);
  };

  const fullParentPath = (key: string, data: TreeData[], treeItem: TreeData[]) => {
    for (let i = 0; i < data.length; i++) {
      const temp = data[i];
      if (key === temp.key) {
        setBreadcrumbItem([...treeItem, temp]);
        return;
      }
      if (temp.children) {
        fullParentPath(key, temp.children, [...treeItem, temp]);
      }
    }
  };

  const handleSubmit = (value) => {
    getResourceData(value);
  };

  const onClose = () => {
    const copyData = cloneDeep(dawerInfo);
    copyData.visible = false;
    setDawerInfo(copyData);
  };

  const onOpenDrawer = (record?: ResourceObj) => {
    const checkList = [];
    if (record) {
      checkList.push(record);
    } else {
      selectedRowKeys.forEach((item) => {
        dataSource.forEach((i) => {
          if (item === i.id) {
            checkList.push(i);
          }
        });
      });
    }
    const copyData = cloneDeep(dawerInfo);
    copyData.visible = true;
    copyData.data = {
      breadcrumbItem,
      checkList,
    };
    setDawerInfo(copyData);
  };

  const onChange = (pagination: TablePaginationConfig) => {
    const cloneData = cloneDeep(paginationProps);
    cloneData.current = pagination.current;
    cloneData.pageSize = pagination.pageSize;
    setPaginationProps(cloneData);
  };

  const getColumns = () => {
    const columns = getResourcesTabColumns(onOpenDrawer);
    if (breadcrumbItem.length === 1) {
      columns.splice(2, 4);
    } else if (breadcrumbItem.length === 2) {
      columns.splice(0, 2);
      columns.splice(2, 2);
    } else if (breadcrumbItem.length === 3) {
      columns.splice(0, 4);
    } else {
      columns.splice(2, 4);
    }
    return columns;
  };

  const onSelectChange = (selectedRowKeys) => {
    setSelectedRowKeys(selectedRowKeys);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "批量分配用户",
        className: "ant-btn-primary",
        disabled: selectedRowKeys?.length ? false : true,
        clickFunc: () => {
          onOpenDrawer();
        },
      },
    ];
  };

  const renderTree = () => {
    return (
      <>
        {treeData.length >= 1 ? (
          <Tree
            defaultSelectedKeys={[`0-0`]}
            defaultExpandedKeys={["0-0"]}
            selectedKeys={selectedKeys}
            showIcon
            blockNode
            onSelect={onSelect}
            treeData={treeData}
          />
        ) : null}
      </>
    );
  };

  const renderBreadcrumb = () => {
    return (
      <>
        <Breadcrumb className="resources-tab-table-breadcrumb">
          {breadcrumbItem.map((item, index) => {
            return (
              <Breadcrumb.Item
                key={item.key}
                onClick={() => {
                  setSelectedKeys([item.key]);
                  setBreadcrumbItem(breadcrumbItem.slice(0, index + 1));
                }}
              >
                <a>{item.title}</a>
              </Breadcrumb.Item>
            );
          })}
        </Breadcrumb>
      </>
    );
  };

  return (
    <div className="resources-tab">
      <div className="resources-tab-tree">{renderTree()}</div>
      <div className="resources-tab-table">
        {renderBreadcrumb()}
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={dataSource}
          columns={getColumns()}
          getOpBtns={getOpBtns}
          tableHeaderSearchInput={{ submit: debounce(handleSubmit, 500), placeholder: searchPlaceholder }}
          paginationProps={paginationProps}
          attrs={{
            rowSelection: {
              selectedRowKeys,
              onChange: onSelectChange,
            },
            onChange: onChange,
          }}
        />
      </div>
      {dawerInfo.visible ? <AssetDrawer {...dawerInfo} onClose={onClose} reloadData={getResourceData} /> : null}
    </div>
  );
});
