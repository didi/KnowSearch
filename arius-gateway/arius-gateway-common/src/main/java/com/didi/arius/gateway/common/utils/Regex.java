package com.didi.arius.gateway.common.utils;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
public class Regex {

	private Regex(){}
	
	public static boolean indexContainExp(String index, String exp) {
		if (isEmpty(index, exp)) return false;

		int indexPointer = 0;
		int expPointer = 0;

		// compare prefix
		boolean hasStar = false;
		while (indexPointer < index.length() && expPointer < exp.length()) {
			char indexC = index.charAt(indexPointer);
			char expC = exp.charAt(expPointer);
			
			if (indexC == '*' || expC == '*') {
				hasStar = true;
				break;
			} else if (indexC != expC) {
				// not the same, failed
				return false;
			}
			
			indexPointer++;
			expPointer++;
		}
		
		if (!hasStar) {
			return starDeal(index, exp, indexPointer, expPointer);
		}
		
		// no *
		
		indexPointer = index.length() -1;
		expPointer = exp.length() - 1;
		
		// compare suffix
		while ( indexPointer >= 0 && expPointer >= 0) {
			char indexC = index.charAt(indexPointer);
			char expC = exp.charAt(expPointer);
			
			if (indexC == '*' || expC == '*') {
				break;
			} else if (indexC != expC) {
				// not the same, failed
				return false;
			}
			
			indexPointer--;
			expPointer--;
		}
		
		return true;
	}

	private static boolean isEmpty(String index, String exp) {
		if (index == null || index.length() == 0) {
			return true;
		}

		if (exp == null || exp.length() == 0) {
			return true;
		}
		return false;
	}

	private static boolean starDeal(String index, String exp, int indexPointer, int expPointer) {
		while (indexPointer < index.length()) {
			char indexC = index.charAt(indexPointer);
			if (indexC != '*') {
				return false;
			}

			indexPointer++;
		}

		while (expPointer < exp.length()) {
			char expC = exp.charAt(expPointer);
			if (expC != '*') {
				return false;
			}

			expPointer++;
		}

		// both end or either end with *
		return true;
	}

	public static boolean expContainIndex(String index, String exp) {
		if (isEmpty(index, exp)) return false;

		int indexPointer = 0;
		int expPointer = 0;

		
		while (expPointer < exp.length()) {
			char expC = exp.charAt(expPointer);
			
			if (expC == '*') {
				expPointer++;
				boolean expPointerEnd = true;

				while (expPointer < exp.length()) {
					expC = exp.charAt(expPointer);
					if (expC != '*') {
						expPointerEnd = false;
						break;
					}
					expPointer++;
				}
				
				// * is the last char in exp
				if (expPointerEnd) {
					return true;
				}
				
				int nextStar = exp.indexOf('*', expPointer);
				String expInter = null;
				if (nextStar < 0) {
					expInter = exp.substring(expPointer);
				} else {
					expInter = exp.substring(expPointer, nextStar);
				}
				
				int indexPos = index.indexOf(expInter, indexPointer);
				if (indexPos <= 0) {
					return false;
				}
				
				expPointer = expPointer + expInter.length();
				indexPointer = indexPos + expInter.length();
			} else if (indexPointer < index.length()) {
				char indexC = index.charAt(indexPointer);
				if (indexC != expC) {
					// not the same, failed
					return false;
				}
				
				indexPointer++;
				expPointer++;
			} else {
				return false;
			}
		}

		boolean res = true;
		if (indexPointer < index.length()) {
			// index has chars left
			res = false;
		}
		return res;
	}		
	
	public static boolean ipMaskMatch(String ip, String mask) {
		if (mask.endsWith("*")) {
			String prefix = mask.substring(0, mask.length()-1);
			boolean res = false;
			if (ip.startsWith(prefix)) {
				res = true;
			}
			return res;
		} else {
			return ip.equals(mask);
		}
	}
}
