import React, { memo, useState, useEffect } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import * as actions from "../../../actions";
import { Button, Modal, Checkbox, Divider, Form } from "knowdesign";
const CheckboxGroup = Checkbox.Group;
import "./index-config.less";

interface selectPropsType {
  title: string;
  plainOptions: any[];
  defaultCheckedList: any[];
  value?: [];
  onChange?: (value) => void;
}

const SelectCheckBox: React.FC<selectPropsType> = memo(({ title, defaultCheckedList, plainOptions, onChange }) => {
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
    setCheckedList(e.target.checked ? plainOptions.map((item) => item.value) : []);
    setIndeterminate(false);
    setCheckAll(e.target.checked);
  };

  return (
    <>
      <Checkbox indeterminate={indeterminate} onChange={onCheckAllChange} checked={checkAll}>
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
});

export const IndexConfig = memo(() => {
  const {
    params: { title, optionList, defaultCheckedData, needShortcut = false },
    cb: callback,
  } = useSelector(
    (state: any) => ({
      params: state.modal.params,
      cb: state.modal.cb,
    }),
    shallowEqual
  );
  const indicatorsConfig = {
    gold: {},
    performance: {},
    hot: {},
    memory: {},
  };
  if (needShortcut) {
    optionList.forEach((item) => {
      indicatorsConfig.gold[item.title] = [];
      indicatorsConfig.performance[item.title] = [];
      indicatorsConfig.hot[item.title] = [];
      indicatorsConfig.memory[item.title] = [];
      item.plainOptions.forEach((indicator) => {
        // 黄金指标1、性能指标2、热点指标3、内存指标4
        if (indicator.indicatorType?.indexOf(1) > -1) indicatorsConfig.gold[item.title].push(indicator.value);
        if (indicator.indicatorType?.indexOf(2) > -1) indicatorsConfig.performance[item.title].push(indicator.value);
        if (indicator.indicatorType?.indexOf(3) > -1) indicatorsConfig.hot[item.title].push(indicator.value);
        if (indicator.indicatorType?.indexOf(4) > -1) indicatorsConfig.memory[item.title].push(indicator.value);
      });
    });
  }

  const dispatch = useDispatch();
  const [isDisabled, setIsDisabled] = useState(true);
  const [check, setCheck] = useState(false);
  const [count, setCount] = useState(0);
  const [selectedData, setSelectedData] = useState(defaultCheckedData || indicatorsConfig.gold || {});
  const [indicatorConfig, setIndicatorConfig] = useState({});
  const [goldActive, setGoldActive] = useState(defaultCheckedData ? false: true);
  const [performanceActive, setPerformanceActive] = useState(false);
  const [hotActive, setHotActive] = useState(false);
  const [memoryActive, setMemoryActive] = useState(false);

  const filterIndicators = (type) => {
    switch (type) {
      case 'gold':
        setGoldActive((state) => {
          return !state
        });
        break;
      case 'performance':
        setPerformanceActive((state) => {
          return !state
        });
        break;
      case 'hot':
        setHotActive((state) => {
          return !state
        });
        break;
      case 'memory':
        setMemoryActive((state) => {
          return !state
        });
        break;
      default:
        break;
    }
  };

  useEffect(() => {
    const newConfig = Object.keys(indicatorsConfig.gold).reduce((obj:any, item:string) => {
      obj[item] = [
        ...new Set([
          ...goldActive ? indicatorsConfig.gold[item] : [],
          ...performanceActive ? indicatorsConfig.performance[item] : [],
          ...hotActive ? indicatorsConfig.hot[item] : [],
          ...memoryActive ? indicatorsConfig.memory[item] : []
        ])
      ];
      return obj
    },{})
    setIndicatorConfig(newConfig);
    setCheck(true);
    setCount((state) => state + 1);
  }, [goldActive,performanceActive,hotActive,memoryActive]);

  const renderFooter = () => {
    return (
      <div style={{ overflow: "hidden" }} className="indicators">
        {needShortcut ? (
          <>
            <Button key="gold" className={ goldActive ? 'activ': '' } onClick={() => filterIndicators("gold")} style={{ float: "left" }}>
              黄金指标
            </Button>
            <Button key="performance" className={ performanceActive ? 'activ': '' } onClick={() => filterIndicators("performance")} style={{ float: "left" }}>
              性能指标
            </Button>
            <Button key="hot" className={ hotActive ? 'activ' : '' } onClick={() => filterIndicators("hot")} style={{ float: "left" }}>
              热点指标
            </Button>
            <Button key="memory" className={ memoryActive ? 'activ' : '' } onClick={() => filterIndicators("memory")} style={{ float: "left" }}>
              内存指标
            </Button>
          </>
        ) : null}
        <Button
          type="primary"
          key="ok"
          disabled={isDisabled}
          onClick={() => {
            dispatch(actions.setModalId(""));
            callback(selectedData);
          }}
          style={{ float: "right", marginLeft: 8 }}
        >
          确认
        </Button>
        <Button key="cancel" onClick={() => dispatch(actions.setModalId(""))} style={{ float: "right" }}>
          取消
        </Button>
      </div>
    );
  };

  useEffect(() => {
    if (JSON.stringify(selectedData) !== JSON.stringify(indicatorConfig)) {
      setCheck(false);
    }
  }, [selectedData]);

  return (
    <>
      <Modal
        title={title}
        centered
        visible={true}
        onCancel={() => dispatch(actions.setModalId(""))}
        width={660}
        footer={renderFooter()}
        maskClosable={false}
        className="index-config"
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
                  key={count}
                  defaultCheckedList={check ? indicatorConfig[item.title] : selectedData[item.title]}
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
