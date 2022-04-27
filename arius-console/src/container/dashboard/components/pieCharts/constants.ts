export const createOption = (data: any) => {
  const options = {
    title: {
      text: '健康度',
      subtext: parseInt((data[0].prrcent || '0')) + '%',
      textAlign: "center",
      textVerticalAlign: "center",
      textStyle: {
        fontSize: 12,
        color: '#74788D',
        fontFamily: "PingFangSC-Regular"
      },
      subtextStyle: {
        fontSize: 20,
        color: 'rgba(0,0,0,0.85)',
        fontFamily: "PingFangSC-Medium"
      },
      left: "47%",
      top: "40%",
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        let tip = params.marker + params.name + ': ' + params?.data?.value + '个<br /> <span style=\"display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color: #fff;\"> </span>占比: ' + params?.data?.prrcent + '%';
        return tip
      }
    },
    color: ['#6BD9C5', '#F9CC7E', '#FA8787', '#A8ADBD'],
    grid: {
      left: 10,
      top: 0
    },
    series: [
      {
        type: 'pie',
        radius: ['100%', '60%'],
        avoidLabelOverlap: false,
        hoverAnimation: false,
        label: {
          show: false,
          position: 'center'
        },
        animation: false,
        labelLine: {
          show: false
        },
        data: data,
      }
    ]
  };
  return options;
}