/**
 * STEP 6 - The "simple" approach we compare TrustCheck against.
 *
 * A basic spam filter: flag the message if it contains ANY scary-sounding word.
 * It ignores who sent it, when, the links, and the AI-attack phrases.
 * This is what most people would build first, so it's a fair baseline.
 */
public class KeywordFilter {

    private String[] scaryWords = {
            "urgent", "immediately", "asap", "today", "password", "verify",
            "click", "gift card", "bank", "account", "confidential"
    };

    /**
     * True if the subject or body contains any scary word.
     */
    public boolean isFlagged(Message m) {
        String text = (m.getSubject() + " " + m.getBody()).toLowerCase();
        for (int i = 0; i < scaryWords.length; i++) {
            if (text.contains(scaryWords[i])) {
                return true;
            }
        }
        return false;
    }
}