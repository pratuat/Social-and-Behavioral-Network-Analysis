package user_tweet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManyToManyMap<S, C> {
    private Map<S, Set<C>> firstToSecondMap = new HashMap<>();

    private Map<C, Set<S>> secondToFirstMap = new HashMap<>();

    public void put(S first, C second) {
        if (!firstToSecondMap.containsKey(first)) {
            firstToSecondMap.put(first, new HashSet<>());
        }
        firstToSecondMap.get(first).add(second);

        if (!secondToFirstMap.containsKey(second)) {
            secondToFirstMap.put(second, new HashSet<>());
        }
        secondToFirstMap.get(second).add(first);
    }

    public Set<C> getFirst(S first) {
        return firstToSecondMap.get(first);
    }

    public Set<S> getSecond(C second) {
        return secondToFirstMap.get(second);
    }

    public Set<C> removeByFirst(S first) {
        Set<C> itemsToRemove = firstToSecondMap.remove(first);
        for (C item : itemsToRemove) {
            secondToFirstMap.get(item).remove(first);
        }

        return itemsToRemove;
    }

    public Set<S> removeBySecond(C second) {
        Set<S> itemsToRemove = secondToFirstMap.remove(second);
        for (S item : itemsToRemove) {
            firstToSecondMap.get(item).remove(second);
        }

        return itemsToRemove;
    }
}