import React from "react";
import { InputNumber } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import { connect } from "react-redux";
import * as actions from "actions";

const mapDispatchToProps = (dispatch) => ({
  setClusterCost: (n: number) => dispatch(actions.setClusterCost(n)),
});
// 成本计算
const ExpectInput: React.FC<any> = (props) => {
  const [InputValue, setValue] = React.useState(0);
  const [isExpect, setIsExpect] = React.useState(true);

  React.useEffect(() => {
    setValue(props.podNumber || 0);
  }, []);

  const onChange = (value) => {
    value = Number(value);
    if (!value || isNaN(value) || value < 0) {
      value = 0;
    }
    setValue(value);
    setIsExpect(props.podNumber - value <= 0);
    const { onChange } = props;
    onChange && onChange(value);
    // 判断是否是正整数
    if (!regNonnegativeInteger.test(value) || value <= 1) {
      return false;
    }
  };

  return (
    <>
      <InputNumber min={props.min} value={InputValue} onInput={onChange} onStep={onChange} />
      <span style={{ paddingLeft: 10 }}>
        {`${isExpect ? "增加" : "减少"}${Math.abs(props.podNumber - InputValue)}节点，集群${
          isExpect ? "扩容" : "缩容"
        }至${InputValue}个节点`}
      </span>
    </>
  );
};
export const ExpectDataNodeNu = connect(null, mapDispatchToProps)(ExpectInput);

const mapStateToProps = (state: any) => ({
  cost: state.clusterBase.cost,
});
// 成本展示
const ShowCostComponent: React.FC<any> = (props: any) => {
  return <span>{props.cost}</span>;
};
export const ShowCost = connect(mapStateToProps)(ShowCostComponent);
