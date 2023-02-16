import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { FormItemType, IFormItem } from "component/x-form";
import { Transfer } from "antd";
import { useGlobalLoginStatus } from "store";
import { getUserList } from "api/logi-security";
import { createProject, getUnassignedUsers, updateProject, updateOwner, updateUser } from "api/app-api";
import { UserSelect } from "component/UserSelect";
import { getCookie } from "lib/utils";
import "./index.less";
import Tooltip from "antd/es/tooltip";

interface ITaskValue {
  taskName: string;
  sinkType: string;
  sourceType: string;
  taskDesc: string;
  owners: string[];
  quota: number;
}

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const UserTransfer = (props: any) => {
  const { value, list, isNew, onChange, userListWithAdminRole } = props;

  const [targetKeys, setTargetKeys] = React.useState(value || []);
  const [dataSource, setDataSource] = React.useState(list || []);
  const filterOption = (inputValue: any, option: any) => option?.text?.indexOf(inputValue) > -1;

  React.useEffect(() => {
    if (isNew) {
      let adminIdList = (userListWithAdminRole || []).map((item) => item?.id);
      let initList = [+getCookie("userId")];
      if (getCookie("isAdminUser") === "yes") {
        initList = [...initList, ...adminIdList];
      }
      setTargetKeys(initList);
      onChange(initList);
      getDataSource();
    }
  }, [userListWithAdminRole]);

  React.useEffect(() => {
    getDataSource();
  }, [list, targetKeys]);

  const getDataSource = () => {
    let dataSource = (list || []).map((item) => {
      if (targetKeys.includes(item?.key)) {
        let adminIdList = (userListWithAdminRole || []).map((item) => item?.id);
        if (adminIdList.includes(item?.key)) {
          return {
            ...item,
            disabled: true,
            label: <Tooltip title="管理员角色用户，不可删除！">{item.label}</Tooltip>,
            text: item.label,
          };
        } else {
          return { ...item, text: item.label };
        }
      } else {
        return { ...item, text: item.label };
      }
    });
    setDataSource(dataSource);
  };

  const handleChange = (targetKeys: any) => {
    setTargetKeys(targetKeys);
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(targetKeys);
  };

  return (
    <Transfer
      dataSource={dataSource}
      showSearch={true}
      filterOption={filterOption}
      titles={["未分配用户", "已分配用户"]}
      targetKeys={targetKeys}
      onChange={handleChange}
      render={(item) => item.label}
      listStyle={{ width: 290 }}
    />
  );
};

const AddOrEditProjectModal = (props: { dispatch: any; cb: any; params: any }) => {
  const { params = {}, cb } = props;
  const { type, callback, ownersList, usersList } = params || {};
  const [loginStatus, setLoginStatus] = useGlobalLoginStatus();
  const [list, setList] = useState(usersList);
  const [targetKeys, setTargetKeys] = useState(params?.userIdList || []);
  const [ownerIdList, setOwnerIdList] = useState([]);
  const [userListWithAdminRole, setUserListWithAdminRole] = useState([]);

  const fetchOptions = (value, size = 100) => {
    let params = {
      userName: value,
      page: 1,
      size,
    } as any;
    let isAdmin = getCookie("isAdminUser") === "yes";
    if (!isAdmin) {
      params.containsAdminRole = false;
    }
    return getUserList(params)
      .then((res) => {
        return res?.bizData || [];
      })
      .catch((err) => {
        return [];
      });
  };

  const getInitUserList = () => {
    if (!params || type === "create") {
      // 如果是新建项目调用全量用户接口
      fetchOptions("", type === "create" ? 100 : 1000).then((res) => {
        const data = (res || []).map((item) => ({
          label: item.userName,
          value: item.id,
          title: item.id,
          key: item.id,
        }));
        setList(data);
        setUserListWithAdminRole(res?.[0]?.userListWithAdminRole || []);
      });
    } else {
      // 如果是编辑项目调用未分配用户接口
      getUnassignedUsers(params.id).then((res) => {
        res = (res || []).map((item) => {
          return {
            label: item.userName,
            title: item.id,
            key: item.id,
          };
        });
        setList([...list, ...res]);
      });
    }
  };

  useEffect(() => {
    getInitUserList();
  }, []);

  const addProject = async (values) => {
    return createProject({
      project: {
        deptId: 0, // TODO: 后端接口待确认
        ...values,
        userIdList: values?.userIdList || [],
      },
      config: {
        memo: values.description,
        slowQueryTimes: +values.slowQueryTimes,
      },
    });
  };

  const editProject = async (values) => {
    let adminIdList = (params?.userListWithAdminRole || []).map((item) => item.id);
    let userList = (targetKeys || []).filter((item) => !adminIdList.includes(item));

    let project = {
      deptId: 0,
      id: params.id,
      description: values.description,
      projectName: values.projectName,
      slowQueryTimes: values.slowQueryTimes,
    };

    await updateOwner(params?.id, ownerIdList);
    await updateUser(params?.id, userList);
    return updateProject({
      project,
      config: {
        memo: values.description,
        slowQueryTimes: +values.slowQueryTimes,
      },
    });
  };

  const renderFormMap = () => {
    let formMap = [
      {
        key: "tips",
        type: FormItemType.custom,
        customFormItem: (
          <div className="project-tips">
            <svg className="icon svg-icon svg-style" aria-hidden="true">
              <use xlinkHref="#iconinfo-circle"></use>
            </svg>
            SuperApp无需分配用户，管理员角色的用户默认加入到SuperApp中
          </div>
        ),
      },
      {
        key: "projectName",
        label: "应用名称",
        attrs: {
          placeholder: "请输入应用名称",
        },
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              const reg = /^[a-zA-Z0-9\u4e00-\u9fa5][a-zA-Z0-9_\-\u4e00-\u9fa5]+$/;

              if (!value) {
                return Promise.reject("请输入应用名称");
              }
              if (value && (value.length > 30 || value.length < 8)) {
                return Promise.reject("请输入8-30字符");
              }
              if (!reg.test(value)) {
                return Promise.reject("应用名称不能以_、-为前缀， 支持中文、英文、数字、-、_，8-30位字符");
              } else {
                return Promise.resolve();
              }
            },
          },
        ],
      },
      {
        key: "ownerIdList",
        label: "责任人",
        type: FormItemType.custom,
        defaultValue: [+getCookie("userId")],
        customFormItem: (
          <UserSelect
            targetKeys={targetKeys}
            list={list}
            ownersList={ownersList}
            isNew={!params || type === "create"}
            fetchOptions={fetchOptions}
            setOwnerIdList={setOwnerIdList}
            mode="multiple"
            placeholder="请选择责任人"
          />
        ),
        rules: [
          {
            required: true,
            message: "请选择责任人",
          },
        ],
      },
      {
        key: "userIdList",
        type: FormItemType.custom,
        label: "用户分配",
        invisible: type === "create",
        customFormItem: (
          <UserTransfer
            list={list}
            isNew={!params || type === "create"}
            userListWithAdminRole={params?.userListWithAdminRole || userListWithAdminRole}
            fetchOptions={(value) => fetchOptions(value, 2000)}
            onChange={(key) => {
              setTargetKeys(key);
            }}
          />
        ),
      },
      {
        key: "slowQueryTimes",
        type: FormItemType.input,
        label: "慢查询耗时",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              const reg = /^[1-9]\d*$/;
              if (!reg.test(value)) {
                return Promise.reject("请输入非0正整数");
              } else if (+value > 1000000) {
                return Promise.reject("请输入小于1000000的正整数");
              } else {
                return Promise.resolve();
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入慢查询耗时",
          suffix: <span> ms</span>,
        },
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "备注",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (!value) {
                return Promise.reject("请输入1-100字备注");
              }
              if (value && value.length > 100) {
                return Promise.reject("请输入1-100字备注");
              }
              return Promise.resolve();
            },
          },
        ],
        attrs: {
          placeholder: "请输入1-100字备注",
        },
      },
    ] as IFormItem[];
    if (params?.projectName === "SuperApp") {
      formMap.splice(3, 1);
    } else {
      formMap = formMap.slice(1);
    }
    return { formMap };
  };

  const xFormModalConfig = {
    visible: true,
    title: type === "create" ? "创建应用" : props.params ? "编辑应用" : "新建应用",
    formData: { slowQueryTimes: 1000, ...props.params } || { slowQueryTimes: 1000 },
    isWaitting: true,
    width: 660,
    onChangeVisible: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onCancel: () => {
      if (type === "create") {
        callback();
      }
    },
    onSubmit: (value: ITaskValue) => {
      if (!params || type === "create") {
        return addProject(value);
      }
      if (params) {
        return editProject(value);
      }
    },
    actionAfterSubmit: () => {
      if (type === "create") {
        return setLoginStatus(!loginStatus);
      }
      cb && cb();
    },
  };

  return (
    <>
      <XFormWrapper visible={true} type="drawer" {...xFormModalConfig} {...renderFormMap()} />
    </>
  );
};

export default connect(mapStateToProps)(AddOrEditProjectModal);
