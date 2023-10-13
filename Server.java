import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    //socket do servidor
    private ServerSocket serverSocket;

    /**
     * construtor do server
     * @param serverSocket
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * inicia o servidor
     * em um loop infinito aceita os clientes que se conectarem, criando um ClientHandler e iniciando uma thread nova pra cada um 
     */
    public void startServer () {
        try {
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e){
        }
    }

    /**
     * fecha o socket do servidor
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(12345);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
