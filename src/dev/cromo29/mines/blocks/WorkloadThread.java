package dev.cromo29.mines.blocks;

import java.util.ArrayDeque;
import java.util.Deque;

public class WorkloadThread implements Runnable {

    private static final int MAX_MS_PER_TICK = 5;

    private final Deque<Workdload> workdloadDeque;

    public WorkloadThread() {
        this.workdloadDeque = new ArrayDeque<>();
    }

    @Override
    public void run() {
        long stopTime = System.currentTimeMillis() + MAX_MS_PER_TICK;

        while (!workdloadDeque.isEmpty() && System.currentTimeMillis() <= stopTime) {
            Workdload poll = workdloadDeque.poll();

            if (poll != null) poll.compute();
        }
    }

    public void addLoad(Workdload workdload) {

        if (workdload != null) workdloadDeque.add(workdload);
    }

    public boolean hasEnded() {
        return workdloadDeque.isEmpty();
    }
}
