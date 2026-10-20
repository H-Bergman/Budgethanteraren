import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Summary {

    private final List<Transaction> transactions;

    public Summary(List<Transaction> transactions) {
        this.transactions = List.copyOf(transactions);
    }

    public double balance() {
        return transactions.stream().mapToDouble(Summary::signedAmount).sum();
    }

    public Map<String, Double> sumPerCategory() {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::category,
                        Collectors.summingDouble(Summary::signedAmount)
                ));
    }

    private static double signedAmount(Transaction t) {
        return t.type() == TransactionType.INCOME ? t.amount() : -t.amount();
    }
}
