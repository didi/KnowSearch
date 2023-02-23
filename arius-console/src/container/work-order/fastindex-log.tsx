import React, { useState, useEffect, useRef } from "react";
import { getFastIndexLog, getFastIndexBrief } from "api/fastindex-api";
import { Spin, Empty } from "antd";
import QueryForm from "../../d1-packages/QueryForm";
import Url from "lib/url-parser";
import { uuid } from "lib/utils";
import "./index.less";

export default function FastIndexLog(props) {
  const urlParam: any = Url().search;

  const [logData, setLogData] = useState([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [queryFormObject, setQueryFormObject]: any = useState({});
  const [startAndEnd, setStartAndEnd] = useState([]);
  const [loading, setLoading] = useState(false);
  const [templateIndex, setTemplateIndex] = useState([]);
  const [template, setTemplate] = useState([]);
  const [index, setIndex] = useState([]);
  const [search, setSearch] = useState(false);

  useEffect(() => {
    getSelectData();
  }, []);

  useEffect(() => {
    getLogData();
  }, [queryFormObject, page]);

  const getSelectData = async () => {
    let res = await getFastIndexBrief(Number(urlParam?.taskid));
    if (urlParam?.datatype == 2) {
      let index = (res?.[0]?.indexName || []).map((item) => ({ title: item, value: item }));
      setIndex(index);
    } else {
      let template = [];
      (res || []).forEach((item) => {
        let t = { title: item?.templateName, value: item?.templateName };
        template.push(t);
      });
      setTemplate(template);
      setTemplateIndex(res);
    }
  };

  const getLogData = async () => {
    let params = {
      taskId: Number(urlParam?.taskid),
      page,
      size: 20,
      startTime: startAndEnd[0],
      endTime: startAndEnd[1],
      indexName: queryFormObject?.indexName,
      templateName: queryFormObject?.templateName,
      executionNode: queryFormObject?.executionNode,
    };
    let res = await getFastIndexLog(params);
    let data = res?.bizData || [];
    setTotal(res?.pagination?.total || 0);
    setLogData(search ? data : [...logData, ...data]);
    setLoading(false);
    setSearch(false);
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setSearch(true);
    setQueryFormObject(result);
    page !== 1 && setPage(1);
  };

  const resetSubmit = (result) => {
    setStartAndEnd([]);
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setSearch(true);
    setQueryFormObject({});
    page !== 1 && setPage(1);
  };

  const onScroll = (e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;
    if (scrollHeight - scrollTop === clientHeight && total > logData.length) {
      let nextPage = page + 1;
      setPage(nextPage);
      setLoading(true);
    }
  };

  const getFastIndexXForm = () => {
    const formMap = [
      {
        dataIndex: "executionNode",
        title: "执行节点:",
        type: "input",
        placeholder: "请输入",
      },
      {
        dataIndex: "indexName",
        title: "索引:",
        type: "select",
        options: index,
        placeholder: "请选择",
      },
    ] as any;
    if (urlParam?.datatype == 1) {
      formMap.splice(1, 0, {
        dataIndex: "templateName",
        title: "索引模板:",
        type: "select",
        options: template,
        placeholder: "请选择",
      });
    }
    return formMap;
  };

  const renderLog = () => {
    if (!logData.length) {
      return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }
    return (
      <>
        {(logData || [])?.map((item) => {
          return (
            <div key={uuid()} className="item">
              {item?.message}
            </div>
          );
        })}
      </>
    );
  };

  return (
    <div className="log-container">
      <div className="filter-content">
        <QueryForm
          columns={getFastIndexXForm()}
          defaultCollapse
          onReset={resetSubmit}
          onSearch={handleSubmit}
          isResetClearAll
          onChange={(val) => {
            if (urlParam?.datatype == 1 && val[1].value) {
              let template = val[1].value;
              let list = templateIndex.filter((item) => item?.templateName === template);
              let index = (list?.[0]?.indexName || []).map((item) => ({ title: item, value: item }));
              setIndex(index);
            }
          }}
        />
      </div>
      <div className="log-content" onScroll={onScroll}>
        <div className="content">{renderLog()}</div>
        {loading && <Spin />}
      </div>
    </div>
  );
}
