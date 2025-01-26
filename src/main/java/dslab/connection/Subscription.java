package dslab.connection;

import java.util.function.Consumer;

public class Subscription extends Thread {
    private final IChannel channel;
    private final Consumer<String> callback;

    public Subscription(IChannel channel, Consumer<String> callback) {
        this.channel = channel;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String message = channel.getFromSubscription();
                if (message != null && !Thread.currentThread().isInterrupted()) {
                    callback.accept(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

