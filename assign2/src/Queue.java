import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Queue {
    private ReentrantLock lock = new ReentrantLock();
    private List<Player> queue = new ArrayList<>();

    public void add(Player item) {
        queue.add(item);
    }

    public Player pop() throws IndexOutOfBoundsException {
        return queue.remove(0);
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public List<Player> toList() {
        return new ArrayList<>(queue);
    }

    public Iterator<Player> iterator() {
        return queue.iterator();
    }

    public boolean isInQueue(Player item) {
        return queue.contains(item);
    }

    public void setSocket(Player item, Socket socket) {
        int idx = queue.indexOf(item);
        queue.get(idx).setSocket(socket);
    }

    // list related methods below are related to ranked games
    // making the queue behave less like a queue

    public void sort() {
        queue.sort((a, b) -> a.getRank() - b.getRank());
    }

    public Player get(int idx) {
        return queue.get(idx);
    }

    public Player remove(int idx) {
        return queue.remove(idx);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
