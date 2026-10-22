import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final Repository<Transaction> repo = new Repository<>();
    private final FileHandler fileHandler;

    private static final String ROW_FORMAT = "%-12s %-9s %-15s %10s";

    public Main (FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    static void main() {
        FileHandler fh = new FileHandler(Path.of("transactions.csv"));
        Main main = new Main(fh);
        main.run();
    }

    private void run() {
        fileHandler.load().forEach(repo::add);
        while (true) {
            printMenu();
            int choice = getInput(1, 5);
            switch (choice) {
                case 1 -> addTransaction();
                case 2 -> showAll();
                case 3 -> showBalance();
                case 4 -> filter();
                case 5 -> save();
                case 0 -> {
                    if (askYesNo("Save before quitting?")) save();
                    return;
                }
            }
        }
    }

    private void printMenu() {
        IO.println("""
            
            ==============================
              BUDGETHANTERAREN
            ==============================
              1) Add a transaction
              2) Show all transactions
              3) Show balance per category
              4) Filter transactions
              5) Save to file
            
              q) Quit
            ------------------------------""");
        IO.print("> ");
    }

    private void addTransaction() {
        IO.println();
        LocalDate date = readDate("Date");
        TransactionType type = readType();
        String category = readCategory();
        double amount = readAmount();

        try {
            Transaction t = new Transaction(date, type, category, amount);
            repo.add(t);
            log.info("Transaction added: {}", t);
            IO.println("Transaction added");
        } catch (InvalidTransactionException e) {
            log.warn("Failed to add transaction: {}", e.getMessage());
            IO.println("Failed to add transaction: " + e.getMessage());
        }
    }

    private void showAll() {
        printTransactions(repo.findAll());
    }

    private void showBalance() {
        Summary summary = new Summary(repo.findAll());
        IO.println();
        IO.println(String.format(Locale.ROOT, "%-15s %10s", "Balance", formatAmount(summary.balance())));
        IO.println("-".repeat(26));
        IO.println("By category (income +, expenses -):");
        new TreeMap<>(summary.sumPerCategory()).forEach((category, sum) ->
                IO.println(String.format(Locale.ROOT, "%-15s %10s", category, formatAmount(sum))));
    }

    private void filter() {
        IO.println();
        IO.println("""
                Filter by:
                1) Type
                2) Date range
                
                q) Back
                """);
        IO.print("> ");
        int choice = getInput(1, 2);
        try {
            switch (choice) {
                case 1 -> printTransactions(repo.findWhere(TransactionFilters.filterByType(readType())));
                case 2 -> {
                    LocalDate from = readDate("From date");
                    LocalDate to = readDate("To date");
                    printTransactions(repo.findWhere(TransactionFilters.filterBetweenDates(from, to)));
                }
                default -> {}
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid filter: {}", e.getMessage());
            IO.println("Invalid filter: " + e.getMessage());
        }
    }

    private void save() {
        List<Transaction> transactions = repo.findAll();
        try {
            fileHandler.save(transactions);
            IO.println("Saved " + transactions.size() + " transactions.");
        } catch (UncheckedIOException e) {
            log.error("Failed to save transactions", e);
            IO.println("Could not save the file, see logs/budget.log for details.");
        }
    }

    private static void printTransactions(List<Transaction> transactions) {
        IO.println();
        if (transactions.isEmpty()) {
            IO.println("No transactions found");
            return;
        }
        IO.println(String.format(Locale.ROOT, ROW_FORMAT, "Date", "Type", "Category", "Amount"));
        IO.println("-".repeat(49));
        transactions.forEach(Main::printTransaction);
        IO.println("-".repeat(49));
        IO.println(transactions.size() + (transactions.size() == 1 ? " transaction" : " transactions"));
    }

    private static void printTransaction(Transaction t) {
        IO.println(String.format(Locale.ROOT, ROW_FORMAT,
                t.date(), t.type(), t.category(), formatAmount(t.amount())));
    }

    private static String formatAmount(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    // Input loop methods

    private static String readLine() {
        String line = IO.readln();
        return line == null ? "q" : line.trim();
    }

    private static boolean askYesNo(String prompt) {
        while (true) {
            IO.print(prompt + " [y/n] ");
            String line = readLine();
            if (line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes")) return true;
            if (line.equalsIgnoreCase("n") || line.equalsIgnoreCase("no") || line.equalsIgnoreCase("q")) return false;
            IO.println("Answer y/yes or n/no");
        }
    }

    private static String getRequiredInput(String prompt) {
        while (true) {
            IO.print(prompt + ": ");
            String line = readLine();
            if (!line.isBlank()) return line;
            IO.println("The field can't be blank, try again.");
        }
    }

    private static int getInput(int min, int max) {
        while (true) {
            String line = readLine();
            if (line.equalsIgnoreCase("q")) return 0;
            try {
                int input = Integer.parseInt(line);
                if (input >= min && input <= max) return input;
            } catch (NumberFormatException _) {}
            log.warn("Invalid menu input: {}", line);
            IO.println("Invalid menu input, try again.");
            IO.print("> ");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            String line = getRequiredInput(prompt + " (YYYY-MM-DD)");
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException _) {
                log.warn("Invalid date input: {}", line);
                IO.println("Invalid date, use format: YYYY-MM-DD");
            }
        }
    }

    private static double readAmount() {
        while (true) {
            String line = getRequiredInput("Amount");
            try {
                return Double.parseDouble(line.replace(',', '.'));
            } catch (NumberFormatException _) {
                log.warn("Invalid amount input: {}", line);
                IO.println("Invalid amount, use e.g. 50 or 49,90");
            }
        }
    }

    private static String readCategory() {
        while (true) {
            String line = getRequiredInput("Category");
            if (!line.contains(",")) return line;
            log.warn("Category contained a comma: {}", line);
            IO.println("The category cannot contain a comma, try again.");
        }
    }

    private static TransactionType readType() {
        while (true) {
            IO.print("Type (1 = Income, 2 = Expense): ");
            String line = readLine();
            if (line.equals("1")) return TransactionType.INCOME;
            if (line.equals("2")) return TransactionType.EXPENSE;
            log.warn("Invalid type input: {}", line);
            IO.println("Invalid type input, use either 1 or 2");
        }
    }
}
