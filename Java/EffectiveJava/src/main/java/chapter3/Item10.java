package chapter3;

/**
 * 第10条：覆盖equals时请遵守通用约定
 *
 * 类的每个实例本质上都是唯一的
 * 类没有必要提供“逻辑相等”的测试功能
 * 超类已经覆盖equals，超类的行为对于这个类也是合适的
 * 类是私有的，或者包级别私有的，可以确定它的equals方法永远不会被调用
 *
 * 什么时候应该覆盖equals方法呢？
 * 逻辑相等logical equality概念，值类型value class
 *
 * equals方法实现了等价关系 equivalence relation
 *  自反性 reflexive
 *  对称性 symmetric
 *  传递性 transitive
 *  一致性 consistent
 *  对于任何非null的引用值x，x.equals(null)必须返回false
 *
 * 实现高质量equals方法的诀窍
 *  使用==操作符检查“参数是否为这个对象的引用”
 *  使用instanceof操作符检查“参数是否为正确的类型”
 *  把参数转换成正确的类型
 *  对于该类中的每个“关键”significant域，检查参数中的域是否与该对象中对应的域相匹配
 *
 */
public class Item10 {
}
