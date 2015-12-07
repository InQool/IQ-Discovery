package com.inqool.dcap.office.config.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.googlecode.webutilities.common.Constants.HTTP_CONTENT_TYPE_HEADER;

/**
 * Common Simple Servlet Response Wrapper using WebUtilitiesResponseOutputStream
 *
 * @author rpatil
 * @version 1.0
 * @see WebUtilitiesResponseOutputStream
 */

public class WebUtilitiesResponseWrapper extends HttpServletResponseWrapper {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(WebUtilitiesResponseWrapper.class
                    .getName());

    private WebUtilitiesResponseOutputStream stream;
    private Map<String, Object> headers = new HashMap<String, Object>();
    private Set<Cookie> cookies = new HashSet<Cookie>();
    private String contentType;
    private int status = 0;
    private boolean getWriterCalled = false;
    private boolean getStreamCalled = false;
    private PrintWriter printWriter;

    @Override
    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);
        cookies.add(cookie);
    }

    @Override
    public void setStatus(int sc, String sm) {
        if (this.status != 0) return;
        super.setStatus(sc, sm);
        this.status = sc;
    }

    @Override
    public void setStatus(int sc) {
        if (this.status != 0) return;
        super.setStatus(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        if (this.status != 0) return;
        super.sendError(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (this.status != 0) return;
        super.sendError(sc, msg);
        this.status = sc;
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        headers.put(name, date);
    }

    @Override
    public void addHeader(String name, String value) {
        if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            this.setContentType(value);
        } else {
            super.addHeader(name, value);
            headers.put(name, value);
        }
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        headers.put(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        headers.put(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            this.setContentType(value);
        } else {
            super.setHeader(name, value);
            headers.put(name, value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        headers.put(name, value);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        this.contentType = type;
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter already called.");
        }
        getStreamCalled = true;
        return this.stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (getStreamCalled) {
            throw new IllegalStateException("getStream already called.");
        }
        getWriterCalled = true;
        if (printWriter == null) {
            printWriter = new PrintWriter(stream);
        }
        return printWriter;
    }

    public int getStatus() {
        return status;
    }

    private void flushWriter() {
        if (getWriterCalled && printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.flush();
        }
    }

    @Override
    public void reset() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.reset();
        }
        //getResponse().reset();

    }

    @Override
    public void resetBuffer() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.reset();
        }
        //getResponse().resetBuffer();
    }

    public String getContents() {
        return new String(getBytes());
    }

    public byte[] getBytes() {
        return stream.getByteArrayOutputStream().toByteArray();
    }

    public WebUtilitiesResponseWrapper(HttpServletResponse response) {
        super(response);
        stream = new WebUtilitiesResponseOutputStream(this);
    }

    public void fill(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(this.getCharacterEncoding());
        response.setContentType(this.getContentType());

        for (Cookie cookie : this.getCookies()) {
            response.addCookie(cookie);
        }
        for (String headerName : this.getHeaders().keySet()) {
            Object value = this.getHeaders().get(headerName);
            if (value instanceof Long) {
                response.setDateHeader(headerName, ((Long) value));
            } else if (value instanceof Integer) {
                response.setIntHeader(headerName, ((Integer) value));
            } else {
                response.setHeader(headerName, value.toString());
            }
        }

        if (this.getStatus() > 0)
            response.setStatus(this.getStatus());

        this.flushWriter();
        try {
            byte[] bytes = this.getBytes();
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().close();
        } catch (RuntimeException ex) {
            try {
                response.getWriter().write(this.getContents());
                response.getWriter().close();
            } catch (Exception ex1) {
                LOGGER.error(ex1.getMessage(), ex1);
            }
            //   ex.printStackTrace();
        }

        if (response instanceof WebUtilitiesResponseWrapper) {
            ((WebUtilitiesResponseWrapper) response).fill((HttpServletResponse) ((WebUtilitiesResponseWrapper) response).getResponse());
        }
    }
}
