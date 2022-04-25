---
category: 自定义Hook
cols: 1
type: hook
title: useAsyncRetry
subtitle: retry请求
---

## 何时使用

使用 `useAsync` 和一个额外的retry方法来轻松重试/刷新异步函数;


```jsx
import { useAsyncRetry } from 'antd-advanced';

// Returns a Promise that resolves after one second.
const fn = () => new Promise((resolve, reject) => {
  setTimeout(() => {
    if (Math.random() > 0.5) {
      reject(new Error('Random error!'));
    } else {
      resolve('RESOLVED');
    }
  }, 1000);
});

const Demo = () => {
  const state = useAsyncRetry(fn);

  return (
    <div>
      {state.loading?
        <div>Loading...</div>
        : state.error?
        <div>Error...</div>
        : <div>Value: {state.value}</div>
      }
      {!state.loading?
        <a href='javascript:void 0' onClick={() => state.retry()}>Retry</a>
        : null
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

copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useAsyncRetry.md)
