package merkleServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class merkleServer {

    private final int serverPort;

    public static String END_SESSION_MSG = "exit";
    public static String END_TRANSMISSION_MSG = "done";
    public static String LOCALHOST = "localhost";

    private merkleServer(int port) {
        this.serverPort = port;
    }

    static merkleServer openOnPort(int port) {
        return new merkleServer(port);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        merkleServer mServer = merkleServer.openOnPort(2323);
        mServer.run();
    }

    public void run() throws IOException, InterruptedException {
        /*
            Initializing the server socket
         */
        InetSocketAddress socketAddress = new InetSocketAddress(LOCALHOST, serverPort);
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
        /*
            Configuring the socket
         */
            serverSocketChannel.bind(socketAddress);
            serverSocketChannel.configureBlocking(false);
         /*
            Multiplexor of SelectableChannel objects
         */
            Selector selector = Selector.open();

            int validOps = serverSocketChannel.validOps();
            SelectionKey selectKey = serverSocketChannel.register(selector, validOps, null);
        /*
            Infinite loop to keep server running
         */
            while (true) {
                log("Server running on port " + serverPort + ", waiting for a connection...", "out");
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> currKey = selectedKeys.iterator();

                while (currKey.hasNext()) {
                    SelectionKey activeKey = currKey.next();
                /*
                    Tests whether this key's channel is ready to accept a new socket connection
                 */
                    if (activeKey.isAcceptable()) {
                        SocketChannel clientSocket = serverSocketChannel.accept();

                        clientSocket.configureBlocking(false);

                        clientSocket.register(selector, SelectionKey.OP_READ);
                        log("--- Connection accepted from client with address:" + clientSocket.getLocalAddress() + "\n", "out");
                 /*
                    Tests whether this key's channel is ready to for reading
                 */
                    } else if (activeKey.isReadable()) {
                        /*
                            Reads the request from the client; it also splits the incoming message into:
                            requestText (a short description of the request)
                            requestObject (the object of said request)
                            It may be useful should we implement the merkleTree and need the transaction hash
                         */
                        SocketChannel clientSocket = (SocketChannel) activeKey.channel();
                        ByteBuffer requestBuffer = ByteBuffer.allocate(256);

                        requestBuffer.clear();
                        requestBuffer.put(new byte[requestBuffer.capacity()]);
                        requestBuffer.clear();

                        clientSocket.read(requestBuffer);
                        String[] incomingMsg = new String(requestBuffer.array()).trim().split(":");
                        String requestText = incomingMsg[0];
                        String requestObject = incomingMsg[1];

                        requestBuffer.clear();
                        requestBuffer.put(new byte[requestBuffer.capacity()]);
                        requestBuffer.clear();

                        log("--- Request received: " + requestText + " : " + requestObject, "out");
                        /*
                            If the server reads the end session message it disconnects, otherwise processes the request
                        */
                        if (requestObject.equals(END_SESSION_MSG)) {
                            clientSocket.close();
                            log("\n--- Exit request received: closing the connection...", "out");
                        } else if (clientSocket.isConnected()){
                            /*
                                Random hashes for testing purposes
                             */
                            ByteBuffer nodeBuffer = ByteBuffer.allocate(256);
                            List<String> mNodes = new ArrayList<>();
                            mNodes.add("5e5bdd83110e1b9aeb9bf23d89211ceb");
                            mNodes.add("bfdb43dcb57b4705db1608b61892d638");
                            mNodes.add(END_TRANSMISSION_MSG);
                            for (String node : mNodes) {
                                nodeBuffer.clear();
                                byte[] message = node.getBytes();
                                nodeBuffer = ByteBuffer.wrap(message);
                                try {
                                    clientSocket = (SocketChannel) activeKey.channel();
                                    clientSocket.write(nodeBuffer);
                                } catch (IOException e) {
                                    log("Failed to send node " + node + " to client.","err");
                                }
                                System.out.println(new String(nodeBuffer.array()).trim());
                                Thread.sleep(1000);
                            }
                            log("--- Task completed: All nodes have been sent.","out");
                        }
                    }
                    currKey.remove();
                }

            }
        } catch (IOException e) {
            log("The server has encountered an error and will shut down...","err");
            e.printStackTrace();
        }
    }

    public static void log(String msg, String mode) {
        switch(mode) {
            case "out": {System.out.println(msg); break;}
            case "err": {System.err.println(msg); break;}
            default: {}
        }
    }
}