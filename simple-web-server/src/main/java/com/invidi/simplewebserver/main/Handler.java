package com.invidi.simplewebserver.main;

import java.io.IOException;

public interface Handler {
    public void handle(Request request, Response response) throws IOException;
}
