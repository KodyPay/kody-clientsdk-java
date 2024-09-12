package cli;

/**
 * Command interface
 */
public interface PaymentCommand extends Command {
    /**
     * Let's do something...
     * This is where the magic happens.
     */
    void execute();

    void gatherInput();
}
