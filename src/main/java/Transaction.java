import java.time.LocalDate;

public record Transaction(LocalDate date, TransactionType type, String category, double amount) {
    public Transaction {
        if (!Double.isFinite(amount)) {
            throw new InvalidTransactionException("Amount must be a finite number");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException("Category must not be blank");
        }
        if (date == null) {
            throw new InvalidTransactionException("Date must not be null");
        }
        if (type == null) {
            throw new InvalidTransactionException("Type must not be null");
        }
    }
}
