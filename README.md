# sockets
Sockets project is a simpler implementation of Client Server interaction using Java Sockets.

Created a server at port 5556
  SocketServer server = new SocketServer(5556, new RequestHandler());

Created a client communicating to Server at port # 5556
  ClientEmulator client = new ClientEmulator(InetAddress.getLocalHost(), 5556);
  
Send an xml string to Server
  client.sendMessageToServer(str);
  
RequestHandler implements the Message Handler Interface and overrides onRecieve method 
class RequestHandler implements MessageHandler {
    @Override
    public void onReceive(Connection connection, String message) {
      InputSource newstream = new InputSource(new StringReader(message));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(newstream);
			if(getElementValue("Command",doc.getDocumentElement()).equals("") || getElementValue("Command",doc.getDocumentElement())==null){
				message  = message + " Unknown Command";
				System.out.println("Unknown Command");
			}else{
				System.out.println(getElementValue("Command",doc.getDocumentElement()));
      }
    }
  }
  
  Features: 
  1) SocketServer hanldles incoming client connections using threads. Each time a new client connects or open the socket connection it creates a new thraed.
  2) Includes a server Listening Thread which is looking for incoming client connections and whenever it sees a new connection it creates a new ConnectionThread 
     and let it handle the communication between sever and client.
  3) This can let many incoming threads keep coming in while an existing client is communicating with server.
  
  Possible Extension:
  1) To include Gui interface using Jwing and Crate a Server JPanel which controls the starting of server and another button to initiate a connection as client with server.
  2) Along with GUI SocketServer can have its statusBoard of connected clients, so make the class observable and all other clients being connected as
    as observers which can update all clients whenever a new client connects and corrosponding client can be displayed on server status board.
    
  
