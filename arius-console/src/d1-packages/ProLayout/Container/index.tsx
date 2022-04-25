import React from "react";
import './Grid/style/index';
import { Col, Row } from "./Grid/index";

interface OptionsProps {
    [propertys: string]: any;
}

interface LayoutProps {
    [propertys: string]: any;
}

interface LayoutState {
    [propertys: string]: any;
}

export function withContainer(Cmp: React.ComponentType, options: OptionsProps) {
    return class FusionLayout extends React.Component<LayoutProps, LayoutState> {
        constructor(props: LayoutProps) {
            super(props);
            this.state = {
                ...options
            }
        }
        render() {
            return (
                <Container>
                    <Cmp {...this.state} {...this.props}></Cmp>
                </Container>
            )
        }
    }
}
export function withContainers(options: OptionsProps) {
    return (Cmp: React.ComponentType) => withContainer(Cmp, options)
}

type ColSpanType = number | string;

type FlexType = number | 'none' | 'auto' | string;

interface ColSize {
    flex?: FlexType;
    span?: ColSpanType;
    order?: ColSpanType;
    offset?: ColSpanType;
    push?: ColSpanType;
    pull?: ColSpanType;
}

interface propsType extends React.HTMLAttributes<HTMLDivElement> {
    children?: React.ReactNode;
    grid?: ColSpanType | ColSpanType[];
    fluid?: ColSpanType;
    flex?: FlexType | FlexType[];
    awd?: boolean | number[];
    rwd?: boolean;
    xl?: ColSpanType | ColSize | ColSpanType[] | ColSize[];
    xxl?: ColSpanType | ColSize | ColSpanType[] | ColSize[];
    wrap?: boolean;
    gutter?: number;

}
const Container: React.FC<propsType> = (props) => {
    const { children, className, gutter, wrap = true, grid, fluid, flex, awd, rwd, xl = 12, xxl = 8 } = props;

    const items = React.Children.toArray(children);
    const itemNumber = items.length || 0;

    const getGrid = () => {
        if (!grid) return null;
        const span = typeof (grid) === 'number' ?
            Array(itemNumber).fill(grid || Math.round(24 / itemNumber)) : grid;

        return items.map((child: React.ReactNode, index: number) =>
            <Col span={span[index]} key={index}>{child}</Col>
        )
    };

    const getFluid = () => {
        if (!fluid) return null;
        return items.map((child: React.ReactNode, index: number) =>
            <Col flex={`0 0 ${fluid}px`} key={index}>{child}</Col>
        )
    }

    const getFlex = () => {
        if (!flex) return null;
        let flexList: ColSpanType[] = Array(itemNumber);
        if (Array.isArray(flex)) {
            // @ts-ignore
            flexList = flexList.fill('').map((f, i) => flex[i] || 'auto');
        } else {
            flexList.fill(flex)
        }
        return items.map((child: React.ReactNode, index: number) =>
            <Col flex={flexList[index]} key={index}>{child}</Col>
        )
    }

    const getAWD = () => {
        if (!awd) return null;
        let xlList: (ColSpanType | ColSize)[] = Array(itemNumber);
        if (Array.isArray(xl)) {
            // @ts-ignore
            xlList = xlList.fill('').map((f, i) => xl[i] || 12);
        } else {
            xlList.fill(xl);
        }
        let xxlList: (ColSpanType | ColSize)[] = Array(itemNumber);
        if (Array.isArray(xxl)) {
            // @ts-ignore
            xxlList = xxlList.fill('').map((f, i) => xl[i] || 12);
        } else {
            xxlList.fill(xxl);
        }
        return items.map((child: React.ReactNode, index: number) =>
            <Col xl={xlList[index]} xxl={xxlList[index]} key={index}>{child}</Col>
        )
    }

    const getRWD = () => {
        if (!rwd) return null;
        return items.map((child: React.ReactNode, index: number) =>
            <Col flex="auto" key={index}>{child}</Col>
        )
    }

    return (
        <Row gutter={gutter} className={className} wrap={wrap} justify={fluid ? 'space-between' : 'start'}>
            {getGrid() || getFluid() || getFlex() || getAWD() || getRWD()}
        </Row>
    );
}
export default Container;