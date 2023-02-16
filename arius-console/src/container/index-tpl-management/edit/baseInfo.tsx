import * as React from "react";
import { XForm as XFormComponent, IFormItem } from "component/x-form";
import { getStepOneFormMap } from "../create/config";
import Url from "lib/url-parser";
import { CancelActionModal } from "container/custom-component";
import { Button, Spin } from "antd";
import { getIndexBaseInfo, updateIndexInfo, getTimeFormat, getIndexDataType } from "api/cluster-index-api";
import { dropByCacheKey } from "react-router-cache-route";
import { XNotification } from "component/x-notification";

export class EditBaseInfo extends React.Component<any> {
  public state = {
    formData: {} as any,
    loading: false,
    timeFormatList: [],
    dataTypeList: [],
  };
  private $formRef: any = null;
  private id: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public componentDidMount() {
    this.setState({
      loading: true,
    });
    getIndexBaseInfo(this.id)
      .then((data = {}) => {
        data.cyclicalRoll = data.cyclicalRoll ? "more" : "one";
        data.clusterInfo = {
          cluster: data.cluster,
          clusterName: data.cluster,
          clusterType: data.clusterType,
        };
        data.type = data.clusterType;
        data.clusterName = data.cluster;
        this.setState({
          formData: data,
        });
        this.$formRef.setFieldsValue(data);
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
    getTimeFormat().then((res = []) => {
      this.setState({
        timeFormatList: res,
      });
    });
    getIndexDataType().then((res = {}) => {
      const dataTypeList = Object.keys(res).map((key) => {
        return {
          title: res[key],
          label: res[key],
          value: Number(key),
        };
      });
      this.setState({ dataTypeList });
    });
  }

  public componentWillUnmount() {
    this.$formRef = null;
  }

  public onSave = () => {
    this.$formRef.validateFields().then((result) => {
      const formData = {
        id: this.id,
        desc: result.desc,
        dataType: result.dataType,
        expireTime: result.expireTime,
      };
      updateIndexInfo(formData).then(() => {
        XNotification({ type: "success", message: "更新成功" });
        dropByCacheKey("menu.es.index-tpl.management");
        this.clearStore(["/es/index-tpl/management/modify"]);
        this.props?.history.push("/index-tpl/management");
      });
    });
  };

  public getFormMap = (): IFormItem[] => {
    const { formData, timeFormatList, dataTypeList } = this.state;
    const cyclicalRoll = formData.cyclicalRoll;
    return getStepOneFormMap(cyclicalRoll, true, this.$formRef, timeFormatList, dataTypeList);
  };

  public clearStore = (str) => {
    this.props?.setRemovePaths([str]);
    // 修改已不在缓存列，取消缓存
    // dropByCacheKey("menu.es.index-tpl.modify");
  };

  public render() {
    const { loading, formData } = this.state;
    return (
      <>
        <Spin spinning={loading}>
          <div className="content-wrapper edit">
            <XFormComponent wrappedComponentRef={(formRef) => (this.$formRef = formRef)} formData={formData} formMap={this.getFormMap()} />
            <div className="op-btn-group">
              <Button type="primary" onClick={this.onSave}>
                确定
              </Button>
              <CancelActionModal routeHref={"/index-tpl/management"} history={this.props?.history} cb={this.clearStore} />
            </div>
          </div>
        </Spin>
      </>
    );
  }
}
