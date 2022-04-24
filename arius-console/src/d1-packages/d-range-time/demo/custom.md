---
order: 0
title: 基础用法
---

DRangeTime示例   

``` tsx
import React, { useState, useEffect } from 'react';
import DRangeTime from '../index';

const Containers = (): JSX.Element => {
  const [isGroup, setIsgroup] = useState(false); 
  const handleTimeChange = (times) => {
    console.log(times)
  }
  return (
      <>
        <DRangeTime timeChange={handleTimeChange}/>         
      </>
  )
}

ReactDOM.render(
  <div>
    <Containers />
  </div>,
  mountNode,
);
```

```css
.drag-sort-item {
  /* background: #4482D4; */
  box-shadow: 0 2px 4px 0 rgba(0,0,0,0.01), 0 3px 6px 3px rgba(0,0,0,0.01), 0 2px 6px 0 rgba(0,0,0,0.03);
  border-radius: 4px; 
  color: #fff;
}

```
