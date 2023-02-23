import React from "react";
import { Row, Col } from "antd";
import EditTable from "./editTable";
import "./index.less";
import { cloneDeep } from "lodash";

const basicClass = "tpl-table-form";

interface ITableFormAddRowProps {
  type: string;
  onChange?: (result: string[]) => any;
  value?: any;
  machineList?: [];
}

export interface IState {
  dataSource: any[];
  type: string;
}

export const XFormContext = React.createContext({});

export const TableFormAddRow = (props: ITableFormAddRowProps) => {
  const reducer = (state: any, action: any) => {
    state = cloneDeep(state);
    const { dataSource } = state;
    switch (action.key) {
      case "rowData": // 给表格添加一行数据
        dataSource.push(action.data || {});
        break;
      case "deleteRow": // 删除一行数据
        dataSource?.splice(action.data.index, 1);
        break;
      default:
        break;
    }
    return state;
  };

  const [state, dispatch] = React.useReducer(reducer, {
    dataSource: [{}],
    type: props.type,
  } as IState);

  React.useEffect(() => {
    const { onChange } = props;
    onChange && onChange(state.dataSource);
  }, [state.dataSource]);

  return (
    <XFormContext.Provider value={{ state, dispatch }}>
      <div className={basicClass}>
        <Row>
          <Col span={24}>
            <EditTable machineList={props.machineList} />
          </Col>
        </Row>
      </div>
    </XFormContext.Provider>
  );
};
