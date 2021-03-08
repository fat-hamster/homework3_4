package FirstTask;

public class Lock {
    private volatile String task = "A";

    public synchronized void printA() {
        synchronized (task) {
            for (int i = 0; i < 5; i++) {
                while (!"A".equals(task)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(task);
                task = "B";
                notifyAll();
            }
        }
    }

    public synchronized void printB() {
        synchronized (task) {
            for (int i = 0; i < 5; i++) {
                while (!"B".equals(task)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(task);
                task = "C";
                notifyAll();
            }
        }
    }

    public synchronized void printC() {
        synchronized (task) {
            for (int i = 0; i < 5; i++) {
                while (!"C".equals(task)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(task);
                task = "A";
                notifyAll();
            }
        }
    }
}
