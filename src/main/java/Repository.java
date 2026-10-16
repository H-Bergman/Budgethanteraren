import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Repository<T> {
    private final ArrayList<T> list = new ArrayList<>();

    public void add(T item) {
        Objects.requireNonNull(item, "Item cannot be null");
        list.add(item);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(list);
    }

    public ArrayList<T> findWhere(Predicate<T> predicate) {
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
