import java.util.ArrayList;
import java.util.function.Predicate;

public class Repository<T> {
    private final ArrayList<T> list = new ArrayList<>();

    public void add(T item) {
        throw new UnsupportedOperationException("TODO");
    }

    public ArrayList<T> findAll() {
        throw new UnsupportedOperationException("TODO");
    }

    public ArrayList<T> findWhere(Predicate<T> predicate) {
        throw new UnsupportedOperationException("TODO");
    }

}
