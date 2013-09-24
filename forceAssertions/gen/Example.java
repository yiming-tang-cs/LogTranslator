
public class Example {
    
    public Example() {
        super();
    }
    
    public static void main(String[] args) {
        String str = null;
        if (!(str != null)) throw new AssertionError("Must not be null");
    }
}
