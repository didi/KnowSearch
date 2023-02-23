import React, { useEffect, useState } from "react";
import { Table, Input, Pagination } from "antd";
import { getIndexMappingInfo } from "api/cluster-index-api";
import { getMapping } from "api/index-admin";
import { getTemplateSetting, getIndexSetting } from "api/fastindex-api";
import { mappingFormatJsonStr, getFormatJsonStr } from "lib/utils";
import EditDrawer from "./edit-drawer";
import "./index.less";

export default function TargetTable(props) {
  const { dataType, onChange, targetTaskList, relationType, logic } = props;

  const [orginData, setOrginData] = useState(targetTaskList || []);
  const [data, setData] = useState(targetTaskList || []);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [name, setName] = useState("");
  const [visible, setVisible] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [content, setContent] = useState({} as any);

  useEffect(() => {
    reloadData();
  }, [targetTaskList, logic]);

  const reloadData = async () => {
    setLoading(true);
    let list = targetTaskList;
    if (dataType === 1) {
      (targetTaskList || []).forEach(async (item, index) => {
        let mapping = await getIndexMappingInfo(item.id);
        let setting = logic?.value && (await getTemplateSetting(logic?.value, item.id));
        const mappingData = mapping?.typeProperties?.[0];
        list[index].mapping = mappingFormatJsonStr(mappingData?.properties, data?.["dynamic_templates"]);
        list[index].setting = getFormatJsonStr(setting?.settings);
        list[index].orginMapping = mappingFormatJsonStr(mappingData?.properties, data?.["dynamic_templates"]);
        list[index].orginSetting = getFormatJsonStr(setting?.settings);
      });
    } else {
      (targetTaskList || []).forEach(async (item, index) => {
        let mappingRes = await getMapping(item.cluster, item.index);
        let settingRes = await getIndexSetting(item.cluster, item.index);
        list[index].mapping = getFormatJsonStr(JSON.parse(mappingRes?.mappings));
        list[index].setting = getFormatJsonStr(settingRes?.properties);
        list[index].orginMapping = getFormatJsonStr(JSON.parse(mappingRes?.mappings));
        list[index].orginSetting = getFormatJsonStr(settingRes?.properties);
      });
    }
    setOrginData(list);
    setData(list);
    setLoading(false);
    onChange(list);
  };

  const filterData = () => {
    let list = orginData.filter((item) => (dataType === 1 ? item.name?.includes(name) : item.index?.includes(name)));
    setData(list);
  };

  const getColumns: any = () => {
    let columns = [
      {
        title: dataType === 1 ? "源索引模板" : "源索引",
        dataIndex: dataType === 1 ? "name" : "index",
        width: 140,
        render: (val, record) => {
          return (
            <div className="source-index">
              <span className="name">{val}</span>
              <span
                className="icon iconfont iconxiangqing"
                onClick={() => {
                  setVisible(true);
                  setContent(record);
                }}
              ></span>
            </div>
          );
        },
      },
      {
        title: dataType === 1 ? "目标索引模板" : "目标索引",
        dataIndex: "targetName",
        width: 140,
        onCell: (_, index: number) => {
          if (relationType === 1) {
            return { rowSpan: index === 0 ? data.length : 0 };
          }
          return {};
        },
        render: (val, record, index: number) => {
          let targetContent = (
            <div className="target-index">
              <span className="name">{val}</span>
              <span
                className="icon iconfont iconbianji"
                onClick={() => {
                  setVisible(true);
                  setIsEdit(true);
                  setContent(record);
                }}
              ></span>
            </div>
          );
          if (relationType === 1) {
            return {
              children: targetContent,
              props: { rowSpan: index === 0 ? data.length : 0 },
            };
          }
          return targetContent;
        },
      },
    ];
    return columns;
  };

  return (
    <>
      <Input
        allowClear
        className="source-search"
        placeholder={dataType === 1 ? "请输入索引模板名称" : "请输入索引名称"}
        onChange={(e) => {
          let val = e.target.value;
          setName(val);
          if (!val) {
            setData(orginData);
          }
        }}
        onPressEnter={filterData}
        suffix={<span className="icon iconfont icontubiao-sousuo" onClick={filterData}></span>}
      />
      <Table
        loading={loading}
        rowKey={dataType === 1 ? "id" : "key"}
        columns={getColumns()}
        dataSource={data}
        pagination={false}
        bordered={relationType === 1}
      ></Table>
      <Pagination
        className="source-pagination"
        simple
        current={page}
        total={targetTaskList?.length || 0}
        onChange={(page) => setPage(page)}
      />
      {visible && (
        <EditDrawer
          visible={visible}
          onClose={() => {
            setVisible(false);
            setIsEdit(false);
          }}
          content={content}
          isEdit={isEdit}
          dataType={dataType}
          onOk={({ mapping, setting, value }) => {
            let orginList = (orginData || []).map((item) => {
              if (dataType === 1 ? item.id === content.id : item.key === content.key) {
                return { ...item, mapping, setting, targetName: value ? value : item.targetName };
              }
              return { ...item };
            });
            let list = (data || []).map((item) => {
              if (dataType === 1 ? item.id === content.id : item.key === content.key) {
                return { ...item, mapping, setting, targetName: value ? value : item.targetName };
              }
              return { ...item };
            });
            setOrginData(orginList);
            setData(list);
            onChange(orginList);
          }}
        ></EditDrawer>
      )}
    </>
  );
}
