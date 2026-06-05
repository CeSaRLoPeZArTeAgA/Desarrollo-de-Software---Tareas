package pe.edu.uni.fc.cc.citizenvoice.service;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.config.Constants;
import pe.edu.uni.fc.cc.citizenvoice.crypto.KeyCodec;
import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPrivate;
import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPublic;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.KeyPairFactory;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.RsaCryptoFactory;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.RepositoryAbstractFactory;
import pe.edu.uni.fc.cc.citizenvoice.repository.CitizenPrivateRepository;
import pe.edu.uni.fc.cc.citizenvoice.repository.CitizenPublicRepository;

import java.security.KeyPair;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RegistrySeeder {
    private static final String[] NAMES = {"Carlos", "Maria", "Jose", "Ana", "Luis", "Lucia", "Miguel", "Rosa", "Jorge", "Elena", "Cesar", "Omar", "Valeria", "Diego", "Camila", "Fernando"};
    private static final String[] LAST_NAMES = {"Lopez", "Arteaga", "Garcia", "Perez", "Ramirez", "Torres", "Flores", "Vargas", "Castro", "Rojas", "Mendoza", "Quispe", "Huaman", "Soto", "Chavez", "Diaz"};

    private final CitizenPrivateRepository privateRepository;
    private final CitizenPublicRepository publicRepository;
    private final KeyPairFactory keyPairFactory;

    public RegistrySeeder(RepositoryAbstractFactory repositories, RsaCryptoFactory cryptoFactory) {
        this.privateRepository = repositories.privateCitizenRepository();
        this.publicRepository = repositories.publicCitizenRepository();
        this.keyPairFactory = cryptoFactory.createKeyPairFactory();
    }

    public void seed(int count, int keySize, int workers, boolean clean) {
        if (clean) {
            privateRepository.clear();
            publicRepository.clear();
        }
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, workers));
        AtomicInteger finished = new AtomicInteger();
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);
        for (int i = 1; i <= count; i++) {
            final int index = i;
            completion.submit(() -> {
                String dni = String.format("%08d", 10_000_000 + index);
                String fullName = buildName(index);
                KeyPair pair = keyPairFactory.generate(keySize);
                privateRepository.save(new CitizenPrivate(dni, fullName, KeyCodec.encodePrivate(pair)));
                publicRepository.save(new CitizenPublic(dni, fullName, KeyCodec.encodePublic(pair)));
                int done = finished.incrementAndGet();
                if (done % 1000 == 0 || done == count) System.out.println("Generados " + done + " / " + count);
                return null;
            });
        }
        try {
            for (int i = 0; i < count; i++) completion.take().get();
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar registros", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private static String buildName(int i) {
        String n1 = NAMES[i % NAMES.length];
        String n2 = NAMES[(i * 7) % NAMES.length];
        String l1 = LAST_NAMES[(i * 5) % LAST_NAMES.length];
        String l2 = LAST_NAMES[(i * 11) % LAST_NAMES.length];
        return n1 + " " + n2 + " " + l1 + " " + l2;
    }

    public static void main(String[] args) {
        int count = intArg(args, "--count", Constants.DEFAULT_GENERAL_REGISTRY_SIZE);
        int keySize = intArg(args, "--key-size", ApplicationConfig.getInstance().defaultKeySize());
        int workers = intArg(args, "--workers", Runtime.getRuntime().availableProcessors());
        boolean clean = !hasArg(args, "--append");
        var context = ApplicationContext.getInstance();
        new RegistrySeeder(context.repositories(), context.cryptoFactory()).seed(count, keySize, workers, clean);
        System.out.println("Bases generadas correctamente: private_registry.db y public_registry.db");
    }

    private static int intArg(String[] args, String key, int fallback) {
        for (int i = 0; i < args.length - 1; i++) if (args[i].equals(key)) return Integer.parseInt(args[i + 1]);
        return fallback;
    }

    private static boolean hasArg(String[] args, String key) {
        for (String arg : args) if (arg.equals(key)) return true;
        return false;
    }
}
