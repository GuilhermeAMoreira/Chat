import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    //socket utilizado para a comunicação com o servidor
    private Socket socket;
    //le o fluxo de entrada de dados do servidor
    private BufferedReader bufferedReader;
    //escreve o fluxo de saída de dados para o servidor
    private BufferedWriter bufferedWriter;
    //nome do usuário
    private String username;

    /**
     * construtor, configura os fluxos de entrada e saída, além do nome do usuário
     * @param socket
     * @param username
     */
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * lê o teclado através do scanner e envia tais mensagens para o servidor por meio do buffer de leitura
     */
    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            String messageToSend;
            while(socket.isConnected()) {
                messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            scanner.close();

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * com uma thread nova, lê uma mensagem do servidor através do buffer de leitura e exibe no console
     */
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while(socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    /**
     * fecha todos os recursos que estão abertos ao desconectar o cliente
     * @param socket
     * @param bufferedReader
     * @param bufferedWriter
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Coloque seu nome de usuário para o chat em grupo: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 12345);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();

        scanner.close();
    }
}
