import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private Repository<String> repo;

    @BeforeEach
    void setUp() {
        repo = new Repository<>();
    }

    @Test
    void findAll_onEmptyRepository_returnsEmptyList() {
        // Arrange: empty repo from setUp

        // Act
        List<String> result = repo.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void add_null_throwsNullPointerException() {
        // Arrange: empty repo from setUp

        // Act + Assert
        assertThrows(NullPointerException.class, () -> repo.add(null));
    }

    @Test
    void add_oneItem_isReturnedByFindAll() {
        // Arrange
        repo.add("one");

        // Act
        List<String> result = repo.findAll();

        // Assert
        assertEquals(List.of("one"), result);
    }

    @Test
    void add_severalItems_returnsSeveralItems() {
        // Arrange
        repo.add("one");
        repo.add("two");
        repo.add("three");

        // Act
        List<String> result = repo.findAll();

        // Assert
        assertEquals(List.of("one", "two", "three"), result);
    }

    @Test
    void findWhere_someItemsMatch_returnsOnlyMatchingItems() {
        // Arrange
        repo.add("one");
        repo.add("two");
        repo.add("three");
        repo.add("four");

        // Act
        List<String> result = repo.findWhere(t -> t.startsWith("t"));

        // Assert
        assertEquals(List.of("two", "three"), result);
    }

    @Test
    void findWhere_noItemsMatch_returnsEmptyList() {
        // Arrange
        repo.add("one");
        repo.add("two");
        repo.add("three");

        // Act
        List<String> result = repo.findWhere(t -> t.startsWith("h"));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findWhere_onEmptyRepository_returnsEmptyList() {
        // Arrange: empty repo from setUp

        // Act
        List<String> result = repo.findWhere(t -> t.startsWith("t"));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_returnedList_isACopyOfTheRepositoryList() {
        // Arrange
        repo.add("one");
        List<String> result = repo.findAll();

        // Act
        result.add("two");

        // Assert
        assertEquals(List.of("one"), repo.findAll());
    }
}