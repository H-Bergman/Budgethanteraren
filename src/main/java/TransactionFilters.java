import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

public class TransactionFilters {

    public static Predicate<Transaction> filterByType(TransactionType type) {
        Objects.requireNonNull(type, "Type must not be null");
        return t -> t.type() == type;
    }

    public static Predicate<Transaction> filterBetweenDates(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "From date must not be null");
        Objects.requireNonNull(to, "To date must not be null");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after To date");
        }
        return t -> !t.date().isBefore(from) && !t.date().isAfter(to);
    }
}
