import * as React from "react";
import "./index.less";
import { BasicInfoPreview } from "./basicInfo-preview";
import { TEMP_FORM_MAP_KEY } from "./constant";
import { CancelActionModal } from "container/custom-component";
import { Tabs, Button, message } from "antd";
import * as actions from "actions";
import { connect } from "react-redux";
import { createIndex } from "api/cluster-index-api";
import { getFormatJsonStr, formatJsonStr } from "lib/utils";
import { dropByCacheKey, refreshByCacheKey } from "react-router-cache-route";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});
const connects: Function = connect;

@connects(mapStateToProps)
export class LastStep extends React.Component<any> {
  public extraInfo = {};

  constructor(props: any) {
    super(props);
    this.getExtraInfo();
  }

  public getExtraInfo = () => {
    const firstStepFormData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData) || {};
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.firstStepFormData, firstStepFormData));
  };

  public clearStore = () => {
    dropByCacheKey("menu.es.index-tpl.management.create");
    this.props?.setRemovePaths(["/es/index-tpl/management/create"]);
    this.props.dispatch(actions.setClearCreateIndex());
    refreshByCacheKey(`menu.es.index-tpl.management`);
  };

  public onSubmit = () => {
    const mappingData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.jsonMappingValue) || "";
    const formData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData);
    const { customerAnalysisValue, dataCenter } = this.props.createIndex;
    const postData = {
      dataCenter,
      name: `${formData.name}`,
      resourceId: formData.clusterInfo?.cluster,
      level: formData.level,
      cyclicalRoll: formData.cyclicalRoll === "more" ? 1 : 0,
      expireTime: formData.expireTime,
      dateField: formData.dateField,
      dateFieldFormat: formData.dateFieldFormat,
      diskSize: Number(formData.diskSize),
      dataType: formData.dataType,
      desc: formData.desc,
      mapping: formatJsonStr(mappingData),
      setting: formatJsonStr(customerAnalysisValue),
    };
    const backUrl = "/index-tpl/management";

    this.props.dispatch(actions.setLoadingMap("create-loading", true));
    createIndex(postData)
      .then(() => {
        message.success("模板创建成功");
        this.clearStore();
        this.props?.history?.push(backUrl);
      })
      .finally(() => {
        this.props.dispatch(actions.setLoadingMap("create-loading", false));
        this.props.dispatch(
          actions.setCreateIndex({
            customerAnalysisValue: getFormatJsonStr({
              index: {
                "translog.durability": "async",
                "translog.sync_interval": "15s",
                refresh_interval: "1s",
              },
            }),
          })
        );
      });
  };

  public render() {
    const mapping = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.jsonMappingValue) || "{}";
    const setting = this.props.createIndex.customerAnalysisValue || "{}";

    return (
      <>
        <Tabs type="card" className="tab-content mt-20 ">
          <Tabs.TabPane tab="基本信息" key="1">
            <BasicInfoPreview dataTypeList={this.props.dataTypeList} />
          </Tabs.TabPane>
          <Tabs.TabPane tab="Mapping" key="2">
            <div className="json-content-wrapper">
              <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={getFormatJsonStr(JSON.parse(mapping))} />
            </div>
          </Tabs.TabPane>
          <Tabs.TabPane tab="Setting" key="3">
            <div className="json-content-wrapper">
              <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={getFormatJsonStr(JSON.parse(setting))} />
            </div>
          </Tabs.TabPane>
        </Tabs>
        <div className="op-btn-group">
          <Button onClick={() => this.props.dispatch(actions.setCurrentStep(2))}>上一步</Button>
          <Button type="primary" loading={this.props.createIndex.loadingMap["create-loading"]} onClick={this.onSubmit}>
            确定
          </Button>
          <CancelActionModal routeHref={"/index-tpl/management"} history={this.props?.history} cb={this.clearStore} />
        </div>
      </>
    );
  }
}
