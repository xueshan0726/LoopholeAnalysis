/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools;

import java.util.*;


public class MyCounter<E> {

    HashMap<E, Integer> counter = new HashMap<>();

    public void count(E key) {
        if (counter.containsKey(key)) {
            counter.put(key, counter.get(key) + 1);
        } else {
            counter.put(key, 1);
        }
    }

    public void setCount(E key, Integer count) {
        counter.put(key, count);
    }

    public void count(E key, Integer freq) {
        if (counter.containsKey(key)) {
            counter.put(key, counter.get(key) + freq);
        } else {
            counter.put(key, freq);
        }
    }

    public Integer getCounts(E key) {
        if (counter.containsKey(key)) {
            return counter.get(key);
        } else {
            return 0;
        }
    }

    public Integer sum() {
        Integer count = 0;
        for (E key : counter.keySet()) {
            count = count + counter.get(key);
        }
        return count;
    }

    public void clearCounts(String key) {
        counter.remove(key);
    }

    public HashMap<E, Integer> getCounter() {
        return counter;
    }

    public List<E> top(int n) {
        ArrayList<E> keys = new ArrayList<>(counter.keySet());
        Collections.sort(keys, new Comparator<E>() {

            @Override
            public int compare(E o1, E o2) {
                return counter.get(o2).compareTo(counter.get(o1));
            }
        });
        return keys.subList(0, Math.min(keys.size(), n));
    }
}
