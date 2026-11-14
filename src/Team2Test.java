import java.util.ArrayList;

/* TEAM 2 tester - run this instead of Main while building AIGuardCheck.
Expected: M010 gets a high score; M011's SSN and student ID are redacted.*/
public class Team2Test {
    public static void main(String[] args) throws Exception {
        ArrayList<Message> messages = MessageLoader.load("Message.txt");
        AIGuardCheck guard = new AIGuardCheck();

        // Quick checks of the helper methods
        System.out.println("isSSN(\"123-45-6789\") should be true:  " + guard.isSSN("123-45-6789"));
        System.out.println("isSSN(\"12-345-6789\") should be false: " + guard.isSSN("12-345-6789"));
        System.out.println("isStudentID(\"A01234567\") should be true: " + guard.isStudentID("A01234567"));
        System.out.println();

        // Run the guard on every message
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            System.out.println(m.getId() + " (" + m.getLabel() + ")");
            System.out.println("  " + guard.check(m));
            System.out.println("  Redacted body: " + guard.redact(m.getBody()));
        }
    }
}
