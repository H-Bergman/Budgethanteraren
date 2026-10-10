import java.time.LocalDate;

public record Transaction(LocalDate date, TransactionType type, String category, double amount) {
}
