/**
 * One incoming message (like an email) that TrustCheck will check.
 * This class only holds data. The checking happens in other classes.
 */
public class Message {

    private String id;           // a short ID like "M001"
    private String senderName;   // who the message CLAIMS to be from, e.g. "Prof. Smith"
    private String senderEmail;  // the From address
    private String replyTo;      // where replies actually go (attackers often change this)
    private int hour;            // hour it was sent, 0-23 (14 = 2 PM)
    private String subject;
    private String body;
    private String label;        // the right answer, ONLY used for testing:
    // LEGIT, IMPERSONATION, PHISHING, or INJECTION

    public Message(String id, String senderName, String senderEmail, String replyTo,
                   int hour, String subject, String body, String label) {
        this.id = id;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.replyTo = replyTo;
        this.hour = hour;
        this.subject = subject;
        this.body = body;
        this.label = label;
    }

    public String getId() { return id; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getReplyTo() { return replyTo; }
    public int getHour() { return hour; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getLabel() { return label; }

    // Replace the body (used later when we redact SSNs and student IDs)
    public void setBody(String newBody) { body = newBody; }

    public String toString() {
        return id + " | From: " + senderName + " <" + senderEmail + "> | "
                + hour + ":00 | " + subject;
    }
}