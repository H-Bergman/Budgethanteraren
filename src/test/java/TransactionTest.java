import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final LocalDate DATE = LocalDate.of(2020, 6, 23);


    @Test
    void newTransaction_validValues_keepsFields() {
        // Arrange: DATE constant

        // Act
        Transaction transaction = new Transaction(DATE, TransactionType.INCOME, "Salary", 25000);

        // Assert
        assertEquals(DATE, transaction.date());
        assertEquals(TransactionType.INCOME, transaction.type());
        assertEquals("Salary", transaction.category());
        assertEquals(25000, transaction.amount());
    }

    @Test
    void newTransaction_smallestValidAmount_keepsFields() {
        // Arrange: DATE constant

        // Act
        Transaction transaction = new Transaction(DATE, TransactionType.INCOME, "Salary", 0.01);

        // Assert
        assertEquals(DATE, transaction.date());
        assertEquals(TransactionType.INCOME, transaction.type());
        assertEquals("Salary", transaction.category());
        assertEquals(0.01, transaction.amount());
    }

    @Test
    void newTransaction_amountZero_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, "Salary", 0));
    }

    @Test
    void newTransaction_amountNegative_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, "Salary", -1));

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void newTransaction_amountNaN_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, "Salary", Double.NaN));
    }

    @Test
    void newTransaction_amountInfinity_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, "Salary", Double.POSITIVE_INFINITY));
    }

    @Test
    void newTransaction_nullDate_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(null, TransactionType.INCOME, "Salary", 5));
    }

    @Test
    void newTransaction_nullType_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, null, "Salary", 5));
    }

    @Test
    void newTransaction_nullCategory_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act + Assert
        assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, null, 5));
    }

    @Test
    void newTransaction_blankCategory_throwsInvalidTransactionException() {
        // Arrange: DATE constant

        // Act
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class, () -> new Transaction(DATE, TransactionType.INCOME, " ", 5));

        // Assert
        assertEquals("Category must not be blank", exception.getMessage());
    }
}