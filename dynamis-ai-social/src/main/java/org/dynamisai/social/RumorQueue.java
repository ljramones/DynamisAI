package org.dynamisai.social;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RumorQueue {

    private final Queue<Rumor> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(Rumor rumor) {
        queue.add(rumor);
    }

    public List<Rumor> drain() {
        java.util.ArrayList<Rumor> out = new java.util.ArrayList<>();
        Rumor r;
        while ((r = queue.poll()) != null) {
            out.add(r);
        }
        return List.copyOf(out);
    }

    public List<Rumor> peek() {
        return List.copyOf(queue);
    }

    public int size() {
        return queue.size();
    }
}
