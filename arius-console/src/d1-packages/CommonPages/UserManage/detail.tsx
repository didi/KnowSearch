import React, { useState, useEffect } from "react";
import { CheckCircleFilled } from "@ant-design/icons";
import { Button, message, Drawer, Col, Transfer } from "antd";
import { readableForm } from "./config";
import { queryUserDetail, queryAssignedRoleByUser, assignRoleToUser } from "./service";

import "./detail.less";
const basicClass = "user-tpl-form";
enum Eflag {
  detail = "用户详情",
  update = "分配角色",
}
export default function Detail(props: any) {
  const { detailVisible, flag, closeDetail, submitCb, userId } = props;
  const [visible, setVisible] = useState(detailVisible);
  const [roleList, setRoleList] = useState([]);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitDisabled, setSubmitDisabled] = useState(true);
  const [initialTargetKeys, setInitialTargetKeys] = useState([]);
  const [targetKeys, setTargetKeys] = useState(initialTargetKeys);
  const [selectedKeys, setSelectedKeys] = useState([]);
  const [permissionVo, setPermissionVo] = useState([]);
  const [formModel, setFormModel] = useState({
    deptList: [],
    roleList: [],
  });

  const onSubmit = () => {
    const params = {
      id: userId,
      flag: true, // true：N个角色分配给1个用户、false：1个角色分配给N个用户
      idList: targetKeys,
    };
    setSubmitLoading(true);
    assignRoleToUser(params)
      .then(() => {
        message.success("提交成功");
        submitCb();
      })
      .finally(() => {
        setSubmitLoading(false);
      });
  };

  const onClose = () => {
    submitCb();
  };

  const fetchDetail = async (userId) => {
    const data = await queryUserDetail(userId);
    const { permissionTreeVO } = data;
    const permissionVoData = permissionTreeVO.childList.map((item) => {
      return {
        ...item,
        isCheckAll: item.childList.every((subItem) => subItem.has),
      };
    });
    setPermissionVo(permissionVoData);
    console.log(data, "data");
    setFormModel(data);
  };

  const fetchRoleList = async (name?: string) => {
    const res: any = await queryAssignedRoleByUser(userId, name);
    const assignedRoles = res.filter((item) => item.has).map((item) => item.id);
    setInitialTargetKeys(assignedRoles);
    setRoleList(res);
  };

  const renderReadCol = () => {
    const data = {
      ...formModel,
    };
    const formCol = readableForm.filter((item) => item.flag.includes(flag));
    return formCol.map((item, i) => {
      return (
        <Col key={i} span={24} className={i && `${basicClass}-readonlyText`}>
          <span className="read-lable">{item.label}：</span>
          <span className="read-content">
            {Array.isArray(item.prop)
              ? item.prop.length > 0 && item.prop.map((k) => data[k]).join("/")
              : item.render
              ? item.render(data && data[item.prop])
              : data[item.prop]}
          </span>
        </Col>
      );
    });
  };

  const renderCheckItem = (item: any, isAll = false) => {
    const isChecked = isAll ? item.isCheckAll : item.has;
    return (
      <>
        <CheckCircleFilled style={{ color: isChecked ? "#46D677" : "#ddd", cursor: "not-allowed", marginRight: "4px" }} />
        <span>{isAll ? "全部操作" : item.permissionName}</span>
      </>
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
    return permissionVo
      .filter((item) => item.has)
      .map((item) => {
        return (
          <div className="tr tBody" key={item.id}>
            <div className="td col-menu">{renderCheckItem(item)}</div>
            <div className="td">
              <div className="col-permission">
                <div className="permission-all">{renderCheckItem(item, true)}</div>
                <div className="permission-content">
                  {item.childList.length > 0 &&
                    item.childList.map((subItem) => (
                      <div key={subItem.id} className="content-item">
                        {renderCheckItem(subItem)}
                      </div>
                    ))}
                </div>
              </div>
            </div>
          </div>
        );
      });
  };

  const renderPermission = () => {
    return (
      <>
        <h4 className="bind-role-title">角色绑定权限项</h4>
        <div className="bind-role-table">
          <div className="tr tHead">
            <div className="td">菜单</div>
            <div className="td">权限项</div>
          </div>
          {permissionVo.every((item) => !item.has) ? renderEmpty() : renderContent()}
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

  const onChange = (nextTargetKeys, direction, moveKeys) => {
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
          dataSource={roleList}
          titles={["未分配角色", "已分配角色"]}
          targetKeys={targetKeys}
          selectedKeys={selectedKeys}
          showSearch={true}
          onChange={onChange}
          onSearch={onSearch}
          onSelectChange={onSelectChange}
          rowKey={(record) => record.id}
          render={(record) => record.name}
        />
      </div>
    );
  };

  useEffect(() => {
    setVisible(detailVisible);
  }, [detailVisible]);

  useEffect(() => {
    fetchDetail(userId);
    fetchRoleList();
  }, []);

  useEffect(() => {
    setTargetKeys(initialTargetKeys);
  }, [initialTargetKeys]);

  return (
    <Drawer
      className="user-manage-detail"
      width="640"
      title={Eflag[flag] || ""}
      onClose={onClose}
      visible={visible}
      bodyStyle={{ paddingBottom: 80 }}
      footer={renderFooter()}
    >
      {renderReadCol()}
      {flag === "detail" ? renderPermission() : renderTransfer()}
    </Drawer>
  );
}
