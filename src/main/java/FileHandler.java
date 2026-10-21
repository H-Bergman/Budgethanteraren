import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);
    private final Path filePath;

    public FileHandler(Path filePath) {
        Objects.requireNonNull(filePath, "filePath cannot be null");
        this.filePath = filePath;
    }

    public List<Transaction> load() {
        ensureFileExists();
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                try {
                    transactions.add(parseLine(line));
                } catch (FileFormatException e) {
                    log.warn("Skipping line {}: {}", lineNum, e.getMessage());
                }
            }
            log.info("Loaded {} transactions from {}", transactions.size(), filePath);
        } catch (IOException e) {
            log.error("Error while reading file {}", filePath, e);
        }
        return transactions;
    }

    public void save(List<Transaction> transactions) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Transaction t : transactions) {
                bw.write(t.date() + "," + t.type() + "," + t.category() + "," + t.amount());
                bw.newLine();
            }
            log.info("Saved {} transactions to {}", transactions.size(), filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save transactions to " + filePath, e);
        }
    }

    Transaction parseLine(String line) {
        log.debug("Parsing line: {}", line);
        String[] parts = line.split(",");
        LocalDate date;
        TransactionType type;
        String category;
        double amount;

        if (parts.length != 3) {
            throw new FileFormatException("Invalid line format: " +  line);
        }

        try {
            date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new FileFormatException("Invalid date format: " + parts[0]);
        }

        try {
            type = TransactionType.valueOf(parts[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new FileFormatException("Invalid transaction type: " + parts[1]);
        }

        if (parts[2] != null && parts[2].matches("^\\s*\\w+\\s*$")) {
            category = parts[2].trim();
        } else {
            throw new FileFormatException("Invalid category: " + parts[2]);
        }

        try {
            amount = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            throw new FileFormatException("Invalid transaction amount: " + parts[2]);
        }

        try {
            return new Transaction(date, type, category, amount);
        } catch (InvalidTransactionException e) {
            throw new FileFormatException("Invalid transaction: " + e.getMessage());
        }
    }

    void ensureFileExists() {
        try {
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
                log.info("Created new file {}", filePath.toAbsolutePath());
            } else {
                log.debug("File {} already exists!", filePath);
            }
        } catch (IOException e) {
            log.error("Could not create file {}", filePath, e);
        }
    }
}
