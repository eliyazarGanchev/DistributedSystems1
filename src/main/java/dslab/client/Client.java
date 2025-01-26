package dslab.client;

import dslab.ComponentFactory;
import dslab.cli.ClientCLI;
import dslab.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Client implements IClient {
    private String componentId;
    private InputStream in;
    private OutputStream out;
    private ClientCLI clientCLI;
    private Config config;

    public Client(String componentId, InputStream in, OutputStream out) {
        this.componentId = componentId;
        this.in = in;
        this.out = out;
        this.config = new Config(componentId);
        this.clientCLI = new ClientCLI(this, config, in, out);
    }

    @Override
    public void run() {
        try {
            clientCLI.run();
        } catch (Exception e) {
        } finally {
            shutdown();
        }
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    @Override
    public void shutdown() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link Client} and runs it.
     * Standard input, output and error streams are passed to the client which are then used by the {@link ClientCLI}.
     *
     * @param args the client config filename found in classpath resources without the file extension
     */
    public static void main(String[] args) {
        ComponentFactory.createClient(args[0], System.in, System.out).run();
    }
}
