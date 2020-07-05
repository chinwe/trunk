package defaultmethod;

interface IData {
    default boolean Empty() {
        return false;
    }
}

class TestData implements IData {
}
public class DefaultMain {
    public static void main(String[] args) {
        TestData testData = new TestData();
        testData.Empty();
    }
}
