import * as React from 'react';
import './grid.less';

import Container from '../index';

const Containers = (): JSX.Element => {
    return (
        <>
            <div>栅格布局 共24格 {`grid={6}`}</div>
            <Container gutter={10} grid={6}>
                <div className="grid-demo-div">栅格布局 6格</div>
                <div className="grid-demo-div">栅格布局 6格</div>
                <div className="grid-demo-div">栅格布局 6格</div>
                <div className="grid-demo-div">栅格布局 6格</div>
            </Container>
            <div>栅格布局 共24格 {`grid={[4, 4, 8, 8]}`}</div>
            <Container gutter={10} grid={[4, 4, 8, 8]}>
                <div className="grid-demo-div">栅格布局 4格</div>
                <div className="grid-demo-div">栅格布局 4格</div>
                <div className="grid-demo-div">栅格布局 8格</div>
                <div className="grid-demo-div">栅格布局 8格</div>
            </Container>
            <div>流式布局 {`fluid={300}`}</div>
            <Container gutter={10} fluid={300}>
                <div className="grid-demo-div">流式布局 300px</div>
                <div className="grid-demo-div">流式布局 300px</div>
                <div className="grid-demo-div">流式布局 300px</div>
                <div className="grid-demo-div">流式布局 300px</div>
            </Container>
            <div>flex auto布局 {`flex={'auto'}`}</div>
            <Container gutter={10} flex={'auto'}>
                <div className="grid-demo-div">flex布局 auto</div>
                <div className="grid-demo-div">flex布局 auto</div>
                <div className="grid-demo-div">flex布局 auto</div>
                <div className="grid-demo-div">flex布局 auto</div>
            </Container>
            <div>flex 部分定宽布局 {`flex={['200px', 'auto']}`}</div>
            <Container gutter={10} flex={['200px', 'auto']}>
                <div className="grid-demo-div">flex布局 200px</div>
                <div className="grid-demo-div">flex布局 auto</div>
                <div className="grid-demo-div">flex布局 auto</div>
                <div className="grid-demo-div">flex布局 auto</div>
            </Container>
            <div>自适应 断点1440和1920{`awd={true} `}</div>
            <Container gutter={10} awd={true}>
                <div className="grid-demo-div">自适应布局</div>
                <div className="grid-demo-div">自适应布局</div>
                <div className="grid-demo-div">自适应布局</div>
            </Container>
            <div>响应式 {`rwd={true}`} 须使用rem为单位</div>
            <Container gutter={10} rwd={true}>
                <div className="grid-demo-div1">响应式布局</div>
                <div className="grid-demo-div1">响应式布局</div>
                <div className="grid-demo-div1">响应式布局</div>
                <div className="grid-demo-div1">响应式布局</div>
            </Container>
            <div>组合使用1</div>
            <Container gutter={10} awd={true} xl={[8, 8, 8, 24]} xxl={6}>
                <Container fluid={400}><div className="grid-demo-div">饼图1</div></Container>
                <Container fluid={400}><div className="grid-demo-div">饼图2</div></Container>
                <Container fluid={400}>
                    <div className="grid-demo-div">summary</div>
                    <div className="grid-demo-div">Volume</div>
                </Container>
                <Container awd xl={8} xxl={24}>
                    <Container fluid={400}><div className="grid-demo-div">peak</div></Container>
                    <Container fluid={400}><div className="grid-demo-div">latest</div></Container>
                </Container>
            </Container>
            <div>组合使用2 默认自适应+流式+flex+自定义自适应</div>
            <Container gutter={10} awd={true} xl={[12, 12, 24]}>
                <div className="grid-demo-div">
                    <Container gutter={10} fluid={100}>
                        <div className="grid-demo-div grid-demo-div3">流式布局 100px</div>
                        <div className="grid-demo-div grid-demo-div3">流式布局 100px</div>
                        <div className="grid-demo-div grid-demo-div3">流式布局 100px</div>
                        <div className="grid-demo-div grid-demo-div3">流式布局 100px</div>
                    </Container>
                </div>
                <div className="grid-demo-div">
                    <Container gutter={10} flex={['200px', 'auto']}>
                        <div className="grid-demo-div grid-demo-div3">flex布局 200px</div>
                        <div className="grid-demo-div grid-demo-div3">flex布局 auto</div>
                        <div className="grid-demo-div grid-demo-div3">flex布局 auto</div>
                    </Container>
                </div>
                <div className="grid-demo-div">
                    <Container gutter={10} awd={true} xl={12} xxl={24}>
                        <div className="grid-demo-div grid-demo-div3">自适应布局</div>
                        <div className="grid-demo-div grid-demo-div3">自适应布局</div>
                    </Container>
                </div>
            </Container>

        </>
    )
}

export default Containers;
