import { Drawer }  from 'antd';
import React from 'react';
import { connect } from "react-redux";
import * as actions from 'actions';
import { PageIFrameContainer } from 'container/iframe-page';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
});
class NodeMonitorDrawer extends React.Component<any> {


  public render() {
    const { dispatch } = this.props
     const str = `/console/arius/kibana7/app/kibana#/dashboard/17d17640-5a32-11eb-af34-ad99b4265825?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-15m,to:now))&_a=(description:Arius%E8%8A%82%E7%82%B9%E7%9A%84%E7%9B%91%E6%8E%A7%E6%8C%87%E6%A0%87,filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(),gridData:(h:15,i:a4f4c258-36d1-4a51-b2bb-fb1158d053d8,w:24,x:0,y:0),id:d5c80080-5a2b-11eb-af34-ad99b4265825,panelIndex:a4f4c258-36d1-4a51-b2bb-fb1158d053d8,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:c60cb2ad-6713-4da2-a335-8d4a1d7071ca,w:24,x:24,y:0),id:'55633270-5a2b-11eb-af34-ad99b4265825',panelIndex:c60cb2ad-6713-4da2-a335-8d4a1d7071ca,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:c350f4e6-23c6-4df8-9b8a-73075225de46,w:24,x:0,y:15),id:'9a3d8a30-5a2b-11eb-af34-ad99b4265825',panelIndex:c350f4e6-23c6-4df8-9b8a-73075225de46,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:e0141327-2671-45e1-8b9b-bda621977a22,w:24,x:24,y:15),id:'1cd26cc0-5a2e-11eb-af34-ad99b4265825',panelIndex:e0141327-2671-45e1-8b9b-bda621977a22,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'8922c138-37ac-4976-9ae5-83c3dca54416',w:24,x:0,y:30),id:'278d76f0-5a2e-11eb-af34-ad99b4265825',panelIndex:'8922c138-37ac-4976-9ae5-83c3dca54416',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:bf137eb1-b906-46e3-967a-a06687967065,w:24,x:24,y:30),id:'9519d0d0-5a2c-11eb-af34-ad99b4265825',panelIndex:bf137eb1-b906-46e3-967a-a06687967065,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'1e3b73d1-5235-4160-8e00-4a03f203a5dc',w:24,x:0,y:45),id:'707954c0-5a2d-11eb-af34-ad99b4265825',panelIndex:'1e3b73d1-5235-4160-8e00-4a03f203a5dc',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'78fb0a32-3ea8-4657-a21b-0dad044f087a',w:24,x:24,y:45),id:d2e620d0-5a2c-11eb-af34-ad99b4265825,panelIndex:'78fb0a32-3ea8-4657-a21b-0dad044f087a',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:e704b854-1cc7-4e87-a27e-7fdd2a5f200d,w:24,x:0,y:60),id:'21ec8930-5a2d-11eb-af34-ad99b4265825',panelIndex:e704b854-1cc7-4e87-a27e-7fdd2a5f200d,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:b988c487-eef5-4665-8da1-1ed2584195af,w:24,x:24,y:60),id:'1b240b60-5a2c-11eb-af34-ad99b4265825',panelIndex:b988c487-eef5-4665-8da1-1ed2584195af,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'684a51e5-b145-4dc4-8bd3-9384002dbc7c',w:24,x:0,y:75),id:'536ceb40-5a2c-11eb-af34-ad99b4265825',panelIndex:'684a51e5-b145-4dc4-8bd3-9384002dbc7c',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'924bdd72-8936-4366-9ec1-92f8c245b2fb',w:24,x:24,y:75),id:c3554440-5a2f-11eb-af34-ad99b4265825,panelIndex:'924bdd72-8936-4366-9ec1-92f8c245b2fb',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'397d936d-6f73-439d-9f13-595298024449',w:24,x:0,y:90),id:'2f5f9c90-5a2f-11eb-af34-ad99b4265825',panelIndex:'397d936d-6f73-439d-9f13-595298024449',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:ca9241be-9d5c-4333-8c28-5f97a06ec6d8,w:24,x:24,y:90),id:'5fc3e040-5a2e-11eb-af34-ad99b4265825',panelIndex:ca9241be-9d5c-4333-8c28-5f97a06ec6d8,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'785f9ff6-79b8-4da0-88f7-203d8c1be7a7',w:24,x:0,y:105),id:'986b81f0-5a2e-11eb-af34-ad99b4265825',panelIndex:'785f9ff6-79b8-4da0-88f7-203d8c1be7a7',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:a40d3e46-f4f3-43d8-9c60-d87369cd89f3,w:24,x:24,y:105),id:fcb03060-5a2f-11eb-af34-ad99b4265825,panelIndex:a40d3e46-f4f3-43d8-9c60-d87369cd89f3,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:d9d56a54-0dbc-4537-a840-7bf3ccfeb81f,w:24,x:0,y:120),id:'56ca7150-5a30-11eb-af34-ad99b4265825',panelIndex:d9d56a54-0dbc-4537-a840-7bf3ccfeb81f,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'50520d14-ff37-483c-a7d7-3c5dc0fec975',w:24,x:24,y:120),id:'2cdcd760-5a31-11eb-af34-ad99b4265825',panelIndex:'50520d14-ff37-483c-a7d7-3c5dc0fec975',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:a6a26f96-c93d-468f-a31b-8d4eceea060a,w:24,x:0,y:135),id:e92cced0-5a30-11eb-af34-ad99b4265825,panelIndex:a6a26f96-c93d-468f-a31b-8d4eceea060a,type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'25f879e6-b74f-4720-9fa4-1eac812ac5b1',w:24,x:24,y:135),id:'76fe3280-5a31-11eb-af34-ad99b4265825',panelIndex:'25f879e6-b74f-4720-9fa4-1eac812ac5b1',type:visualization,version:'7.6.0-SNAPSHOT'),(embeddableConfig:(),gridData:(h:15,i:'87545db1-d8ee-4800-9977-0f949077e177',w:24,x:0,y:150),id:b3e68b20-5a31-11eb-af34-ad99b4265825,panelIndex:'87545db1-d8ee-4800-9977-0f949077e177',type:visualization,version:'7.6.0-SNAPSHOT')),query:(language:kuery,query:'node:${this.props.params.node}'),timeRestore:!f,title:arius_node_monitor,viewMode:view)`;
    return (
      <Drawer
        title={'节点监控'}
        visible={true}
        onClose={() => dispatch(actions.setDrawerId(''))}
        width={1000}
        maskClosable={true}
      >
        <PageIFrameContainer src={str} />
      </Drawer>
    );
  }
}
export default connect(mapStateToProps)(NodeMonitorDrawer);


