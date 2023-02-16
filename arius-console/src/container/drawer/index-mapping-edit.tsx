import * as React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Drawer, Button } from "antd";
import { SetMapping } from "../index-admin/component";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const connects: any = connect;

@connects(mapStateToProps)
export class EditIndexMapping extends React.Component<any, any> {
  public state = {
    btnLoading: false,
    disabled: false,
    mapping: "",
  };

  $mappingRef: any = React.createRef();

  public updateState = (keyValue) => {
    this.setState(keyValue);
  };

  setValid = (valid: boolean) => {
    this.setState({
      disabled: !valid,
    });
  };

  public handleSubmit = () => {
    this.$mappingRef.handleSave(() => {
      this.props.dispatch(actions.setDrawerId(""));
      this.props.cb && this.props.cb(); // 重新获取数据列表
    });
  };

  public handleCancel = () => {
    this.props.dispatch(actions.setDrawerId(""));
  };

  public render() {
    const { btnLoading, mapping } = this.state;

    return (
      <>
        <Drawer
          title="编辑Mapping"
          visible={true}
          width={600}
          closable={true}
          maskClosable={false}
          destroyOnClose={true}
          onClose={this.handleCancel}
          footer={
            <div className="footer-btn">
              <Button disabled={this.state.disabled} className="mr-10" type="primary" loading={btnLoading} onClick={this.handleSubmit}>
                确定
              </Button>
              <Button onClick={this.handleCancel}>取消</Button>
            </div>
          }
        >
          <div className="warning-container" style={{ margin: "0 -24px 16px" }}>
            <span className="icon iconfont iconbiaogejieshi"></span>
            <span>索引Mapping只允许新增，不允许清空或变更原有字段</span>
          </div>
          <SetMapping
            childEvevnt={(child) => (this.$mappingRef = child)}
            setValid={this.setValid}
            updateState={this.updateState}
            data={mapping}
          />
        </Drawer>
      </>
    );
  }
}
