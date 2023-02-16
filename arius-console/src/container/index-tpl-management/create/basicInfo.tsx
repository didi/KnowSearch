import * as React from "react";
import { XForm as XFormComponent, IFormItem } from "component/x-form";
import { getStepOneFormMap } from "./config";
import { TEMP_FORM_MAP_KEY } from "./constant";
import Url from "lib/url-parser";
import { CancelActionModal } from "container/custom-component";
import { Button, Spin } from "antd";
import { getTimeFormat } from "api/cluster-index-api";
import { connect } from "react-redux";
import * as actions from "actions";
import { dropByCacheKey } from "react-router-cache-route";

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});

const connects: Function = connect;
@connects(mapStateToProps)
export class BasicInfo extends React.Component<any> {
  public state = {
    cyclicalRoll: "",
    formData: null as any,
    loading: false,
    timeFormatList: [],
  };
  private $formRef: any = null;
  private id: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public async componentDidMount() {
    getTimeFormat().then((res = []) => {
      this.setState({
        timeFormatList: res,
      });
    });
  }

  public componentWillUnmount() {
    this.$formRef = null;
  }

  public onHandleValuesChange = (value: any, allValues: object) => {
    Object.keys(value).forEach((key) => {
      switch (key) {
        case "cyclicalRoll":
          this.setState({
            cyclicalRoll: value[key],
          });
          break;
        case "clusterInfo":
          this.$formRef.setFieldsValue({ level: value?.clusterInfo?.level }); // 选择集群后给level初始值
          this.props.dispatch(actions.setCreateIndex({ dataCenter: value?.clusterInfo?.dataCenter }));
          this.setState({ ...this.state });
          break;
        default:
          break;
      }
    });
  };

  public onSubmit = () => {
    this.$formRef.validateFields().then((result) => {
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.firstStepFormData, result));
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.isCyclicalRoll, result.cyclicalRoll === "more"));
      this.props.dispatch(actions.setCurrentStep(1));
    });
  };

  public getFormMap = (formData: any): IFormItem[] => {
    const { cyclicalRoll: cyclicalRollFromStore, timeFormatList } = this.state;
    const cyclicalRoll = cyclicalRollFromStore || formData.cyclicalRoll;
    return getStepOneFormMap(cyclicalRoll, false, this.$formRef, timeFormatList, this.props.dataTypeList);
  };

  public clearStore = (str) => {
    this.props?.setRemovePaths([str]);
    dropByCacheKey("menu.es.index-tpl.management.create");
    this.props.dispatch(actions.setClearCreateIndex());
  };

  public render() {
    const formData = this.state.formData || this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData) || {};

    return (
      <>
        <Spin spinning={this.state.loading}>
          <div className="step">
            <XFormComponent
              wrappedComponentRef={(formRef) => (this.$formRef = formRef)}
              formData={formData}
              formMap={this.getFormMap(formData)}
              onHandleValuesChange={this.onHandleValuesChange}
            />
            <div className="op-btn-group">
              <Button type="primary" onClick={this.onSubmit}>
                下一步
              </Button>
              <CancelActionModal routeHref={"/index-tpl/management"} history={this.props?.history} cb={this.clearStore} />
            </div>
          </div>
        </Spin>
      </>
    );
  }
}
