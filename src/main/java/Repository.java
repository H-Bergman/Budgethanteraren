import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Repository<T> {

    private static final Logger log = LoggerFactory.getLogger(Repository.class);
    private final ArrayList<T> list = new ArrayList<>();

    public void add(T item) {
        Objects.requireNonNull(item, "Item cannot be null");
        list.add(item);
        log.debug("Added item: {}", item);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(list);
    }

    public ArrayList<T> findWhere(Predicate<T> predicate) {
        ArrayList<T> result = list.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
        log.debug("findWhere found {} of {} items", result.size(), list.size());
        return result;
    }
}
