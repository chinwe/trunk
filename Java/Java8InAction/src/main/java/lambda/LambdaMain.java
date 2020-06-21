package lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LambdaMain {
    public static void main(String[] args) {

        // Lambda
        // (parameters) -> expression
        // (parameters) -> { statements; }

        List<Integer> list = new ArrayList<Integer>();
        list.add(5);
        list.add(9);
        list.add(3);

        list.sort((i, j) -> i.compareTo(j));
        System.out.println(list);

        Runnable r = () -> System.out.println("test runnable");
        r.run();

        // Function
        List<Integer> mapResult = map(list, (i) -> i * 2);
        System.out.println(mapResult);
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<R>();
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }
}
