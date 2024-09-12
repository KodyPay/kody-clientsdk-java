package cli;

import ecom.EcomAsyncJavaClient;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import terminal.TerminalJavaClient;

public class KodyDemo {
    public static void main(String[] args) {
        LineReader reader = LineReaderBuilder.builder()
                .completer(new StringsCompleter("1", "2", "3", "4", "5", "99", "exit", "quit", "q", "x"))
                .build();

        while (true) {
            try {
                String line = reader.readLine("\n=== Main Menu ===\n" +
                        " 1. Get Ecom payments (async)\n" +
                        " 2. Send an Ecom payment (async)\n" +
                        " 3. List terminals (blocking)\n" +
                        " 4. Send a terminal payment (blocking)\n" +
                        " 5. Get terminal payment details (blocking)\n" +
                        "99. Exit\n" +
                        "Choose an option: ");

                switch (line.toLowerCase()) {
                    case "1":
                        new EcomAsyncJavaClient.GetPaymentsCommand().execute();
                        break;
                    case "2":
                        new EcomAsyncJavaClient.SendPaymentCommand().gatherInput();
                        new EcomAsyncJavaClient.SendPaymentCommand().execute();
                        break;
                    case "3":
                        new TerminalJavaClient.GetTerminalsCommand().execute();
                        break;
                    case "4":
                        new TerminalJavaClient.SendPaymentCommand().gatherInput();
                        new TerminalJavaClient.SendPaymentCommand().execute();
                        break;
                    case "5":
                        new TerminalJavaClient.GetPaymentDetailsCommand().execute();
                        break;
                    case "99":
                    case "exit":
                    case "quit":
                    case "q":
                    case "x":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (UserInterruptException e) {
                System.out.println("Interrupted! Exiting...");
                break;
            }
        }
    }
}
