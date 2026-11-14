import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * TrustCheck - Step 1: load the test messages and print them.
 * Later steps will run each message through the three checks.
 */
public class Main {

    public static void main(String[] args) {
        ArrayList<Message> messages;

        try {
            messages = MessageLoader.load("Message.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find data/messages.txt");
            System.out.println("Make sure the program runs from the project's main folder.");
            return;
        }

        System.out.println("Loaded " + messages.size() + " messages:\n");

        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            System.out.println(m.toString());
            System.out.println("   Label: " + m.getLabel());
        }
    }
}