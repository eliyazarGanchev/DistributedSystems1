package dslab.connection;

import dslab.connection.types.ExchangeType;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Channel implements IChannel {

    private String host;
    private int port;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader bufferedReader;

    public Channel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean connect() throws IOException {
        socket = new Socket(host, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String toRet = bufferedReader.readLine();
        return toRet != null && toRet.equals("ok SMQP");
    }

    @Override
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                this.writeToBroker("exit");
                socket.close();
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exchangeDeclare(ExchangeType exchangeType, String exchangeName) { // ask
        String command = String.format("exchange %s %s", exchangeType.name().toLowerCase(), exchangeName);
        try {
            writeToBroker(command);
            String response = bufferedReader.readLine();
            return response != null && response.equals("ok");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean queueBind(String queueName, String bindingKey) {
        try {
            String queueCommand = String.format("queue %s", queueName);
            String bindingCommand = String.format("bind %s", bindingKey);

            writeToBroker(queueCommand);
            String response = bufferedReader.readLine();
            if (response == null || !response.equals("ok"))
                return false;
            writeToBroker(bindingCommand);
            response = bufferedReader.readLine();
            return response != null && response.equals("ok");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Thread subscribe(Consumer<String> callback) {
        try {
            writeToBroker("subscribe");
            String response = bufferedReader.readLine();
            if (response == null || !response.equals("ok"))
                return null;
        } catch (IOException e) {
            return null;
        }
        return new Subscription(this, callback);
    }

    @Override
    public String getFromSubscription() {
        try {
            String message = this.bufferedReader.readLine();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean publish(String routingKey, String message) {
        try {

            OutputStream out = socket.getOutputStream();
            String formattedMessage = String.format("publish %s %s", routingKey, message);
            writeToBroker(formattedMessage);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void writeToBroker(String message) {
        try {
            outputStream.write((message + "\n").getBytes());
        } catch (IOException e) {
            System.err.println("error: failed writing to socket.");
        }
    }
}