package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IndexTire {
    private Node root;

    public IndexTire() {
        root = new Node(new Char(' ', 0));
    }

    public Node getRoot() {
        return root;
    }

    public static class Node {
        private Char content; // the character in the node

        private boolean isEnd; // whether the end of the words

        private LinkedList<Node> childList; // the child list

        private IndexTemplate indexTemplate;

        public Node(Char c) {
            childList = new LinkedList<>();
            isEnd = false;
            content = c;
        }

        public Node subNode(char c) {
            if (childList != null) {
                for (Node eachChild : childList) {
                    if (equalsChar(c, eachChild.content)) {
                        return eachChild;
                    }
                }
            }
            return null;
        }

        public Node subNode(Char c) {
            if (childList != null) {
                for (Node eachChild : childList) {
                    if (equalsChar(c, eachChild.content)) {
                        return eachChild;
                    }
                }
            }
            return null;
        }

        public void addSubNode(Node child) {
            childList.add(child);
        }

        public List<Node> subNodes() {
            return childList;
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            this.isEnd = end;
        }

        public IndexTemplate getIndexTemplate() {
            return indexTemplate;
        }

        public void setIndexTemplate(IndexTemplate indexTemplate) {
            this.indexTemplate = indexTemplate;
        }
    }

    public void insert(List<Char> chars, IndexTemplate indexTemplate) {
        Node current = root;
        int i = 0;
        for ( ; i < chars.size(); ++i) {
            Node child = current.subNode(chars.get(i));
            if (child == null) {
                current.setEnd(false);
                break;
            } else {
                current = child;
            }
        }

        for ( ; i < chars.size(); ++i) {
            Node child = new Node(chars.get(i));
            current.addSubNode(child);
            current = child;
        }

        if (current.getIndexTemplate() == null) {
            current.setIndexTemplate(indexTemplate);
            if (current.subNodes() == null || current.subNodes().isEmpty()) {
                current.setEnd(true);
            }
        }
    }

    public IndexTemplate search(String index){
        Node current = root;
        int pos = 0;

        // 从根节点开始遍历字符
        return searchSubNode(current, index, pos);
    }

    private IndexTemplate scanNodes(Node current, String index) {
        // 遍历子节点，找到对应的模板，如果匹配到超过2个模板，则直接返回错误。
        IndexTemplate indexTemplate = null;

        if (current.getIndexTemplate() != null
            && checkIndexMatchTemplate(index, current.getIndexTemplate())) {
            indexTemplate = current.getIndexTemplate();
        }

        for (Node child : current.subNodes()) {

            IndexTemplate subTemplate = scanNodes(child, index);

            if (indexTemplate != null && subTemplate != null && !indexTemplate.equals(subTemplate)) {
                throw new TooManyIndexException(String.format("search query match more then one index template, index=%s, template 1=%s, template 2=%s", index, indexTemplate.getExpression(), subTemplate.getExpression()));
            }

            if (indexTemplate == null && subTemplate != null) {
                indexTemplate = subTemplate;
            }
        }

        return indexTemplate;
    }

    private IndexTemplate searchSubNode(Node current, String index, int pos) {
        if (current.isEnd()) {
            // 已经遍历到模板的末尾
            boolean result = checkIndexMatchTemplate(index, current.getIndexTemplate());
            IndexTemplate indexTemplate = null;
            if (result) {
                indexTemplate = current.getIndexTemplate();
            }
            return indexTemplate;
        }

        // 判断是否有带*号的查询，有的话，顺序过滤多个*号，到下一个字符
        boolean wildcard = false;
        while (pos < index.length() && index.charAt(pos) == '*') {
            pos ++;
            wildcard = true;
            break;
        }

        if (wildcard) {
            // 前缀有带*号的case，则遍历剩下全部节点，找到符合的模板
            return scanNodes(current, index);
        }

        // 前缀没有带*号，则一直往下遍历
        if (pos < index.length()) {
            current = current.subNode(index.charAt(pos));
            if (current == null) {
                // 没找到对应的模板
                return null;
            } else {
                return searchSubNode(current, index, pos + 1);
            }
        } else if (current.getIndexTemplate() != null) {
            // index已经遍历完，且当前节点包含索引模板
            boolean result = checkIndexMatchTemplate(index, current.getIndexTemplate());
            if (result) {
                return current.getIndexTemplate();
            } else {
                return null;
            }
        } else {
            // index已经遍历完，但没命中到查询模板
            return null;
        }
    }

    public static class Char {
        char c;
        int type;

        Char(char c, int type) {
            this.c = c;
            this.type = type;
        }
    }

    public static boolean checkIndexMatchTemplate(String index, IndexTemplate indexTemplate) {
        String expression = indexTemplate.getExpression();
        expression = expression.replace("*", "");

        //如果索引匹配上索引模板，则直接返回true
        if (index.equals(indexTemplate.getName())) {
            return true;
        }

        //如果索引匹配上别名，这直接返回true
        if (indexTemplate.getAliases() != null) {
            for (String alias : indexTemplate.getAliases()) {
                if (alias.equals(index)) {
                    return true;
                }
            }
        }

        if (index.contains("*")) {
            return matchTemplateByStar(index, indexTemplate, expression);
        } else {
            return matchTemplateByOther(index, indexTemplate, expression);
        }
    }

    private static boolean matchTemplateByOther(String index, IndexTemplate indexTemplate, String expression) {
        if (StringUtils.isEmpty(indexTemplate.getDateFormat())) {
            // 不是时间后缀的索引，直接比较
            return index.equals(expression);
        } else {
            int pos = index.indexOf(expression);
            if (pos < 0) {
                return false;
            }

            pos += expression.length();

            String format = index.substring(pos);

            int end = format.indexOf("_v");

            if (end > 0) {
                format = format.substring(0, end);
            }

            try {
                DateTime.parse(format, DateTimeFormat.forPattern(indexTemplate.getDateFormat()));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static boolean matchTemplateByStar(String index, IndexTemplate indexTemplate, String expression) {
        // 按*号，将index分成多个token
        Tokens tokens = formTokens(index);

        List<Char> expCharList = IndexTireBuilder.getIndexChars(expression, indexTemplate.getDateFormat());

        Iterator<String> tokenIter = tokens.tokenList.iterator();
        String token = tokenIter.next();

        int tokenPos = 0;
        if (!tokens.indexStarStart) {
            boolean startResult = startWithToken(expCharList, token);
            if (!startResult) {
                return false;
            }

            tokenPos = token.length();
        } else {
            tokenPos = indexOfToken(expCharList, tokenPos, token);
            if (tokenPos < 0) {
                return false;
            }
        }

        while (tokenIter.hasNext()) {
            token = tokenIter.next();
            tokenPos = indexOfToken(expCharList, tokenPos, token);
            if (tokenPos < 0) {
                return false;
            }
        }

        boolean res = false;
        if (tokenPos == expCharList.size() || tokens.indexStarEnd) {
            res = true;
        }
        return res;
    }

    private static boolean startWithToken(List<Char> expCharList, String token) {
        int pos = 0;
        int tokenPos = 0;
        boolean missed = false;
        while (tokenPos < token.length()) {
            if (pos >= expCharList.size()) {
                return false;
            }

            char tokenC = token.charAt(tokenPos);
            Char expC = expCharList.get(pos);
            if (!equalsChar(tokenC, expC)) {
                missed = true;
                break;
            }

            tokenPos++;
            pos++;
        }

        return !missed;
    }

    private static int indexOfToken(List<Char> expCharList, int pos, String token) {
        char tokenFirst = token.charAt(0);
        while (pos < expCharList.size()) {
            Integer tokenIndex = getToken(expCharList, pos, token, tokenFirst);
            if (tokenIndex != null) return tokenIndex;

            pos ++;
        }

        // token没命中exp
        return -1;
    }

    private static Integer getToken(List<Char> expCharList, int pos, String token, char tokenFirst) {
        Char expC = expCharList.get(pos);
        if (equalsChar(tokenFirst, expC)) {
            int tokenPos = 0;
            int expPos = pos;
            boolean missed = false;
            while (tokenPos < token.length()) {
                if (expPos >= expCharList.size()) {
                    // token没命中exp
                    return -1;
                }

                char tokenC = token.charAt(tokenPos);
                expC = expCharList.get(expPos);
                if (!equalsChar(tokenC, expC)) {
                    missed = true;
                    break;
                }

                tokenPos++;
                expPos++;
            }

            if (!missed) {
                return expPos;
            }
        }
        return null;
    }

    private static boolean equalsChar(char a, Char b) {
        boolean res = false;
        if ((b.type == 0 && a == b.c)
                || (b.type == 1 && a >= '0' && a <= '9')) {
            res = true;
        }
        return res;
    }

    private static boolean equalsChar(Char a, Char b) {
        boolean res = false;
        if ((b.type == 0 && a.type == 0 && a.c == b.c)
                || (b.type == 1 && a.type == 1)) {
            res = true;
        }
        return res;
    }

    public static class Tokens {
        List<String> tokenList;
        boolean indexStarEnd;
        boolean indexStarStart;

        public Tokens(List<String> tokenList, boolean indexStarEnd, boolean indexStarStart) {
            this.tokenList = tokenList;
            this.indexStarEnd = indexStarEnd;
            this.indexStarStart = indexStarStart;
        }
    }

    public static Tokens formTokens(String index) {
        List<String> tokens = new ArrayList<>();

        int tokenStart = 0;
        int indexPointer = 0;

        boolean indexStarEnd = false;
        boolean indexStarStart = false;
        if (index.charAt(0) == '*') {
            indexStarStart = true;
            for (; indexPointer < index.length(); indexPointer++) {
                if (index.charAt(indexPointer) != '*') {
                    tokenStart = indexPointer;
                    break;
                }
            }
        }

        if (index.charAt(index.length() - 1) == '*') {
            indexStarEnd = true;
        }

        while (indexPointer < index.length()) {
            char indexC = index.charAt(indexPointer);
            if (indexC == '*') {
                if (indexPointer != 0) {
                    tokens.add(index.substring(tokenStart, indexPointer));
                }


                for (; indexPointer < index.length(); indexPointer++) {
                    if (index.charAt(indexPointer) != '*') {
                        tokenStart = indexPointer;
                        break;
                    }
                }
            } else {
                indexPointer++;
            }
        }

        if (!indexStarEnd) {
            tokens.add(index.substring(tokenStart, indexPointer));
        }


        return new Tokens(tokens, indexStarEnd, indexStarStart);
    }
}
