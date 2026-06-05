package pe.edu.uni.fc.cc.citizenvoice;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.crypto.*;
import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.*;
import pe.edu.uni.fc.cc.citizenvoice.patterns.structural.HashingArchiveDecorator;
import pe.edu.uni.fc.cc.citizenvoice.patterns.structural.ArchivePackage;
import pe.edu.uni.fc.cc.citizenvoice.service.*;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;

public class TestRunner {
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        Path data = Path.of("build", "test-data");
        FileUtil.deleteDirectory(data);
        Files.createDirectories(data);
        ApplicationConfig.getInstance().overrideForTests(data, 5, 512);
        ApplicationContext.resetForTests();
        ApplicationContext context = ApplicationContext.getInstance();

        testPatternsCatalog(context);
        testPrototype();
        testRsaSignature();
        testRegistrySeeder(context);
        testFacadeFlow(context);
        testFreezeDecorator();

        System.out.println("TESTS OK: " + passed + " verificaciones generales superadas");
    }

    private static void testPatternsCatalog(ApplicationContext context) {
        var catalog = context.facade().patterns();
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Factory Method")), "Debe existir Factory Method");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Abstract Factory")), "Debe existir Abstract Factory");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Builder")), "Debe existir Builder");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Prototype")), "Debe existir Prototype");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Singleton")), "Debe existir Singleton");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Adapter")), "Debe existir Adapter");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Bridge")), "Debe existir Bridge");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Composite")), "Debe existir Composite");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Decorator")), "Debe existir Decorator");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Facade")), "Debe existir Facade");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Flyweight")), "Debe existir Flyweight");
        assertTrue(catalog.keySet().stream().anyMatch(k -> k.contains("Proxy")), "Debe existir Proxy");
    }

    private static void testPrototype() {
        ProposalDraftPrototype a = ProposalTemplateRegistry.getInstance().cloneTemplate("transparencia-digital");
        ProposalDraftPrototype b = ProposalTemplateRegistry.getInstance().cloneTemplate("transparencia-digital");
        b.withTitle("Otro titulo");
        assertTrue(!a.title().equals(b.title()), "El clon no debe alterar la plantilla original");
    }

    private static void testRsaSignature() {
        RsaCryptoFactory factory = new RsaCryptoFactory();
        KeyPair pair = factory.createKeyPairFactory().generate(512);
        DigitalSignatureAdapter adapter = factory.createSignatureAdapter();
        byte[] payload = "mensaje PC3".getBytes();
        String sig = adapter.sign(payload, pair.getPrivate());
        assertTrue(adapter.verify(payload, sig, pair.getPublic()), "Firma RSA debe verificar");
        assertTrue(!adapter.verify("mensaje alterado".getBytes(), sig, pair.getPublic()), "Firma alterada debe fallar");
    }

    private static void testRegistrySeeder(ApplicationContext context) {
        new RegistrySeeder(context.repositories(), context.cryptoFactory()).seed(20, 512, 2, true);
        assertTrue(context.repositories().privateCitizenRepository().count() == 20, "Base privada debe tener 20 ciudadanos");
        assertTrue(context.repositories().publicCitizenRepository().count() == 20, "Base publica debe tener 20 ciudadanos");
        assertTrue(context.repositories().privateCitizenRepository().findByDni("10000001").isPresent(), "DNI 10000001 debe existir");
        assertTrue(context.repositories().publicCitizenRepository().findByDni("10000001").isPresent(), "DNI 10000001 debe existir en publica");
    }

    private static void testFacadeFlow(ApplicationContext context) throws Exception {
        CitizenVoiceFacade facade = context.facade();
        Proposal p = facade.createProposal("Ley PC3", "Descripcion de iniciativa", "Colectivo Test");
        facade.addResource(p.id(), "comentario", "Sustento ciudadano de prueba");
        OperationResult one = facade.signProposal(p.id(), "10000001");
        assertTrue(one.ok(), "Firma individual debe registrar");
        assertTrue(facade.verifyStoredSignature(p.id(), "10000001"), "Firma almacenada debe verificar");
        OperationResult dup = facade.signProposal(p.id(), "10000001");
        assertTrue(!dup.ok(), "Firma duplicada debe rechazarse");
        OperationResult demo = facade.runPc3Demo(p.id(), 5, true);
        assertTrue(demo.ok(), "Demo debe completarse");
        assertTrue(!demo.hash().isBlank(), "Debe generar hash de congelamiento");
        assertTrue(Files.exists(ApplicationConfig.getInstance().archiveDirectory().resolve("proposal-" + p.id() + "-frozen.txt")), "Debe existir expediente congelado");
        assertTrue(Files.exists(ApplicationConfig.getInstance().submissionDirectory().resolve("congreso-propuesta-" + p.id() + ".txt")), "Debe existir envio al Congreso");
    }

    private static void testFreezeDecorator() {
        ArchivePackage archive = new ArchivePackage();
        archive.add(() -> "contenido");
        HashingArchiveDecorator dec = new HashingArchiveDecorator(archive);
        String hash = dec.hashHex();
        assertTrue(hash.matches("[0-9a-f]{64}"), "Hash SHA-256 debe tener 64 hex");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        passed++;
    }
}
