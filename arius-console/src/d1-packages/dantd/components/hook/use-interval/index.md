---
category: 自定义Hook
cols: 1
type: hook
title: useInterval
subtitle: 定时器
---

## 何时使用

一个定时器的hook，基于[Josh的文章](https://joshwcomeau.com/snippets/react-hooks/use-interval)。
可以通过将延迟设置为null来暂停时间间隔。
当组件卸载的时候，会自动清除定时器。
也可以通过 `window.clearInterval` 来强行停止定制器。

### 关于 Antd 的 Tab 组件

但是一般不推荐这么做，比如 `Antdv3` 的 [Tab](https://ant-design-3x.gitee.io/components/tabs-cn/) 组件，
可以添加 [destroyInactiveTabPane](https://github.com/ant-design/ant-design/issues/15102) 属性，当切换Tab时，自动卸载Tab里面的东西即可。

```jsx
import { useInterval, Button } from 'antd-advanced';

const IntervalDemo = () => {
  const [status, setStatus] = React.useState('running');
  const [timeElapsed, setTimeElapsed] = React.useState(0);
  const instance = useInterval(
    () => {
      setTimeElapsed(timeElapsed => timeElapsed + 1);
    },
    status === 'running' ? 1000 : null,
  );
  const toggle = () => {
    setTimeElapsed(0);
    setStatus(status => (status === 'running' ? 'idle' : 'running'));
  };

  const stop = () => {
    window.clearInterval(instance);
  };
  return (
    <>
      Time Elapsed: {timeElapsed} second(s) <br />
      <Button onClick={toggle}>{status === 'running' ? 'Stop' : 'Start'}</Button>
      <Button onClick={stop}>强行停止</Button>
    </>
  );
};
```


copy 自 [useInterval](https://joshwcomeau.com/snippets/react-hooks/use-interval)
