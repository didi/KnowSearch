import moment from 'moment';
export interface IPeriod {
  label: string;
  key: string;
  dateRange: [moment.Moment, moment.Moment];
}

export const timeFormat = 'YYYY-MM-DD HH:mm:ss';

export const timeDate = 'YYYY-MM-DD';

export const oneDayMillims = 24 * 60 * 60 * 1000;

export const PERIOD_RADIO = [
  {
    label: '10分钟',
    key: 'tenMin',
    get dateRange() {
      return [moment().subtract(10, 'minute'), moment()];
    },
  },
  {
    label: '6小时',
    key: 'sixHour',
    get dateRange() {
      return [moment().subtract(6, 'hour'), moment()];
    },
  },
  {
    label: '近1天',
    key: 'oneDay',
    get dateRange() {
      return [moment().subtract(1, 'day'), moment()];
    },
  },
  {
    label: '近1周',
    key: 'oneWeek',
    get dateRange() {
      return [moment().subtract(1, 'week'), moment()];
    },
  },
] as IPeriod[];

const periodRadioMap = new Map<string, IPeriod>();
PERIOD_RADIO.forEach(p => {
  periodRadioMap.set(p.key, p);
});
export const PERIOD_RADIO_MAP = periodRadioMap;

export const KEEP_LIVE_LIST = [3, 7, 15, 30, 60, 90, 180, -1];

export const TIME_LENEND = {
  today: '当前',
  yesterday: '昨天',
  lastWeek: '上周',
} as {
  [key: string]: string,
};
