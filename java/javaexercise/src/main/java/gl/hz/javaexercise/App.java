package gl.hz.javaexercise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

class ReadbackHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange http) throws IOException {
        final JsonObjectBuilder headers = Json.createObjectBuilder();
        http.getRequestHeaders().entrySet()
                .forEach(e -> headers.add(e.getKey(), Json.createArrayBuilder(e.getValue())));
        final ByteArrayOutputStream req = new ByteArrayOutputStream();
        int size = 0;
        byte[] buf = new byte[100];
        while ((size = http.getRequestBody().read(buf)) > 0) {
            req.write(Arrays.copyOfRange(buf, 0, size));
        }
        final String resp = Json.createObjectBuilder()
                .add("method", http.getRequestMethod())
                .add("protocol", http.getProtocol())
                .add("uri", http.getRequestURI().toString())
                .add("headers", headers)
                .add("local_address", http.getLocalAddress().toString())
                .add("remote_address", http.getRemoteAddress().toString())
                .add("request_body", new String(req.toByteArray()))
                .build().toString();
        try {
            http.sendResponseHeaders(200, resp.length());
            http.getResponseBody().write(resp.getBytes());
            http.getResponseBody().close();
        } finally {
            http.close();
        }
    }
}

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel(Level.ALL);
        rootLog.getHandlers()[0].setLevel(Level.ALL);
        logger.log(Level.INFO, "about to start web server");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
            server.createContext("/readback", new ReadbackHandler());
            server.setExecutor(null);
            server.start();
        } catch (final Exception ex) {
            logger.log(Level.SEVERE, "failed to start web server: " + ex.getMessage());
        }
    }
}
