import React, { memo, useState, useRef, Fragment, useEffect } from "react";
import { EditOutlined, SaveOutlined, QuestionCircleOutlined } from "@ant-design/icons";
import { Popover, Form, Input, Select, message, Popconfirm } from "antd";
import { useClickOutSide } from "./useClickOutSide";
import { updateDynamicConfig } from "../../../../api/cluster-api";
import _ from "lodash";
import urlParser from "lib/url-parser";

const { Option } = Select;

interface propsType {
  name: string;
  title: string;
  info: string;
  value: any;
  type: "input" | "select";
  unit?: string;
  check?: (val: string) => string;
  selectList?: { value: string; name: string }[];
  mode?: "tags" | "multiple";
  confirmMessage?: string;
  setBackground?: boolean;
  reloadData: any;
}
type validateStatusType = "" | "success" | "warning" | "error" | "validating";

export const EditListItem: React.FC<propsType> = memo(
  ({ name, title, info, value, type, check, mode, selectList = [], unit, confirmMessage, setBackground, reloadData }) => {
    if (unit && unit == "%" && parseInt(value) != value) {
      value = Number(value).toFixed(2);
    }
    if (String(value) == "NaN") {
      value = "";
    }
    const [res, setRes] = useState(value);
    const [isEdit, setIsEdit] = useState(false);
    const [inpVal, setInpVal] = useState(value);
    const [isRight, setIsRight] = useState(true);
    const [validateStatus, setValidateStatus] = useState<validateStatusType>("validating");
    const [help, setHelp] = useState("");
    const isOutSide = useRef(true);
    const componentRef = useRef<HTMLDivElement>(null);

    const updateAsyncDynamicConfig = () => {
      const clusterName = urlParser().search.physicsCluster;
      if (Array.isArray(inpVal)) {
        return updateDynamicConfig(clusterName, name, inpVal);
      }
      let val = inpVal;
      if (unit) {
        val += unit;
      }
      return updateDynamicConfig(clusterName, name, val);
    };

    useClickOutSide(
      componentRef,
      () => {
        if (!isOutSide.current) {
          return;
        }
        setValidateStatus("validating");
        setHelp("");
        setIsRight(true);
        setInpVal(res);
        setIsEdit(false);
      },
      true
    );

    const onInpValChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const help = check && check(e.target.value);
      if (help) {
        setIsRight(false);
        setValidateStatus("error");
        setHelp(help);
      } else {
        setIsRight(true);
        setValidateStatus("validating");
        setHelp("");
      }
      setInpVal(e.target.value);
    };

    const onSelectChange = (value) => {
      setInpVal(value);
    };

    const onSave = async () => {
      isOutSide.current = false;
      if (isRight) {
        if (inpVal !== res) {
          try {
            const res = await updateAsyncDynamicConfig();
            if (res && (res.code === 0 || res.code === 200)) {
              message.success("修改成功");
              setRes(inpVal);
              reloadData && reloadData(title, name, inpVal);
            } else {
              setInpVal(res);
            }
          } catch (error) {
            setInpVal(res);
          }
        }
        setIsEdit(false);
      } else {
        if (type === "input") {
          message.error(help);
        }
      }
      isOutSide.current = true;
    };

    const renderEdit = () => {
      if (type === "input") {
        return (
          <Form>
            <Form.Item validateStatus={validateStatus} help={help}>
              <Input
                onChange={onInpValChange}
                value={inpVal}
                allowClear
                onPressEnter={() => {
                  if (!confirmMessage) {
                    onSave();
                  }
                }}
              />
            </Form.Item>
          </Form>
        );
      }
      return (
        <Select
          value={inpVal}
          mode={mode}
          onChange={onSelectChange}
          getPopupContainer={() => document.getElementById(`detail-edit-list-row-col-item-${name}`)}
        >
          {selectList.map((item, index) => (
            <Option value={item.value} key={item.value + index}>
              {item.name}
            </Option>
          ))}
        </Select>
      );
    };

    const renderRes = () => {
      if (Array.isArray(res)) {
        return res.join(", ");
      }
      if (res == null || res == "null") {
        return "-";
      }
      return String(res) || "-";
    };

    return (
      <div className={`ant-col ant-col-12 detail-edit-list-row-col ${setBackground ? "background" : ""}`} ref={componentRef}>
        <div className="detail-edit-list-row-col-label">
          <Popover content={<div className="table-popover-content">{info}</div>}>{name}</Popover>
        </div>
        <div className="detail-edit-content">
          <div className="detail-edit-list-row-col-item" id={`detail-edit-list-row-col-item-${name}`}>
            {isEdit ? (
              renderEdit()
            ) : (
              <div className="edit-box">
                {renderRes() || "-"}
                {renderRes() ? unit : null}
              </div>
            )}
          </div>
          {isEdit && confirmMessage ? (
            <div className="detail-edit-list-btn" id={`detail-edit-list-btn-${name}`}>
              <Popconfirm
                placement="topRight"
                title={confirmMessage}
                icon={<QuestionCircleOutlined style={{ color: "red" }} />}
                getPopupContainer={() => document.getElementById(`detail-edit-list-btn-${name}`)}
                onConfirm={() => {
                  onSave();
                }}
              >
                <SaveOutlined />
              </Popconfirm>{" "}
            </div>
          ) : null}
          {isEdit && !confirmMessage ? (
            <div className="detail-edit-list-btn" onClick={onSave}>
              <SaveOutlined />
            </div>
          ) : null}

          {!isEdit ? (
            <div className="detail-edit-list-btn" onClick={() => setIsEdit(true)}>
              <EditOutlined />
            </div>
          ) : null}
        </div>
      </div>
    );
  }
);
