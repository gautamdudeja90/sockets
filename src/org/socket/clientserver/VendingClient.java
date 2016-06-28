package org.socket.clientserver;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by burak on 06.03.2016.
 */
public class VendingClient {

    Socket clientSocket;
    List<Stock> stockList;

    public VendingClient (String ip, int port) {
        try {
            this.clientSocket = new Socket(ip, port);
            this.stockList = new ArrayList<Stock>();
        }
        catch (UnknownHostException ue) {
            ue.printStackTrace();
        }
        catch ( IOException ie )
        {
            ie.printStackTrace();
        }
    }

    public String readMultilineMessage( BufferedReader reader ) throws IOException {
        boolean messageFinished = false;
        StringBuffer buffer = new StringBuffer();
        String response;


        while( !messageFinished )
        {
            response = reader.readLine();

            if(response.equals(""))
                messageFinished = true;
            else
            {
                buffer.append(response + "\n");
            }
        }

        String tmp = buffer.toString();
        tmp = tmp.substring(0, tmp.lastIndexOf("\n") ); //remove the last \n character
        return tmp;
    }

    public void run() throws IOException {
        String sentence;
        String response;

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));


        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));

        //System.out.println("I connected to server " + clientSocket.getRemoteSocketAddress() );
        System.out.println("The connection is established.");

        while (true) {
            try {
                System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT): ");
                String userCommand = inFromUser.readLine();

                if (userCommand.equals("L")) {
                    outToServer.writeBytes("GET ITEM LIST\r\n\r\n");
                    outToServer.flush();

                    System.out.println(readMultilineMessage(inFromServer));
                } else if (userCommand.equals("G")) {
                    System.out.print("Give the item id: ");
                    int id = Integer.parseInt(inFromUser.readLine());
                    System.out.print("Give the number of items: ");
                    int amount = Integer.parseInt(inFromUser.readLine());
                    outToServer.writeBytes("GET ITEM\r\n" + id + " " + amount + "\r\n\r\n");
                    outToServer.flush();

                    String serverResponse = readMultilineMessage(inFromServer);
                    //System.out.println("servResp: " + serverResponse);
                    if (serverResponse.equals("SUCCESS")) {
                        boolean purchasedBefore = false;
                        for (Stock s : stockList) {
                            if (id == s.getProductId()) {
                                s.setAmount(s.getAmount() + amount);
                                purchasedBefore = true;
                                break;
                            }
                        }
                        if (!purchasedBefore) {
                            stockList.add(new Stock(id, "unknown", amount));
                        }
                        System.out.println("The received message:\n\tSUCCESS");
                    } else {
                        System.out.println("The received message:\n\tOUT OF STOCK");
                    }

                } else if (userCommand.equals("Q")) {
                    break;
                } else {
                    System.out.println("Wrong command.");
                }


            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        System.out.println("The summary of received items:");
        for( Stock s : stockList)
            System.out.println("\t" + s.getProductId() + " " + s.getAmount());

    }

}