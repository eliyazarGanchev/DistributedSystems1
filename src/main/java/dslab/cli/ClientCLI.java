package dslab.cli;

import dslab.client.IClient;
import dslab.config.Config;
import dslab.connection.Channel;
import dslab.connection.types.ExchangeType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class ClientCLI implements IClientCLI {
    private final IClient client;
    private final Config config;
    private Channel channel;
    private final PrintWriter writer;
    private final Scanner scanner;

    public ClientCLI(IClient client, Config config, InputStream in, OutputStream out) {
        this.client = client;
        this.config = config;
        this.writer = new PrintWriter(out, true);
        this.scanner = new Scanner(in);
    }

    @Override
    public void run() {
        try {
            while (true) {
                printPrompt();
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine();
                if (input == null || input.isBlank())
                    continue;
                if ("shutdown".equalsIgnoreCase(input)) {
                    if (channel != null)
                        channel.disconnect();
                    break;
                }
                processCommand(input.split(" "));
            }
        } finally {
            scanner.close();
        }
    }

    private void processCommand(String[] command) {
        if (command.length == 0) {
            writeToCli("error: command not recognized");
            return;
        }
        try {
            switch (command[0].toLowerCase()) {
                case "channel" -> {
                    if (command.length != 2) {
                        writeToCli("error: invalid channel command");
                        return;
                    }
                    connectToBroker(command[1]);
                }
                case "subscribe" -> {
                    if (command.length != 5 || channel == null) {
                        writeToCli("error: invalid subscribe command or channel not connected");
                        return;
                    }
                    channel.exchangeDeclare(ExchangeType.valueOf(command[2].toUpperCase()), command[1]);
                    channel.queueBind(command[3], command[4]);

                    Thread subscriptionThread = channel.subscribe(writer::println);
                    subscriptionThread.start();
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }

                    subscriptionThread.interrupt();
                }
                case "publish" -> {
                    if (command.length != 5 || channel == null) {
                        writeToCli("error: invalid publish command or channel not connected");
                        return;
                    }
                    channel.exchangeDeclare(ExchangeType.valueOf(command[2].toUpperCase()), command[1]);
                    channel.publish(command[3], command[4]);
                }
                default -> writeToCli("error: unknown command");
            }
        } catch (IllegalArgumentException e) {
            writeToCli("error: " + e.getMessage());
        }
    }

    private void connectToBroker(String broker) {
        if (channel != null) {
            channel.disconnect();
        }

        if (!config.containsKey(broker + ".host")) {
            writeToCli("error: broker does not exist");
            return;
        }

        String host = config.getString(broker + ".host");
        int port = config.getInt(broker + ".port");
        channel = new Channel(host, port);

        try {
            if (!channel.connect()) {
                writeToCli("error: could not connect to broker");
                channel = null;
            }
        } catch (IOException e) {
            writeToCli("error: broker connection failed: " + e.getMessage());
            channel = null;
        }
    }

    @Override
    public void printPrompt() {
        writeToCli(client.getComponentId() + "> ");
    }

    private void writeToCli(String message) {
        writer.println(message);
    }
}
