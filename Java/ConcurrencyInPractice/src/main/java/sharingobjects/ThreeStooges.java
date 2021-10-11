package sharingobjects;

import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.HashSet;
import java.util.Set;

/**
 * 不可变对象
 * 它的状态不能在创建后再修改
 * 所有域都是final类型
 * 它被正确创建（创建期间没有发生this引用的逸出）
 *
 * @author chinwe
 * 2021/10/10
 */
@Immutable
public final class ThreeStooges {
    private final Set<String> stooges = new HashSet<>();

    public ThreeStooges() {
        stooges.add("Moe");
        stooges.add("Larry");
        stooges.add("Curly");
    }

    public boolean isStooge(String name) {
        return stooges.contains(name);
    }
}
