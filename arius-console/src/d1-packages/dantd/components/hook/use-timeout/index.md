---
category: 自定义Hook
cols: 1
type: hook
title: useTimeout
subtitle: 间隔
---

## 何时使用

在指定的毫秒数后重新渲染组件。 提供取消或重置超时的调用方式。


```jsx
import { useTimeout } from 'antd-advanced';

function TestComponent(props: { ms?: number } = {}) {
  const ms = props.ms || 5000;
  const [isReady, cancel] = useTimeout(ms);

  return (
    <div>
      { isReady() ? 'I\'m reloaded after timeout' : `I will be reloaded after ${ ms / 1000 }s` }
      { isReady() === false ? <button onClick={ cancel }>Cancel</button> : '' }
    </div>
  );
}

const Demo = () => {
  return (
    <div>
      <TestComponent />
      <TestComponent ms={ 10000 } />
    </div>
  );
};
```


copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useTimeout.md)
