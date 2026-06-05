package pe.edu.uni.fc.cc.citizenvoice.app;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.config.Constants;
import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.service.*;

public class CitizenVoiceCli {
    public static void main(String[] args) {
        if (args.length == 0 || has(args, "--help")) {
            help();
            return;
        }
        String cmd = args[0];
        ApplicationContext context = ApplicationContext.getInstance();
        CitizenVoiceFacade facade = context.facade();
        switch (cmd) {
            case "seed" -> {
                int count = intArg(args, "--count", Constants.DEFAULT_GENERAL_REGISTRY_SIZE);
                int keySize = intArg(args, "--key-size", ApplicationConfig.getInstance().defaultKeySize());
                int workers = intArg(args, "--workers", Runtime.getRuntime().availableProcessors());
                new RegistrySeeder(context.repositories(), context.cryptoFactory()).seed(count, keySize, workers, !has(args, "--append"));
            }
            case "create-proposal" -> {
                Proposal p = facade.createProposal(strArg(args, "--title", "Propuesta PC3"), strArg(args, "--description", "Descripcion de prueba"), strArg(args, "--collective", "Colectivo Civil"));
                System.out.println("Propuesta creada con ID=" + p.id());
            }
            case "create-from-template" -> {
                Proposal p = facade.createProposalFromTemplate(strArg(args, "--template", "transparencia-digital"), strArg(args, "--title", ""), strArg(args, "--collective", ""));
                System.out.println("Propuesta creada desde plantilla con ID=" + p.id());
            }
            case "sign" -> {
                long id = longArg(args, "--proposal-id", 1);
                String dni = strArg(args, "--dni", "");
                OperationResult r = facade.signProposal(id, dni);
                print(r);
            }
            case "run-demo" -> {
                long id = longArg(args, "--proposal-id", 1);
                int sampleSize = intArg(args, "--sample-size", Constants.DEFAULT_SIGNATURE_THRESHOLD);
                OperationResult r = facade.runPc3Demo(id, sampleSize, true);
                print(r);
            }
            case "freeze" -> {
                long id = longArg(args, "--proposal-id", 1);
                print(facade.freezeAndSubmit(id));
            }
            case "patterns" -> facade.patterns().forEach((k, v) -> System.out.println(k + ": " + v));
            default -> help();
        }
    }

    private static void print(OperationResult r) {
        System.out.println("ok=" + r.ok());
        System.out.println("mensaje=" + r.message());
        System.out.println("proposalId=" + r.proposalId());
        System.out.println("firmas=" + r.signatures());
        if (!r.hash().isBlank()) System.out.println("hash=" + r.hash());
    }

    private static void help() {
        System.out.println("Citizen Voice PC3 - Java");
        System.out.println("Comandos:");
        System.out.println("  seed --count 100000 --key-size 2048 --workers 8");
        System.out.println("  create-proposal --title ... --description ... --collective ...");
        System.out.println("  create-from-template --template transparencia-digital");
        System.out.println("  sign --proposal-id 1 --dni 10000001");
        System.out.println("  run-demo --proposal-id 1 --sample-size 25000");
        System.out.println("  freeze --proposal-id 1");
        System.out.println("  patterns");
    }

    private static String strArg(String[] args, String key, String fallback) {
        for (int i = 0; i < args.length - 1; i++) if (args[i].equals(key)) return args[i + 1];
        return fallback;
    }
    private static int intArg(String[] args, String key, int fallback) { return Integer.parseInt(strArg(args, key, String.valueOf(fallback))); }
    private static long longArg(String[] args, String key, long fallback) { return Long.parseLong(strArg(args, key, String.valueOf(fallback))); }
    private static boolean has(String[] args, String key) { for (String a : args) if (a.equals(key)) return true; return false; }
}
