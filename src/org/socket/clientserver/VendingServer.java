package org.socket.clientserver;
/**
 * Created by burak on 06.03.2016.
 */

//import sun.rmi.runtime.Log;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VendingServer
{
    private ServerSocket welcomeSocket;
    private ArrayList<Stock> stocks;
    private ArrayList<VendingServerThread> threads;

    public VendingServer (int port) throws IOException
    {
        initStocks("item_list.txt");

        welcomeSocket = new ServerSocket(port);
        threads = new ArrayList<VendingServerThread>();
        //welcomeSocket.setSoTimeout(100000);
    }

    public void initStocks(String filename) throws IOException {

        stocks = new ArrayList<Stock>();

        // Open the file
        FileInputStream fstream = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;
        String pattern = "(\\d+)[ \\t\\n\\r]+([^ \\t\\n\\r]+)[ \\t\\n\\r]+(\\d+)";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern, Pattern.MULTILINE);

        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {

            // Now create matcher object.
            Matcher m = r.matcher(strLine);
            if (m.find()) {

                //parse parameters from text
                int productId = Integer.parseInt(m.group(1));
                String productName = m.group(2);
                int amount = Integer.parseInt(m.group(3));

                //add the stock to the list
                stocks.add( new Stock( productId, productName, amount) );
            } else {
                //System.out.println("NO MATCH");
            }
        }

        //Close the input stream
        br.close();

        System.out.println(filename + " is read");
        System.out.println("The current list of items:");
        for ( Stock s : stocks)
        {
            System.out.println('\t' + s.toString());
        }
    }

    public void start() throws IOException {
        VendingServerThread firstThread = new VendingServerThread(welcomeSocket, stocks, threads);
        firstThread.start();

        for( VendingServerThread t : threads)
        {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All threads are finished. Server closes.");


    }

    public class VendingServerThread extends Thread
    {
        private ServerSocket welcomeSocket;
        private Socket cliSpecificSocket;
        private List<Stock> stocks;
        private ArrayList<VendingServerThread> threads;

        public VendingServerThread(ServerSocket welcomeSocket,
                                   List<Stock> stocks,
                                   ArrayList<VendingServerThread> threads)
        {
            this.welcomeSocket = welcomeSocket;
            this.stocks = stocks;
            this.threads = threads;
        }

        public void run()
        {
            //client search
            //System.out.println("Waiting    for client on port " + welcomeSocket.getLocalPort() + "...");



            System.out.print("Listening for a client on  " + this.toString() +
                                "\n**********************\n");

            this.cliSpecificSocket = null;

            try {
                cliSpecificSocket = welcomeSocket.accept();
                if( areAllItemsOutOfStock() ) //finish this thread
                {
                    System.out.println("All items finished. Killing thread " + this);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println("Just connected to " + cliSocket.getRemoteSocketAddress());
            System.out.print("A client is connected on  " + this.toString() + "\n");

            VendingServerThread cli = new VendingServerThread(welcomeSocket, stocks, threads);
            cli.start();
            threads.add(this); //add the thread itself to client threads list


            BufferedReader inFromClient = null;
            DataOutputStream outToClient = null;

            try {
                inFromClient = new BufferedReader(
                        new InputStreamReader(cliSpecificSocket.getInputStream()));

                outToClient = new DataOutputStream(cliSpecificSocket.getOutputStream());

                //outToClient.writeBytes("Thank you for connecting to "
                  //      + cliSpecificSocket.getLocalSocketAddress() + '\n');

            } catch (IOException e) {
                e.printStackTrace();
            }


            while(true)
            {
                try
                {
                    String clientRcvdMsg = inFromClient.readLine();

                    if( clientRcvdMsg.equals("GET ITEM"))
                    {
                        System.out.println("Message received on " + this + ":");
                        System.out.println('\t' + clientRcvdMsg);

                        //read the id and amount
                        clientRcvdMsg = inFromClient.readLine();
                        System.out.println('\t' + clientRcvdMsg);

                        String tmp = (clientRcvdMsg.split(" "))[0];
                        int id = Integer.parseInt(tmp);

                        tmp = (clientRcvdMsg.split(" "))[1];
                        int amount = Integer.parseInt(tmp);

                        //SEARCH THE REQUESTED ITEM
                        boolean outOfStock = true;
                        for( Stock s : stocks)
                        {
                            if( s.getProductId() == id && s.getAmount() >= amount)
                            {
                                s.setAmount(s.getAmount() - amount);
                                outToClient.writeBytes("SUCCESS\r\n\r\n");
                                outToClient.flush();
                                System.out.println("Send the message:\n\tSUCCESS\n********************\n");
                                outOfStock = false;
                                break;
                            }
                        }
                        // IF ITEM NOT FOUND
                        if ( outOfStock ) {
                            outToClient.writeBytes("OUT OF STOCK\r\n\r\n");
                            outToClient.flush();
                            System.out.println("Send the message:\n\tOUT OF STOCK\n****************\n");
                        }

                        //pass the empty line
                        inFromClient.readLine();
                    }
                    else if ( clientRcvdMsg.equals("GET ITEM LIST"))
                    {
                        System.out.println("Message received on " + this + ": \n\t" + clientRcvdMsg + "\n");
                        inFromClient.readLine(); //pass the empty line
                        outToClient.writeBytes(getStockListString() + "\r\n"); //dont forget to append the last empty line
                        outToClient.flush();
                        System.out.println("Send the message:\n" + getStockListString() + "\n******************\n");
                    }
                    //server.close();

                }catch(SocketTimeoutException s)
                {
                    System.out.println("Socket timed out!");
                    break;
                }catch(IOException e)
                {
                    //break the loop and print the message
                    break;
                } catch (NullPointerException e)
                {
                    break;
                }
            }

            System.out.println(this + ": The client has terminated the connection.\nThe current list of items:");
            for(Stock s : stocks)
            {
                System.out.println(s.toString());
            }
            System.out.println();

        }

        public String getStockListString()
        {
            StringBuffer str = new StringBuffer();
            str.append("ITEM LIST\r\n");
            for( Stock s : stocks)
            {
                str.append(s.toString() + "\r\n");
            }

            return str.toString();
        }

        public boolean areAllItemsOutOfStock()
        {
            for(Stock s : stocks)
                if(s.getAmount() > 0)
                   return false;
            return true;
        }
    }
}