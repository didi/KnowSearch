import React, { useState, useEffect } from "react";
import { Modal, Checkbox, Input, Button, Utils } from "knowdesign";
import { IconFont } from "@knowdesign/icons";
export default (props) => {
  const { columns, setFilterColumns, visible = false, setVisible, tableId, title = "自定义列", modalSize } = props;
  const [checkBoxOption, setCheckBoxOption] = useState([]);
  const [searchCheckBox, setSearchCheckBox] = useState(null);
  const [checked, setChecked] = useState([]);
  const [searchValue, setSearchValue] = useState("");

  const setCheckBoxColumnsOption = () => {
    if (!Array.isArray(columns)) return;
    if (columns.length < 1) return;
    // 根据表格Id获取本地存储的不展示的数据项
    const checkedCol = tableId ? Utils.getLocalStorage(tableId) : null;
    // 依据columns遍历出新的checkBox的options
    const newcheckBoxOption = columns
      .filter((item) => !item.filterTitle)
      .map((item) => {
        return {
          ...item,
          label: item.title,
          value: item.key || item.dataIndex,
        };
      });

    // 设置新的checkBox的options
    setCheckBoxOption(newcheckBoxOption);
    if (checkedCol?.length > 0) {
      // 根据本地存储的不展示项筛选出需勾选项数组
      const changeChecked = newcheckBoxOption
        .filter((item) => !checkedCol?.includes(item.value))
        .map((item) => {
          return item.value;
        });

      setChecked(changeChecked);
      return;
    }
    // 根据item.invisible获取新的勾选项数组
    const newChecked = newcheckBoxOption
      .filter((item) => !item.invisible)
      .map((item) => {
        return item.value;
      });
    setChecked(newChecked);
  };

  const checkBoxChange = (e) => {
    // 每次change都筛选更新勾选项数组
    const searchChecked = checkBoxOption
      .filter((item) => {
        if (!searchCheckBox) return;
        return checked.includes(item.value) && !searchCheckBox.map((item) => item.value).includes(item.value);
      })
      .map((item) => item.value);

    setChecked([...searchChecked, ...e]);
  };

  // 确认按钮
  const onOk = () => {
    // 如果localstarage没有存储不展示项，table渲染会用这个新的columns
    const newColumns = columns.map((item) => {
      return {
        ...item,
        invisible: !item.filterTitle && !checked.includes(item.dataIndex || item.key),
      };
    });
    // 向localstarage存入数据
    const filterChecked = checkBoxOption
      .filter((item) => !item.filterTitle && !checked?.includes(item.value))
      .map((item) => {
        return item.value;
      });
    tableId && Utils.setLocalStorage(tableId, filterChecked);
    // 调用DTable传入设置columns的方法
    setFilterColumns(newColumns);
    // 关闭弹窗
    setVisible(false);
    // 清空搜索结果
    setSearchCheckBox(null);
    // 清空Search框
    setSearchValue("");
  };

  // 取消按钮
  const onCancel = () => {
    const checkedCol = tableId ? Utils.getLocalStorage(tableId) : null;
    if (checkedCol?.length > 0) {
      const changeChecked = checkBoxOption
        .filter((item) => !checkedCol?.includes(item.value))
        .map((item) => {
          return item.value;
        });
      setChecked(changeChecked);
    } else {
      const newChecked = checkBoxOption
        .filter((item) => !item.invisible)
        .map((item) => {
          return item.value;
        });
      setChecked(newChecked);
    }
    setSearchCheckBox(null);
    setSearchValue("");
    setVisible(false);
  };

  // 搜索
  const searchChange = (e) => {
    const value = e.target.value || "";
    setSearchValue(value);
    const newCheckBoxOption = checkBoxOption.filter((item) => {
      return item.title.includes(value);
    });
    setSearchCheckBox(newCheckBoxOption);
  };

  // 恢复系统默认
  const restoringDefaults = () => {
    const newChecked = checkBoxOption.map((item) => {
      return item.value;
    });
    setChecked(newChecked);
    // 清空搜索结果
    setSearchCheckBox(null);
    // 清空Search框
    setSearchValue("");
  };

  useEffect(() => {
    setCheckBoxColumnsOption();
  }, [props.columns, props.visible]);

  return (
    <Modal
      className="dcloud-filter-modal"
      title={title}
      visible={visible}
      onOk={onOk}
      onCancel={onCancel}
      width={340}
      bodyStyle={{
        padding: "0 24px",
      }}
      footer={
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Button size={modalSize} onClick={restoringDefaults}>
              恢复系统默认
            </Button>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <Button size={modalSize} onClick={onCancel} type="default">
              取消
            </Button>
            <Button size={modalSize} onClick={onOk} type="primary">
              确定
            </Button>
          </div>
        </div>
      }
    >
      <div className={"dcloud-checkbox-table-serch"}>
        <Input
          size={modalSize}
          value={searchValue}
          onChange={searchChange}
          suffix={<IconFont type="icon-fangdajing" />}
          placeholder="搜索字段"
        />
      </div>
      <Checkbox.Group
        className={"dcloud-checkbox-table-columns"}
        options={searchCheckBox || checkBoxOption}
        value={checked}
        onChange={checkBoxChange}
      />
    </Modal>
  );
};
