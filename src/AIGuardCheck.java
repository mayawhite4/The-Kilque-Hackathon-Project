/**
 * TEAM 2 - Layer 3: AI Assistant Guard (Operation Secret Vault)
 *
 * This layer protects the AI assistant that reads the inbox. It has two jobs:
 *   1. Find hidden instructions aimed at an AI assistant (prompt injection).
 *   2. Redact private info (SSNs, student IDs) BEFORE any AI reads the message.
 *
 * Redaction adds 0 risk points. It is a safety feature, not an accusation.
 */
public class AIGuardCheck {

    // Phrases attackers use to take over an AI assistant.
    // They are all lowercase because we compare against a lowercase copy of the message.
    private String[] injectionPhrases = {
            "ignore your previous instructions",
            "ignore previous instructions",
            "ignore all instructions",
            "disregard your instructions",
            "ai assistant:",
            "you are now",
            "system prompt"
    };

    /**
     * Check one message for prompt injection and private info.
     * Returns a CheckResult with the risk points and the reasons for them.
     */
    public CheckResult check(Message m) {
        CheckResult result = new CheckResult("AI Assistant Guard");

        // Lowercase copy so "AI Assistant:" and "ai assistant:" both match
        String text = m.getBody().toLowerCase();

        // ---- Job 1: look for each injection phrase in the message ----
        for (int i = 0; i < injectionPhrases.length; i++) {
            String phrase = injectionPhrases[i];
            if (text.contains(phrase)) {
                // \" puts a quotation mark inside the String
                result.addPoints(40, "Hidden AI instruction: \"" + phrase + "\"");
            }
        }

        // ---- Job 2: report redaction (0 points, because it protects people) ----
        int count = countPrivateItems(m.getBody());
        if (count > 0) {
            result.addPoints(0, "Redacted " + count + " private item(s) before AI processing");
        }

        return result;
    }

    /**
     * Return the body with SSNs replaced by [SSN] and student IDs by [STUDENT-ID].
     * Works one word at a time.
     * Example: "SSN 123-45-6789." becomes "SSN [SSN]."
     */
    public String redact(String body) {
        String[] words = body.split(" ");   // break the sentence into words
        String output = "";

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String clean = stripEnd(word);                    // "6789." becomes "6789"
            String ending = word.substring(clean.length());   // the "." we removed (or "")

            if (isSSN(clean)) {
                output = output + "[SSN]" + ending + " ";
            } else if (isStudentID(clean)) {
                output = output + "[STUDENT-ID]" + ending + " ";
            } else {
                output = output + word + " ";                 // normal word, keep it as is
            }
        }

        return output.trim();   // remove the extra space at the very end
    }

    /**
     * Count how many SSNs and student IDs are in the body.
     * Same loop as redact(), but counts matches instead of building a String.
     */
    public int countPrivateItems(String body) {
        String[] words = body.split(" ");
        int count = 0;

        for (int i = 0; i < words.length; i++) {
            String clean = stripEnd(words[i]);
            if (isSSN(clean) || isStudentID(clean)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Remove periods, commas, ! and ? from the END of a word.
     * Needed because "123-45-6789." (with a period) would not pass isSSN().
     */
    public String stripEnd(String word) {
        while (word.length() > 0) {
            char last = word.charAt(word.length() - 1);
            if (last == '.' || last == ',' || last == '!' || last == '?') {
                word = word.substring(0, word.length() - 1);   // chop off the last character
            } else {
                break;   // last character is normal, so stop
            }
        }
        return word;
    }

    /**
     * An SSN looks like 123-45-6789:
     * 11 characters, dashes at index 3 and 6, digits everywhere else.
     */
    public boolean isSSN(String word) {
        // Wrong length? Can't be an SSN.
        if (word.length() != 11) {
            return false;
        }

        // Look at every character one at a time
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (i == 3 || i == 6) {
                if (c != '-') {                  // these spots must be dashes
                    return false;
                }
            } else {
                if (!Character.isDigit(c)) {     // every other spot must be a digit
                    return false;
                }
            }
        }

        return true;   // passed every test
    }

    /**
     * A student ID looks like A01234567:
     * 9 characters, 'A' first, then 8 digits.
     */
    public boolean isStudentID(String word) {
        if (word.length() != 9) {
            return false;
        }
        if (word.charAt(0) != 'A') {
            return false;
        }

        // Start at 1 because index 0 is the 'A'
        for (int i = 1; i < word.length(); i++) {
            if (!Character.isDigit(word.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}