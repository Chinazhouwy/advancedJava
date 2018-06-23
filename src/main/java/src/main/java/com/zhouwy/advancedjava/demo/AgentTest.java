package src.main.java.com.zhouwy.advancedjava.demo;

public class AgentTest {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            System.out.println("process result: " + process());
            Thread.sleep(5000);
        }
    }

    public static String process() {
        System.out.println("process!");
        return "success";
    }

}
