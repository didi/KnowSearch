import * as React from "react";
import { BasicInfoPreview } from "container/index-tpl-management/create/basicInfo-preview";
import { Tabs } from "knowdesign";
import * as actions from "actions";
import { connect } from "react-redux";
import { getFormatJsonStr, formatJsonStr } from "lib/utils";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});
const connects: Function = connect;

@connects(mapStateToProps)
export class Preview extends React.Component<any> {
  constructor(props: any) {
    super(props);
  }

  public componentDidMount() {
    this.props.childEvevnt(this);
  }

  public handlePre = () => {
    this.props.updateState({
      current: 2,
    });
  };

  public handleSubmit = (cb) => {
    cb && cb();
  };

  public render() {
    const { isCyclicalRoll, dataCenter, baseInfoData, mapping, setting } = this.props.data;
    const { dataTypeList } = this.props.params;

    return (
      <>
        <Tabs type="card" className="tab-content mt-20 ">
          <Tabs.TabPane tab="基本信息" key="1">
            <BasicInfoPreview baseInfo={{ isCyclicalRoll, dataCenter, ...baseInfoData }} dataTypeList={dataTypeList} />
          </Tabs.TabPane>
          <Tabs.TabPane tab="Mapping" key="2">
            <div className="json-editor-wrapper">
              <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={getFormatJsonStr(JSON.parse(mapping || "{}"))} />
            </div>
          </Tabs.TabPane>
          <Tabs.TabPane tab="Setting" key="3">
            <div className="json-editor-wrapper">
              <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={getFormatJsonStr(JSON.parse(setting || "{}"))} />
            </div>
          </Tabs.TabPane>
        </Tabs>
      </>
    );
  }
}
