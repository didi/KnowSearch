import React, { useState, useEffect } from "react";
import { Button, message, Drawer, Col, Transfer, Descriptions, Alert } from "antd";
import { readableForm } from "./config";
import { queryUserDetail, queryAssignedRoleByUser, assignRoleToUser } from "./service";

import "./detail.less";
import PermissionTree from "../RoleManage/PermissionTree";
enum Eflag {
  detail = "用户详情",
  update = "分配角色",
}
export default function Detail(props: any) {
  const { detailVisible, flag, setDetailVisible, submitCb, userId } = props;
  const [curretFlag, setCurretFlag] = useState(flag);

  const [visible, setVisible] = useState(detailVisible);
  const [roleList, setRoleList] = useState([]);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitDisabled, setSubmitDisabled] = useState(true);
  const [initialTargetKeys, setInitialTargetKeys] = useState([]);
  const [targetKeys, setTargetKeys] = useState(initialTargetKeys);
  const [selectedKeys, setSelectedKeys] = useState([]);
  const [permissionVo, setPermissionVo] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formModel, setFormModel] = useState({
    deptList: [],
    roleList: [],
  });

  useEffect(() => {
    setCurretFlag(curretFlag);
  }, [flag]);

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
    setDetailVisible(false);
  };

  const fetchDetail = async (userId) => {
    setLoading(true);
    try {
      const data = await queryUserDetail(userId);
      const { permissionTreeVO } = data;
      const permissionVoData = permissionTreeVO.childList.map((item) => {
        return {
          ...item,
          isCheckAll: item.childList.every((subItem) => subItem.has),
        };
      });
      setPermissionVo(permissionVoData);
      setFormModel(data);
      setLoading(false);
    } catch (err) {
      setLoading(false);
    }
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
    const formCol = readableForm.filter((item) => item.flag.includes(curretFlag));
    const hasAlert = curretFlag === "detail" && !loading && permissionVo.every((item) => !item.has);
    return (
      <Descriptions className={`custom-desc ${hasAlert ? "" : "no-alert"}`} title="" column={2}>
        {formCol.map((row, index) => (
          <Descriptions.Item className="read-lable" label={row.label} key={index}>
            <span className="read-content">
              {Array.isArray(row.prop)
                ? row.prop.length > 0 && row.prop.map((k) => data[k]).join("/")
                : row.render
                ? row.render(data?.[row.prop])
                : data[row.prop]}
            </span>
          </Descriptions.Item>
        ))}
      </Descriptions>
    );
  };

  const renderAlert = () => {
    return curretFlag === "detail" && !loading && permissionVo.every((item) => !item.has) ? (
      <>
        <Alert
          className="detail-alert"
          showIcon
          message={
            <span>
              当前用户未分配角色，无任何权限，如需分配，请点击：{" "}
              <a type="javascript;" onClick={() => setCurretFlag("update")}>
                分配角色
              </a>
            </span>
          }
          type="info"
        />
        <div style={{ height: 40 }}></div>
      </>
    ) : null;
  };

  const renderContent = () => {
    const permissionData = permissionVo.filter((item) => item.has);
    return <PermissionTree isEdit={false} permissionData={permissionData} />;
  };

  const renderPermission = () => {
    return (
      <>
        {curretFlag === "detail" ? <h4 className="bind-role-title"></h4> : null}
        {curretFlag === "detail" && permissionVo.every((item) => !item.has) ? null : renderContent()}
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
        {curretFlag === "detail" ? (
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
      title={Eflag[curretFlag] || ""}
      onClose={onClose}
      visible={visible}
      footer={renderFooter()}
    >
      {renderAlert()}
      {renderReadCol()}
      {curretFlag === "detail" ? renderPermission() : renderTransfer()}
    </Drawer>
  );
}
