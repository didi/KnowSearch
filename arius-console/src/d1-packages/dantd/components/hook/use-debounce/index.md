---
category: 自定义Hook
cols: 1
type: hook
title: useDebounce
subtitle: 防抖
---

## 何时使用

这个 `Hook` 可以用防抖动的方式来处理改变过快的值。当 `useDebounce` 在指定的时间段内没有被调用时，`hook` 仅返回最新的值。当与 `useEffect` 一起使用时，就像我们在下面的示例中所做的那样，您可以轻松地确保昂贵的操作(如API调用)不会执行得太频繁。
