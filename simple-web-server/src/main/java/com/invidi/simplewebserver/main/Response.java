package com.invidi.simplewebserver.main;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Response class.
 *  The response looks like:
 *       * version status code message
 *          headers
 *         (empty line)
 *         content
 *         (empty line)
 *       *
 *
 */
public class Response {
    private OutputStream out;
    private int statusCode;
    private String statusMessage;
    //key-value pairs
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;

    public Response(OutputStream out)  {
        this.out = out;
    }

    public void setResponseCode(int statusCode, String statusMessage)  {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public OutputStream getOut() {
        return out;
    }

    public void addHeader(String headerName, String headerValue)  {
        this.headers.put(headerName, headerValue);
    }

    public void addBody(String body)  {
        headers.put("Content-Length", Integer.toString(body.length()));
        this.body = body;
    }

    //each new line is \r\n
    public void send() throws IOException {
        //ObjectMapper mapper = new ObjectMapper();

        out.write(("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n").getBytes());
        for (String headerName : headers.keySet())  {
            out.write((headerName + ": " + headers.get(headerName) + "\r\n").getBytes());
        }
        out.write("\r\n".getBytes());
        if (body != null)  {
            out.write(body.getBytes());
        }
        //mapper.writeValue(out, this);
    }
}
