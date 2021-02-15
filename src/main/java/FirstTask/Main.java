package FirstTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        Lock lockSample = new Lock();
        ExecutorService service = Executors.newFixedThreadPool(3);

        service.submit(new Thread(lockSample::printA));
        service.submit(new Thread(lockSample::printB));
        service.submit(new Thread(lockSample::printC));

        service.shutdown();
    }
}
