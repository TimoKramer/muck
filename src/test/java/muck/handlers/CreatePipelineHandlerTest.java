package muck.handlers;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServer;
import muck.client.BobClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CreatePipelineHandlerTest {

    private static WebServer server;
    private static Http1Client client;
    private static BobClient bobClient;

    @BeforeAll
    static void setUp() {
        bobClient = mock(BobClient.class);

        server = WebServer.builder()
                .port(0)
                .routing(r -> r.post("/create", new CreatePipelineHandler(bobClient)))
                .build()
                .start();

        client = Http1Client.builder()
                .baseUri("http://localhost:" + server.port())
                .build();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) server.stop();
    }

    @Test
    void createPipelineSuccess() {
        when(bobClient.createPipeline(anyString())).thenReturn(true);

        var response = client.post("/create")
                .header(io.helidon.http.HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .submit("group=dev&name=test&image=alpine:latest&steps=echo+hello");

        assertEquals(Status.OK_200, response.status());
        verify(bobClient).createPipeline(anyString());
    }

    @Test
    void createPipelineMissingFields() {
        var response = client.post("/create")
                .header(io.helidon.http.HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .submit("group=dev&name=&image=&steps=");

        assertEquals(Status.BAD_REQUEST_400, response.status());
    }

    @Test
    void createPipelineFailure() {
        when(bobClient.createPipeline(anyString())).thenReturn(false);

        var response = client.post("/create")
                .header(io.helidon.http.HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .submit("group=dev&name=test&image=alpine&steps=echo+hi");

        assertEquals(Status.INTERNAL_SERVER_ERROR_500, response.status());
    }
}
