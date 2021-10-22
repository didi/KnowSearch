package com.didichuxing.datachannel.arius.admin.rest.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jinbinbin
 * @version $Id: TranslateHttpServletRequestWrapper.java, v 0.1 2018年11月22日 10:28 jinbinbin Exp $
 */
class TranslateHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private String body;
    private String encoding = "UTF-8";

    public TranslateHttpServletRequestWrapper(HttpServletRequest request) throws IOException{
        super(request);
        if (StringUtils.isNotBlank(request.getCharacterEncoding())) {
            this.encoding = request.getCharacterEncoding();
        }
        body = IOUtils.toString(request.getInputStream(), encoding);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss过滤。<br/>
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br/>
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     */
    @Override
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);
    }

    @Override
    public Map getParameterMap() {

        return (HashMap) super.getParameterMap();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream inputStream;
        if (StringUtils.isNotEmpty(body)) {
            inputStream = new TranslateServletInputStream(body.getBytes(encoding));
        } else {
            inputStream = new TranslateServletInputStream(new byte[0]);
        }
        return inputStream;
    }

    /**
     * 覆盖getHeader方法，将参数名和参数值都做xss过滤。<br/>
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
     * getHeaderNames 也可能需要覆盖
     */
    @Override
    public String getHeader(String name) {
        return super.getHeader(name);
    }
}
