import CryptoJS from 'crypto-js';

const AES_KEY = 'Szjx2022@666666$'; // 密钥, AES-128 需16个字符, AES-256 需要32个字符
const key = CryptoJS.enc.Utf8.parse(AES_KEY);

export function encryptAES(str: string) {
  const srcs = CryptoJS.enc.Utf8.parse(str);
  const encrypted = CryptoJS.AES.encrypt(srcs, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  });
  return encrypted.toString();
}
