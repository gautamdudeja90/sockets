package org.socket.clientserver;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by burak on 06.03.2016.
 */
public class VendingMachine {

    public static void main(String[] args) throws IOException {

        // if IP is given it's a client
        if(args.length == 2)
        {
            String ip = args[0];
            int port = Integer.parseInt(args[1]);

            VendingClient client = new VendingClient(ip, port);
            client.run();

        }
        // if only port is given, it's a server
        else if ( args.length == 1)
        {
            int port = Integer.parseInt(args[0]);

            try
            {
                /*Thread t = new VendingServer(port);
                t.start();*/

                VendingServer server = new VendingServer(port);
                server.start();

            } catch(IOException e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            System.out.println("Invalid arguments. Must be VendingMachine [<IP>] <Port Addr.>");
        }

    }
}
