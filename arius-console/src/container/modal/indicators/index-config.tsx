import React, { memo, useState, useEffect } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import * as actions from "../../../actions";
import { Button, Modal, Checkbox, Divider, Form, notification } from "antd";
const CheckboxGroup = Checkbox.Group;
import "./index-config.less";

interface selectPropsType {
  title: string;
  plainOptions: any[];
  defaultCheckedList: any[];
  value?: [];
  onChange?: (value) => void;
}

const SelectCheckBox: React.FC<selectPropsType> = memo(
  ({ title, defaultCheckedList, plainOptions, onChange }) => {
    const [checkedList, setCheckedList] = useState(defaultCheckedList || []);
    const [indeterminate, setIndeterminate] = useState(true);
    const [checkAll, setCheckAll] = useState(false);

    useEffect(() => {
      if (checkedList.length === plainOptions.length) {
        setIndeterminate(false);
        setCheckAll(true);
      }
      onChange(checkedList);
    }, [checkedList]);

    const onCheckboxGroupChange = (list) => {
      setCheckedList(list);
      setIndeterminate(!!list.length && list.length < plainOptions.length);
      setCheckAll(list.length === plainOptions.length);
    };

    const onCheckAllChange = (e) => {
      setCheckedList(
        e.target.checked ? plainOptions.map((item) => item.value) : []
      );
      setIndeterminate(false);
      setCheckAll(e.target.checked);
    };

    return (
      <>
        <Checkbox
          indeterminate={indeterminate}
          onChange={onCheckAllChange}
          checked={checkAll}
        >
          {title}
        </Checkbox>
        <br />
        <CheckboxGroup
          className="checkboxGroup"
          options={plainOptions.map((item) => ({
            ...item,
            value: item.value,
          }))}
          value={checkedList}
          onChange={onCheckboxGroupChange}
        />
      </>
    );
  }
);

export const IndexConfig = memo(() => {
  const {
    params: { title, optionList, defaultCheckedData },
    cb: callback,
  } = useSelector(
    (state: any) => ({
      params: state.modal.params,
      cb: state.modal.cb,
    }),
    shallowEqual
  );
  const dispatch = useDispatch();
  const [isDisabled, setIsDisabled] = useState(true);
  const [selectedData, setSelectedData] = useState(defaultCheckedData || {});

  return (
    <>
      <Modal
        title={title}
        centered
        visible={true}
        onCancel={() => dispatch(actions.setModalId(""))}
        width={660}
        footer={[
          <Button key="cancel" onClick={() => dispatch(actions.setModalId(""))}>
            取消
          </Button>,
          <Button
            type="primary"
            key="ok"
            disabled={isDisabled}
            onClick={() => {
              dispatch(actions.setModalId(""));
              callback(selectedData);
            }}
          >
            确认
          </Button>,
        ]}
      >
        <Form
          onValuesChange={(changedValues, allValues) => {
            if (!allValues) {
              setIsDisabled(true);
              return;
            }
            let flag = true;
            for (let key in allValues) {
              if (!allValues[key] || allValues[key].length > 0) {
                flag = false;
                break;
              }
            }
            setSelectedData(allValues);
            setIsDisabled(flag);
          }}
        >
          {optionList.map((item, index) => (
            <div key={item.title + index}>
              <Form.Item name={item.title}>
                <SelectCheckBox
                  title={item.title}
                  defaultCheckedList={selectedData[item.title]}
                  plainOptions={item.plainOptions}
                />
              </Form.Item>
              {index < optionList.length - 1 ? <Divider /> : null}
            </div>
          ))}
        </Form>
      </Modal>
    </>
  );
});
