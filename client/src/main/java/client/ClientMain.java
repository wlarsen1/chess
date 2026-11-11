package client;

import ui.ConsoleUI;

public class ClientMain {

    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade(8080);
        new ConsoleUI(facade).run();
    }

}
