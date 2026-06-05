package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.SubmissionRecord;

/** Bridge: separa el servicio de envio del canal concreto usado. */
public class SubmissionServiceBridge {
    private final SubmissionChannel channel;

    public SubmissionServiceBridge(SubmissionChannel channel) { this.channel = channel; }

    public void send(SubmissionRecord record, String archiveBody) { channel.submit(record, archiveBody); }
    public String channelName() { return channel.name(); }
}
