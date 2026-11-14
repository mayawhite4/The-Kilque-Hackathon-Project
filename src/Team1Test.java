import java.util.ArrayList;

/**
 * TEAM 1 tester - run this instead of Main while building ImpersonationCheck.
 * Expected when done: M006 and M007 get high scores, M001-M005 stay low.
 */
public class Team1Test {
    public static void main(String[] args) throws Exception {
        ArrayList<Message> history = MessageLoader.load("data/history.txt");
        ArrayList<Message> messages = MessageLoader.load("data/messages.txt");

        ImpersonationCheck checker = new ImpersonationCheck(history);

        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            System.out.println(m.getId() + " (" + m.getLabel() + ")");
            System.out.println("  " + checker.check(m));
        }
    }
}