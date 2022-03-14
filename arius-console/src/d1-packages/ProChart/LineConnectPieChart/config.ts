export const getLineOption = (config: any) => {
  const { title, tooltip, xAxis, yAxis, dataZoom, series, ...rest } = config;
  return {
    title: {
      show: true,
      left: 15,
      top: 10,
      textStyle: {
        rich: {
          titleIcon: {
            width: 4,
            height: 4,
            backgroundColor: '#4E72FF',
            borderRadius: 50,
          },
          titleText: {
            fontSize: 14,
            fontWeight: 'bolder',
            padding: [0, 8, 0, 4],
            color: '#374053',
          },
        },
      },
      ...title,
      text: `{titleIcon|}{titleText|${title.text}}`,
    },
    tooltip: {
      show: true,
      trigger: 'axis',
      borderColor: '#1473FF',
      formatter: (args) => {
        const data = args[0];
        return `<div style="color:#4E72FF;"><span style="font-size:14px;">${title}：<span style="font-size:16px;">${data.value}</span></span><br /><span style="background:rgba(78,114,255,0.1);border-radius:2px;font-size:12px;color:#4E72FF;">${data.name}</span></div>`;
      },
      ...tooltip,
    },
    xAxis: {
      type: 'category',
      axisLine: {
        lineStyle: {
          color: '#EBEDEF',
          fontSize: 12,
        },
      },
      axisLabel: {
        textStyle: {
          fontSize: 12,
          color: '#919AAC',
        },
      },
      axisTick: {
        alignWithLabel: true, //坐标值是否在刻度中间
      },
      ...xAxis,
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        textStyle: {
          fontSize: 12,
          color: '#919AAC',
        },
      },
      ...yAxis,
    },
    dataZoom: [
      {
        show: true,
        realtime: true,
        bottom: 24,
        height: 16,
        ...dataZoom[0],
      },
    ],
    series: [
      {
        type: 'line',
        smooth: true,
        seriesLayoutBy: 'row',
        emphasis: { focus: 'series' },
        color: '#1473FF',
        animation: true,
        animationDuration: 1000,
        animationEasing: 'linear',
        animationDurationUpdate: 300,
        animationEasingUpdate: 'linear',
        ...series[0],
      },
    ],
    ...rest,
  };
};

export const getPieOption = (config) => {
  const { series, ...rest } = config;
  return {
    series: [
      {
        type: 'pie',
        radius: 50,
        animation: true,
        ...series[0],
      },
    ],
    ...rest,
  };
};
