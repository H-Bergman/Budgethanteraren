import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Summary {

    private static final Logger log = LoggerFactory.getLogger(Summary.class);
    private final List<Transaction> transactions;

    public Summary(List<Transaction> transactions) {
        this.transactions = List.copyOf(transactions);
    }

    public double balance() {
        double result = transactions.stream()
                .mapToDouble(Summary::signedAmount)
                .sum();
        log.debug("Balance calculated for {} transactions: {}", transactions.size(), result);
        return result;
    }

    public Map<String, Double> sumPerCategory() {
        Map<String, Double> result = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::category,
                        Collectors.summingDouble(Summary::signedAmount)
                ));
        log.debug("Summary calculated for {} transactions in {} categories", transactions.size(), result.size());
        return result;
    }

    private static double signedAmount(Transaction t) {
        return t.type() == TransactionType.INCOME ? t.amount() : -t.amount();
    }
}
