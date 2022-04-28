---
category: 自定义Hook
cols: 1
type: 工厂
title: createReducerContext
subtitle: 全局Reducer工厂
---

## 何时使用

react上下文 hook 的工厂，除了状态将在提供者中的所有组件之间共享之外，它的行为与react的useReducer一样。

这允许您拥有任何组件都可以轻松更新的共享状态。


```jsx
import { createReducerContext } from 'antd-advanced';

type Action = 'increment' | 'decrement';

const reducer = (state: number, action: Action) => {
  switch (action) {
    case 'increment':
      return state + 1;
    case 'decrement':
      return state - 1;
    default:
      throw new Error();
  }
};

const [useSharedCounter, SharedCounterProvider] = createReducerContext(reducer, 0);

const ComponentA = () => {
  const [count, dispatch] = useSharedCounter();
  return (
    <p>
      Component A &nbsp;
      <button type="button" onClick={() => dispatch('decrement')}>
        -
      </button>
      &nbsp;{count}&nbsp;
      <button type="button" onClick={() => dispatch('increment')}>
        +
      </button>
    </p>
  );
};

const ComponentB = () => {
  const [count, dispatch] = useSharedCounter();
  return (
    <p>
      Component B &nbsp;
      <button type="button" onClick={() => dispatch('decrement')}>
        -
      </button>
      &nbsp;{count}&nbsp;
      <button type="button" onClick={() => dispatch('increment')}>
        +
      </button>
    </p>
  );
};

const Demo = () => {
  return (
    <SharedCounterProvider>
      <p>Those two counters share the same value.</p>
      <ComponentA />
      <ComponentB />
    </SharedCounterProvider>
  );
};
```

```jsx

const [useSharedState, SharedStateProvider] = createReducerContext(reducer, initialState);

// In wrapper
const Wrapper = ({ children }) => (
  // You can override the initial state for each Provider
  <SharedStateProvider initialState={overrideInitialState}>
    { children }
  </SharedStateProvider>
)

// In a component
const Component = () => {
  const [sharedState, dispatch] = useSharedState();

  // ...
}
```

copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/createReducerContext.md)
