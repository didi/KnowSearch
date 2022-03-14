---
category: 自定义Hook
cols: 1
type: hook
title: useMountedState
subtitle: Mounted
---

## 何时使用

生命周期钩子提供检查组件的挂载状态的能力。

给出一个函数，如果组件被挂载，该函数将返回true，否则返回false。


```jsx
import { useMountedState } from 'antd-advanced';


const Demo = () => {
  const isMounted = useMountedState();

  React.useEffect(() => {
    setTimeout(() => {
      if (isMounted()) {
        // ...
      } else {
        // ...
      }
    }, 1000);
  });
};

ReactDOM.render(
  <div>
    <Demo />
  </div>,
  mountNode,
);
```


copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/useMountedState.md)
