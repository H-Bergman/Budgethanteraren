import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @TempDir
    Path tempDir;

    private Path file;
    private FileHandler fileHandler;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("transactions.csv");
        fileHandler = new FileHandler(file);
    }

    @Test
    void load_missingFile_createsFileAndReturnsEmptyList() {
        // Arrange: file (does not exist yet) and fileHandler from setUp

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertTrue(result.isEmpty());
        assertTrue(Files.exists(file));
    }

    @Test
    void load_emptyFile_returnsEmptyList() throws IOException {
        // Arrange
        Files.writeString(file, "");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void load_fileWithValidLines_returnsTransactionsInOrder() throws IOException {
        // Arrange
        Files.writeString(file, "2026-03-12,INCOME,Salary,100\n2026-03-13,EXPENSE,Food,50\n");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertEquals(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 50)
        ), result);
    }

    @Test
    void load_brokenLineBetweenValidLines_skipsOnlyTheBrokenLine() throws IOException {
        // Arrange
        Files.writeString(file, "2026-03-12,INCOME,Salary,100\nnot a valid line\n2026-03-13,EXPENSE,Food,50");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertEquals(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 50)
        ), result);
    }

    @Test
    void load_onlyBrokenLines_returnsEmptyList() throws IOException {
        // Arrange
        Files.writeString(file, "Rent, 500\n2026-03-12, INCOME");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void load_blankLines_areSkipped() throws IOException {
        // Arrange
        Files.writeString(file, """
                2026-03-12,INCOME,Salary,100
                \s
                2026-03-13,EXPENSE,Food,50""");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertEquals(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 50)
        ), result);
    }

    @Test
    void load_lowercaseType_isAccepted() throws IOException {
        // Arrange
        Files.writeString(file, "2026-03-12,income,Salary,100");

        // Act
        List<Transaction> result = fileHandler.load();

        // Assert
        assertEquals(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100)
        ), result);
    }

    @Test
    void parseLine_validLine_returnsTransaction() {
        // Arrange
        String validLine = "2026-03-12,INCOME,Salary,100";

        // Act
        Transaction result = fileHandler.parseLine(validLine);

        // Assert
        assertEquals(new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100), result);
    }

    @Test
    void parseLine_tooFewFields_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_tooManyFields_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME,Salary,100,Work";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_missingAmount_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME,Salary,";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_trailingCommaAfterAmount_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME,Salary,100,";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_badDate_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-13-45,INCOME,Salary,100";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_unknownType_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,SAVINGS,Salary,100";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_nonNumericAmount_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME,Salary,abc";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_negativeAmount_throwsFileFormatException() {
        // Arrange
        String invalidLine = "2026-03-12,INCOME,Salary,-50";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void parseLine_categoryWithSwedishLetters_returnsTransaction() {
        // Arrange
        String validLine = "2026-03-12,EXPENSE,Nöje,50";

        // Act
        Transaction result = fileHandler.parseLine(validLine);

        // Assert
        assertEquals("Nöje", result.category());
    }

    @Test
    void parseLine_surroundingWhitespace_throwsFileFormatException() {
        // Arrange
        String invalidLine = " 2026-03-12 , INCOME , Salary , 50 ";

        // Act + Assert
        assertThrows(FileFormatException.class, () -> fileHandler.parseLine(invalidLine));
    }

    @Test
    void save_transactions_writesOneLinePerTransaction() throws IOException {
        // Arrange
        List<Transaction> transactions = new ArrayList<>(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 50.5)
        ));

        // Act
        fileHandler.save(transactions);

        // Assert
        List<String> lines = Files.readAllLines(file);
        assertEquals(List.of(
                "2026-03-12,INCOME,Salary,100.0",
                "2026-03-13,EXPENSE,Food,50.5"
        ), lines);
    }

    @Test
    void save_emptyList_producesEmptyFile() throws IOException {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();

        // Act
        fileHandler.save(transactions);

        // Assert
        assertTrue(Files.exists(file));
        List<String> lines = Files.readAllLines(file);
        assertEquals(0, lines.size());
    }

    @Test
    void save_calledTwice_overwritesTheFile() throws IOException {
        // Arrange
        fileHandler.save(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 50)
        ));

        // Act
        fileHandler.save(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100)
        ));

        // Assert
        assertEquals(List.of("2026-03-12,INCOME,Salary,100.0"), Files.readAllLines(file));
    }

    @Test
    void save_unwritablePath_throwsUncheckedIOException() {
        // Arrange
        Path missingFolderFile = tempDir.resolve("missing/folder/file.csv");
        FileHandler badFileHandler = new FileHandler(missingFolderFile);
        List<Transaction> transactions = new ArrayList<>();

        // Act + Assert
        assertThrows(UncheckedIOException.class, () -> badFileHandler.save(transactions));
    }

    @Test
    void saveThenLoad_returnsSameTransactions() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>(List.of(
                new Transaction(LocalDate.of(2026, 3, 12), TransactionType.INCOME, "Salary", 100),
                new Transaction(LocalDate.of(2026, 3, 13), TransactionType.EXPENSE, "Food", 49.9),
                new Transaction(LocalDate.of(2026, 3, 17), TransactionType.INCOME, "Swish", 30),
                new Transaction(LocalDate.of(2026, 4, 1), TransactionType.EXPENSE, "Grocery", 80),
                new Transaction(LocalDate.of(2026, 4, 23), TransactionType.EXPENSE, "Netflix", 120)
        ));

        // Act
        fileHandler.save(transactions);
        List<Transaction> loadedTransactions = fileHandler.load();

        // Assert
        assertEquals(transactions, loadedTransactions);
    }
}