package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.SubmissionRecord;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.file.Path;

/** Implementador Bridge: canal digital simulado de la Oficina del Congreso. */
public class CongressOfficeDigitalChannel implements SubmissionChannel {
    private final Path outputDirectory;

    public CongressOfficeDigitalChannel(Path outputDirectory) { this.outputDirectory = outputDirectory; }

    @Override public String name() { return "OFICINA_CONGRESO_DIGITAL"; }

    @Override public void submit(SubmissionRecord record, String archiveBody) {
        FileUtil.ensureDirectory(outputDirectory);
        Path file = outputDirectory.resolve("congreso-propuesta-" + record.proposalId() + ".txt");
        FileUtil.writeString(file, archiveBody + "\n[ENVIO]\nCanal: " + record.channel() + "\nDestino: " + record.destinationOffice() + "\nFecha: " + record.submittedAt() + "\n");
    }
}
