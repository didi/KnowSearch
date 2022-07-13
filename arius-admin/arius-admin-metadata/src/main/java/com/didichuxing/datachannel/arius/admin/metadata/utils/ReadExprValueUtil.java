package com.didichuxing.datachannel.arius.admin.metadata.utils;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import java.math.BigDecimal;
import java.util.*;

public class ReadExprValueUtil {
    protected static final ILog LOGGER = LogFactory.getLog(ReadExprValueUtil.class);

    private ReadExprValueUtil() {
    }

    /**
     * 返回 表达式的值(double)
     *
     * @param expr
     * @return
     */
    public static double readExprValue(String expr) {
        try {
            if (!isExpression(expr)) {
                LOGGER.error("class=ReadExprValueUtil||method=readExprValue||expr={}", expr);
                return -1;
            }
            List<String> list = resolveString(expr);
            list = nifixToPost(list);
            return getPostfisResult(list);
        } catch (Exception e) {
            LOGGER.error("class=ReadExprValueUtil||method=readExprValue||expr={}", expr, e);
            return 0.0d;
        }
    }

    /**
     * 计算后缀表达式
     *
     * @param list
     * @return
     */
    private static double getPostfisResult(List<String> list) {
        Deque<String> stack = new ArrayDeque<>();
        for (String str : list) {
            if (isDouble(str)) {
                stack.push(str);
            } else if (isStrOperator(str)) {
                double n2 = Double.parseDouble(stack.pop());
                double n1 = Double.parseDouble(stack.pop());
                stack.push("" + getCountResult(str, n1, n2));
            }
        }
        return Double.valueOf(stack.pop());
    }

    /**
     * 字符是否为数字
     *
     * @param ch
     * @return
     */
    private static boolean isNum(char ch) {
        return (ch <= '9' && ch >= '0');
    }

    /**
     * 字符串是否为Double类型
     *
     * @param s
     * @return
     */
    private static boolean isDouble(String s) {
        try {
            Double.valueOf(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 字符是否为运输符
     *
     * @param ch
     * @return
     */
    private static boolean isOperator(char ch) {
        return (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '=');
    }

    /**
     * 字符串是否为运算符
     *
     * @param s
     * @return
     */
    private static boolean isStrOperator(String s) {
        return (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("(") || s.equals(")"));
    }

    /**
     * 比较运算符优先级
     *
     * @param o1
     * @param o2
     * @return
     */
    private static boolean heightOperator(String o1, String o2) {
        if ((o1.equals("*") || o1.equals("/")) && (o2.equals("+") || o2.equals("-")) || o2.equals("(")) {
            return true;
        } else if ((o1.equals("+") || o1.equals("-")) && (o2.equals("*") || o2.equals("/"))) {
            return false;
        } else if ((o1.equals("*") || o1.equals("/")) && (o2.equals("*") || o2.equals("/"))) {
            return true;
        } else if ((o1.equals("+") || o1.equals("-")) && (o2.equals("+") || o2.equals("-"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 中缀表达式转换后缀
     *
     * @param list
     * @return
     */
    private static List<String> nifixToPost(List<String> list) {
        Deque<String> stack = new ArrayDeque<>();
        List<String> plist = new ArrayList<>();
        for (String str : list) {
            if (isDouble(str)) {
                plist.add(str);
            }
            if (isStrOperator(str) && stack.isEmpty()) {
                stack.push(str);
            } else if (isStrOperator(str) && !stack.isEmpty()) {
                change(stack, plist, str);
            }

        }
        while (!stack.isEmpty()) {
            plist.add(stack.pop());
        }

        return plist;
    }

    private static void change(Deque<String> stack, List<String> plist, String str) {
        String last = stack.getLast();
        if (heightOperator(str, last) || str.equals("(")) {
            stack.push(str);
        } else if (!heightOperator(str, last) && !str.equals(")")) {
            while (!stack.isEmpty() && !stack.getLast().equals("(")) {
                plist.add(stack.pop());
            }
            stack.push(str);
        } else if (str.equals(")")) {
            while (!stack.isEmpty()) {
                String pop = stack.pop();
                if (!pop.equals("(")) {
                    plist.add(pop);
                }
                if (pop.equals("(")) {
                    break;
                }
            }
        }
    }

    /**
     * 分解表达式
     *
     * @param str
     * @return
     */
    private static List<String> resolveString(String str) {
        List<String> list = new ArrayList<>();
        String temp = "";
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            if (isNum(ch) || ch == '.') {
                temp += ch;
            } else if (isOperator(ch) || ch == ')') {
                if (!temp.equals("")) {
                    list.add(temp);
                }
                list.add("" + ch);
                temp = "";
            } else if (ch == '(') {
                list.add("" + ch);
                // 科学计数法E
            } else if (ch == 'E') {
                list.add("(");
                list.add(temp);
                temp = "";
                i++;

                // 科学计数法E换算为10*x方式
                // E后面的+-符号
                char symbol = str.charAt(i);
                if (symbol == '+') {
                    list.add("*");
                } else {
                    list.add("/");
                }

                i++;
                String numStr = "";
                for (; i < str.length(); i++) {
                    char a = str.charAt(i);
                    if (isOperator(a)) {
                        i--;
                        break;
                    }
                    numStr += a;
                }
                double num = Math.pow(10.0d, Double.parseDouble(numStr));
                list.add(String.valueOf(num));
                list.add(")");
            }
            if (i == str.length() - 1) {
                list.add(temp);
            }
        }
        return list;
    }

    /**
     * 是否为算术表达式
     *
     * @param str
     * @return
     */
    private static boolean isExpression(String str) {
        int flag = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            char ch = str.charAt(i);
            char chb = str.charAt(i + 1);
            if ((!isNum(ch) && i == 0) && ch != '(' || !isNum(chb) && (i == str.length() - 2) && chb != ')') {
                LOGGER.error("class=ReadExprValueUtil||method=isExpression||errMsg=首尾不是数字---->{}", ch + chb);
                return false;
            }
            if ((ch == '.' && !isNum(chb)) || (!isNum(ch) && chb == '.')) {
                LOGGER.error("class=ReadExprValueUtil||method=isExpression||errMsg=小数点前后不是数字--->{}", ch + chb);
                return false;
            }
            if (isOperator(ch) && !isNum(chb) && chb != '(') {
                LOGGER.error("class=ReadExprValueUtil||method=isExpression||errMsg=运算符不是数字--->{}", ch + chb);
                return false;
            }
            if (isNum(ch) && !isOperator(chb) && chb != '.' && chb != ')' && chb != 'E' && !isNum(chb)) {
                LOGGER.error("class=ReadExprValueUtil||method=isExpression||errMsg=数字后不是运算符--->{}", ch + chb);
                return false;
            }
            if (ch == '(') {
                flag++;
            }
            if (chb == ')') {
                flag--;
            }
        }
        if (flag != 0) {
            LOGGER.error("class=ReadExprValueUtil||method=isExpression||errMsg=括号不匹配--->");
            return false;
        }
        return true;
    }

    /**
     * 数字的量级
     */
    public static int numberOfDigits(int accessNum) {
        int i = 0;
        while (accessNum > 0) {
            accessNum = accessNum / 10;
            i++;
        }
        return i;
    }

    /**
     * 两数算术运算
     */
    static double getCountResult(String oper, double num1, double num2) {
        if (oper.equals("+")) {
            return num1 + num2;
        } else if (oper.equals("-")) {
            return num1 - num2;
        } else if (oper.equals("*")) {
            return num1 * num2;
        } else if (oper.equals("/")) {
            return num1 / num2;
        } else {
            return 0;
        }
    }

    /**
     * 将double数值保留2位小数
     *
     * @param value double数值
     * @return 返回2位小数数值
     */
    public static double getDouble2(Double value) {
        BigDecimal bg = BigDecimal.valueOf(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getDouble3(Double value) {
        BigDecimal bg = BigDecimal.valueOf(value);
        return bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
