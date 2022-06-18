package main;

import java.util.ArrayList;
import java.util.List;

public class Util {
    @SuppressWarnings("unchecked")
    public static <T> List<T> unsafeListCast(List<?> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        list.forEach(x -> result.add((T) x));
        return result;
    }
}
