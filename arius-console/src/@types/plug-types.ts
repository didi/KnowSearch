export interface IPlug {
  createTime?: string;
  id: number;
  creator: string;
  desc: string;
  type?: number;
  updateTime?: string;
  md5: string;
  name: string;
  pdefault: number;
  s3url: string;
  url: string;
  version?: string;
  deleteFlag: boolean;
  installed: boolean;
}

