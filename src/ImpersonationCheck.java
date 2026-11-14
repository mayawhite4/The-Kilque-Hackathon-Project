import java.util.ArrayList;
import java.util.HashMap;

/**
 * TEAM 1 - Layer 1: Impersonation Alert (Operation Human Trust)
 *
 * Idea: learn how each sender NORMALLY behaves from their past messages
 * (data/History.txt), then add risk points when a new message breaks that pattern
 * or uses scam-style language.
 *
 * PRIVACY: we only keep a few facts per sender (email, reply-to, hour range).
 * We never store the full past messages. Tell the judges this!
 *
 * FAIRNESS: casual writing, slang, or typos never add points. We only look at
 * WHO sent it, WHEN, and WHAT they are asking for.
 */
public class ImpersonationCheck {

    // Baseline facts, looked up by sender name (e.g. "Prof. Smith")
    private HashMap<String, String> usualEmail = new HashMap<String, String>();
    private HashMap<String, String> usualReplyTo = new HashMap<String, String>();
    private HashMap<String, Integer> earliestHour = new HashMap<String, Integer>();
    private HashMap<String, Integer> latestHour = new HashMap<String, Integer>();

    // Phrase lists (always compare in lowercase)
    private String[] urgentWords = {"urgent", "immediately", "right now", "asap", "today"};
    private String[] secretWords = {"do not tell", "don't tell", "keep this between", "confidential"};
    private String[] riskyRequests = {"gift card", "wire", "spreadsheet", "password", "payroll", "bank"};

    /**
     * CONSTRUCTOR - builds the baseline from known-good past messages.
     * Note: NO "void" here. A constructor has no return type.
     */
    public ImpersonationCheck(ArrayList<Message> history) {
        for (int i = 0; i < history.size(); i++) {
            learn(history.get(i));
        }
    }

    /**
     * Add one known-good message to the baseline.
     * Used by the constructor, AND when a person clicks "This was legit" (the appeal).
     */
    public void learn(Message m) {
        String name = m.getSenderName();
        int hour = m.getHour();

        // Remember this sender's normal email and reply-to address
        usualEmail.put(name, m.getSenderEmail());
        usualReplyTo.put(name, m.getReplyTo());

        // Track the earliest and latest hour this sender usually writes
        if (!earliestHour.containsKey(name)) {
            // First message we've seen from this sender
            earliestHour.put(name, hour);
            latestHour.put(name, hour);
        } else {
            if (hour < earliestHour.get(name)) {
                earliestHour.put(name, hour);
            }
            if (hour > latestHour.get(name)) {
                latestHour.put(name, hour);
            }
        }
    }

    /**
     * Check one message and return its risk points with reasons.
     */
    public CheckResult check(Message m) {
        CheckResult result = new CheckResult("Impersonation Alert");
        String name = m.getSenderName();

        // Lowercase copy of subject + body so "URGENT" and "urgent" both match
        String text = (m.getSubject() + " " + m.getBody()).toLowerCase();

        // ---- Part 1: does this match how the sender normally behaves? ----
        if (!usualEmail.containsKey(name)) {
            // We have no history for this sender
            result.addPoints(10, "First message from " + name + " (no history to compare)");
        } else {
            // Different email address than usual?  (use .equals, never ==)
            if (!m.getSenderEmail().equals(usualEmail.get(name))) {
                result.addPoints(30, name + " usually writes from " + usualEmail.get(name)
                        + ", but this came from " + m.getSenderEmail());
            }

            // Replies secretly going somewhere else?
            if (!m.getReplyTo().equals(usualReplyTo.get(name))) {
                result.addPoints(25, "Replies would go to " + m.getReplyTo()
                        + " instead of " + usualReplyTo.get(name));
            }

            // Sent at an unusual time? (more than 2 hours outside their normal range)
            int early = earliestHour.get(name);
            int late = latestHour.get(name);
            if (m.getHour() < early - 2 || m.getHour() > late + 2) {
                result.addPoints(15, "Sent at " + m.getHour() + ":00; " + name
                        + " usually writes between " + early + ":00 and " + late + ":00");
            }
        }

        // ---- Part 2: scam-style language (each list adds points only once) ----
        if (containsAny(text, urgentWords)) {
            result.addPoints(15, "Pressure / urgency language");
        }
        if (containsAny(text, secretWords)) {
            result.addPoints(20, "Asks to keep it secret");
        }
        if (containsAny(text, riskyRequests)) {
            result.addPoints(20, "Asks for money, passwords, or sensitive files");
        }

        return result;
    }

    /**
     * Helper: true if the text contains ANY of the words in the list.
     */
    public boolean containsAny(String text, String[] words) {
        for (int i = 0; i < words.length; i++) {
            if (text.contains(words[i])) {
                return true;
            }
        }
        return false;
    }
}