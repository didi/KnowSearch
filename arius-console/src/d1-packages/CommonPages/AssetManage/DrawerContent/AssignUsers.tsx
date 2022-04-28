import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Form, Menu, message, Spin, Tag, Transfer } from "antd";
import React, { forwardRef, useImperativeHandle } from "react";
import { MenuInfo } from "..";
import { assignUser, batchAssign, getResourceUserList, getStatus } from "../api";
import { ResourceObj, TreeData, UserInfo } from "../type";
import { ParticleType, TAB_LIST, TAB_LIST_KEY } from "./config";
import "./index.less";

const AssignUsers = (
  props: {
    callback: (b: boolean) => void;
    data: { breadcrumbItem: TreeData[]; checkList: ResourceObj[] };
  },
  ref
): JSX.Element => {
  const [menu, setMenu] = React.useState<string>(TAB_LIST[0]?.key);
  const [initTargetKeys, setInitialTargetKeys] = React.useState([]);
  const [targetKeys, setTargetKeys] = React.useState([]);
  const [users, setUsers] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [checked, setChecked] = React.useState<boolean>(false);
  const [excludeUserIdList, setExcludeUserIdList] = React.useState<number[]>([]);

  const [form] = Form.useForm();

  const changeMenu = (info: MenuInfo) => {
    setMenu(info.key);
  };

  React.useEffect(() => {
    getUsers();
    getStatus()
      .then((res) => {
        setChecked(res);
      })
      .catch(() => {
        message.error("获取查看权限状态失败！");
      });
  }, []);

  React.useEffect(() => {
    if (checked) {
      getUsers();
    }
  }, [menu]);

  const getUsers = () => {
    const { data } = props;
    if (!data.checkList[0] || !data.checkList[0]?.projectId) {
      return;
    }
    const params = {
      batch: data.checkList?.length > 1 ? true : false,
      controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1, //1（默认，查看权限）、2（管理权限）
      name: null,
      projectId: data.checkList[0]?.projectId,
      resourceId: data.checkList[0]?.resourceId || null,
      resourceTypeId: data.checkList[0]?.resourceTypeId || null,
    };
    setLoading(true);
    getResourceUserList(params).then((res: UserInfo[]) => {
      const arr = res.map((item) => {
        return {
          key: item.userId,
          title: item.hasLevel === 1 ? getTitle(item) : item.username + "/" + item.realName,
          description: item.realName,
          hasLevel: item.hasLevel, //0 不拥有、1 半拥有、2 全拥有
        };
      });
      const initialTargetKeys = arr.filter((item) => item.hasLevel === 2).map((item) => item.key);
      setUsers(arr);
      setTargetKeys(initialTargetKeys);
      setInitialTargetKeys(initialTargetKeys);
      setExcludeUserIdList(arr.filter((item) => item.hasLevel === 1).map((item) => item.key)); // 半拥有idlist
      setLoading(false);
    });
  };

  const getTitle = (item: UserInfo) => {
    return (
      <>
        <span style={{ color: "#3746b5" }}>{item.username + "/" + item.realName}</span>
        <span style={{ color: "#d8d312" }}>(半拥有)</span>
      </>
    );
  };

  useImperativeHandle(ref, () => ({
    submit,
  }));

  const submit = async () => {
    const { data } = props;
    let variable = false;
    if (data.checkList?.length > 1) {
      const req = {
        assignFlag: true,
        controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1,
        idList: targetKeys,
        projectId: props.data?.breadcrumbItem.length > 1 ? props.data?.breadcrumbItem[1].key : null, //项目id（2，3展示级别不可为null）
        resourceTypeId: props.data?.breadcrumbItem.length > 2 ? props.data?.breadcrumbItem[2].key.split("-")[1] : null, // 资源类别id（3展示级别不可为null）
        userIdList: data.checkList.map((item) => Number(item.id)),
        excludeUserIdList: excludeUserIdList.filter((item) => targetKeys.filter((i) => item === i).length === 0),
      };
      await batchAssign(req).then((res) => {
        variable = true;
      });
    } else {
      const req = {
        controlLevel: menu === TAB_LIST_KEY.adminAuthor ? 2 : 1,
        projectId: data.checkList[0]?.projectId,
        resourceId: data.checkList[0]?.resourceId || null,
        resourceTypeId: data.checkList[0]?.resourceTypeId || null,
        userIdList: targetKeys,
        excludeUserIdList: excludeUserIdList.filter((item) => targetKeys.filter((i) => item === i).length === 0),
      };
      await assignUser(req).then((res) => {
        variable = true;
      });
    }
    return variable;
  };

  const handleChange = (targetKeys: string[]) => {
    setTargetKeys(targetKeys);
    form.setFieldsValue({ users: targetKeys });
    props.callback(targetKeys.sort().toString() === initTargetKeys.sort().toString()); // 按钮的禁用限制
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
        <Spin spinning={loading}>
          <Transfer
            key={"users"}
            dataSource={users}
            titles={["未分配用户", "已分配用户"]}
            targetKeys={targetKeys}
            onChange={handleChange}
            showSearch
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
        <div className="assign-users-header-author">资源权限管理颗粒度：{ParticleType[props.data?.breadcrumbItem.length - 1]}</div>
        <div className="assign-users-header-object">
          资源对象：
          {props.data?.checkList.map((item, index) => {
            return (
              <>
                <Tag key={index}>
                  {props.data?.breadcrumbItem.length === 1
                    ? item.projectName
                    : props.data?.breadcrumbItem.length === 2
                    ? item.resourceTypeName
                    : props.data?.breadcrumbItem.length === 3
                    ? item.resourceName
                    : item.projectName}
                </Tag>
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
export default forwardRef(AssignUsers);
