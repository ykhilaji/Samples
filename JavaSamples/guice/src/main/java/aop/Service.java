package aop;

public class Service {
    @AopAnnotation
    public void doAction() {
        System.out.println("Inside service");
    }
}
