package chapter3;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingInt;

/**
 * 第14条：考虑实现Comparable接口
 *
 *
 *
 */
public class Item14 {

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    static class SortCondition implements Comparable<SortCondition> {
        private int time;
        private int seq;

        // Comparator
        private static final Comparator<SortCondition> COMPARATOR = comparingInt((SortCondition sc) -> sc.time)
                .thenComparingInt(sc -> sc.seq);

        @Override
        public int compareTo(SortCondition o) {
            return COMPARATOR.compare(this, o);
        }
    }

    public static void main(String[] args) {
        List<SortCondition> sortConditionList = Lists.newArrayList(
                new SortCondition(2, 1),
                new SortCondition(1, 2),
                new SortCondition(1, 3)
        );

        sortConditionList.sort(SortCondition::compareTo);
        System.out.println(sortConditionList);
    }

}
