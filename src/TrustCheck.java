import java.util.ArrayList;

/**
 * STEP 5 - TrustCheck: runs all three layers on a message and combines them.
 *
 *   Layer 1: ImpersonationCheck  (Human Trust)
 *   Layer 2: LinkCheck           (Digital Shield)
 *   Layer 3: AIGuardCheck        (Secret Vault)
 *
 * TrustCheck RECOMMENDS an action. It never blocks, deletes, or accuses.
 * A human always makes the final decision.
 */
public class TrustCheck {

    private ImpersonationCheck impersonation;
    private LinkCheck links;
    private AIGuardCheck aiGuard;

    // Score cutoffs for the risk levels
    private static final int MEDIUM_AT = 30;
    private static final int HIGH_AT = 60;

    public TrustCheck(ArrayList<Message> history) {
        impersonation = new ImpersonationCheck(history);
        links = new LinkCheck();
        aiGuard = new AIGuardCheck();
    }

    /**
     * Run all three layers. Returns a list with one CheckResult per layer.
     */
    public ArrayList<CheckResult> analyze(Message m) {
        ArrayList<CheckResult> results = new ArrayList<CheckResult>();
        results.add(impersonation.check(m));
        results.add(links.check(m));
        results.add(aiGuard.check(m));
        return results;
    }

    /**
     * Add up the scores from every layer.
     */
    public int totalScore(ArrayList<CheckResult> results) {
        int total = 0;
        for (int i = 0; i < results.size(); i++) {
            total = total + results.get(i).getScore();
        }
        return total;
    }

    /**
     * Turn a score into LOW, MEDIUM, or HIGH.
     */
    public String riskLevel(int score) {
        if (score >= HIGH_AT) {
            return "HIGH";
        } else if (score >= MEDIUM_AT) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * What we suggest the person does. Notice: no accusations, just safe next steps.
     */
    public String recommendedAction(String level) {
        if (level.equals("HIGH")) {
            return "Do not act on this yet. Verify with the sender using a phone number "
                    + "or address you already know, not one in this message.";
        } else if (level.equals("MEDIUM")) {
            return "Be careful. Double-check the sender and links before replying or clicking.";
        } else {
            return "Looks normal. No action needed.";
        }
    }

    /**
     * The safe version of the message body that an AI assistant is allowed to read.
     */
    public String safeBodyForAI(Message m) {
        // If hidden AI instructions were found, the AI does not get the message at all
        if (aiGuard.check(m).getScore() > 0) {
            return "[WITHHELD - possible hidden AI instructions. A person must review this message first.]";
        }
        // Otherwise the AI gets the version with SSNs and student IDs removed
        return aiGuard.redact(m.getBody());
    }

    /* Called when a person says "this was legit".
     Teaches Layer 1 that this sender, address, and time are normal. */
    public void markAsLegit(Message m) {
        impersonation.learn(m);
    }
}