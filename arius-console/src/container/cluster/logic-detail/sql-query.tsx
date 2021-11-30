import CodeMirror from "codemirror/lib/codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/mode/sql/sql";
import "codemirror/mode/javascript/javascript";
import "codemirror/addon/hint/show-hint.js";
import "codemirror/addon/hint/sql-hint.js";
import "codemirror/addon/hint/show-hint.css";
import * as React from "react";
import {
  explainSql,
  getIndexNameList,
  getMappingList,
  toDsl,
} from "api/cluster-index-api";
import { Checkbox, Empty, Input, Table }  from 'antd';
import "./sql-index.less";
import store from "store";
import { RightOutlined } from "@ant-design/icons";

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

export class SQLQuery extends React.Component {
  public state = {
    checkedList: [] as string[],
    indexStore: [] as IIndexItem[],
    indexList: [] as IIndexItem[],
    mappingStore: [] as any,
    mappingList: [] as any,
    sqlData: {} as any,
    loadingMapping: false,
    consoleVisible: false,
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
  };

  public getIndex = () => {
    getIndexNameList().then((data) => {
      this.setState({
        indexList: data,
        indexStore: data,
      });
    });
  };

  public eventBox = (e: any) => {
    const dom = e.target;
    if (dom.classList[0] === "dsl_icon") {
      const pdom = dom.parentNode;
      const sign = pdom.getAttribute("data-sign");
      const data = this.state.sqlData[sign];
      this.sqlToDsl(data);
    } else if (dom.classList[0] === "explain_icon") {
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
    explainSql(value)
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
        this.customAnchor(".bottom-console");
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
      this.customAnchor(".bottom-console");
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
      if (
        !cm.state.completionActive &&
        ![13, 8, 9, 32, 27].includes(event.keyCode)
      ) {
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
      const lineDoms = document.querySelectorAll(
        ".codemirror .CodeMirror-code>div"
      );
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
              `<span class="query" data-sign="${line}"><span class="dsl_icon">toDSL</span><span class="explain_icon font" /></span>`
            );
        }
      }
      this.setState({
        sqlData,
      });
      const eventDom = document.querySelector(".codemirror");
      eventDom.addEventListener("click", this.eventBox);
    });
    const cacheKey = `${user.currentUser.getName('domainAccount')}_${app.currentAppInfo.app.appInfo()?.id}_sql_cookie`;
    if (localStorage.getItem(cacheKey)) {
      editor.setValue(localStorage.getItem(cacheKey));
    }
    editor.on("blur", (a: any) => {
      const data = a.getValue();
      localStorage.setItem(cacheKey, data);
    });
  }

  public onInputIndexChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    type?: string
  ) => {
    const searchKey = e.target.value.trim();
    if (type === "mapping") {
      return this.setState({
        mappingStore: searchKey
          ? this.state.mappingList.filter((row: any) =>
            row.v1.includes(searchKey)
          )
          : this.state.mappingList,
      });
    }
    this.setState({
      indexStore: searchKey
        ? this.state.indexList.filter((row) => row.v1.includes(searchKey))
        : this.state.indexList,
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
          analyzeField(
            obj.properties[key],
            field ? `${field}.${key}` : `${key}`
          );
        }
      } else {
        allArray.push({
          field,
          type: obj.type,
          index: obj.index
            ? obj.index
            : obj.type === "string"
              ? "analyzed"
              : "not_analyzed",
        });
      }
    };

    getMappingList(item.v2, item.v1)
      .then((data) => {
        const store = JSON.parse(data || 'null');
        for (const key in store) {
          if (store[key].properties) {
            analyzeField(store[key]);
          }
        }
        this.setState({
          mappingStore: allArray,
          mappingList: JSON.parse(JSON.stringify(allArray) || 'null'),
        });
      })
      .finally(() => {
        this.setState({
          loadingMapping: false,
        });
      });
  };

  public toggleConsole = () => {
    this.setState({
      consoleVisible: !this.state.consoleVisible,
    });
  };

  public renderIndexTable = () => {
    return (
      <>
        <div className="head-tip">索引</div>
        <div className="iam-box">
          <Input
            onChange={this.onInputIndexChange}
            placeholder="请输入关键字"
          />
          {this.state.indexStore.length ? (
            <ul className="index-ul">
              {this.state.indexStore.map((item: any, index: number) => {
                return (
                  <li key={index} onClick={() => this.getMapping(item)}>
                    {item.v1 || ""}
                  </li>
                );
              })}
            </ul>
          ) : (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </div>
      </>
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
          Mapping 对照 @{" "}
          <span className="text-link">{this.state.mappingTitle}</span>
        </div>
        <Input
          onChange={(e) => this.onInputIndexChange(e, "mapping")}
          placeholder="请输入关键字"
        />
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
          key,
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
      indeterminate:
        !!checkedList.length &&
        checkedList.length < this.state.plainOptions.length,
      checkAll: checkedList.length === this.state.plainOptions.length,
    });
  };

  public renderCheckboxList = () => {
    return (
      <>
        <div className="checkbox-content">
          <Checkbox
            indeterminate={this.state.indeterminate}
            onChange={this.onCheckAllChange}
            checked={this.state.checkAll}
          >
            全选
          </Checkbox>
          <CheckboxGroup
            options={this.state.plainOptions}
            value={this.state.checkedList}
            onChange={this.onChange}
          />
        </div>
      </>
    );
  };

  public renderResTable = () => {
    const { tableInfo, tableData } = this.state;

    return (
      <>
        <div className="cm-s-default result-title">
          <span>
            响应时间: <span className="text-success">{tableInfo.took} ms </span>
          </span>
          <span>
            超时:{" "}
            <span
              className={tableInfo.timed_out ? "text-error" : "text-success"}
            >
              {tableInfo.timed_out ? "是" : "否"}
            </span>
          </span>
          <span>
            总shard数:{" "}
            <span className="cm-keyword">{tableInfo["_shards.total"]}</span>
          </span>
          <span>
            失败shard数:{" "}
            <span className="tableInfo['_shards.failed'] ? 'text-error' : '' ">
              {tableInfo["_shards.failed"]}
            </span>
          </span>
          <span>
            总条数:{" "}
            <span className="cm-keyword">{tableInfo["hits.total"]}</span>
          </span>
          <span className="switch-btn" onClick={this.tableToJSON}>
            JSON
          </span>
        </div>
        {this.renderCheckboxList()}
        <Table
          dataSource={tableData}
          className="data-table"
          columns={this.getResTableColumns()}
        />
      </>
    );
  };

  public renderJsonRes = () => {
    return (
      <div
        style={{ display: this.state.tableShow ? "none" : "block" }}
        className="box-right"
      >
        <span
          onClick={this.jsonToTable}
          style={{ display: this.state.tableReady ? "block" : "none" }}
          className="switch-btn"
        >
          TABLE
        </span>
        <div className="querycallback">
          <pre />
        </div>
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
          <div className="expand-btn">
            <div
              className={`console-switch ${this.state.consoleVisible ? "console-switch-open" : ""
                }`}
              onClick={this.toggleConsole}
            >
               <RightOutlined />
            </div>
          </div>

          {this.state.consoleVisible ? (
            <div className="tip-content">
              {this.renderIndexTable()}
              {this.renderMappingTable()}
            </div>
          ) : null}
        </div>
        <div className="bottom-console">
          <div className="head-tip">执行结果</div>
          {this.renderResultContent()}
        </div>
      </div>
    );
  }
}
