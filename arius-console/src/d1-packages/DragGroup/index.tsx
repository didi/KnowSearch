import { Col, Row } from "antd";
import Container from "../container";
import React from "react";
import { SortableContainer, SortableContainerProps, SortableHandle, SortableElement, SortableElementProps } from "react-sortable-hoc";
import "./index.less";
import { ArrowsAltOutlined } from "@ant-design/icons";

interface PropsType extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  sortableContainerProps?: SortableContainerProps;
  gridProps?: {
    grid: number;
    gutter: [number, number];
  };
  dragItemProps?: Omit<SortableElementProps, "index">;
}

interface SortableItemProps {
  useDragHandle?: boolean;
  span: number;
}

// 拖拽容器
const DragContainer = SortableContainer(({ children, gridProps }: any) => <Container {...gridProps}>{children}</Container>);

// 拖拽按钮
const DragHandle = SortableHandle(() => (
  <div className="drag-handle-icon">
    <ArrowsAltOutlined />
  </div>
));

// 拖拽项
const SortableItem = SortableElement(
  ({ children, sortableItemProps }: { children: React.ReactNode; sortableItemProps: SortableItemProps }) => (
    <div
      className="drag-sort-item"
      style={{
        cursor: "move",
      }}
    >
      {sortableItemProps?.useDragHandle && <DragHandle />}
      {children}
    </div>
  )
);

const DragGroup: React.FC<PropsType> = ({ children, gridProps = { grid: 8, gutter: [10, 10] }, sortableContainerProps }) => {
  return (
    <DragContainer pressDelay={0} {...sortableContainerProps} gridProps={gridProps}>
      {React.Children.map(children, (child: any, index: number) => {
        // 如果传入 child 有 key 值就复用，没有的话使用 index 作为 key
        const key = typeof child === "object" && child !== null ? child.key || index : index;

        return (
          <SortableItem
            key={key}
            index={index}
            disabled={child.key?.indexOf("health") !== -1}
            sortableItemProps={{
              useDragHandle: child.key?.indexOf("health") !== -1 ? false : sortableContainerProps?.useDragHandle || false,
            }}
          >
            {child}
          </SortableItem>
        );
      })}
    </DragContainer>
  );
};

export default DragGroup;
