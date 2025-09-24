package com.example.starter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class HttpOverTCPTest {
    Buffer requestBytes;

    @Test
    @DisplayName("HTTP over tcp")
    void tcpClientHttpServer(final Vertx vertx, final VertxTestContext testContext) {
        final String httpBody = "Hello";
        final String httpRequest = "POST / HTTP/1.1\n" +
            "Host: localhost:" + 1234 +"\n" +
            "Content-Length: " + httpBody.length() + "\n" +
            "Accept: */*\r\n\r\n" + httpBody;
        
        HttpServer server = vertx.createHttpServer();

        server.requestHandler(request -> {
            request.body()
                .onSuccess(b -> testContext.verify( () -> {
                    assertThat(b.toString()).isEqualTo(httpBody);
                }).completeNow())
                .onFailure(testContext::failNow);
            request.response().end("Done");
        });

        server.listen(1234, "localhost")
            .onComplete(res -> {
            if (res.succeeded()) {
                System.out.println("Server is now listening!");
                requestBytes = Buffer.buffer(httpRequest.getBytes(StandardCharsets.UTF_8));
                NetClient client = vertx.createNetClient();
                client.connect(1234,"localhost")
                    .compose(socket -> 
                        socket.write(requestBytes)
                        .onSuccess(v -> System.out.println("jh log: successful write"))
                        .onFailure(testContext::failNow)
                    ).onFailure(testContext::failNow);
            } else {
                System.out.println("Failed to bind!");
            }
        });
    }
}
