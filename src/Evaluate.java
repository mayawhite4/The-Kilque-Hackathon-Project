import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * STEP 6 - Validation: run all 120 test messages through TrustCheck automatically
 * (no y/n questions) and compare it with a simple keyword filter.
 *
 * "Flagged" means TrustCheck rated the message MEDIUM or HIGH.
 *
 * Numbers we report (these go on the Results slide):
 *   Detection rate      = attacks flagged / all attacks         (higher is better)
 *   False-positive rate = legit messages flagged / all legit    (lower is better)
 *   Fairness check      = false positives on casual / typo-filled messages
 *
 * Message ID prefixes in TestSet.txt:
 *   L = normal legit, LC = casual/typo legit, LU = urgent-but-real legit,
 *   I = impersonation, P = phishing, J = prompt injection
 */
public class Evaluate {

    // Change these if your files are somewhere else
    static final String TEST_FILE = "TestSet.txt";
    static final String HISTORY_FILE = "TestHistory.txt";
    static final String REPORT_FILE = "evaluation_report.txt";

    public static void main(String[] args) {
        ArrayList<Message> tests;
        ArrayList<Message> history;
        try {
            tests = MessageLoader.load(TEST_FILE);
            history = MessageLoader.load(HISTORY_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find a test file: " + e.getMessage());
            return;
        }

        TrustCheck trustCheck = new TrustCheck(history);
        KeywordFilter keywords = new KeywordFilter();

        // ---- Counters: [0] = TrustCheck, [1] = keyword filter ----
        int legitTotal = 0;
        int[] legitFlagged = new int[2];
        int casualTotal = 0;
        int[] casualFlagged = new int[2];
        int urgentLegitTotal = 0;
        int[] urgentLegitFlagged = new int[2];

        String[] attackTypes = {"IMPERSONATION", "PHISHING", "INJECTION"};
        int[] attackTotal = new int[3];
        int[][] attackCaught = new int[3][2];   // [attack type][which tool]

        int lowCount = 0;
        int mediumCount = 0;
        int highCount = 0;

        ArrayList<String> missed = new ArrayList<String>();
        ArrayList<String> falseAlarms = new ArrayList<String>();

        // ---- Run every test message through both tools ----
        for (int i = 0; i < tests.size(); i++) {
            Message m = tests.get(i);
            int score = trustCheck.totalScore(trustCheck.analyze(m));
            String level = trustCheck.riskLevel(score);

            boolean tcFlag = !level.equals("LOW");
            boolean kwFlag = keywords.isFlagged(m);

            if (level.equals("LOW")) {
                lowCount++;
            } else if (level.equals("MEDIUM")) {
                mediumCount++;
            } else {
                highCount++;
            }

            if (m.getLabel().equals("LEGIT")) {
                legitTotal++;
                if (tcFlag) {
                    legitFlagged[0]++;
                    falseAlarms.add(m.getId() + " (" + score + " pts) " + m.getSubject());
                }
                if (kwFlag) {
                    legitFlagged[1]++;
                }

                // Fairness subgroup: casual / typo-filled writing
                if (m.getId().startsWith("LC")) {
                    casualTotal++;
                    if (tcFlag) casualFlagged[0]++;
                    if (kwFlag) casualFlagged[1]++;
                }
                // Hard subgroup: real messages that sound urgent
                if (m.getId().startsWith("LU")) {
                    urgentLegitTotal++;
                    if (tcFlag) urgentLegitFlagged[0]++;
                    if (kwFlag) urgentLegitFlagged[1]++;
                }
            } else {
                // It's an attack - find which type
                for (int t = 0; t < attackTypes.length; t++) {
                    if (m.getLabel().equals(attackTypes[t])) {
                        attackTotal[t]++;
                        if (tcFlag) attackCaught[t][0]++;
                        if (kwFlag) attackCaught[t][1]++;
                    }
                }
                if (!tcFlag) {
                    missed.add(m.getId() + " " + m.getLabel() + " (" + score + " pts) " + m.getSubject());
                }
            }
        }

        // ---- Totals across all attack types ----
        int allAttacks = 0;
        int[] allCaught = new int[2];
        for (int t = 0; t < 3; t++) {
            allAttacks += attackTotal[t];
            allCaught[0] += attackCaught[t][0];
            allCaught[1] += attackCaught[t][1];
        }

        // ---- Build the report ----
        ArrayList<String> report = new ArrayList<String>();
        report.add("================ TrustCheck Validation Report ================");
        report.add("Test set: " + tests.size() + " synthetic messages ("
                + legitTotal + " legit, " + allAttacks + " attacks)");
        report.add("");
        report.add(String.format("%-34s %12s %16s", "", "TrustCheck", "Keyword filter"));
        report.add(String.format("%-34s %12s %16s", "Detection rate (all attacks)",
                pct(allCaught[0], allAttacks), pct(allCaught[1], allAttacks)));
        for (int t = 0; t < 3; t++) {
            report.add(String.format("%-34s %12s %16s", "   " + attackTypes[t].toLowerCase(),
                    pct(attackCaught[t][0], attackTotal[t]), pct(attackCaught[t][1], attackTotal[t])));
        }
        report.add(String.format("%-34s %12s %16s", "False-positive rate (all legit)",
                pct(legitFlagged[0], legitTotal), pct(legitFlagged[1], legitTotal)));
        report.add(String.format("%-34s %12s %16s", "   urgent-but-real messages",
                pct(urgentLegitFlagged[0], urgentLegitTotal), pct(urgentLegitFlagged[1], urgentLegitTotal)));
        report.add(String.format("%-34s %12s %16s", "Fairness: casual/typo flagged",
                pct(casualFlagged[0], casualTotal), pct(casualFlagged[1], casualTotal)));
        report.add("");
        report.add("TrustCheck risk levels: " + lowCount + " LOW, " + mediumCount + " MEDIUM, " + highCount + " HIGH");
        report.add("");
        report.add("Attacks TrustCheck MISSED (" + missed.size() + ") - use these for the Limitations slide:");
        for (int i = 0; i < missed.size(); i++) {
            report.add("   " + missed.get(i));
        }
        report.add("");
        report.add("Legit messages TrustCheck wrongly flagged (" + falseAlarms.size() + "):");
        for (int i = 0; i < falseAlarms.size(); i++) {
            report.add("   " + falseAlarms.get(i));
        }

        // ---- Print it AND save it to a file ----
        try {
            PrintWriter out = new PrintWriter(new FileWriter(REPORT_FILE));
            for (int i = 0; i < report.size(); i++) {
                System.out.println(report.get(i));
                out.println(report.get(i));
            }
            out.close();
            System.out.println("\nSaved to " + REPORT_FILE);
        } catch (IOException e) {
            System.out.println("Could not save report: " + e.getMessage());
        }
    }

    /**
     * Turns 45 out of 60 into "75.0% (45/60)".
     */
    static String pct(int part, int whole) {
        if (whole == 0) {
            return "n/a";
        }
        double percent = 100.0 * part / whole;
        return String.format("%.1f%% (%d/%d)", percent, part, whole);
    }
}