---
category: 自定义Hook
cols: 1
type: 工厂
title: createGlobalState
subtitle: 全局状态工厂
---

## 何时使用

一个创造全局共享状态的 React Hook，可以理解为一个全局的 `useState`。

`createGlobalState` 工厂会返回一个自定义hook，这个自定义hook的使用方式和 `useState` 是一样的，
返回一个 `state`，以及更新 `state` 的函数

> 与 class 组件中的 setState 方法不同，useState 不会自动合并更新对象。你可以用函数式的 setState 结合展开运算符来达到合并更新对象的效果。

```jsx
setState(prevState => {
  // 也可以使用 Object.assign
  return {...prevState, ...updatedValues};
});
```

唯一不同的地方，是初始值的设置。因为是全局共享，所以自定义hook不支持传参设置初始值，
只能调用返回值的第二个参数，来设置状态的改变。

### Demo

```jsx
import { createGlobalState } from 'antd-advanced';

const useGlobalValue = createGlobalState<number>(0);

const CompA: FC = () => {
  const [value, setValue] = useGlobalValue();

  return <button onClick={() => setValue(value + 1)}>+</button>;
};

const CompB: FC = () => {
  const [value, setValue] = useGlobalValue();

  return <button onClick={() => setValue(value - 1)}>-</button>;
};

const Demo: FC = () => {
  const [value] = useGlobalValue();
  return (
    <div>
      <p>{value}</p>
      <CompA />
      <CompB />
    </div>
  );
};
```

copy 自 [react-use](https://github.com/streamich/react-use/blob/master/docs/createGlobalState.md)
