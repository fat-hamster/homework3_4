package FirstTask;

public class Lock {
    private volatile String task = "A";

    public synchronized void printA() {
        synchronized (task) {
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

    public synchronized void printB() {
        synchronized (task) {
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

    public synchronized void printC() {
        synchronized (task) {
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
