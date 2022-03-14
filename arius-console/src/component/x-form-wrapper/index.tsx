import * as React from "react";
import { Drawer, Modal, Button, message, Alert } from "antd";
import { XForm as XFormComponent } from "component/x-form";
import { IXFormWrapper } from "interface/common";

export class XFormWrapper extends React.Component<IXFormWrapper> {
  public state = {
    confirmLoading: false,
    formMap: this.props.formMap || ([] as any),
    formData: this.props.formData || {},
  };

  private $formRef: any = React.createRef();

  public componentWillUnmount() {
    this.$formRef = null;
  }

  public updateFormMap$(formMap?: any, formData?: any, isResetForm?: boolean, resetFields?: string[]) {
    if (isResetForm) {
      resetFields ? this.resetForm(resetFields) : this.resetForm();
    }

    this.setState({
      formMap,
      formData,
    });
  }

  public getFieldValue = (key: string) => {
    if (this.$formRef) {
      return this.$formRef.current!.getFieldValue(key);
    }
    return "";
  };

  public setFieldValue = (ob: any) => {
    if (this.$formRef) {
      return this.$formRef.current!.setFieldsValue(ob);
    }
    return "";
  };

  public handleSubmit = () => {
    this.$formRef
      .current!.validateFields()
      .then((result) => {
        const { onSubmit, isWaitting, actionAfterFailedSubmit, actionAfterSubmit } = this.props;

        if (typeof onSubmit === "function") {
          if (isWaitting) {
            this.setState({
              confirmLoading: true,
            });
            onSubmit(result)
              .then((res: any) => {
                this.setState({
                  confirmLoading: false,
                });
                if (typeof actionAfterSubmit === "function") {
                  actionAfterSubmit(res);
                }
                message.success("操作成功");
                this.resetForm();
                this.closeModalWrapper();
              })
              .catch((err) => {
                this.setState({
                  confirmLoading: false,
                  showErrorTip: true,
                });
                if (typeof actionAfterFailedSubmit === "function") {
                  actionAfterFailedSubmit();
                }
              });
            return;
          }

          // tslint:disable-next-line:no-unused-expression
          onSubmit && onSubmit(result);

          // this.resetForm(); //接口报错会情调表单数据
          this.closeModalWrapper();
        }
      })
      .catch((errs) => {});
  };

  public handleCancel = () => {
    const { onCancel } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onCancel && onCancel();
    this.resetForm();
    this.closeModalWrapper();
  };

  public resetForm = (resetFields?: string[]) => {
    // tslint:disable-next-line:no-unused-expression
    this.$formRef && this.$formRef.current!.resetFields(resetFields || "");
  };

  public closeModalWrapper = () => {
    const { onChangeVisible } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChangeVisible && onChangeVisible(false);
  };

  public render() {
    const { type } = this.props;
    switch (type) {
      case "drawer":
        return this.renderDrawer();
      default:
        return this.renderModal();
    }
  }

  public renderDrawer() {
    const { visible, title, width, formLayout, cancelText, okText, customRenderElement, noform, nofooter, onHandleValuesChange } =
      this.props;
    const { formMap, formData } = this.state;

    return (
      <Drawer
        title={title}
        visible={visible}
        width={width}
        closable={true}
        onClose={this.handleCancel}
        destroyOnClose={true}
        footer={
          !nofooter && (
            <div className="footer-btn">
              <Button style={{ marginRight: 10 }} type="primary" onClick={this.handleSubmit}>
                {okText || "确定"}
              </Button>
              <Button onClick={this.handleCancel}>{cancelText || "取消"}</Button>
            </div>
          )
        }
      >
        <>{customRenderElement}</>
        {!noform && (
          <XFormComponent
            layout="vertical"
            wrappedComponentRef={this.$formRef}
            formData={formData}
            formMap={formMap}
            formLayout={formLayout}
            onHandleValuesChange={onHandleValuesChange}
          />
        )}
      </Drawer>
    );
  }

  public renderModal() {
    const { visible, title, width, formLayout, cancelText, okText, layout } = this.props;
    const { formMap, formData } = this.state;

    return (
      <Modal
        width={width || 600}
        title={title}
        visible={visible}
        confirmLoading={this.state.confirmLoading}
        maskClosable={false}
        onOk={this.handleSubmit}
        onCancel={this.handleCancel}
        okText={okText || "确定"}
        cancelText={cancelText || "取消"}
      >
        <XFormComponent
          wrappedComponentRef={this.$formRef}
          formData={formData}
          formMap={formMap}
          formLayout={formLayout}
          layout={layout || "vertical"}
        />
      </Modal>
    );
  }
}
