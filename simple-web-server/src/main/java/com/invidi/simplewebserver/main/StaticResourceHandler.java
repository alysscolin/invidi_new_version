package com.invidi.simplewebserver.main;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class StaticResourceHandler implements Handler{
    @Override
    public void handle(Request request, Response response) throws IOException {
        //the static resource/file path from http request. for example: /index.html
        String path = request.getPath();
        Path fileName = Paths.get(path).getFileName();
        String fn =fileName.toString();
        File file = new File(ServerProperties.root, fn);
        String userDirectory = System.getProperty("user.dir");
        if (isSubDirectory(new File(userDirectory), file)) {
            response.setResponseCode(200, "OK");
            response.addHeader("Content-Type", "text/html");
            String line;
            String resp = "";

            try {
                String abPath = file.getAbsolutePath();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(/*file*/new File(abPath))));

                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    resp += line;
                }
                bufferedReader.close();
                response.addBody(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            response.setResponseCode(404, "File not found");
            response.addHeader("Content-Type", "text/html");
            response.addBody("404 not found, this page doesn't exist!");
        }
    }

    /**
     * Checks, whether the child directory is a subdirectory of the base
     * directory.
     *
     * @param base the base directory.
     * @param child the suspected child directory.
     * @return true, if the child is a subdirectory of the base directory.
     * @throws IOException if an IOError occured during the test.
     */
    public boolean isSubDirectory(File base, File child)
            throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }
}
