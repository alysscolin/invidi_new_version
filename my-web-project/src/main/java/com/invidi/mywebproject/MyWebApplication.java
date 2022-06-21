package com.invidi.mywebproject;

import com.invidi.mywebproject.controllers.MyController;
import com.invidi.simplewebserver.annotations.Path;
import com.invidi.simplewebserver.main.*;
import com.invidi.simplewebserver.model.RequestMethod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MyWebApplication {

   public static void main(String[] args) throws IOException {
      final WebServer ws = new SimpleWebServer(ServerProperties.port);
      ws.getWebContext().setHandler("GET", "/test", new Handler() {
         @Override
         public void handle(Request request, Response response) throws IOException {
            String html = "http server, " + request.getPath()+ "";
            response.setResponseCode(200, "OK");
            response.addHeader("Content-Type", "text/html");
            response.addBody(html);
         }
      });

      ws.getWebContext().setHandler("POST", "/api/data?key=[a-zA-Z]+\\d.*&value=[a-zA-Z]+\\d.*", new Handler() {
         @Override
         @Path(value = "/api/data",  method = RequestMethod.POST)
         public void handle(Request request, Response response) throws IOException {
            String key = request.getParameter("key");
            String value = request.getParameter("value");
            MyController myController = new MyController();
            myController.saveKeyValuePair(key, value);

            response.setResponseCode(200, "OK");
            response.addHeader("Content-Type", "application/json");
            response.addBody("write record " + "key=" + key + ",value=" + value);
         }
      });

      //key is Regex which should be only characters and digits
      ws.getWebContext().setHandler("GET", "/api/data?key=[a-zA-Z]+\\d.*", new Handler() {
         @Override
         public void handle(Request request, Response response) throws IOException {
            String key = request.getParameter("key");
            MyController myController = new MyController();
            MyController.Result result = myController.getValueByKey(key);

            if (result != null) {
               response.setResponseCode(200, "OK");
               response.addHeader("Content-Type", "application/json");
               response.addBody("Find the record: " + key + "=" + result);
            } else {
               response.setResponseCode(404, "Not found");
               response.addHeader("Content-Type", "application/json");
               response.addBody("Could not find this record");
            }
         }
      });

      //static resource handler, if you type localhost:8080, index.html will display
      ws.getWebContext().setHandler("GET", "/index.html", new StaticResourceHandler());

      ws.start(ServerProperties.port);
   }
}
