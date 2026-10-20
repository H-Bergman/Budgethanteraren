import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SummaryTest {

    private static final LocalDate DATE = LocalDate.of(2020, 6, 23);
    private static final double DELTA = 0.001;

    private static Transaction income(String category, double amount) {
        return new Transaction(DATE, TransactionType.INCOME, category, amount);
    }

    private static Transaction expense(String category, double amount) {
        return new Transaction(DATE, TransactionType.EXPENSE, category, amount);
    }

    @Test
    void balance_emptyList_returnsZero() {
        // Arrange
        Summary summary = new Summary(List.of());

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(0, balance, DELTA);
    }

    @Test
    void balance_onlyIncome_returnsSumOfIncome() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Income", 100),
                income("Income", 50)
        ));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(150.0, balance, DELTA);
    }

    @Test
    void balance_onlyExpenses_returnsNegativeSum() {
        // Arrange
        Summary summary = new Summary(List.of(
                expense("Expense", 100),
                expense("Expense", 50)
        ));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(-150.0, balance, DELTA);
    }

    @Test
    void balance_incomeAndExpenses_returnsIncomeMinusExpenses() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Income", 1000),
                expense("Expense", 500)
        ));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(500.0, balance, DELTA);
    }

    @Test
    void balance_expensesLargerThanIncome_returnsNegativeBalance() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Income", 100),
                expense("Expense", 500)
        ));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(-400.0, balance, DELTA);
    }

    @Test
    void balance_decimalAmounts_returnsCorrectSum() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Income", 0.1),
                income("Income", 0.2)
        ));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(0.3, balance, DELTA);
    }

    @Test
    void sumPerCategory_emptyList_returnsEmptyMap() {
        // Arrange
        Summary summary = new Summary(List.of());

        // Act
        Map<String, Double> sums = summary.sumPerCategory();

        // Assert
        assertEquals(Map.of(), sums);
    }

    @Test
    void sumPerCategory_oneCategory_returnsOneEntry() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Food", 100),
                income("Food", 75)
        ));

        // Act
        Map<String, Double> sums = summary.sumPerCategory();

        // Assert
        assertEquals(1, sums.size());
        assertEquals(175.0, sums.get("Food"), DELTA);
    }

    @Test
    void sumPerCategory_severalTransactionsInSameCategory_sumsThem() {
        // Arrange
        Summary summary = new Summary(List.of(
                expense("Food", 100),
                expense("Food", 75)
        ));

        // Act
        Map<String, Double> sums = summary.sumPerCategory();

        // Assert
        assertEquals(-175.0, sums.get("Food"), DELTA);
    }

    @Test
    void sumPerCategory_severalCategories_returnsOneEntryPerCategory() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Salary", 1000),
                expense("Food", 150),
                expense("Rent", 500)
        ));

        // Act
        Map<String, Double> sums = summary.sumPerCategory();

        // Assert
        assertEquals(3, sums.size());
        assertEquals(1000.0, sums.get("Salary"), DELTA);
        assertEquals(-150.0, sums.get("Food"), DELTA);
        assertEquals(-500.0, sums.get("Rent"), DELTA);
    }

    @Test
    void sumPerCategory_incomeAndExpenseInSameCategory_nets() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Other", 100),
                expense("Other", 50)
        ));

        // Act
        Map<String, Double> sums = summary.sumPerCategory();

        // Assert
        assertEquals(50.0, sums.get("Other"), DELTA);
    }

    @Test
    void sumPerCategory_valuesAddUpToBalance() {
        // Arrange
        Summary summary = new Summary(List.of(
                income("Salary", 1000),
                expense("Rent", 500),
                expense("Food", 100),
                income("Swish", 75),
                expense("Activity", 200),
                expense("Food", 75)
        ));

        // Act
        double sumOfCategories = summary.sumPerCategory().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // Assert
        assertEquals(200.0, sumOfCategories, DELTA);
        assertEquals(summary.balance(), sumOfCategories, DELTA);
    }

    @Test
    void newSummary_nullList_throwsNullPointerException() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new Summary(null));
    }

    @Test
    void newSummary_listChangedAfterwards_doesNotAffectResult() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(income("Income", 100));
        Summary summary = new Summary(transactions);
        transactions.add(income("Income", 200));

        // Act
        double balance = summary.balance();

        // Assert
        assertEquals(100.0, balance, DELTA);
    }
}