import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * STEP 5 - Audit log: writes every TrustCheck decision to a text file.
 *
 * ACCOUNTABILITY: anyone can look back and see what was flagged and why.
 * PRIVACY: we log the message ID, score, and reasons, but NEVER the message body.
 */
public class AuditLog {

    private String fileName;

    public AuditLog(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Add one line to the end of the log file.
     */
    public void write(String entry) {
        try {
            // "true" means APPEND to the file instead of erasing it
            PrintWriter out = new PrintWriter(new FileWriter(fileName, true));
            out.println(LocalDateTime.now().withNano(0) + " | " + entry);
            out.close();
        } catch (IOException e) {
            System.out.println("Could not write to audit log: " + e.getMessage());
        }
    }
}