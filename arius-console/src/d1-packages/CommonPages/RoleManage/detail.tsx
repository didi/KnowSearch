import React, { useState, useEffect, FC, useRef } from "react";
import { Button, message, Drawer, Form, Input, Row, Col, Transfer, Checkbox, Descriptions } from "antd";
import { readableForm } from "./config";
import { queryRoleDetail, createOrUpdateRole, assignRole, queryPermissionTree, getRolePermission } from "./service";
import "./detail.less";
import PermissionTree from "./PermissionTree";
import BindUser from "./BindUser";
import RecylceUser from "./RecycleUser";

const { TextArea } = Input;

enum Eflag {
  detail = "角色详情",
  create = "新建角色",
  update = "编辑角色",
  assign = "绑定用户",
  recycle = "回收用户",
}
export default function Detail(props: any) {
  const { detailVisible, setDetailVisible, flag, submitCb, roleId, width, roleName } = props;

  const [form] = Form.useForm();
  const [submitDisabled, setSubmitDisabled] = useState(flag === "assign");
  const [submitLoading, setSubmitLoading] = useState(false);
  const [detailData, setDetailData] = useState({
    // userVoList: [],
    // permissionVo: {},
  });
  const [permissionVo, setPermissionVo] = useState([]);
  const [loading, setLoading] = useState(true);

  const permissionRef: any = React.createRef();
  const currentSelectUser = useRef([]);

  const onSubmit = () => {
    if (flag === "assign") {
      handleAssign();
    } else {
      handleCreateOrUpdate();
    }
  };

  const getIdList = (permissionVo) => {
    return permissionVo.reduce((res, curr) => {
      if (curr.has && !res.includes(curr.id)) {
        res.push(curr.id);
      }
      return res.concat(curr.childList && curr.childList.length > 0 ? getIdList(curr.childList) : []);
    }, []);
  };

  const handleAssign = () => {
    const params = {
      id: roleId,
      flag: false, // true：N个角色分配给1个用户、false：1个角色分配给N个用户
      idList: currentSelectUser.current,
    };
    setSubmitLoading(true);
    assignRole(params)
      .then(() => {
        message.success("提交成功");
        setSubmitLoading(false);
        submitCb();
      })
      .catch(() => {
        setSubmitLoading(false);
      });
  };

  const handleCreateOrUpdate = () => {
    form
      .validateFields()
      .then((values) => {
        const permissionIdList = permissionRef.current.getPermissionIdList();

        if (permissionIdList.length < 1) {
          message.warning("请勾选权限项");
          return;
        }
        const isCreate = flag === "create";
        const params = isCreate
          ? {
              ...values,
              permissionIdList,
            }
          : {
              ...values,
              id: roleId,
              permissionIdList,
            };

        setSubmitLoading(true);
        createOrUpdateRole(isCreate, params)
          .then(() => {
            message.success("提交成功");
            submitCb();
          })
          .finally(() => {
            setSubmitLoading(false);
          });
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const onClose = () => {
    setDetailVisible(false);
  };

  const filterPermission = (permissionData: any[]) => {
    let _permissionData = Array.from(permissionData).filter((item) => item.has);
    for (let permission of _permissionData) {
      permission.childList = (permission.childList || []).filter((item) => item.has);
    }
    return _permissionData;
  };

  const fetchDetail = async (roleId) => {
    setLoading(true);
    try {
      const res: any = await queryRoleDetail(roleId);
      let permissionVoData =
        res.permissionTreeVO &&
        res.permissionTreeVO.childList.map((item) => {
          if (res.roleName !== "管理员" && item.permissionName === "应用管理") {
            // 非管理员无访问设置权限
            item.childList = (item?.childList || []).filter((child) => child.permissionName !== "访问设置");
          }
          if (res.roleName === "管理员" && item.permissionName === "物理集群") {
            // 0.3版本隐藏绑定Gateway功能
            item.childList = (item?.childList || []).filter((child) => child.has);
          }
          return {
            ...item,
            isCheckAll: item.childList.every((subItem) => subItem.has),
          };
        });

      if (flag !== "update") {
        setDetailData(res);
      } else {
        form.setFieldsValue(res);
        // 编辑权限点时展示所有权限点
        const roleList: any = await getRolePermission();

        // roleList?.childList 中has表示该角色是否能展示权限点
        const renderRoleList = (roleList?.childList || []).filter((item) => item.has);

        for (let item of renderRoleList) {
          // permissionVoData中has表示该权限点是否生效
          item.childList = (item.childList || []).filter((row) => row.has);
        }

        for (let parent of renderRoleList) {
          // permissionVoData中has表示该权限点是否生效
          parent.has = !!permissionVoData.find((row) => row.id === parent.id)?.has;
          const childList = parent.childList || [];
          for (let child of childList) {
            // permissionVoData中has表示该权限点是否生效
            const pChildList = permissionVoData.find((row) => row.id === parent.id)?.childList || [];
            child.has = !!pChildList.find((row) => row.id === child.id)?.has;
          }
          parent.childList = childList;
        }
        permissionVoData = renderRoleList;
      }
      setPermissionVo(permissionVoData);
      setLoading(false);
    } catch (err) {
      setLoading(false);
    }
  };

  const fetchPermissionTree = async () => {
    const res: any = await getRolePermission();
    let permissionVoData = (res.childList || []).map((item) => {
      if (item.permissionName === "应用管理") {
        // 新建角色无访问设置权限
        item.childList = (item?.childList || []).filter((child) => child.permissionName !== "访问设置");
      }
      return {
        ...item,
      };
    });
    setPermissionVo(permissionVoData);
  };

  const renderReadOnlyCol = () => {
    let titleForm = readableForm.map((item, i) => {
      return (
        <Col key={i} span={24} className="mb-10">
          <div className="read-lable">{item.label}：</div>
          <div className="read-content">{detailData[item.prop]}</div>
        </Col>
      );
    });
    let bindAdminTips = (
      <Col key={"tips"} span={24} className="mb-10">
        <div className="bind-admin-tips">
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconinfo-circle"></use>
          </svg>
          管理员角色用户会拥有全部应用的管理权限，包括SuperApp，请谨慎分配！
        </div>
      </Col>
    );
    return (
      <>
        {roleName === "管理员" && flag === "assign" && bindAdminTips}
        {titleForm}
      </>
    );
  };

  const renderEditableCol = () => {
    return (
      <Form layout="vertical" form={form}>
        <Row>
          <Col span={24}>
            <Form.Item
              label="角色名称"
              name="roleName"
              rules={[
                {
                  required: true,
                  //message: "请输入角色名称",
                  validator: async (rule: any, value: string) => {
                    let flat_1_50 = value && value.length >= 3 && value.length <= 20;
                    if (!value) {
                      return Promise.reject("请输入角色名称，支持3-20个字符");
                    }
                    if (flat_1_50) {
                      return Promise.resolve();
                    } else {
                      return Promise.reject("请输入角色名称，支持3-20个字符");
                    }
                  },
                },
              ]}
            >
              <Input allowClear style={{ width: "60%" }} placeholder="请输入角色名称" />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item
              label="描述"
              name="description"
              rules={[
                {
                  required: true,
                  validator: async (rule: any, value: string) => {
                    if (!value) {
                      return Promise.reject("请输入描述，支持1-100个字符");
                    }
                    if (value && value.length > 100) {
                      return Promise.reject("请输入描述，支持1-100个字符");
                    }

                    return Promise.resolve();
                  },
                },
              ]}
            >
              <TextArea allowClear style={{ width: "60%" }} placeholder="请输入描述，支持1-100个字符" />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    );
  };

  const renderEmpty = () => {
    return (
      <div className="empty-item">
        <span>暂无权限项</span>
      </div>
    );
  };

  const renderContent = () => {
    // 编辑权限点不做过滤，查看详情和新建角色过滤掉不可展示的权限点
    const permissionData = flag === "detail" || flag === "create" ? filterPermission(permissionVo) : permissionVo;
    return (
      <PermissionTree
        ref={permissionRef}
        isUpdate={flag === "update" || flag === "create"}
        isEdit={flag !== "detail"}
        permissionData={permissionData}
      />
    );
  };

  const renderRolePermission = () => {
    return (
      <>
        {flag === "detail" ? <h4 className="bind-role-title"></h4> : null}
        {flag === "detail" && !loading && permissionVo.every((item) => !item.has) ? renderEmpty() : renderContent()}
      </>
    );
  };

  const renderFooter = () => {
    return (
      <div
        style={{
          textAlign: "left",
        }}
      >
        {flag === "detail" || flag === "recycle" ? (
          <Button onClick={onClose} type="primary">
            关闭
          </Button>
        ) : (
          <Button disabled={submitDisabled} loading={submitLoading} onClick={onSubmit} type="primary">
            保存
          </Button>
        )}
      </div>
    );
  };

  const onUserChange = (canSubmit: Boolean, userList: number[]) => {
    setSubmitDisabled(!canSubmit);
    currentSelectUser.current = userList;
  };

  const renderOpTable = () => {
    return (
      <div className="edit-table">
        {flag === "assign" ? (
          <BindUser roleId={props.roleId} onUserChange={onUserChange} />
        ) : (
          <RecylceUser submitCb={submitCb} roleId={props.roleId} roleName={roleName} />
        )}
      </div>
    );
  };

  useEffect(() => {
    flag === "create" ? fetchPermissionTree() : fetchDetail(roleId);
    return () => {
      permissionRef.current = null;
      currentSelectUser.current = null;
    };
  }, []);

  return (
    <Drawer
      className="role-manage-detail"
      width={width || 800}
      title={Eflag[flag] || ""}
      onClose={onClose}
      visible={detailVisible}
      footer={renderFooter()}
    >
      <div style={{ paddingTop: 16 }}>
        {(flag === "detail" || flag === "assign" || flag === "recycle") && renderReadOnlyCol()}
        {(flag === "create" || flag === "update") && renderEditableCol()}
        {flag === "assign" || flag === "recycle" ? renderOpTable() : renderRolePermission()}
      </div>
    </Drawer>
  );
}
