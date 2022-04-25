---
category: 自定义Hook
cols: 1
type: hook
title: useDeepCompareEffect
subtitle: 深比较Effect
---

## 何时使用

需要深比较依赖，触发Effect的时候


```jsx
import { useState } from 'react';
import { useDeepCompareEffect } from 'antd-advanced';

const Demo = () => {
  const [count, setCount] = useState(0);
  const options = { step: 2 };

  useDeepCompareEffect(() => {
    setCount(count + options.step);
  }, [options]);

  return (
    <div>
      <p>useDeepCompareEffect: {count}</p>
    </div>
  );
};
```


copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useAsyncFn.md)
