import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * TrustCheck - Step 5: the full program.
 *
 * For every message:
 *   1. Run all three layers and add up the score
 *   2. Show the risk level, recommended action, and "Why was this flagged?"
 *   3. Show the redacted version an AI assistant may read
 *   4. For MEDIUM/HIGH messages, let a person appeal: "Was this actually legit?"
 *   5. Write the decision to the audit log
 */
public class Main {

    // Change these two lines if your files are somewhere else
    static final String MESSAGE_FILE = "Message.txt";
    static final String HISTORY_FILE = "History.txt";
    static final String LOG_FILE = "audit_log.txt";

    public static void main(String[] args) {
        ArrayList<Message> messages;
        ArrayList<Message> history;

        try {
            messages = MessageLoader.load(MESSAGE_FILE);
            history = MessageLoader.load(HISTORY_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find a data file: " + e.getMessage());
            System.out.println("Check MESSAGE_FILE and HISTORY_FILE at the top of Main.java.");
            return;
        }

        TrustCheck trustCheck = new TrustCheck(history);
        AuditLog log = new AuditLog(LOG_FILE);
        Scanner keyboard = new Scanner(System.in);

        System.out.println("========== TrustCheck ==========");
        System.out.println("Checking " + messages.size() + " messages...\n");

        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);

            // ---- 1. Run all three layers ----
            ArrayList<CheckResult> results = trustCheck.analyze(m);
            int score = trustCheck.totalScore(results);
            String level = trustCheck.riskLevel(score);

            // ---- 2. Show the report ----
            System.out.println("--------------------------------");
            System.out.println(m.toString());
            System.out.println("RISK: " + level + " (" + score + " points)");
            System.out.println("Recommended: " + trustCheck.recommendedAction(level));

            if (score > 0 || hasNotes(results)) {
                System.out.println("Why was this flagged?");
                for (int j = 0; j < results.size(); j++) {
                    CheckResult r = results.get(j);
                    for (int k = 0; k < r.getReasons().size(); k++) {
                        System.out.println("   [" + r.getCheckName() + "] " + r.getReasons().get(k));
                    }
                }
            }

            // ---- 3. Safe version for the AI assistant ----
            System.out.println("AI assistant sees: " + trustCheck.safeBodyForAI(m));

            // ---- 4. Human review / appeal for MEDIUM and HIGH ----
            String decision = "auto-cleared";
            if (!level.equals("LOW")) {
                System.out.print("Was this message actually legit? (y/n): ");
                String answer = keyboard.nextLine().trim().toLowerCase();
                if (answer.equals("y")) {
                    trustCheck.markAsLegit(m);
                    decision = "APPEAL: marked legit by reviewer, baseline updated";
                    System.out.println("Thanks. TrustCheck will remember this sender's pattern.");
                } else {
                    decision = "confirmed suspicious by reviewer";
                }
            }

            // ---- 5. Audit log (ID, score, decision - never the message body) ----
            log.write(m.getId() + " | " + level + " | " + score + " pts | " + decision);
            System.out.println();
        }

        keyboard.close();
        System.out.println("Done. Every decision was saved to " + LOG_FILE);
    }

    /**
     * True if any layer left a note (like a redaction) even with 0 points.
     */
    static boolean hasNotes(ArrayList<CheckResult> results) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getReasons().size() > 0) {
                return true;
            }
        }
        return false;
    }
}