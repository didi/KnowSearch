import CodeMirror from "codemirror/lib/codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/mode/sql/sql";
import "codemirror/mode/javascript/javascript";
import "codemirror/addon/hint/show-hint.js";
import "codemirror/addon/hint/sql-hint.js";
import "codemirror/addon/hint/show-hint.css";
import * as React from "react";
import { explainSql, toDsl } from "api/cluster-index-api";
import { Empty, Table, Tooltip, Spin, Form, Select, Row } from "antd";
import "./sql-index.less";
import store from "store";
import { isArray, isEmpty } from "lodash";
import { isSuperApp } from "lib/utils";
import { getLogicListTemplates, getPhyListTemplates } from "../../api/cluster-kanban";
import { getIndexMappingInfo } from "api/cluster-index-api";
import { getIndexColumns, TAB_JSON, getResTableColumns, selectDt } from "./config";
import { XNotification } from "component/x-notification";
import { v4 as uuidv4 } from "uuid";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
const imgSrc = require("./../../assets/empty-icon.png");
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";

const FormItem = Form.Item;
const app = {
  currentAppInfo: {
    app: store.getState().app,
  },
};
const user = {
  currentUser: store.getState().user,
};

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
class SQLQueryCom extends React.Component<any> {
  public state = {
    checkedList: [] as string[],
    indexStore: [],
    selectDataSource: [] as selectDt[],
    tableLoading: false,
    indexList: [],
    sqlData: {} as any,
    tableShow: false,
    tableReady: false,
    indeterminate: true,
    tableInfo: {} as any,
    tableData: [] as any,
    plainOptions: [] as string[],
    tableHeader: {} as any,
    jsonData: {} as any,
    phyClusterName: null,
    loading: false,
    activeKey: "",
    mappingJson: [],
    selectMapJson: [],
    activeInstance: null,
    lineSign: 0,
    showButton: true,
    jsonKey: "TABLE",
    isError: false,
    resultLoading: false,
  };
  $selectRef: any = React.createRef();
  $clickRef: any = React.createRef();
  $mappingJsonRef: any = React.createRef();
  $formRef: any = React.createRef();

  public getMapFun = (cluster, indexName) => {
    const templeteId = this.$selectRef.current.filter((row) => row.label === indexName);
    this.setState({
      tableLoading: true,
      mappingJson: [],
      selectMapJson: [],
    });
    getIndexMappingInfo(templeteId[0].value)
      .then((res) => {
        const data = res?.fields;
        if (!data.length) return;
        const copyData = JSON.parse(JSON.stringify(data));
        copyData.forEach((item) => {
          ///添加检索方式,fields.keyword里面有这个字段的配置
          item["value"] = item["name"];
          item["label"] = item["name"];
          if (!item.hasOwnProperty("search")) {
            if (item["type"] === "text") {
              item.search = "fuzzy";
            } else {
              item.search = "Accurate";
            }
          }
          if (!item.hasOwnProperty("doc_value")) {
            item["doc_value"] = true;
          }
        });
        this.setState({
          mappingJson: copyData,
          selectMapJson: copyData,
        });
        this.$formRef.current.setFieldsValue({ mapping: null });
        this.$mappingJsonRef.current = copyData || [];
      })
      .catch(() => {
        this.setState({
          mappingJson: [],
          selectMapJson: [],
        });
        this.$formRef.current.setFieldsValue({ mapping: null });
        this.$mappingJsonRef.current = [];
      })
      .finally(() => {
        this.$formRef.current.setFieldsValue({ mapping: null });
        this.setState({
          tableLoading: false,
        });
      });
  };

  public clusterOrLogic = async (name: string) => {
    return isSuperApp() ? getPhyListTemplates(name) : getLogicListTemplates(name);
  };

  public dataSelectFun = (name: string, currentClusterInfo: any) => {
    if (currentClusterInfo && currentClusterInfo?.v2?.health === -1) {
      this.setState({
        indexList: [],
        indexStore: [],
        phyClusterName: name || "",
        selectDataSource: [],
        mappingJson: [],
        selectMapJson: [],
      });
      return;
    }
    this.setState({ loading: true });
    this.clusterOrLogic(name)
      .then((data) => {
        let nameList = [];
        if (isArray(data)) {
          let selectData = data.map((i) => {
            nameList.push(i?.name);
            return {
              label: i?.name,
              value: i?.id,
            };
          });
          this.$selectRef.current = selectData;
          this.setState({
            indexList: nameList || [],
            indexStore: nameList || [],
            phyClusterName: name || "",
            selectDataSource: selectData || [],
            mappingJson: [],
            selectMapJson: [],
          });
        }
      })
      .finally(() => {
        this.setState({ loading: false });
      });
  };

  public componentWillReceiveProps(nextProps) {
    if (this.state.phyClusterName !== nextProps.phyClusterName) {
      this.dataSelectFun(nextProps.phyClusterName, nextProps.currentClusterInfo);
      // 在这里进行异步操作或者更新状态，切换集群时清除sql记录，忽略最初的集群赋值
      if (this.state.phyClusterName !== null) {
        const cacheKey = `${user.currentUser.getName("userName")}_${app.currentAppInfo.app.appInfo()?.id}_sql_cookie`;
        localStorage.removeItem(cacheKey);
        this.state.activeInstance.setValue("");
      }
      this.$formRef.current.setFieldsValue({ mapping: null, index: null });
      this.setState({
        tableShow: false,
        tableInfo: {},
        tableData: [],
        jsonData: "",
      });
    }
    if (this.state.activeInstance) {
      const value = this.state.activeInstance.getValue();
      setTimeout(() => {
        this.state.activeInstance.setValue(value);
      }, 0);
    }
  }

  public eventBox = (e: any) => {
    const dom = e.target;
    if (!this.props.phyClusterName) {
      XNotification({ type: "error", message: "执行错误，无集群信息" });
      return;
    }
    if (dom.classList[0] === "dsl_icon") {
      const pdom = dom.parentNode;
      const sign = pdom.getAttribute("data-sign");
      const data = this.state.sqlData[sign];
      this.sqlToDsl(data);
    } else if (dom.classList[0] === "explain_icons") {
      const pdom = dom.parentNode;
      const sign = pdom.getAttribute("data-sign");
      const data = this.state.sqlData[sign];
      this.sqlExplain(data);
    }
  };

  public sqlExplain = (info: any) => {
    if (this.$clickRef.current) {
      return;
    }
    this.$clickRef.current = true;
    this.setState({
      isError: false,
      resultLoading: true,
    });
    const { value } = info;
    explainSql(value, this.props.actPhyCluster)
      .then((data) => {
        data = JSON.parse(data);
        this.setState({
          jsonData: data,
        });
        if (data.hits) {
          const tableInfo = {} as any;
          tableInfo.took = data.took;
          tableInfo.timed_out = data.timed_out;
          tableInfo["_shards.total"] = data._shards.total;
          tableInfo["_shards.failed"] = data._shards.failed;
          tableInfo["hits.total"] = data.hits.total;
          const tmpHits = data.hits.hits;
          const tmpData = [] as any;
          const tmpHeader = {} as any;
          tmpHits.forEach((node: any) => {
            if (node._source) {
              for (const key in node._source) {
                if (!tmpHeader[key]) {
                  tmpHeader[key] = true;
                }
              }
              tmpData.push(node._source);
            }
          });
          this.setState({
            tableInfo,
            tableShow: !!tmpHits.length,
            showButton: true,
            tableReady: true,
            tableHeader: tmpHeader,
            checkedList: this.getCurrentCheckList(tmpHeader),
            plainOptions: this.getCurrentCheckList(tmpHeader),
            tableData: tmpData,
            jsonKey: tmpHits.length ? "TABLE" : "JSON",
          });
        } else {
          this.setState({
            tableShow: false,
            tableReady: false,
            tableInfo: {},
            showButton: false,
            isError: true,
          });
        }
      })
      .catch(() => {
        this.setState({
          jsonData: "",
        });
      })
      .finally(() => {
        this.$clickRef.current = false;
        this.setState({
          resultLoading: false,
        });
      });
  };

  public sqlToDsl = (info: any) => {
    if (this.$clickRef.current) {
      return;
    }
    const { value } = info;
    this.$clickRef.current = true;
    this.setState({
      resultLoading: true,
    });
    toDsl(this.props.actPhyCluster, value)
      .then((data) => {
        data = JSON.parse(data);
        this.setState({
          tableShow: false,
          showButton: false,
          tableReady: false,
          jsonData: data,
        });
      })
      .catch(() => {
        this.setState({
          jsonData: "",
        });
      })
      .finally(() => {
        this.$clickRef.current = false;

        this.setState({
          resultLoading: false,
        });
      });
  };

  public customAnchor = (anchorName: string) => {
    const anchorElement = document.querySelector(anchorName);
    window.setTimeout(() => {
      if (anchorElement) {
        anchorElement.scrollIntoView();
      }
    }, 0);
  };

  public componentDidMount() {
    this.props.phyClusterName && this.dataSelectFun(this.props.phyClusterName, this.props.currentClusterInfo);
    const code = document.querySelector(".codemirror");
    code.innerHTML = "";
    const editor = CodeMirror(document.querySelector(".codemirror"), {
      mode: "text/x-sql",
      lineNumbers: true,
      styleActiveLine: true,
      lineWrapping: true,
      completeSingle: false,
    });

    editor.on("keyup", (cm: any, event: any) => {
      if (!cm.state.completionActive && ![13, 8, 9, 32, 27].includes(event.keyCode)) {
        const tables = {} as any;
        this.state.indexStore.forEach((node: any) => {
          tables[node] = [];
        });
        // this.state.mappingStore.forEach((node: any) => {
        //   tables[node.field] = [];
        // });
        CodeMirror.commands.autocomplete(cm, null, {
          completeSingle: false,
          tables,
        });
      }
    });
    editor.on("changes", (a: any) => {
      const data = a.getValue().split("\n");
      let status = "false";
      const sqlData = {} as any;
      let tmp = "";
      let array = [] as any;
      let tmpkey = 0;
      data.forEach((node: any, i: number) => {
        if (node) {
          if (status === "false") {
            tmpkey = i;
          }
          array.push(i);
          tmp = `${tmp + node}\n`;
          sqlData[tmpkey] = {
            value: tmp,
            line: array,
          };
          status = "start";
        } else {
          if (status === "start") {
            status = "false";
            tmpkey = 0;
            tmp = "";
            array = [];
          }
        }
      });
      const lineDoms = document.querySelectorAll(".codemirror .CodeMirror-code>div");
      lineDoms.forEach((lineDom) => {
        lineDom.className = "CodeMirror-line-container";
        if (lineDom.querySelector(".query")) {
          lineDom.removeChild(lineDom.querySelector(".query"));
        }
      });
      for (const key in sqlData) {
        if (sqlData[key]) {
          const line = parseInt(key, 10);
          const lineDom = lineDoms[line];
          // tslint:disable-next-line:no-unused-expression bcui-icon-treenode-arrow
          lineDom &&
            lineDom.insertAdjacentHTML(
              "beforeend",
              `<span class="query" data-sign="${line}"><span class="dsl_icon">toDSL</span><span class="explain_icons">执行</span></span>`
            );
        }
      }
      this.setState({
        sqlData,
      });
      const eventDom = document.querySelector(".codemirror");
      eventDom.addEventListener("click", this.eventBox);
    });
    this.setState({
      activeInstance: editor,
    });
    const cacheKey = `${user.currentUser.getName("userName")}_${app.currentAppInfo.app.appInfo()?.id}_sql_cookie`;
    if (localStorage.getItem(cacheKey)) {
      editor.setValue(localStorage.getItem(cacheKey));
    }
    editor.on("blur", (a: any) => {
      this.setState({ lineSign: a.getCursor().line });
      const data = a.getValue();
      localStorage.setItem(cacheKey, data);
    });
  }

  public onIndexChange = (value, option) => {
    this.setState({
      indexStore: value ? this.state.indexList.filter((row) => row === option.label) : this.state.indexList,
    });
  };

  public onMappingChange = (value) => {
    this.setState({
      mappingJson: value ? this.$mappingJsonRef.current.filter((row) => row.value === value) : this.$mappingJsonRef.current,
    });
  };

  public indexHandleSearch = (value) => {
    this.setState({
      selectDataSource: value ? this.$selectRef.current.filter((row) => row.label.includes(value)) : this.$selectRef.current,
    });
  };

  public mappingHandleSearch = (value) => {
    this.setState({
      selectMapJson: value ? this.$mappingJsonRef.current.filter((row) => row.value.includes(value)) : this.$mappingJsonRef.current,
    });
  };

  public changeMenu = (e) => {
    this.getMapFun(this.state.phyClusterName, e);
    const cacheKey = `${user.currentUser.getName("userName")}_${app.currentAppInfo.app.appInfo()?.id}_sql_cookie`;
    if (localStorage.getItem(cacheKey)) {
      const curentValue = localStorage.getItem(cacheKey);
      const array = curentValue.split("\n");
      const index = this.state.lineSign;
      array[index] = array[index] + e;
      localStorage.setItem(cacheKey, array.join("\n"));
      this.state.activeInstance.setValue(localStorage.getItem(cacheKey));
      this.state.activeInstance.setCursor({ line: index, ch: array[index].length }); //设置聚焦，默认聚焦为最后一个字节
    } else {
      localStorage.setItem(cacheKey, e);
      this.state.activeInstance.setValue(e);
    }
    this.setState({
      activeKey: e,
    });
  };

  public renderIndexTable = () => {
    return (
      <div className="index-temp">
        <div className="iam-box">
          <div className="iam-box-title">
            <span className="title-prefix"></span>索引模版
          </div>
          <Form ref={this.$formRef} layout="vertical">
            <Row>
              <FormItem key="index" name="index" label="" className="indextemp-input">
                <Select
                  showSearch
                  options={this.state.selectDataSource}
                  allowClear
                  placeholder="索引模版"
                  defaultActiveFirstOption={false}
                  filterOption={false}
                  onSearch={this.indexHandleSearch}
                  onChange={this.onIndexChange}
                />
              </FormItem>
            </Row>
          </Form>
          {this.state.indexStore.length ? (
            <Spin spinning={this.state.loading}>
              <div className="index-container">
                <div className="index-tabs-contain">
                  <div className="index-tabs">
                    {(this.state.indexStore || []).map((m, index) => {
                      return (
                        <div
                          key={index}
                          onClick={() => {
                            this.changeMenu(m);
                          }}
                          className={this.state.activeKey === m ? "index-tabs-item check" : "index-tabs-item"}
                        >
                          <Tooltip title={m}>
                            <div className="index-tabs-item-content">
                              <span>{m || ""}</span>
                              <span
                                style={{
                                  display: this.state.activeKey == m ? "inline-block" : "none",
                                }}
                                className="iconfont iconrenwuzhongxin1"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  this.props.setModalId("mapping", {
                                    title: "Mapping",
                                    selectMapJson: this.state.selectMapJson,
                                    mappingHandleSearch: this.mappingHandleSearch,
                                    onMappingChange: this.onMappingChange,
                                    tableLoading: this.state.tableLoading,
                                    columns: getIndexColumns(),
                                    mappingJson: this.state.mappingJson,
                                  });
                                  console.log("click ");
                                }}
                              ></span>
                            </div>
                          </Tooltip>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </Spin>
          ) : (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </div>
      </div>
    );
  };

  public getCurrentCheckList = (tableHeader: any) => {
    const plainOptions = [] as string[];
    for (const key in tableHeader) {
      if (tableHeader[key]) {
        plainOptions.push(key);
      }
    }
    return plainOptions;
  };

  public handleChange = (value: any) => {
    this.setState({
      checkedList: value,
      indeterminate: !!value.length && value.length < this.state.plainOptions.length,
    });
  };

  public changeResTab = (e) => {
    this.setState({
      jsonKey: e.key,
      tableShow: e.key === "TABLE",
    });
  };

  public renderResTable = () => {
    const { tableData } = this.state;
    const height = document.body.clientHeight - 725;
    return (
      <>
        {tableData.length ? (
          <Table
            dataSource={tableData}
            rowKey={() => uuidv4()}
            bordered
            className="data-table"
            columns={getResTableColumns(this.state.checkedList)}
            scroll={{ x: "max-content", y: "max-content" }}
          />
        ) : (
          <div className="empty-contain">
            <Empty
              image={
                <>
                  <img src={imgSrc} />
                </>
              }
              description="您的执行结果为空~"
              className="empty-component"
            />
          </div>
        )}
      </>
    );
  };

  public renderJsonRes = () => {
    return (
      <>
        {this.state.tableShow ? null : (
          <div className="box-right">
            <ACEJsonEditor readOnly={true} data={JSON.stringify(this.state.jsonData, null, 4)} />
          </div>
        )}
      </>
    );
  };

  public renderResultContent = () => {
    const { tableInfo, tableShow, jsonData, isError, showButton } = this.state;
    const selectObj = this.state.plainOptions.map((i) => ({
      label: i,
      value: i,
    }));

    if ((tableShow && isEmpty(tableInfo)) || isEmpty(jsonData)) {
      return (
        <div className="sql-title">
          <div className="sql-title-wrap">
            <span className="title-prefix"></span>
            <span className="title-text">执行结果</span>
          </div>
          <div className="empty-contain">
            <Empty
              image={
                <>
                  <img src={imgSrc} />
                </>
              }
              description="您的执行结果为空~"
              className="empty-component"
            />
          </div>
        </div>
      );
    }

    return (
      <>
        <div className="sql-title">
          <div className="sql-title-wrap">
            <span className="title-prefix"></span>
            <span className="title-text">执行结果</span>
            {isError ? null : (
              <>
                {showButton && (
                  <span>
                    <span className="line"> | </span>
                    <span>
                      响应时间: <span className="text">{tableInfo.took} ms </span>
                      超时: <span className="text">{tableInfo.timed_out ? "是" : "否"}</span>
                      总shard数: <span className="text">{tableInfo["_shards.total"]}</span>
                      失败shard数: <span className="text">{tableInfo["_shards.failed"]}</span>
                      总条数: <span className="text">{tableInfo["hits.total"]}</span>
                    </span>
                    {this.state.tableData.length > 0 && this.state.jsonKey === "TABLE" && (
                      <Select
                        mode="tags"
                        className="select-column"
                        value={this.state.checkedList}
                        onChange={this.handleChange}
                        tokenSeparators={[","]}
                        maxTagCount={"responsive"}
                      >
                        {selectObj.map((i) => {
                          return (
                            <Select.Option key={i.label} value={i.value}>
                              {i.value}
                            </Select.Option>
                          );
                        })}
                      </Select>
                    )}
                    <div className="detail-tabs">
                      {(TAB_JSON || []).map((m) => {
                        return (
                          <div
                            key={m.key}
                            onClick={() => this.changeResTab(m)}
                            className={this.state.jsonKey == m.key ? "detail-tabs-item check" : "detail-tabs-item"}
                          >
                            {m.label}
                          </div>
                        );
                      })}
                    </div>
                  </span>
                )}
              </>
            )}
          </div>
        </div>
        <Spin spinning={this.state.resultLoading}>
          {this.state.tableShow && this.renderResTable()}
          {this.renderJsonRes()}
        </Spin>
      </>
    );
  };

  public render() {
    return (
      <div className="sql-query-page">
        <div className="top-console-open">
          <div className="codemirror-contain">
            <div className="codemirror-title">
              <span className="title-prefix"></span>
              SQL编辑器
            </div>
            <div className="codemirror" />
          </div>
          <div className="tip-content">{this.renderIndexTable()}</div>
        </div>
        <div className="bottom-console">{this.renderResultContent()}</div>
      </div>
    );
  }
}
export const SQLQuery = connect(null, mapDispatchToProps)(SQLQueryCom);
