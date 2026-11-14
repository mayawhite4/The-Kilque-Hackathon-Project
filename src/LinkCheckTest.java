import java.util.ArrayList;

/**
 * TEAM 2 tester for Step 4 - run this to test LinkCheck.
 * Expected: M007, M008 and M009 get points; all LEGIT messages stay at 0.
 */
public class LinkCheckTest {
    public static void main(String[] args) throws Exception {
        LinkCheck links = new LinkCheck();

        // Quick checks of the helper methods
        System.out.println("getEmailDomain: " + links.getEmailDomain("jsmith@ncat.edu") + "  (should be ncat.edu)");
        System.out.println("getLinkDomain:  " + links.getLinkDomain("http://rnybank-secure.com/login") + "  (should be rnybank-secure.com)");
        System.out.println("normalize:      " + links.normalize("rnybank-secure.com") + "  (should be mybanksecure.com)");
        System.out.println("lookalike rnybank.com: " + links.findLookalikeBrand("rnybank.com") + "  (should be mybank.com)");
        System.out.println("lookalike ncat.edu:    " + links.findLookalikeBrand("ncat.edu") + "  (should be null)");
        System.out.println();

        ArrayList<Message> messages = MessageLoader.load("data/Message.txt");
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            System.out.println(m.getId() + " (" + m.getLabel() + ")");
            System.out.println("  " + links.check(m));
        }
    }
}