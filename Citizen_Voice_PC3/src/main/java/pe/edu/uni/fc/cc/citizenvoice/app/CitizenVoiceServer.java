package pe.edu.uni.fc.cc.citizenvoice.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.ProposalDraftPrototype;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.ProposalTemplateRegistry;
import pe.edu.uni.fc.cc.citizenvoice.service.*;
import pe.edu.uni.fc.cc.citizenvoice.util.SimpleJson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class CitizenVoiceServer {
    private final CitizenVoiceFacade facade;

    public CitizenVoiceServer(CitizenVoiceFacade facade) { this.facade = facade; }

    public static void main(String[] args) throws Exception {
        ApplicationConfig config = ApplicationConfig.getInstance();
        HttpServer server = HttpServer.create(new InetSocketAddress(config.serverPort()), 0);
        CitizenVoiceServer app = new CitizenVoiceServer(ApplicationContext.getInstance().facade());
        server.createContext("/", app::route);
        server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        server.start();
        System.out.println("Citizen Voice PC3 Java API corriendo en http://127.0.0.1:" + config.serverPort());
        System.out.println("Endpoints: GET /, GET /patterns, GET /templates, POST /proposals, GET /proposals, POST /proposals/{id}/run-pc3-demo");
    }

    private void route(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            URI uri = ex.getRequestURI();
            String path = uri.getPath();
            if (method.equals("GET") && path.equals("/")) {
                send(ex, 200, SimpleJson.obj("app", "Citizen Voice PC3 Java", "status", "running"));
            } else if (method.equals("GET") && path.equals("/patterns")) {
                send(ex, 200, mapJson(facade.patterns()));
            } else if (method.equals("GET") && path.equals("/templates")) {
                send(ex, 200, templatesJson());
            } else if (method.equals("GET") && path.equals("/proposals")) {
                send(ex, 200, proposalsJson(facade.listProposals()));
            } else if (method.equals("POST") && path.equals("/proposals")) {
                Map<String, String> body = SimpleJson.parseObject(readBody(ex));
                Proposal p = facade.createProposal(body.get("title"), body.get("description"), body.get("collective"));
                send(ex, 201, proposalJson(p));
            } else if (method.equals("POST") && path.equals("/proposals/from-template")) {
                Map<String, String> body = SimpleJson.parseObject(readBody(ex));
                Proposal p = facade.createProposalFromTemplate(body.getOrDefault("template", "transparencia-digital"), body.getOrDefault("title", ""), body.getOrDefault("collective", ""));
                send(ex, 201, proposalJson(p));
            } else if (method.equals("POST") && path.matches("/proposals/\\d+/sign/\\d{8}")) {
                String[] parts = path.split("/");
                long proposalId = Long.parseLong(parts[2]);
                String dni = parts[4];
                send(ex, 200, resultJson(facade.signProposal(proposalId, dni)));
            } else if (method.equals("POST") && path.matches("/proposals/\\d+/run-pc3-demo")) {
                long proposalId = Long.parseLong(path.split("/")[2]);
                int sampleSize = intQuery(uri.getQuery(), "sampleSize", ApplicationConfig.getInstance().signatureThreshold());
                send(ex, 200, resultJson(facade.runPc3Demo(proposalId, sampleSize, true)));
            } else if (method.equals("POST") && path.matches("/proposals/\\d+/freeze")) {
                long proposalId = Long.parseLong(path.split("/")[2]);
                send(ex, 200, resultJson(facade.freezeAndSubmit(proposalId)));
            } else {
                send(ex, 404, SimpleJson.obj("error", "Endpoint no encontrado"));
            }
        } catch (Exception e) {
            send(ex, 400, SimpleJson.obj("error", e.getMessage()));
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    private static int intQuery(String query, String key, int fallback) {
        if (query == null || query.isBlank()) return fallback;
        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return Integer.parseInt(kv[1]);
        }
        return fallback;
    }

    private static String resultJson(OperationResult r) {
        return SimpleJson.obj("ok", r.ok(), "message", r.message(), "proposalId", r.proposalId(), "signatures", r.signatures(), "hash", r.hash());
    }

    private static String proposalJson(Proposal p) {
        return SimpleJson.obj("id", p.id(), "title", p.title(), "description", p.description(), "collective", p.collective(), "createdAt", p.createdAt(), "status", p.status(), "frozenHash", p.frozenHash(), "submittedAt", p.submittedAt());
    }

    private static String proposalsJson(List<Proposal> proposals) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < proposals.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(proposalJson(proposals.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String mapJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (var e : map.entrySet()) {
            if (i++ > 0) sb.append(',');
            sb.append(SimpleJson.quote(e.getKey())).append(':').append(SimpleJson.quote(e.getValue()));
        }
        return sb.append('}').toString();
    }

    private static String templatesJson() {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (var e : ProposalTemplateRegistry.getInstance().list().entrySet()) {
            ProposalDraftPrototype t = e.getValue();
            if (i++ > 0) sb.append(',');
            sb.append(SimpleJson.quote(e.getKey())).append(':')
              .append(SimpleJson.obj("title", t.title(), "description", t.description(), "collective", t.collective()));
        }
        return sb.append('}').toString();
    }
}
