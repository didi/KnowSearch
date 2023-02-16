import React, { memo, useEffect, useState } from "react";
import { Select } from "knowdesign";
import "../style/refresh-time.less";

const { Option } = Select;

interface propsType {
  changeRefreshTime: (val) => void;
}

const seconds = 1000;
const minutes = 60 * seconds;
export const RefreshTime: React.FC<propsType> = memo(({ changeRefreshTime }) => {
  const [isRotate, setIsRotate] = useState(false);
  const [awaitTime, setAwaitTime] = useState(0);
  const refreshConfig = [
    {
      value: 1 * minutes,
      name: "自动刷新：1分钟",
    },
    {
      value: 3 * minutes,
      name: "自动刷新：3分钟",
    },
    {
      value: 5 * minutes,
      name: "自动刷新：5分钟",
    },
    {
      value: 10 * minutes,
      name: "自动刷新：10分钟",
    },
    {
      value: 0,
      name: "关闭",
    },
  ];

  const onSelectChange = (val) => {
    changeRefreshTime(val);
    setAwaitTime(val);
  };

  useEffect(() => {
    if (!awaitTime) {
      return;
    }

    let interval, timeout;

    timeout && clearTimeout(timeout);
    interval && clearInterval(interval);

    setInterval(() => {
      setIsRotate(true);

      setTimeout(() => {
        setIsRotate(false);
      }, 300);
    }, awaitTime);

    return () => {
      clearTimeout(timeout);
      clearInterval(interval);
    };
  }, [awaitTime]);

  return (
    <div className="refresh-time-container">
      {/* <span className="refresh-time-desc">自动刷新</span> */}
      <div className={`refresh-select ${awaitTime ? "refresh-icon-container" : ""}`}>
        <Select onChange={onSelectChange} defaultValue={0}>
          {refreshConfig.map((item) => (
            <Option value={item.value} key={item.name + item.value}>
              {item.name}
            </Option>
          ))}
        </Select>
      </div>
    </div>
  );
});
