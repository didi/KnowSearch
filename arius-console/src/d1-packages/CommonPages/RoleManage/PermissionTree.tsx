import { Checkbox, Form } from "antd";
import React, { useRef, useState } from "react";
import { Tooltip } from "antd";

const getCheckItems = (options) => {
  const _checkList = [];
  for (let item of options) {
    _checkList.push(item.id);
  }
  return _checkList;
};

const SinglePermission = (props) => {
  const { data, isEdit } = props;
  const allOptions = (data.childList || []).map((item) => ({
    ...item,
    label: (
      <Tooltip key={item.id} title={item.permissionName}>
        {item.permissionName}
      </Tooltip>
    ),
    value: item.id,
  }));

  const checkItems = getCheckItems((data.childList || []).filter((item) => item.has));
  const [checkedList, setCheckedList] = useState(checkItems || []);
  const [indeterminate, setIndeterminate] = useState(checkItems.length !== allOptions.length && !!checkItems.length);
  const [checkAll, setCheckAll] = useState(checkItems.length === allOptions.length);

  const onChange = (list) => {
    setCheckedList(list);
    setIndeterminate(!!list.length && list.length < allOptions.length);
    setCheckAll(list.length === allOptions.length);
    props.onChange?.(list, list.length === 0);
  };

  const onCheckAllChange = (e) => {
    const checkItems = e.target.checked ? getCheckItems(allOptions) : [];
    setCheckedList(checkItems);
    setIndeterminate(false);
    setCheckAll(e.target.checked);
    props.onChange?.(checkItems, !e.target.checked);
  };

  return (
    <>
      <div className="check-item" key={data.id}>
        <div className="label">
          {isEdit ? (
            <Checkbox indeterminate={indeterminate} onChange={onCheckAllChange} checked={checkAll}>
              {data.permissionName}
            </Checkbox>
          ) : (
            <span>{data.permissionName}</span>
          )}
        </div>
        <div className="content">
          <Checkbox.Group disabled={!isEdit} options={allOptions} value={checkedList} onChange={onChange} />
        </div>
      </div>
    </>
  );
};

const PermissionTree = React.forwardRef((props: any, ref) => {
  const { isUpdate, permissionData } = props;

  const initPermissionIdList = () => {
    const checkedItems = {};
    if (isUpdate) {
      const checkedData = permissionData.filter((item) => item.has);
      for (let item of checkedData) {
        checkedItems[item.id] = [item.id];
        for (let row of item.childList || []) {
          if (item.has) {
            checkedItems[item.id].push(row.id);
          }
        }
      }
    }
    return checkedItems;
  };

  const permissionIdMap = useRef({});

  React.useEffect(() => {
    permissionIdMap.current = initPermissionIdList();
    return () => {
      permissionIdMap.current = null;
    };
  }, [permissionData]);

  const onChange = (parentId: number, subIds: number[], isNone: boolean) => {
    // isNone 表示该权限项无任何权限点
    permissionIdMap.current[parentId] = isNone ? [] : [parentId, ...subIds];
  };

  const getPermissionIdList = () => {
    const permissionIdList = [];
    for (let key of Object.keys(permissionIdMap.current)) {
      permissionIdList.push(...permissionIdMap.current[key]);
    }
    return permissionIdList;
  };

  React.useImperativeHandle(ref, () => ({
    getPermissionIdList,
  }));

  return (
    <>
      <div className="role-tree-panel">
        {permissionData.map((item, index) => (
          <SinglePermission
            onChange={(values, isNone) => onChange(item.id, values, isNone)}
            isEdit={props.isEdit}
            data={item}
            key={item.id}
          />
        ))}
      </div>
    </>
  );
});

export default PermissionTree;
