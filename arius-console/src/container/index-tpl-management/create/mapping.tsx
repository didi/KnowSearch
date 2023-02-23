import * as React from "react";
import { TEMP_FORM_MAP_KEY } from "./constant";
import { Button, message } from "antd";
import * as actions from "actions";
import { connect } from "react-redux";
import "./index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});

const connects: Function = connect;
@connects(mapStateToProps)
export class Mapping extends React.Component<any> {
  // private isCyclicalRoll: boolean = false;
  static defaultProps: { isShowPlaceholder: boolean } = { isShowPlaceholder: true };

  constructor(props: any) {
    super(props);
    // this.isCyclicalRoll = !!props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.isCyclicalRoll);
    this.props.dispatch(actions.setLoadingMap("mapping-loading", false));
  }

  public state = {
    disabled: false,
  };

  public componentDidMount() {
    //
  }

  public setValid = (valid: boolean) => {
    this.setState({
      disabled: !valid,
    });
  };

  public onHandlePrevStep = async () => {
    const editor = this.props.createIndex.activeInstance;
    const value = editor ? editor.getValue() : "";
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.jsonMappingValue, value));
    this.props.dispatch(actions.setCurrentStep(0));
  };

  public onHandleNextStep = async () => {
    try {
      const editor = this.props.createIndex.activeInstance;
      const value = editor ? editor.getValue() : "";
      let jsonValue = {};

      if (value) {
        jsonValue = JSON.parse(value || "null");
      }
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.jsonMappingValue, value));
      this.props.dispatch(actions.setCurrentStep(2));
    } catch {
      message.error("JSON格式有误");
    }
  };

  public render() {
    const value = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.jsonMappingValue) || "";
    const loading = this.props.createIndex.loadingMap["mapping-loading"];

    return (
      <div id="mappingName" className={"tab-content"}>
        <JsonEditorWrapper
          data={value}
          isNeedAutoIndent={true}
          title={"Mapping编辑器"}
          loading={loading}
          docType="mapping"
          setEditorInstance={(editor) => {
            this.props.dispatch(actions.setEditorInstance(editor));
          }}
          setValid={this.setValid}
          jsonClassName={"tpl-create-mapping"}
        />
        <div className={`op-btns-group`}>
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
