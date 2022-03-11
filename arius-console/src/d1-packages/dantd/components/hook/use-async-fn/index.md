---
category: 自定义Hook
cols: 1
type: hook
title: useAsyncFn
subtitle: 函数请求
---

## 何时使用

这个 `Hook` 用于解析 async 函数或返回 promise 的函数。


```jsx
import { useAsyncFn } from 'antd-advanced';

const Demo = (url) => {
  const [state, fetch] = useAsyncFn(async () => {
    const response = await fetch(url);
    const result = await response.text();
    return result
  }, [url]);

  return (
    <div>
      {state.loading
        ? <div>Loading...</div>
        : state.error
          ? <div>Error: {state.error.message}</div>
          : state.value && <div>Value: {state.value}</div>
      }
      <button onClick={() => fetch()}>Start loading</button>
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

copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useAsync.md)
