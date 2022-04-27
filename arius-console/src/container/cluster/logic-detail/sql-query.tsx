import CodeMirror from "codemirror/lib/codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/mode/sql/sql";
import "codemirror/mode/javascript/javascript";
import "codemirror/addon/hint/show-hint.js";
import "codemirror/addon/hint/sql-hint.js";
import "codemirror/addon/hint/show-hint.css";
import * as React from "react";
import { explainSql, getIndexNameList, getMappingList, toDsl } from "api/cluster-index-api";
import { Button, Checkbox, Empty, Input, Table, Tooltip } from "antd";
import "./sql-index.less";
import store from "store";
import { SearchOutlined, FilterOutlined } from "@ant-design/icons";
import { copyContentFn } from "../../../d1-packages/Utils/tools";
import Url from "lib/url-parser";
import { isArray, isEmpty } from "lodash";

const app = {
  currentAppInfo: {
    app: store.getState().app,
  },
};

const user = {
  currentUser: store.getState().user,
};

const CheckboxGroup = Checkbox.Group;

interface IIndexItem {
  v1: string;
  v2: string;
}

export class SQLQuery extends React.Component<any> {
  public state = {
    checkedList: [] as string[],
    indexStore: [] as IIndexItem[],
    indexList: [] as IIndexItem[],
    mappingStore: [] as any,
    mappingList: [] as any,
    sqlData: {} as any,
    loadingMapping: false,
    tableShow: false,
    explainLoading: false,
    tableReady: false,
    indeterminate: true,
    checkAll: true,
    mappingTitle: "",
    tableInfo: {} as any,
    tableData: [] as any,
    plainOptions: [] as string[],
    tableHeader: {} as any,
    jsonData: {} as any,
    phyClusterName: null,
    toggle: true,
  };

  public componentWillReceiveProps(nextProps) {
    if (this.state.phyClusterName !== nextProps.phyClusterName) {
      // 在这里进行异步操作或者更新状态
      const indexStore = this.state.indexList?.filter((item) => item?.v2 === nextProps?.phyClusterName);
      this.setState({
        indexStore,
        phyClusterName: nextProps.phyClusterName,
      });
    }
  }

  public getIndex = () => {
    getIndexNameList().then((data) => {
      if (isArray(data)) {
        this.setState({
          indexList: data || [],
          indexStore: data || [],
        });
      }
    });
  };

  public eventBox = (e: any) => {
    const dom = e.target;
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
    this.setState({
      explainLoading: true,
    });

    const { value } = info;
    explainSql(value, Url().search.phyClusterName)
      .then((data) => {
        data = JSON.parse(data);
        this.setState({
          jsonData: data,
        });
        const dom = document.querySelector(".querycallback pre");

        if (data.hits) {
          const tableInfo = {} as any;
          // this.headerSelecterAll = true;
          tableInfo.took = data.took;
          tableInfo.timed_out = data.timed_out;
          tableInfo["_shards.total"] = data._shards.total;
          tableInfo["_shards.failed"] = data._shards.failed;
          tableInfo["hits.total"] = data.hits.total;
          this.setState({
            tableInfo,
            tableShow: true,
            tableReady: true,
          });
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
            tableHeader: tmpHeader,
            checkedList: this.getCurrentCheckList(tmpHeader),
            plainOptions: this.getCurrentCheckList(tmpHeader),
            tableData: tmpData,
          });
          if (tmpHits.length) {
            this.jsonToTable();
          } else {
            this.tableToJSON();
          }
        } else {
          this.setState({
            tableShow: false,
            tableReady: false,
          });
          // dom.innerHTML = JSON.stringify(data, null, " ");
          dom.innerHTML = data;
        }
        // this.customAnchor(".bottom-console");
      })
      .finally(() => {
        this.setState({
          explainLoading: false,
        });
      });
  };

  public sqlToDsl = (info: any) => {
    const { value } = info;
    toDsl(value).then((data) => {
      this.setState({
        tableShow: false,
        tableReady: false,
        jsonData: data,
      });
      const dom = document.querySelector(".querycallback pre");
      dom.innerHTML = data;
      // JSON.stringify(data, 2, " ")
      // this.customAnchor(".bottom-console");
    });
  };

  public tableToJSON = () => {
    this.setState({
      tableShow: false,
    });
    const dom = document.querySelector(".querycallback pre");
    dom.innerHTML = JSON.stringify(this.state.jsonData, null, " ");
  };

  public jsonToTable = () => {
    this.setState({
      tableShow: true,
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
    this.getIndex();
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
        this.state.mappingStore.forEach((node: any) => {
          tables[node.field] = [];
        });
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
    const cacheKey = `${user.currentUser.getName("domainAccount")}_${app.currentAppInfo.app.appInfo()?.id}_sql_cookie`;
    if (localStorage.getItem(cacheKey)) {
      editor.setValue(localStorage.getItem(cacheKey));
    }
    editor.on("blur", (a: any) => {
      const data = a.getValue();
      localStorage.setItem(cacheKey, data);
    });
  }

  public onInputIndexChange = (e: React.ChangeEvent<HTMLInputElement>, type?: string) => {
    const searchKey = e.target.value.trim();
    if (type === "mapping") {
      return this.setState({
        mappingStore: searchKey ? this.state.mappingList.filter((row: any) => row.v1.includes(searchKey)) : this.state.mappingList,
      });
    }
    this.setState({
      indexStore: searchKey ? this.state.indexList.filter((row) => row.v1.includes(searchKey)) : this.state.indexList,
    });
  };

  public getMapping = (item: any) => {
    if (!item) {
      return false;
    }
    this.setState({
      loadingMapping: true,
      mappingTitle: item.v1,
    });
    const allArray = [] as any[];
    const analyzeField = (obj: any, field?: any) => {
      if (obj.properties) {
        // tslint:disable-next-line:forin
        for (const key in obj.properties) {
          analyzeField(obj.properties[key], field ? `${field}.${key}` : `${key}`);
        }
      } else {
        allArray.push({
          field,
          type: obj.type,
          index: obj.index ? obj.index : obj.type === "string" ? "analyzed" : "not_analyzed",
        });
      }
    };

    getMappingList(item.v2, item.v1)
      .then((data) => {
        const store = JSON.parse(data || "null");
        for (const key in store) {
          if (store[key].properties) {
            analyzeField(store[key]);
          }
        }
        this.setState({
          mappingStore: allArray,
          mappingList: JSON.parse(JSON.stringify(allArray) || "null"),
        });
      })
      .finally(() => {
        this.setState({
          loadingMapping: false,
        });
      });
  };

  public renderIndexTable = () => {
    return (
      <div style={{ marginLeft: 20, border: '1px solid #EBEDEF', borderRadius: '0 0 4px 0' }}>
        <div className="iam-head-tip"><span>索引</span></div>
        <div className="iam-box">
          <Input style={{ margin: '20px 32px 16px 20px', width: 'calc(100% - 52px)' }} onChange={this.onInputIndexChange} placeholder="搜索" suffix={<SearchOutlined className="iam-box-icon" />} />
          {this.state.indexStore.length ? (
            <ul className="index-ul">
              {this.state.indexStore.map((item: any, index: number) => {
                return (
                  <li key={index} onClick={() => copyContentFn(item?.v1 as string)}>
                    <Tooltip title="点击可复制">
                      <a href="#">{item?.v1 || ""}</a>
                    </Tooltip>
                  </li>
                );
              })}
            </ul>
          ) : (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </div>
      </div>
    );
  };

  public getColumns = () => {
    return [
      {
        title: "字段名称",
        dataIndex: "field",
        key: "field",
      },
      {
        title: "字段类型",
        dataIndex: "type",
        key: "type",
      },
      {
        title: "索引类型",
        dataIndex: "index",
        key: "index",
      },
    ];
  };

  public renderMappingTable = () => {
    return (
      <>
        <div className="head-tip">
          Mapping 对照 @ <span className="text-link">{this.state.mappingTitle}</span>
        </div>
        <Input onChange={(e) => this.onInputIndexChange(e, "mapping")} placeholder="请输入关键字" />
        <Table
          rowKey="field"
          className="mapping-table"
          loading={this.state.loadingMapping}
          dataSource={this.state.mappingStore}
          columns={this.getColumns()}
        />
      </>
    );
  };

  public getResTableColumns = () => {
    const { checkedList } = this.state;
    const arr = [];

    for (const key of checkedList) {
      if (key) {
        arr.push({
          title: key,
          dataIndex: key,
          width: 150,
          key,
          render: (text) => {
            if (typeof text === 'object') {
              let str = '-';
              try {
                str = JSON.stringify(text, null, 4)
              } catch(err) {
                console.log(err)
              }
              return <>
                <Tooltip title={<div style={{ overflow: "scroll", maxHeight: 500 }}><pre>{str}</pre></div>}>
                  <div className="text-oh">{str}</div>
                </Tooltip>
              </>
            }
            return <>
              <Tooltip title={text}>
                <div className="text-oh">{text}</div>
              </Tooltip>
            </>
          }
        });
      }
    }
    return arr;
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

  public onCheckAllChange = (e: any) => {
    this.setState({
      checkedList: e.target.checked ? this.state.plainOptions : [],
      indeterminate: false,
      checkAll: e.target.checked,
    });
  };

  public onChange = (checkedList: string[]) => {
    this.setState({
      checkedList,
      indeterminate: !!checkedList.length && checkedList.length < this.state.plainOptions.length,
      checkAll: checkedList.length === this.state.plainOptions.length,
    });
  };

  public renderCheckboxList = () => {
    return (
      <>
        <div className="checkbox-content">
          <div className="leftbox">
            <Checkbox className="leftboxchildren" indeterminate={this.state.indeterminate} onChange={this.onCheckAllChange} checked={this.state.checkAll}>
              全选
            </Checkbox>
          </div>
          <CheckboxGroup className="rightbox" options={this.state.plainOptions} value={this.state.checkedList} onChange={this.onChange} />
        </div>
      </>
    );
  };

  public renderResTable = () => {
    const { tableInfo, tableData } = this.state;

    return (
      <>
        <div>
          {
            isEmpty(tableInfo) ?
              <div className="title-text">执行结果</div>
              :
              <div className="sql-title">
                <span className="title-text">执行结果</span>
                <span className="line"> | </span>
                <span>
                  响应时间: <span className="text">{tableInfo.took} ms </span>
                </span>
                <span>
                  超时: <span className="text">{tableInfo.timed_out ? "是" : "否"}</span>
                </span>
                <span>
                  总shard数: <span className="text">{tableInfo["_shards.total"]}</span>
                </span>
                <span>
                  失败shard数: <span className="text">{tableInfo["_shards.failed"]}</span>
                </span>
                <span>
                  总条数: <span className="text">{tableInfo["hits.total"]}</span>
                </span>
                <Button type="primary" className="json-button" onClick={this.tableToJSON}>
                  JSON
                </Button>
                <Button icon={<FilterOutlined />} className="filter-button" onClick={() => this.setState({ toggle: !this.state.toggle })} />
                {this.state.toggle ? <div className="filter-button-before"></div> : null }
              </div>
          }
        </div>
        {this.state.toggle ? this.renderCheckboxList() : null}
        <Table dataSource={tableData} className="data-table" columns={this.getResTableColumns()} />
      </>
    );
  };

  public renderJsonRes = () => {
    return (
      <div>
        {
          isEmpty(this.state.jsonData) ?
            <div className="isempty">
              <div className="nulltitle-text">执行结果</div>
              <Empty description="您的执行结果为空~" style={{ marginTop: 100 }} />
            </div>
            : <>
              { this.state.tableShow ? null : <div className="title-text" style={{ marginBottom: 10 }}>执行结果</div>}
              <div style={{ display: this.state.tableShow ? "none" : "block" }} className="box-right">
                <Button type="primary" className="jsontotable-button" onClick={this.jsonToTable}>
                  TABLE
                </Button>
                <div className="querycallback">
                  <pre />
                </div>
              </div>
            </>
        }
      </div>
    );
  };

  public renderResultContent = () => {
    return (
      <>
        {this.state.tableShow && this.renderResTable()}
        {this.renderJsonRes()}
      </>
    );
  };

  public render() {
    return (
      <div className="sql-page">
        <div className="top-console-open">
          <div className="codemirror" />
          <div className="tip-content">
            {this.renderIndexTable()}
            {/* {this.renderMappingTable()} */}
          </div>
        </div>
        <div className="bottom-console">
          {this.renderResultContent()}
        </div>
      </div>
    );
  }
}
