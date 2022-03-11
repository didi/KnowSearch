import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Form, Menu, message, Radio, Select, Spin, Tag, Transfer } from "antd";
import React, { forwardRef, useImperativeHandle } from "react";
import { MenuInfo } from "..";
import { getUserResourceTypeList, getResourceTypeList, assignResource, getStatus, batchAssign } from "../api";
import { TreeData, UserObj } from "../type";
import { optionsWithDisabled, TAB_LIST, TAB_LIST_KEY, TITLE_MAP, VALUE_KEY } from "./config";
import "./index.less";

const AssignAsset = (
  props: {
    callback: (b: boolean) => void;
    data: { breadcrumbItem: TreeData[]; checkList: UserObj[] };
  },
  ref
): JSX.Element => {
  const [menu, setMenu] = React.useState<string>(TAB_LIST[0]?.key);
  const [radioValue, setRadioValue] = React.useState<string>(VALUE_KEY.project);
  const [targetKeys, setTargetKeys] = React.useState([]);
  const [initTargetKeys, setInitialTargetKeys] = React.useState([]);
  const [projectOptions, setProjectOptions] = React.useState([]);
  const [resourceCategory, setResourceCategory] = React.useState([]);
  const [projectList, setProjectList] = React.useState([]);
  const [resourceCategoryList, setResourceCategoryList] = React.useState([]);
  const [resourceDetailsList, setResourceDetailsList] = React.useState([]);
  const [projectId, setProjectId] = React.useState(null);
  const [resourceCategoryId, setResourceCategoryId] = React.useState(null);
  const [loading, setLoading] = React.useState(false);
  const [checked, setChecked] = React.useState<boolean>(false);

  const [form] = Form.useForm();

  const changeMenu = (info: MenuInfo) => {
    setMenu(info.key);
  };

  useImperativeHandle(ref, () => ({
    submit,
  }));

  React.useEffect(() => {
    getResourceList();
  }, [menu]);

  React.useEffect(() => {
    if (projectId) {
      getResourceList();
    }
  }, [projectId]);

  React.useEffect(() => {
    if (resourceCategoryId) {
      getResourceList();
    }
  }, [resourceCategoryId]);

  React.useEffect(() => {
    getResourceTypeList().then((res) => {
      setResourceCategory(
        res.map((i) => {
          return {
            label: i.typeName,
            value: i.id,
          };
        })
      );
    });
    getStatus()
      .then((res) => {
        setChecked(res);
      })
      .catch(() => {
        message.error("获取查看权限状态失败！");
      });
  }, []);

  const submit = async () => {
    const { data } = props;
    let variable = false;
    const showLevel = optionsWithDisabled.filter((item) => item.value === radioValue)[0].showLevel;
    if (data.checkList?.length > 1) {
      const req = {
        assignFlag: false,
        controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1,
        idList: targetKeys,
        projectId: showLevel === 3 || showLevel === 2 ? projectId : null, //项目id（2，3展示级别不可为null）
        resourceTypeId: showLevel === 3 ? resourceCategoryId : null, // 资源类别id（3展示级别不可为null）
        userId: data.checkList.map((item) => item.id),
      };
      await batchAssign(req).then((res) => {
        variable = true;
      });
    } else {
      const req = {
        controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1,
        idList: targetKeys,
        projectId: showLevel === 3 || showLevel === 2 ? projectId : null, //项目id（2，3展示级别不可为null）
        resourceTypeId: showLevel === 3 ? resourceCategoryId : null, // 资源类别id（3展示级别不可为null）
        userId: data.checkList[0]?.userId,
      };
      await assignResource(req).then((res) => {
        variable = true;
      });
    }
    return variable;
  };

  const getResourceList = () => {
    const { data } = props;
    if (!data.checkList[0] || !data.checkList[0]?.userId) {
      return;
    }
    const showLevel = optionsWithDisabled.filter((item) => item.value === radioValue)[0].showLevel;
    if (showLevel === 3 && !resourceCategoryId) {
      return;
    }
    const params = {
      controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1,
      batch: data.checkList?.length > 1 ? true : false,
      showLevel, // ：1 项目展示级别、2 资源类别展示级别、3 具体资源展示级别
      userId: data.checkList[0]?.userId,
      projectId: showLevel === 3 || showLevel === 2 ? projectId : null, //项目id（2，3展示级别不可为null）
      resourceTypeId: showLevel === 3 ? resourceCategoryId : null, // 资源类别id（3展示级别不可为null）
    };
    setLoading(true);
    getUserResourceTypeList(params)
      .then((res) => {
        res = res.map((item) => {
          return {
            key: item.id,
            title: item.name,
            description: item.name,
            hasLevel: item.hasLevel,
          };
        });
        const initialTargetKeys = res.filter((item) => item.hasLevel === 2).map((item) => item.key);
        if (showLevel === 1) {
          setProjectList(res);
          setProjectOptions(
            res.map((item) => {
              return { label: item.title, value: item.key };
            })
          );
        } else if (showLevel === 2) {
          setResourceCategoryList(res);
        } else if (showLevel === 3) {
          setResourceDetailsList(res);
        } else {
          setProjectList(res);
        }
        setTargetKeys(initialTargetKeys);
        setInitialTargetKeys(initialTargetKeys);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  };

  const handleChange = (targetKeys: string[]) => {
    setTargetKeys(targetKeys);
    form.setFieldsValue({ users: targetKeys });
    props.callback(targetKeys.sort().toString() === initTargetKeys.sort().toString()); // 按钮的禁用限制
  };

  const onChangeRadio = (e) => {
    setRadioValue(e.target.value);
  };

  const handleSearch = (dir, value) => {
    console.log("search:", dir, value);
  };

  const onChangeResourceCategory = (value) => {
    setResourceCategoryId(value);
  };

  const onChangeProject = (value) => {
    setProjectId(value);
  };

  const renderErrTip = (isShowResourceCategory) => {
    if (isShowResourceCategory && !resourceCategoryId) {
      return <div style={{ color: "#ff4d4f" }}>请先选择项目及资源类别</div>;
    }
    if (resourceCategoryId || projectId) {
      return null;
    }
    return <div style={{ color: "#ff4d4f" }}>{isShowResourceCategory ? "请先选择项目及资源类别" : "请先选择项目"}</div>;
  };

  const renderContent = () => {
    if (menu === TAB_LIST_KEY.seeAuthor && !checked) {
      return (
        <>
          <div className="assign-users-from-see">
            <ExclamationCircleOutlined />
            当前未启用查看权限控制，所有用户都已默认拥有
          </div>
        </>
      );
    }
    return (
      <>
        <div style={{ paddingTop: 20 }}>
          <Radio.Group options={optionsWithDisabled} onChange={onChangeRadio} value={radioValue} optionType="button" buttonStyle="solid" />
        </div>
        {renderTransfer()}
      </>
    );
  };

  const renderTransfer = () => {
    let dataSource = [];
    if (radioValue === VALUE_KEY.project) {
      dataSource = projectList;
    } else if (radioValue === VALUE_KEY.resourceCategory) {
      dataSource = resourceCategoryList;
    } else if (radioValue === VALUE_KEY.resourceDetails) {
      dataSource = resourceDetailsList;
    }
    const isShowProject = radioValue === VALUE_KEY.resourceCategory || radioValue === VALUE_KEY.resourceDetails;
    const isShowResourceCategory = radioValue === VALUE_KEY.resourceDetails;

    return (
      <>
        <div style={radioValue === VALUE_KEY.project ? { display: "none" } : { paddingTop: 10 }}>
          {isShowProject ? (
            <Select
              showSearch
              style={{ width: 150 }}
              placeholder="请选择项目"
              optionFilterProp="children"
              value={projectId}
              onChange={onChangeProject}
              options={projectOptions}
              filterOption={(input, option) => JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >= 0}
            />
          ) : null}
          {isShowResourceCategory ? (
            <Select
              showSearch
              style={{ width: 200 }}
              placeholder="请选择资源类别"
              optionFilterProp="children"
              value={resourceCategoryId}
              onChange={onChangeResourceCategory}
              options={resourceCategory}
              filterOption={(input, option) => JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >= 0}
            />
          ) : null}
          {renderErrTip(isShowResourceCategory)}
        </div>
        <Spin spinning={loading}>
          <Transfer
            key={"users"}
            dataSource={dataSource}
            titles={TITLE_MAP[radioValue]}
            targetKeys={targetKeys}
            onChange={handleChange}
            showSearch
            onSearch={handleSearch}
            render={(item) => item.title}
            style={{ marginTop: 16 }}
          />
        </Spin>
      </>
    );
  };

  return (
    <div className="assign-users">
      <div className="assign-users-header">
        <div className="assign-users-header-object">
          已选用户：
          {props.data?.checkList.map((item, index) => {
            return (
              <>
                <Tag key={index}>{item.username}</Tag>
              </>
            );
          })}
        </div>
      </div>
      <div className="assign-users-from">
        <Menu selectedKeys={[menu]} mode="horizontal" onClick={changeMenu}>
          {TAB_LIST.map((d) => (
            <Menu.Item key={d.key}>{d.name}</Menu.Item>
          ))}
        </Menu>
        <Form form={form}>
          <Form.Item
            key={"users"}
            name={"users"}
            valuePropName={"targetKeys"}
            rules={[{ required: true, message: "已分配用户不存在变更，不允许修改。" }]}
          >
            {renderContent()}
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};
export default forwardRef(AssignAsset);
