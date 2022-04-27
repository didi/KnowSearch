import { Breadcrumb, TablePaginationConfig, Tree, TreeNodeProps } from "antd";
import { DTable, ITableBtn, pagination } from "../../dantd/DTable";
import React, { forwardRef, useContext, useImperativeHandle } from "react";
import { getUserTabColumns, getUserTabQueryXForm, queryFormText } from "./config";
import "./index.less";
import { FolderOpenTwoTone, FolderTwoTone, FundTwoTone } from "@ant-design/icons";
import { AssetDrawer } from "./drawer";
import { getDeptTree, getDeptResourceList } from "./api";
import { TreeData, UserObj } from "./type";
import { cloneDeep } from "lodash";
import QueryForm from "../../ProForm/QueryForm";
import GlobalState from "../../GlobalStore";
import Progress from '../../CommonComponents/ProgressBar'

export const UserTab: React.FC<any> = forwardRef((props: {}, ref) => {
  const { project } = useContext(GlobalState) as any;
  const [treeData, setTreeData] = React.useState([] as TreeData[]);
  const [dataSource, setDataSource] = React.useState([] as UserObj[]);
  const [loading, setloading] = React.useState(false);
  const [selectedKeys, setSelectedKeys] = React.useState<string[]>([`0-0`]);
  const [breadcrumbItem, setBreadcrumbItem] = React.useState([] as TreeData[]);
  const [selectedRowKeys, setSelectedRowKeys] = React.useState([]);
  const [dawerInfo, setDawerInfo] = React.useState({
    visible: false,
    drawerKey: "AssignAsset",
    data: {} as any,
  });
  const [paginationProps, setPaginationProps] = React.useState(pagination as unknown as TablePaginationConfig);

  React.useEffect(() => {
    reloadData();
  }, []);

  React.useEffect(() => {
    getResourceData();
  }, [breadcrumbItem]);

  React.useEffect(() => {
    if (paginationProps.current || paginationProps.pageSize) {
      getResourceData();
    }
  }, [paginationProps, project]);

  const reloadData = () => {
    getDeptTree().then((res) => {
      const deptTree = [
        {
          key: "0-0",
          title: "全部部门",
          switcherIcon: ({ expanded }: TreeNodeProps) => (expanded ? <FolderOpenTwoTone /> : <FolderTwoTone />),
          children: backTrack(res.childList),
        },
      ];
      setTreeData(deptTree);
      setBreadcrumbItem(deptTree);
    });
  };

  const backTrack = (childList) => {
    return childList.map(({ id, deptName, childList, ...args }) => {
      return {
        key: id,
        title: deptName,
        icon: childList ? ({ expanded }: TreeNodeProps) => (expanded ? <FolderOpenTwoTone /> : <FolderTwoTone />) : <FundTwoTone />,
        children: childList == null ? null : backTrack(childList),
        ...args,
      };
    });
  };

  const getResourceData = (values?: { realName: string; uesrName: string; dept: string }) => {
    setloading(true);
    Progress.start();
    const req = {
      deptId: breadcrumbItem.length > 1 ? breadcrumbItem[breadcrumbItem.length - 1]?.key : null,
      deptName: values?.dept || null,
      realName: values?.realName || null,
      username: values?.uesrName || null,
      page: paginationProps.current || 1,
      size: paginationProps.pageSize || 10,
    };
    getDeptResourceList(req)
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

  const onSelect = (selectedKeys: string[]) => {
    setSelectedKeys(selectedKeys);
    fullPath(selectedKeys[0], treeData, []);
  };

  const fullPath = (key: string, data: TreeData[], treeItem: TreeData[]) => {
    for (let i = 0; i < data.length; i++) {
      const temp = data[i];
      if (key === temp.key) {
        setBreadcrumbItem([...treeItem, temp]);
        return;
      }
      if (temp.children) {
        fullPath(key, temp.children, [...treeItem, temp]);
      }
    }
  };

  const onClose = () => {
    const copyData = cloneDeep(dawerInfo);
    copyData.visible = false;
    setDawerInfo(copyData);
  };

  const onOpenDrawer = (record?: UserObj) => {
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
    const columns = getUserTabColumns(onOpenDrawer);
    return columns;
  };

  const onSelectChange = (selectedRowKeys) => {
    setSelectedRowKeys(selectedRowKeys);
  };

  const handleSubmit = (values) => {
    getResourceData(values);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "批量分配资源",
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
        <div className="resources-tab-table-query">
          <QueryForm
            {...queryFormText}
            defaultCollapse
            columns={getUserTabQueryXForm(breadcrumbItem[breadcrumbItem.length - 1])}
            onChange={() => null}
            // showCollapseButton={false}
            onSearch={handleSubmit}
            onReset={handleSubmit}
            initialValues={{}}
            isResetClearAll
          />
        </div>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={dataSource}
          columns={getColumns()}
          getOpBtns={getOpBtns}
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
      {dawerInfo.visible ? <AssetDrawer {...dawerInfo} title={"分配资源"} onClose={onClose} reloadData={getResourceData} /> : null}
    </div>
  );
});
