import { Select } from "antd";
import { getPhyIndexNameList } from "api/cluster-index-api";
import { getLogicIndexNameList } from "api/cluster-kanban";
import { getDslByIndex } from "api/search-query";
import { isSuperApp, uuid } from "lib/utils";
import debounce from "lodash/debounce";
import React, { useState, useEffect, forwardRef, useImperativeHandle } from "react";

export const IndexSelect = forwardRef((props: any, ref) => {
  const [value, setValue] = useState();
  const [list, setList] = useState([]);

  useEffect(() => {
    getIndexList();
  }, [props.currentCluster?.id]);

  const getIndexList = async (index?: string) => {
    const superApp = isSuperApp();
    const indexNameList = await (superApp
      ? getPhyIndexNameList(props.currentCluster?.id)
      : getLogicIndexNameList(props.currentCluster?.id));

    setValue(indexNameList?.[0] || null);
    setList(indexNameList);
    props.resetProfile();
    props.setBtnDisabled(!indexNameList.length);
    if (indexNameList.length) {
      getIndexDsl(indexNameList[0]);
    }
  };

  const getIndexDsl = (indexName) => {
    getDslByIndex(indexName).then((res) => {
      let editorInstance = props.getEditorInstance();

      let dsl = "";
      try {
        dsl = JSON.parse(res)?.dsl;
        dsl = JSON.stringify(JSON.parse(dsl), null, 4);
      } catch (err) {}

      editorInstance?.setValue && editorInstance.setValue(dsl);
    });
  };

  const onChange = (index) => {
    setValue(index);
    getIndexDsl(index);
  };

  const handleSearch = debounce((newValue: string) => {
    getIndexList(newValue);
  }, 200);

  useImperativeHandle(ref, () => ({
    value,
  }));

  return (
    <Select showSearch value={value} onChange={onChange} placeholder="请选择索引">
      {list?.map((item: any) => (
        <Select.Option value={item} key={uuid()}>
          {item}
        </Select.Option>
      ))}
    </Select>
  );
});
