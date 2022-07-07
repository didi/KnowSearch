package com.didichuxing.datachannel.arius.admin.rest.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * @author jinbinbin
 * @version $Id: TranslateServletInputStream.java, v 0.1 2018年10月17日 14:49 jinbinbin Exp $
 */
public class TranslateServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream inputStream;

    private boolean              finished = false;

    public TranslateServletInputStream(byte[] buffer){
        this.inputStream = new ByteArrayInputStream(buffer);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public int read() throws IOException {
        int data = this.inputStream.read();
        if (data == -1) {
            this.finished = true;
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int data = this.inputStream.read(b, off, len);
        if (data == -1) {
            this.finished = true;
        }
        return data;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.inputStream.close();
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
