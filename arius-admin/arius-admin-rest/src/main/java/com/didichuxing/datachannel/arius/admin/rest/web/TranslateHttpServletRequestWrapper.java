package com.didichuxing.datachannel.arius.admin.rest.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
}
