/**
 * TEAM 2 - Layer 2: Link & Domain Check (Operation Digital Shield)
 *
 * Catches phishing tricks in the sender's address and in links:
 *   1. Lookalike domains  - e.g. rnybank.com ("rn" looks like "m") or ncat-helpdesk.net
 *   2. Shortened links    - e.g. bit.ly hides where the link really goes
 *   3. Insecure links     - http:// instead of https://
 *
 * It only flags and explains. It never blocks or deletes anything on its own.
 */
public class LinkCheck {

    // Trusted organizations: the brand name and its ONE real domain.
    // brandNames[0] goes with realDomains[0], brandNames[1] with realDomains[1], and so on.
    private String[] brandNames  = {"ncat",     "mybank",     "microsoft"};
    private String[] realDomains = {"ncat.edu", "mybank.com", "microsoft.com"};

    // Link-shortening services that hide the real destination
    private String[] shorteners = {"bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly"};

    /**
     * Check one message's sender address and every link in its body.
     */
    public CheckResult check(Message m) {
        CheckResult result = new CheckResult("Link & Domain Check");

        // ---- Part A: check the sender's email domain ----
        String senderDomain = getEmailDomain(m.getSenderEmail());
        String brand = findLookalikeBrand(senderDomain);
        if (brand != null) {
            result.addPoints(30, "Sender domain " + senderDomain
                    + " looks like " + brand + " but is not the real one");
        }

        // ---- Part B: check every link in the body ----
        String[] words = m.getBody().split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = stripEnd(words[i]);

            // Only look at words that are links
            if (word.startsWith("http://") || word.startsWith("https://")) {
                String linkDomain = getLinkDomain(word);

                // Lookalike domain in the link?
                String linkBrand = findLookalikeBrand(linkDomain);
                if (linkBrand != null) {
                    result.addPoints(30, "Link goes to " + linkDomain
                            + ", which imitates " + linkBrand);
                }

                // Shortened link?
                if (isShortener(linkDomain)) {
                    result.addPoints(25, "Shortened link (" + linkDomain
                            + ") hides the real destination");
                }

                // Not secure?
                if (word.startsWith("http://")) {
                    result.addPoints(10, "Link is not secure (http instead of https)");
                }
            }
        }

        return result;
    }

    /**
     * "jsmith@ncat.edu" -> "ncat.edu"
     */
    public String getEmailDomain(String email) {
        int at = email.indexOf('@');
        if (at == -1) {
            return email.toLowerCase();   // no @ found, just return what we have
        }
        return email.substring(at + 1).toLowerCase();
    }

    /**
     * "http://rnybank-secure.com/login" -> "rnybank-secure.com"
     */
    public String getLinkDomain(String link) {
        String domain = link.toLowerCase();

        // Remove the http:// or https:// at the start
        if (domain.startsWith("https://")) {
            domain = domain.substring(8);   // "https://" is 8 characters
        } else if (domain.startsWith("http://")) {
            domain = domain.substring(7);   // "http://" is 7 characters
        }

        // Cut everything after the first "/"
        int slash = domain.indexOf('/');
        if (slash != -1) {
            domain = domain.substring(0, slash);
        }
        return domain;
    }

    /**
     * Undo common tricks so lookalikes match the real name:
     *   "rn" -> "m"  (rnybank looks like mybank)
     *   "0"  -> "o"  (micr0soft)
     *   "1"  -> "l"
     *   remove "-"   (ncat-edu)
     */
    public String normalize(String domain) {
        String d = domain.toLowerCase();
        d = d.replace("rn", "m");
        d = d.replace("0", "o");
        d = d.replace("1", "l");
        d = d.replace("-", "");
        return d;
    }

    /**
     * If the domain is pretending to be a trusted brand, return that brand's
     * real domain (e.g. "mybank.com"). If it is real or unrelated, return null.
     */
    public String findLookalikeBrand(String domain) {
        for (int i = 0; i < brandNames.length; i++) {
            String real = realDomains[i];

            // The real domain (or a real subdomain like mail.ncat.edu) is fine
            if (domain.equals(real) || domain.endsWith("." + real)) {
                return null;
            }

            // Contains the brand name after undoing tricks, but isn't real -> lookalike
            if (normalize(domain).contains(brandNames[i])) {
                return real;
            }
        }
        return null;   // not pretending to be anyone we know
    }

    /**
     * True if the domain is a known link-shortening service.
     */
    public boolean isShortener(String domain) {
        for (int i = 0; i < shorteners.length; i++) {
            if (domain.equals(shorteners[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove periods, commas, ! and ? from the end of a word
     * (so "http://site.com." becomes "http://site.com").
     */
    public String stripEnd(String word) {
        while (word.length() > 0) {
            char last = word.charAt(word.length() - 1);
            if (last == '.' || last == ',' || last == '!' || last == '?') {
                word = word.substring(0, word.length() - 1);
            } else {
                break;
            }
        }
        return word;
    }
}