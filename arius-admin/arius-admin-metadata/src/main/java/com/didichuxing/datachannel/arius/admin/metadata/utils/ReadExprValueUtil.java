package com.didichuxing.datachannel.arius.admin.metadata.utils;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ReadExprValueUtil {
    protected static final ILog LOGGER = LogFactory.getLog(ReadExprValueUtil.class);

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
            list = nifix_to_post(list);
            return get_postfis_result(list);
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
    private static double get_postfis_result(List<String> list) {
        Stack<String> stack = new Stack<String>();
        for (String str : list) {
            if (isDouble(str)) {
                stack.push(str);
            } else if (isStrOperator(str)) {
                double n2 = Double.valueOf(stack.pop());
                double n1 = Double.valueOf(stack.pop());
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
        if (ch <= '9' && ch >= '0') {
            return true;
        } else {
            return false;
        }
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
        if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '=') {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 字符串是否为运算符
     *
     * @param s
     * @return
     */
    private static boolean isStrOperator(String s) {
        if (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("(") || s.equals(")")) {
            return true;
        } else {
            return false;
        }
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
        } else if ((o1.equals("*") || o1.equals("/")) && ((o2.equals("*") || o2.equals("/")))) {
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
    private static List<String> nifix_to_post(List<String> list) {
        Stack<String> stack = new Stack<String>();
        List<String> plist = new ArrayList<String>();
        for (String str : list) {
            if (isDouble(str)) {
                plist.add(str);
            }
            if (isStrOperator(str) && stack.isEmpty()) {
                stack.push(str);
            } else if (isStrOperator(str) && !stack.isEmpty()) {
                String last = stack.lastElement();
                if (heightOperator(str, last) || str.equals("(")) {
                    stack.push(str);
                } else if (!heightOperator(str, last) && !str.equals(")")) {
                    while (!stack.isEmpty() && !stack.lastElement().equals("(")) {
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

        }
        while (!stack.isEmpty()) {
            plist.add(stack.pop());
        }

        return plist;
    }

    /**
     * 分解表达式
     *
     * @param str
     * @return
     */
    private static List<String> resolveString(String str) {
        List<String> list = new ArrayList<String>();
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
                for (;i < str.length(); i++) {
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
                LOGGER.error("首尾不是数字---->" + ch + chb);
                return false;
            }
            if ((ch == '.' && !isNum(chb)) || (!isNum(ch) && chb == '.')) {
                LOGGER.error("小数点前后不是数字--->" + ch + chb);
                return false;
            }
            if (isOperator(ch) && !isNum(chb) && chb != '(') {
                LOGGER.error("运算符不是数字--->" + ch + chb);
                return false;
            }
            if (isNum(ch) && !isOperator(chb) && chb != '.' && chb != ')' && chb != 'E' && !isNum(chb)) {
                LOGGER.error("数字后不是运算符--->" + ch + chb);
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
            LOGGER.error("括号不匹配--->");
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
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getDouble3(Double value) {
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
