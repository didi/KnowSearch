---
category: 自定义Hook
cols: 1
type: 工厂
title: createStateContext
subtitle: 全局State工厂
---

## 何时使用

react上下文 hook 的工厂，除了状态将在提供者中的所有组件之间共享之外，它的行为就像react的useState。

这允许您拥有任何组件都可以轻松更新的共享状态。


```jsx
import { createStateContext } from 'antd-advanced';

const [useSharedText, SharedTextProvider] = createStateContext('');

const ComponentA = () => {
  const [text, setText] = useSharedText();
  return (
    <p>
      Component A:
      <br />
      <input type="text" value={text} onInput={ev => setText(ev.target.value)} />
    </p>
  );
};

const ComponentB = () => {
  const [text, setText] = useSharedText();
  return (
    <p>
      Component B:
      <br />
      <input type="text" value={text} onInput={ev => setText(ev.target.value)} />
    </p>
  );
};

const Demo = () => {
  return (
    <SharedTextProvider>
      <p>Those two fields share the same value.</p>
      <ComponentA />
      <ComponentB />
    </SharedTextProvider>
  );
};
```


copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/createStateContext.md)
