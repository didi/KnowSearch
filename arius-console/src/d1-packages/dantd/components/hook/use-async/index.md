---
category: 自定义Hook
cols: 1
type: hook
title: useAsync
subtitle: 请求
---

## 何时使用

接收一个 async 函数或返回 promise 的函数，返回状态，数据形状与 `useAsync` 相同。


```jsx
import { useAsyncFn } from 'antd-advanced';

const fn = () => new Promise((resolve) => {
  setTimeout(() => {
    resolve('RESOLVED');
  }, 1000);
});

const Demo = () => {
  const state = useAsync(fn);

  return (
    <div>
      {state.loading?
        <div>Loading...</div>
        : state.error?
        <div>Error...</div>
        : <div>Value: {state.value}</div>
      }
    </div>
  );
};

ReactDOM.render(
  <div>
    <Demo />
  </div>,
  mountNode,
);
```


copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useAsyncFn.md)
