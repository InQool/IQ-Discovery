package com.inqool.dcap.office.config.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WebUtilitiesResponseOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream byteArrayOutputStream;

    public WebUtilitiesResponseOutputStream(WebUtilitiesResponseWrapper wrapper){
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public void write(int b) throws IOException {
        byteArrayOutputStream.write(b);
    }
    @Override
    public void close() throws IOException {
        byteArrayOutputStream.close();
    }
    @Override
    public void flush() throws IOException {
        byteArrayOutputStream.flush();
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteArrayOutputStream.write(b, off, len);
    }
    @Override
    public void write(byte[] b) throws IOException {
        byteArrayOutputStream.write(b);
    }
    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }
    void reset() {
        byteArrayOutputStream.reset();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        try {
            writeListener.onWritePossible();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}