package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.SubmissionRecord;

public interface SubmissionChannel {
    String name();
    void submit(SubmissionRecord record, String archiveBody);
}


