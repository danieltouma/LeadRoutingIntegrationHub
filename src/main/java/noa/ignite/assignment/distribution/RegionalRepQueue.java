package noa.ignite.assignment.distribution;

import noa.ignite.assignment.util.ValidationUtils;
import noa.ignite.assignment.model.SalesRep;
import noa.ignite.assignment.model.Territory;
import noa.ignite.assignment.model.TerritoryType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionalRepQueue {
    private final Map<TerritoryType, PriorityQueue<SalesRep>> regionQueues = new ConcurrentHashMap<>();

    public RegionalRepQueue() {
        // Pre create queues for all valid territory types
        for (TerritoryType type : TerritoryType.values()) {
            if (type != TerritoryType.UNKNOWN) {
                // We use LastAssignmentTime to determine the "Round Robin" order
                regionQueues.put(type, new PriorityQueue<>(
                        Comparator.comparingLong(SalesRep::getLastAssignmentTime)
                ));
            }
        }
    }

    public synchronized void registerRep(SalesRep rep) {
        if (!ValidationUtils.isValidRep(rep)) return;
        TerritoryType type = rep.getTerritory().getCode();

        PriorityQueue<SalesRep> queue = regionQueues.get(type);
        if (queue != null && !queue.contains(rep)) {
            queue.add(rep);
            System.out.println("Registered " + rep + " for territory type " + type);
        }
    }

    public synchronized void unregisterRep(SalesRep rep) {
        if (!ValidationUtils.isValidRep(rep)) return;
        PriorityQueue<SalesRep> queue = regionQueues.get(rep.getTerritory().getCode());
        if (queue != null && queue.remove(rep)) {
            System.out.println("Unregistered " + rep + " from queue.");
        }
    }

    public synchronized SalesRep findBestAvailableRep(Territory territory) {
        if (territory == null) return null;
        PriorityQueue<SalesRep> queue = regionQueues.get(territory.getCode());
        if (queue == null || queue.isEmpty()) return null;

        List<SalesRep> busyReps = new ArrayList<>();
        SalesRep selected = null;

        // Find the rep who has been waiting the longest AND has capacity
        while (!queue.isEmpty()) {
            SalesRep candidate = queue.poll();
            if (candidate.hasCapacity()) {
                selected = candidate;
                break;
            } else {
                busyReps.add(candidate);
            }
        }

        // Put everyone back so the queue remains complete
        queue.addAll(busyReps);
        if (selected != null) {
            queue.add(selected);
        }

        return selected;
    }

    public synchronized void updateRepPosition(SalesRep rep) {
        if (!ValidationUtils.isValidRep(rep)) return;
        PriorityQueue<SalesRep> queue = regionQueues.get(rep.getTerritory().getCode());

        // PriorityQueue doesn't auto sort when a field changes.
        // We must remove and re add to force a re sort.
        if (queue != null && queue.remove(rep)) {
            queue.add(rep);
        }
    }
}