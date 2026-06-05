package pe.edu.uni.fc.cc.citizenvoice.service;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.FileRepositoryFactory;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.RepositoryAbstractFactory;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.RsaCryptoFactory;
import pe.edu.uni.fc.cc.citizenvoice.patterns.structural.*;

/** Singleton: contenedor manual de dependencias de la aplicacion. */
public final class ApplicationContext {
    private static volatile ApplicationContext instance;

    private final ApplicationConfig config;
    private final RepositoryAbstractFactory repositories;
    private final RsaCryptoFactory cryptoFactory;
    private final CitizenVoiceFacade facade;

    private ApplicationContext() {
        this.config = ApplicationConfig.getInstance();
        this.repositories = new FileRepositoryFactory(config);
        this.cryptoFactory = new RsaCryptoFactory();
        PublicKeyFlyweightFactory flyweight = new PublicKeyFlyweightFactory();
        PublicRegistryProxy publicRegistryProxy = new PublicRegistryProxy(repositories.publicCitizenRepository(), flyweight);
        SubmissionServiceBridge submissionBridge = new SubmissionServiceBridge(new CongressOfficeDigitalChannel(config.submissionDirectory()));
        this.facade = new CitizenVoiceFacade(
                config,
                repositories.privateCitizenRepository(),
                publicRegistryProxy,
                repositories.proposalRepository(),
                repositories.signatureRepository(),
                repositories.resourceRepository(),
                repositories.submissionRepository(),
                cryptoFactory.createSignatureAdapter(),
                submissionBridge
        );
    }

    public static ApplicationContext getInstance() {
        ApplicationContext local = instance;
        if (local == null) {
            synchronized (ApplicationContext.class) {
                local = instance;
                if (local == null) instance = local = new ApplicationContext();
            }
        }
        return local;
    }

    public static synchronized void resetForTests() { instance = null; }

    public ApplicationConfig config() { return config; }
    public RepositoryAbstractFactory repositories() { return repositories; }
    public RsaCryptoFactory cryptoFactory() { return cryptoFactory; }
    public CitizenVoiceFacade facade() { return facade; }
}
