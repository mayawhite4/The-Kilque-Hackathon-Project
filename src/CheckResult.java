import java.util.ArrayList;

/**
 * SHARED BY BOTH TEAMS - don't change this file without telling the other team.
 *
 * The result of one check on one message: a risk score plus the reasons for it.
 * Every reason is saved so TrustCheck can show "Why was this flagged?"
 */
public class CheckResult {

    private String checkName;            // e.g. "AI Assistant Guard"
    private int score;                   // total risk points from this check
    private ArrayList<String> reasons;   // one line per thing that was found

    public CheckResult(String checkName) {
        this.checkName = checkName;
        this.score = 0;
        this.reasons = new ArrayList<String>();
    }

    // Add risk points and say why. Use 0 points for a note that isn't a risk.
    public void addPoints(int points, String reason) {
        score = score + points;
        reasons.add("+" + points + "  " + reason);
    }

    public String getCheckName() { return checkName; }
    public int getScore() { return score; }
    public ArrayList<String> getReasons() { return reasons; }

    public String toString() {
        String text = checkName + ": " + score + " points";
        for (int i = 0; i < reasons.size(); i++) {
            text = text + "\n    " + reasons.get(i);
        }
        return text;
    }
}