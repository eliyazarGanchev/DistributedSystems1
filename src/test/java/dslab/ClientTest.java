package dslab;

import dslab.annotations.GitHubClassroomGrading;
import dslab.grading.LocalGradingExtension;
import dslab.client.Client;
import dslab.client.IClient;
import dslab.util.streams.TestInputStream;
import dslab.util.streams.TestOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(LocalGradingExtension.class)
public class ClientTest {

    private TestInputStream in;
    private TestOutputStream out;

    public void beforeEach() {
        in = new TestInputStream();
        out = new TestOutputStream();
    }

    public void afterEach() throws IOException {
        in.close();
        out.close();
    }

    @GitHubClassroomGrading(maxScore = 1)
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest
    @ValueSource(strings = {"client-0", "client-1", "client-2"})
    public void client_01_return_correct_componentId(String componentId) throws IOException {
        beforeEach();
        IClient client = new Client(componentId, in, out);
        assertThat(client.getComponentId()).isEqualTo(componentId);
        afterEach();
    }

    @GitHubClassroomGrading(maxScore = 2)
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    @Test
    public void client_02_correct_thread_lifecycle() throws IOException {
        beforeEach();
        Thread t = new Thread(new Client("client-0", in, out));
        t.start();

        await().atMost(250, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertThat(t.isAlive()).isTrue();
        });

        in.addLine("shutdown");

        await().atMost(250, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertThat(t.isAlive()).isFalse();
        });

        afterEach();
    }
}
