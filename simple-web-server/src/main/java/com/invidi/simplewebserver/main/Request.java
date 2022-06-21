package com.invidi.simplewebserver.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Request  {
    private String method;
    private String path;
    private String fullUrl;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private BufferedReader in;

    public Request(BufferedReader in)  {
        this.in = in;
    }

    public String getMethod()  {
        return method;
    }

    public String getPath()  {
        return path;
    }

    public String getParameter(String paramName)  {
        return queryParameters.get(paramName);
    }

    //currently we only support one pair key=key_value&value=value_value in POST /api/data request described in MyController
    private void parseQueryParameters(String queryString)  {
        for (String parameter : queryString.split("&"))  {
            int separator = parameter.indexOf('=');
            if (separator > -1)  {
                queryParameters.put(parameter.substring(0, separator),
                        parameter.substring(separator + 1));
            } else  {
                queryParameters.put(parameter, null);
            }
        }
    }

    public boolean parse() throws IOException {
        // first line is like: GET / HTTP/1.1
        String initialLine = in.readLine();
        String[] requestLine = initialLine.split(" ");
        if (requestLine.length != 3)
            return false;
        method = requestLine[0];
        fullUrl = requestLine[1];

        // retrieve all headers
        while (true)  {
            String headerLine = in.readLine();
            if (headerLine.length() == 0)  {
                break;
            }

            int separator = headerLine.indexOf(":");
            if (separator == -1)  {
                return false;
            }
            headers.put(headerLine.substring(0, separator),
                    headerLine.substring(separator + 1));
        }

        //no query parameters
        if (requestLine[1].indexOf("?") == -1)  {
            path = requestLine[1];
        } else  {  //query parameters
            path = requestLine[1].substring(0, requestLine[1].indexOf("?"));
            parseQueryParameters(requestLine[1].substring(
                    requestLine[1].indexOf("?") + 1));
        }

        //if user wants default resource then return index.html
        if ("/".equals(path))  {
            path = "/index.html";
        }

        return true;
    }

    public String toString()  {
        return method  + " " + path + " " + headers.toString();
    }
}
