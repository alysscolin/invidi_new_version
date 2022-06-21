package com.invidi.simplewebserver.context;

import com.invidi.simplewebserver.main.Handler;


public interface WebServerContext {
    //set the handler for this http method and path
    void setHandler(String method, String path, Handler handler);


}
