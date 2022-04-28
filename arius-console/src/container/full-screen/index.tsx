import React from 'react';
import './index.less';
import { connect } from "react-redux";
import * as actions from '../../actions';

const mapStateToProps = (state: any) => ({
  content: state.fullScreen?.content,
});

const mapDispatchToProps = dispatch => ({
  close: (content: any) => dispatch(actions.setFullScreenContent(content))
});

type Props = ReturnType<typeof mapStateToProps> & ReturnType<typeof mapDispatchToProps>;

class FullScreen extends React.Component<Props> {

  public handleClose = (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    if ((event.target as any).nodeName === 'SECTION') {
      this.props.close(null)
    };
  }

  public render() {
    if (!this.props.content) return null;
    return (
      <section className="full-screen-mark" onClick={this.handleClose}>
        <div className="full-screen-content">
          {this.props.content}
        </div>
      </section>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(FullScreen)
