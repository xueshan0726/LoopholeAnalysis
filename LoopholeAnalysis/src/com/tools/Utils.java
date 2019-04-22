/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {

    //常用变量
    public static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat HOUR = new SimpleDateFormat("HH");
    public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_AND_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATEANDTIME = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static final String NOT_CHINESE_REGEX = "[^\u4E00-\u9FA5]";
    public static final String CHINESE_REGEX = "[\u4E00-\u9FA5]";

    public static final String SPECIAL_CHARACTER = "[^\u4E00-\u9FA5A-Za-z0-9]";
    public static final int HOUR_IN_MILLI = 3600 * 1000;
    public static final int DAY_IN_MILLI = 3600 * 1000 * 24;
    public static final TimeZone ETC_TIME_ZONE = TimeZone.getTimeZone("Etc/GMT0");



    public static <E extends Object> ArrayList<ArrayList<E>> split(int num, ArrayList<E> list) {
        ArrayList<ArrayList<E>> split = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            split.add(new ArrayList<E>());
        }
        for (int i = 0; i < list.size(); i++) {
            split.get(i % num).add(list.get(i));
        }
        return split;
    }

    public static <E extends Object> ArrayList<E> asList(E... es) {
        ArrayList<E> list = new ArrayList<>(Arrays.asList(es));
        return list;
    }




    public static String toRegExp(String s) {
        s = s.replaceAll("\\\\", "\\\\");
        s = s.replaceAll("\\^", "\\\\^");
        s = s.replaceAll("\\.", "\\\\.");
        s = s.replaceAll("\\+", "\\\\+");
        s = s.replaceAll("\\*", "\\\\*");
        s = s.replaceAll("\\[", "\\\\[");
        s = s.replaceAll("\\]", "\\\\]");
        s = s.replaceAll("\\(", "\\\\(");
        s = s.replaceAll("\\)", "\\\\)");
        s = s.replaceAll("\\{", "\\\\{");
        s = s.replaceAll("\\}", "\\\\}");
        s = s.replaceAll("\\|", "\\\\|");
        s = s.replaceAll("\\?", "\\\\?");

        return s;
    }

    public static ArrayList<ArrayList<String>> transpose(ArrayList<ArrayList<String>> m) {
        ArrayList<ArrayList<String>> r = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {

            for (int j = 0; j < m.get(i).size(); j++) {
                while (r.size() <= j) {
                    r.add(new ArrayList<String>());
                }
                r.get(j).add(m.get(i).get(j));
            }
        }
        return r;
    }

    public static ArrayList<String> getIp() throws SocketException {
        ArrayList<String> list = new ArrayList<>();
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface nextElement = e.nextElement();
            Enumeration<InetAddress> ee = nextElement.getInetAddresses();
            while (ee.hasMoreElements()) {
                String hostAddress = ee.nextElement().getHostAddress();
                if (hostAddress.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) {
                    if (!hostAddress.equals("127.0.0.1")) {
                        list.add(hostAddress);
                    }
                }
            }
        }
        return list;
    }

    public static <K, V extends Object> ArrayList<K> getSortedKeyByValue(final Map<K, V> hm, final boolean asc) {

        return getSortedKeyByValue(hm.keySet(), hm, asc);
    }

    public static <K, V extends Object> ArrayList<K> getSortedKey(final Map<K, V> hm, final boolean asc) {
        HashMap<K, K> map = new HashMap<>();
        for (K key : hm.keySet()) {
            map.put(key, key);
        }
        return getSortedKeyByValue(hm.keySet(), map, asc);
    }

    public static <K, V extends Object> ArrayList<K> getSortedKeyByValue(Set<K> keys, final Map<K, V> hm, final boolean asc) {
        ArrayList<K> list = new ArrayList<>(keys);
        Collections.sort(list, new Comparator<K>() {

            @Override
            public int compare(K o1, K o2) {
                if (hm.get(o2) instanceof Comparable) {
                    Comparable c1 = Comparable.class.cast(hm.get(o1));
                    Comparable c2 = Comparable.class.cast(hm.get(o2));
                    int compareTo = c1.compareTo(c2);
                    return asc ? compareTo : -compareTo;
                } else {
                    return 0;
                }
            }
        });
        return list;
    }


    public static String getTimeStamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return DATEANDTIME.format(currentTimeMillis) + (currentTimeMillis % 1000);
    }


    public static <T extends Object> double sum(Collection<T> set) {

        Iterator<T> iterator = set.iterator();

        double sum = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            Number cast = Number.class.cast(next);
            sum = sum + cast.doubleValue();
        }
        return sum;
    }

    public static Double sumInteger(Collection<Integer> set) {
        Iterator<Integer> iterator = set.iterator();

        double sum = 0;
        while (iterator.hasNext()) {
            Number next = iterator.next();
            sum = sum + next.doubleValue();
        }
        return sum;
    }

    public static List<Integer> range(int from, int size) {
        ArrayList<Integer> range = new ArrayList<>();
        for (int i = from; i < from + size; i++) {
            range.add(i);
        }
        return range;
    }

    public static String formatDouble(double d, int n) {
        if (d == 0) {
            return "0";
        }
        String pattern = "0.";
        for (int i = 0; i < n; i++) {
            pattern = pattern.concat("0");
        }

        DecimalFormat df = new DecimalFormat(pattern);
        String output = df.format(d);
        while (output.endsWith("0")) {
            output = output.substring(0, output.length() - 1);
        }
        if (output.endsWith(".")) {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }

    private static long last = 0;


    public static <E extends Object> boolean contains(Set<E> a, Set<E> b) {
        HashSet<E> c = new HashSet<>();
        c.addAll(a);
        int size = c.size();
        c.addAll(b);
        int size2 = c.size();
        return size == size2;
    }


    public static int processInterval(String interval) {
        while (Pattern.compile("\\([^\\)\\(]+\\)").matcher(interval).find()) {
            interval = interval.replaceAll("\\([^\\)\\(]+\\)", "");
        }
        int multi = 1;
        int r = 0;
        if (interval.contains("秒")) {
            multi = 1;
        } else if (interval.contains("分")) {
            multi = 60;
        } else if (interval.contains("小时")) {
            multi = 3600;
        } else if (interval.contains("天")) {
            multi = 60 * 60 * 24;
        }
        interval = interval.replaceAll("[^0-9]", "");
        try {
            r = Integer.parseInt(interval);
        } catch (Exception e) {
            r = 0;
        }
        return r * multi;
    }


    public static String toString(Set<String> set) {
        if (set.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : set) {
            sb.append(item).append(", ");
        }
        String res = sb.toString();
        res = res.substring(0, res.lastIndexOf(", "));
        return res;
    }






    public static <T extends Object> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> al = new ArrayList<T>();
        for (T e : array) {
            al.add(e);
        }
        return al;
    }

}
