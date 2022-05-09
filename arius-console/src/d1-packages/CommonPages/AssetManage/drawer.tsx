import React from "react";
import { Drawer, Button, message } from "antd";
import "./index.less";
import AssignUsers from "./DrawerContent/AssignUsers";
import AssignAsset from "./DrawerContent/AssignAsset";
import { IDrawerProps } from "./type";

const drawerMap = {
  AssignUsers: (data: any) => <AssignUsers {...data} key="AssignUsers" />,
  AssignAsset: (data: any) => <AssignAsset {...data} key="AssignAsset" />,
} as {
  [key: string]: (data: any) => JSX.Element;
};

export const AssetDrawer = (props: IDrawerProps): JSX.Element => {
  const drawerKey = props.drawerKey || "AssignUsers";
  const [loading, setLoading] = React.useState(false);
  const [disabled, setDisabled] = React.useState(true);
  const useFormRef = React.useRef();

  const onOK = () => {
    setLoading(true);
    (useFormRef as any).current.submit().then((res) => {
      if (res) {
        switch (drawerKey) {
          case "AssignAsset":
            message.success("分配资源成功");
            break;
          case "AssignUsers":
            message.success("分配用户成功");
            break;
          default:
            message.success("操作成功");
            break;
        }
        props.reloadData();
        onClose();
      } else {
        switch (drawerKey) {
          case "AssignAsset":
            message.success("分配资源失败");
            break;
          case "AssignUsers":
            message.success("分配用户失败");
            break;
          default:
            message.success("操作失败");
            break;
        }
        onClose();
      }
      setLoading(false);
    });
  };

  const renderFooter = () => {
    if (props.footer) return props.footer;
    return (
      <div
        style={{
          textAlign: "left",
        }}
      >
        <Button disabled={disabled} loading={loading} onClick={onOK} type="primary">
          确定
        </Button>
        <Button onClick={onClose} style={{ marginLeft: 10 }}>
          取消
        </Button>
      </div>
    );
  };

  const { title, width, onClose, visible } = props;
  return (
    <Drawer title={title || "分配用户"} width={width || 600} onClose={onClose} visible={visible || false} footer={renderFooter()}>
      {drawerMap[drawerKey]({ data: props.data, callback: (b: boolean) => setDisabled(b), ref: useFormRef })}
    </Drawer>
  );
};
