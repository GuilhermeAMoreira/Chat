import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * gerencia a comunicação de cada cliente com o servidor, cada um em uma thread separada
 */
public class ClientHandler implements Runnable {
    //lista de clientes ativos
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    //socket de conexão
    private Socket socket;

    //lê o fluxo de entrada que o cliente enviou
    private BufferedReader bufferedReader;
    //escreve o fluxo de saída para o cliente
    private BufferedWriter bufferedWriter;
    //nome do cliente
    private String clientUserName;

    //formato de data e hora
    private DateTimeFormatter dataTime;
    //data e hora atuais
    private LocalDateTime now;
    //escreve as mensagens no arquivo log
    private FileWriter fileWriter;
    private PrintWriter printWriter;
    
    /**
     * construtor do ClientHandler
     * configura os fluxos de entrada e saída, obtém o nome de usuário do cliente, cria um arquivo log com o nome do cliente e adiciona na lista de clientes ativos
     * @param socket
     */
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            this.fileWriter = new FileWriter("logs/log" + clientUserName + ".txt");
            this.printWriter = new PrintWriter(fileWriter);
            this.dataTime = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUserName + " entrou no chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * funciona numa thread separada para ler as mensagens enviadas e enviar para todos os outros clientes conectados
     */
    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                this.now = LocalDateTime.now();
                this.printWriter.println("[" + dataTime.format(now) + "]");
                this.printWriter.println(messageFromClient);
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    /**
     * recebe a mensagem deum cliente e a envia para todos os outros
     * também escreve no log
     * @param messageToSend
     */
    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUserName.equals(clientUserName)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();

                    this.now = LocalDateTime.now();
                    clientHandler.printWriter.println("[" + dataTime.format(now) + "]");
                    clientHandler.printWriter.println(messageToSend);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    /**
     * remove o cliente da lista de conectados quando ele desconecta
     */
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUserName + " saiu do chat!");
    }

    /**
     * fecha todos os recursos abertos, fecha o arquivo de log e remove o cliente da lista de ativos
     * @param socket
     * @param bufferedReader
     * @param bufferedWriter
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
            printWriter.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
