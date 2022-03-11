import React, { useState, useEffect, FC } from "react";
import { CheckCircleFilled } from "@ant-design/icons";
import { Button, message, Drawer, Form, Input, Row, Col, Transfer, Checkbox } from "antd";
import { readableForm } from "./config";
import { queryRoleDetail, createOrUpdateRole, assignRole, queryAssignedUserByRole, queryPermissionTree } from "./service";
import "./detail.less";

const { TextArea } = Input;
const basicClass = "role-tpl-form";
enum Eflag {
  detail = "角色详情",
  create = "新建角色",
  update = "编辑角色",
  assign = "分配角色",
}
export default function Detail(props: any) {
  const { detailVisible, flag, submitCb, roleId } = props;

  const [form] = Form.useForm();
  const [visible, setVisible] = useState(detailVisible);
  const [userList, setUserList] = useState([]);
  const [submitDisabled, setSubmitDisabled] = useState(flag === "assign");
  const [submitLoading, setSubmitLoading] = useState(false);
  const [initialTargetKeys, setInitialTargetKeys] = useState([]);
  const [targetKeys, setTargetKeys] = useState(initialTargetKeys);
  const [selectedKeys, setSelectedKeys] = useState([]);
  const [detailData, setDetailData] = useState({
    // userVoList: [],
    // permissionVo: {},
  });
  const [permissionVo, setPermissionVo] = useState([]);
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
      idList: targetKeys,
    };
    setSubmitLoading(true);
    assignRole(params)
      .then(() => {
        message.success("提交成功");
        submitCb();
      })
      .finally(() => {
        setSubmitLoading(false);
      });
  };

  const handleCreateOrUpdate = () => {
    form
      .validateFields()
      .then((values) => {
        const permissionIdList = getIdList(permissionVo);
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
    submitCb();
  };

  const fetchDetail = async (roleId) => {
    const res: any = await queryRoleDetail(roleId);
    const permissionVoData =
      res.permissionTreeVO &&
      res.permissionTreeVO.childList.map((item) => {
        return {
          ...item,
          isCheckAll: item.childList.every((subItem) => subItem.has),
        };
      });
    setPermissionVo(permissionVoData);
    if (flag !== "update") {
      setDetailData(res);
    } else {
      form.setFieldsValue(res);
    }
  };

  const fetchUserList = async (name?: string) => {
    const res: [] = await queryAssignedUserByRole(roleId, name);
    const initVal = res.filter((item: any) => item.has).map((item: any) => item.id);
    setUserList(res);
    setInitialTargetKeys(initVal);
  };

  const fetchPermissionTree = async () => {
    const res: any = await queryPermissionTree();
    setPermissionVo(res.childList);
  };

  const toggleChange = (index, parentIndex, isAll) => {
    if (parentIndex !== undefined && !permissionVo[parentIndex].has) {
      return;
    }

    if (isAll && !permissionVo[index].has) {
      return;
    }

    const data = [...permissionVo];
    if (isAll) {
      const isCheckAll = data[index].isCheckAll;
      data[index].isCheckAll = !isCheckAll;
      data[index].childList.forEach((subItem) => {
        subItem.has = data[index].isCheckAll;
      });
    } else {
      if (parentIndex === undefined) {
        data[index].has = !data[index].has;
        if (!data[index].has) {
          data[index].isCheckAll = false;
          data[index].childList.forEach((item) => {
            item.has = false;
          });
        }
      } else {
        data[parentIndex].childList[index].has = !data[parentIndex].childList[index].has;
        data[parentIndex].isCheckAll = data[parentIndex].childList.every((subItem) => subItem.has);
      }
    }
    setPermissionVo(data);
  };

  const renderCheckItem = ({ item, index, parentIndex = undefined, isAll = false }) => {
    const isChecked = isAll ? item.isCheckAll : item.has;
    return (
      <>
        <CheckCircleFilled
          onClick={flag === "detail" ? null : () => toggleChange(index, parentIndex, isAll)}
          style={{
            color: isChecked ? "#46D677" : "#ddd",
            cursor:
              flag === "detail" || (parentIndex !== undefined && !permissionVo[parentIndex].has) || (isAll && !item.has)
                ? "not-allowed"
                : "pointer",
            marginRight: "4px",
          }}
        />
        <span>{isAll ? "全部操作" : item.permissionName}</span>
      </>
    );
  };

  const renderReadOnlyCol = () => {
    return readableForm.map((item, i) => {
      return (
        <Col key={i} span={24} className={i && `${basicClass}-readonlyText`}>
          <span className="read-lable">{item.label}：</span>
          <span className="read-content">{detailData[item.prop]}</span>
        </Col>
      );
    });
  };

  const renderEditableCol = () => {
    return (
      <Form layout="vertical" form={form}>
        <Row>
          <Col span={24}>
            <Form.Item label="角色名称" name="roleName" rules={[{ required: true, message: "请输入角色名称" }]}>
              <Input placeholder="请输入角色名称" maxLength={128} />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="描述" name="description" rules={[{ required: true, message: "请输入描述" }]}>
              <TextArea placeholder="请输入描述" maxLength={512} />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    );
  };

  const renderEmpty = () => {
    return (
      <div className="empty-item">
        <span>空</span>
      </div>
    );
  };

  const renderContent = () => {
    return permissionVo.map((item, index) => {
      return !item.has && flag === "detail" ? null : (
        <div className="tr tBody" key={item.id}>
          <div className="td col-menu">{renderCheckItem({ item, index })}</div>
          <div className="td">
            <div className="col-permission">
              <div className="permission-all">{renderCheckItem({ item, index, isAll: true })}</div>
              <div className="permission-content">
                {item.childList.length > 0 &&
                  item.childList.map((subItem, subIndex) => (
                    <div key={subItem.id} className="content-item">
                      {renderCheckItem({ item: subItem, index: subIndex, parentIndex: index })}
                    </div>
                  ))}
              </div>
            </div>
          </div>
        </div>
      );
    });
  };

  const renderRolePermission = () => {
    return (
      <>
        <h4 className="bind-role-title">角色绑定权限项</h4>
        <div className="bind-role-table">
          <div className="tr tHead">
            <div className="td">菜单</div>
            <div className="td">权限项</div>
          </div>
          {flag === "detail" && permissionVo.every((item) => !item.has) ? renderEmpty() : renderContent()}
        </div>
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
        {flag === "detail" ? (
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

  const isEqualArray = (originArr, updateArr) => {
    return originArr.length === updateArr.length && originArr.every((item) => updateArr.includes(item));
  };

  const onChange = (nextTargetKeys) => {
    setSubmitDisabled(isEqualArray(nextTargetKeys, initialTargetKeys) || nextTargetKeys.length < 1);
    setTargetKeys(nextTargetKeys);
  };

  const onSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
    setSelectedKeys([...sourceSelectedKeys, ...targetSelectedKeys]);
  };

  const onSearch = (dir, value) => {
    console.log(dir, value);
  };

  const renderTransfer = () => {
    return (
      <div style={{ width: "100%", height: "600px", marginTop: "24px" }}>
        <Transfer
          dataSource={userList}
          titles={["未分配用户", "已分配用户"]}
          targetKeys={targetKeys}
          selectedKeys={selectedKeys}
          showSearch={true}
          onChange={onChange}
          onSearch={onSearch}
          onSelectChange={onSelectChange}
          rowKey={(record) => record.id}
          render={(item) => `${item.name}`}
        />
      </div>
    );
  };

  useEffect(() => {
    setVisible(detailVisible);
  }, [detailVisible]);

  useEffect(() => {
    setTargetKeys(initialTargetKeys);
  }, [initialTargetKeys]);

  useEffect(() => {
    flag === "assign" && fetchUserList();
    flag === "create" ? fetchPermissionTree() : fetchDetail(roleId);
  }, []);

  return (
    <Drawer
      className="role-manage-detail"
      width="640"
      title={Eflag[flag] || ""}
      onClose={onClose}
      visible={visible}
      bodyStyle={{ paddingBottom: 80 }}
      footer={renderFooter()}
    >
      {(flag === "detail" || flag === "assign") && renderReadOnlyCol()}
      {(flag === "create" || flag === "update") && renderEditableCol()}
      {flag === "assign" ? renderTransfer() : renderRolePermission()}
    </Drawer>
  );
}
