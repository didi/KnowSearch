import * as React from "react";
// import { TEMP_FORM_MAP_KEY } from "./constant";
import { Button, message } from "antd";
import * as actions from "actions";
import { connect } from "react-redux";
import "./index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});

const connects: any = connect;
@connects(mapStateToProps)
export class Setting extends React.Component<any> {
  // private isCyclicalRoll: boolean = false;
  static defaultProps: { isShowPlaceholder: boolean } = { isShowPlaceholder: true };

  constructor(props: any) {
    super(props);
    // this.isCyclicalRoll = !!props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.isCyclicalRoll);
    this.props.dispatch(actions.setLoadingMap("setting-loading", false));
  }

  public state = {
    disabled: false,
  };

  public componentDidMount() {}

  // public skip = () => {
  //   const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
  //   const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : "";
  //   this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }));
  //   this.props.dispatch(actions.setCurrentStep(3));
  // };

  public onHandlePrevStep = () => {
    const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
    const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : "";
    this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }));
    this.props.dispatch(actions.setCurrentStep(1));
  };

  public onHandleNextStep = () => {
    try {
      const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
      const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : "";
      let jsonValue = {};

      if (customerAnalysisValue) {
        jsonValue = JSON.parse(customerAnalysisValue || "null");
      }
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }));
      this.props.dispatch(actions.setCurrentStep(3));
    } catch {
      message.error("JSON格式有误");
    }
  };

  public setValid = (valid: boolean) => {
    this.setState({
      disabled: !valid,
    });
  };

  public render() {
    const loading = this.props.createIndex.loadingMap["setting-loading"];
    const { customerAnalysisValue } = this.props.createIndex;

    return (
      <div id="settingName" className={"tab-content"}>
        <JsonEditorWrapper
          data={customerAnalysisValue}
          title={"Setting编辑器"}
          isNeedAutoIndent={true}
          loading={loading}
          docType="setting"
          setEditorInstance={(editor) => {
            this.props.dispatch(actions.setCreateIndex({ customerAnalysisJson: editor }));
          }}
          setValid={this.setValid}
          isShowMappingTip={false}
          jsonClassName={"tpl-create-mapping"}
        />
        <div className="op-btns-group">
          <Button onClick={this.onHandlePrevStep}>上一步</Button>
          <Button type="primary" onClick={this.onHandleNextStep}>
            下一步
          </Button>
          {/* {!this.isCyclicalRoll && <Button onClick={() => this.skip()}>跳过</Button>} */}
        </div>
      </div>
    );
  }
}
