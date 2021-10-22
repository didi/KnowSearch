package com.didi.arius.gateway.common.utils;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
public class Regex {
	public static boolean indexExpMatchBack(String index, String exp) {
		if (index == null || index.length() == 0) {
			return false;
		}
		
		if (exp == null || exp.length() ==0 ) {
			return false;
		}
		
		int indexPointer = 0;
		int expPointer = 0;
		while (indexPointer < index.length() && expPointer < exp.length()) {
			char indexC = index.charAt(indexPointer);
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

				while(expC != indexC) {
					indexPointer++;
					// not match the char behind '*'
					if (indexPointer >= index.length()) {
						return false;
					}
					indexC = index.charAt(indexPointer);
				}
				
				indexPointer++;
				expPointer++;
			} else if (indexC == expC) {
				// the same, go on
				indexPointer++;
				expPointer++;
			} else {
				// not the same, failed
				return false;
			}
		}

		if (indexPointer == index.length() && expPointer == exp.length()) {
			// both to end
			return true;
		} else if (indexPointer == index.length()){
			// exp ends with * is ok
			while (expPointer < exp.length()) {
				char expC = exp.charAt(expPointer);
				if (expC != '*') {
					return false;
				}
				
				expPointer++;
			}
			
			return true;
		} else {
			// one has chars left
			return false;
		}
	}
	
	public static boolean indexContainExpBack(String index, String exp) {
		if (index == null || index.length() == 0) {
			return false;
		}
		
		if (exp == null || exp.length() ==0 ) {
			return false;
		}
		
		int indexPointer = 0;
		int expPointer = 0;

		
		while (indexPointer < index.length()) {
			char indexC = index.charAt(indexPointer);
			if (indexC == '*') {
				// index contain * and exp endswith *
				if (exp.endsWith("*")) {
					return true;
				}
				indexPointer++;
				boolean indexPointerEnd = true;
				while (indexPointer < index.length()) {
					indexC = index.charAt(indexPointer);
					if (indexC != '*') {
						indexPointerEnd = false;
						break;
					}
					
					indexPointer++;
				}
				
				// * is the last char in index
				if (indexPointerEnd) {
					return true;
				}
				
				int nextStar = index.indexOf('*', indexPointer);
				String indexInter = null;
				if (nextStar < 0) {
					indexInter = exp.substring(indexPointer);
				} else {
					indexInter = exp.substring(indexPointer, nextStar);
				}
				
				int expPos = exp.indexOf(indexPointer, expPointer);
				if (expPos <= 0) {
					return false;
				}
				
				indexPointer = indexPointer + indexInter.length();
				expPointer = expPos + indexInter.length();
			} else if (expPointer < exp.length()) {
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
				} else if (indexC != expC) {
					// not the same, failed
					return false;
				}
				
				indexPointer++;
				expPointer++;
			} else {
				return false;
			}
		}
		
		// exp must end with *
		while (expPointer < exp.length()) {
			char expC = exp.charAt(expPointer);
			if (expC != '*') {
				return false;
			}
			
			expPointer++;
		}
		
		// exp also to end
		return true;
	}
	
	public static boolean indexContainExp(String index, String exp) {
		if (index == null || index.length() == 0) {
			return false;
		}
		
		if (exp == null || exp.length() ==0 ) {
			return false;
		}
		
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
		
		if (hasStar == false) {
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
		
		// no *
		if (hasStar == false) {
			// length the same, so content the same
			if (index.length() == exp.length()) {
				return true;
			} else {
				return false;
			}
		}
		
		indexPointer = index.length() -1;
		expPointer = exp.length() - 1;
		
		// compare suffix
		while ( indexPointer >= 0 && expPointer >= 0) {
			char indexC = index.charAt(indexPointer);
			char expC = exp.charAt(expPointer);
			
			if (indexC == '*' || expC == '*') {
				hasStar = true;
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
	
	public static boolean expContainIndex(String index, String exp) {
		if (index == null || index.length() == 0) {
			return false;
		}
		
		if (exp == null || exp.length() ==0 ) {
			return false;
		}
		
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
		
		if (indexPointer < index.length()) {
			// index has chars left
			return false;
		} else {
			// index also to end
			return true;
		}
	}		
	
	public static boolean ipMaskMatch(String ip, String mask) {
		if (mask.endsWith("*")) {
			String prefix = mask.substring(0, mask.length()-1);
			if (ip.startsWith(prefix)) {
				return true;
			} else {
				return false;
			}
		} else {
			return ip.equals(mask);
		}
	}
}
