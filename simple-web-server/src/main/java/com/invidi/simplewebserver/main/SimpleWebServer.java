package com.invidi.simplewebserver.main;

import com.invidi.mywebproject.controllers.MyController;
import com.invidi.simplewebserver.context.WebServerContext;
import com.invidi.simplewebserver.model.HttpStatus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebServer implements WebServer, Runnable {
   private int port;
   private volatile boolean shutdown;

   private Handler defaultHandler = null;
   /*Two level map: first level is HTTP Method (GET, POST, etc.), second level is the
   request paths.*/
   private Map<String, Map<String, Handler>> handlers = new HashMap<String, Map<String, Handler>>();

   public SimpleWebServer(int port) {
      this.port = port;
   }

   @Override
   public void start(int port) {
      run();
   }

   @Override
   public void stop() {
      shutdown = true;
      this.stop();
   }

   @Override
   public WebServerContext getWebContext() {
      return new WebServerContext() {
         @Override
         public void setHandler(String method, String path, Handler handler) {
            Map<String, Handler> methodHandlers = handlers.get(method);
            if (methodHandlers == null)  {
               methodHandlers = new HashMap<>();
               handlers.put(method, methodHandlers);
            }
            methodHandlers.put(path, handler);
         }
      };
   }

   @Override
   public void run() {
      ServerSocket serverSocket = null;
      try  {
         serverSocket = new ServerSocket(port);
         serverSocket.setReuseAddress(true);

         while (serverSocket.isBound() && !serverSocket.isClosed() && !shutdown) {
            System.out.println("Waiting for connection......");
            // socket object to receive incoming client requests
            Socket socket = serverSocket.accept();
            System.out.println("Connection accepted......");
            // create a new thread object to handle client request and return response
            ClientHandler clientSock  = new ClientHandler(socket,  handlers);
            // this thread will handle the client separately
            new Thread(clientSock).start();
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      } finally {
         try {
            serverSocket.close();
            System.out.println("Finished......");
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   // ClientHandler class
   private static class ClientHandler implements Runnable {
      private final Socket clientSocket;
      //Two level map: first level is HTTP Methods(GET, POST, etc.), second level is the request paths.
      private Map<String, Map<String, Handler>> handlers;

      public ClientHandler(Socket clientSocket, Map<String, Map<String, Handler>> handlers)  {
         this.clientSocket = clientSocket;
         this.handlers = handlers;
      }

      /**
       * Simple responses like errors.  Normal responses come from handlers.
       */
      private void respond(int statusCode, String msg, OutputStream out) throws IOException  {
         String responseLine = "HTTP/1.1 " + statusCode + " " + msg + "\r\n\r\n";
         out.write(responseLine.getBytes());
      }

      @Override
      public void run() {
         BufferedReader in = null;
         OutputStream out = null;

         try  {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = clientSocket.getOutputStream();

            Request request = new Request(in);
            if (!request.parse())  {
               respond(500, "Unable to parse request", out);
               return;
            }

            boolean foundHandler = false;
            Response response = new Response(out);
            //here is path-handler map
            Map<String, Handler> methodHandlers = handlers.get(request.getMethod());
            if (methodHandlers == null)  {
               respond(405, "Method not supported", out);
               return;
            }

            //find a hendler to hand this http method plus path
            for (String handlerPath : methodHandlers.keySet())  {
               if (handlerPath.equals(request.getPath()) || (handlerPath.contains(request.getPath()) && request.getParameter("key") != null))  {
                  methodHandlers.get(handlerPath).handle(request, response);
                  response.send();
                  foundHandler = true;
                  break;
               }
            }

            //no findings, this is the special string "/", default handler is used if no other handler matches.
            if (!foundHandler)  {
               if (methodHandlers.get("/") != null)  {
                  methodHandlers.get("/").handle(request, response);
                  response.send();
               } else  {
                  respond(404, "Not Found", out);
               }
            }
         } catch (IOException e)  {
            try  {
               e.printStackTrace();
               if (out != null)  {
                  respond(500, e.toString(), out);
               }
            } catch (IOException e2)  {
               e2.printStackTrace();
            }
         } finally  {
            try  {
               if (out != null)  {
                  out.close();
               }
               if (in != null)  {
                  in.close();
               }
               clientSocket.close();
            } catch (IOException e)  {
               e.printStackTrace();
            }
         }
      }
   }
}
