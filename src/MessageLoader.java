import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reads messages from a text file. Each line is one message,
 * with fields separated by the | character (so commas in the body are fine).
 * The first line is a header and gets skipped.
 */
public class MessageLoader {

    public static ArrayList<Message> load(String fileName) throws FileNotFoundException {
        ArrayList<Message> messages = new ArrayList<Message>();
        Scanner reader = new Scanner(new File(fileName));

        // Skip the header line
        if (reader.hasNextLine()) {
            reader.nextLine();
        }

        while (reader.hasNextLine()) {
            String line = reader.nextLine();

            // Skip blank lines
            if (line.trim().length() == 0) {
                continue;
            }

            // Skip any header line, even if it's not the first line
            if (line.startsWith("id|")) {
                continue;
            }

            // "\\|" means split on the | character
            String[] parts = line.split("\\|");

            if (parts.length != 8) {
                System.out.println("Skipping bad line: " + line);
                continue;
            }

            int hour = Integer.parseInt(parts[4].trim());
            Message m = new Message(parts[0], parts[1], parts[2], parts[3],
                    hour, parts[5], parts[6], parts[7].trim());
            messages.add(m);
        }

        reader.close();
        return messages;
    }
}