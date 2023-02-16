import React from "react";
import { SortableContainer, SortableElement } from 'react-sortable-hoc';

export interface ISortableElement {
  index: number;
  collection?: string | number;
  disabled?: boolean;
}

interface propsType {
  children: React.ReactNode;
  dragItemProps: ISortableElement;
}

const SortableItem = SortableElement(({ children }) => (
  <div
    className="drag-sort-item"
    style={{
      cursor: 'move'
    }}
  >
    {children}
  </div>
))

const DragableItem: React.FC<propsType> = ({ children, dragItemProps }) => {

  return (
    <SortableItem
      {...dragItemProps}
    >
      {children}
    </SortableItem>
  )

};

export default DragableItem;