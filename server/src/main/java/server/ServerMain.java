package server;

import chess.*;

public class ServerMain {
    public static void main(String[] args) {
        var server = new Server();
        server.run(8080);
        
    }
}
