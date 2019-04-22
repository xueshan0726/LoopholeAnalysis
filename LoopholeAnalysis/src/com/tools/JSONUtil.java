/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools;

import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author 
 */
public class JSONUtil {
    static {
        System.out.println("JSONUtil 1.1");
    }
    static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^:]+:[^,]+)+\\}");
        static final Pattern OBJECT_PATTERN1 = Pattern.compile("\\{([^=]+=[^,]+)+\\}");

    static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\]");

    public static String toString(Object obj) {
        if(obj==null){
            return "null";
        }
        if(obj instanceof JSONNull){
            return "null";
        }

        if (obj instanceof Collection) {
            return JSONArray.fromObject(obj).toString();
        }
        if (obj.getClass().getSimpleName().contains("[]")) {
            return JSONArray.fromObject(obj).toString();
        }
        if (obj instanceof String || obj instanceof Number || obj instanceof Character) {
            return obj.toString();
        } else {
            return JSONObject.fromObject(obj).toString();

        }

    }


    private static Object toObj(Object o) {
//        System.out.println("toObj:"+o);
        if(o instanceof JSONNull){
//            System.out.println("null encountered!");
            return null;
        }
        if (o instanceof MorphDynaBean) {
//            System.out.println("---------------");
            return toObject(toString(o));
        }

        if (o instanceof List) {
            List l = (List) o;
            ArrayList<Object> list = new ArrayList<>();
            for (int i = 0; i < l.size(); i++) {
                list.add(toObj(l.get(i)));
            }
//            System.out.println("+++");
            return list;
        }
        return o;
    }

    public static <T extends Object> T toObject(String json, Class<T> type) {
        if (json.startsWith("{")) {
            JSONObject fromObject = JSONObject.fromObject(json);
            return (T) JSONObject.toBean(fromObject, type);

        }
        return null;
    }

    public static Map<String, Object> objToMap(Object t) {
        String toString = toString(t);
        System.out.println("toString:" + toString);
        return (Map<String, Object>) toObject(toString);
    }

    public static <T extends Object> T mapToObj(Map<String, Object> t, Class<T> clazz) {
        String toString = toString(t);
        return (T) toObject(toString);
    }

    public static Object toObject(String json) {
        if (json.startsWith("{")) {
            JSONObject fromObject = JSONObject.fromObject(json);
            Iterator iterator = fromObject.entrySet().iterator();

//            System.out.println(fromObject.entrySet());
//            HashMap<Object, Object> toBean = (HashMap<Object, Object>) JSONObject.toBean(fromObject, HashMap.class);
//            System.out.println("toBean:"+toBean);
//            Iterator<Map.Entry<Object, Object>> iterator = toBean.entrySet().iterator();
            HashMap<Object, Object> toBean = new HashMap<>();
            while(iterator.hasNext()){
                Object next = iterator.next();
                Map.Entry entry = (Map.Entry) next;
//                System.out.println(entry.getKey());
                toBean.put(entry.getKey(),toObj(entry.getValue()));
//                System.exit(0);
//                if(!(next.getKey() instanceof JSONNull)) {
//                    toBean.put(next.getKey(), toObj(toBean.get(next.getKey())));
//                }else{
//                    iterator.remove();
//                }
            }
//            for (Object key : toBean.keySet()) {
//
//            }
            return toBean;

        } else if (json.startsWith("[")) {
            return toList(json);
        }
        return json;
    }

    private static List<Object> toList(String json) {
        ArrayList<Object> list = new ArrayList<>();
        if (json.startsWith("[")) {
            JSONArray array = JSONArray.fromObject(json);
            for (int i = 0; i < array.size(); i++) {
                Object obj = array.get(i);
                list.add(toObj(obj));
            }
        }
        return list;
    }


    public static <T extends Object> ArrayList<T> toList(String json, Class<T> type) {
        ArrayList<T> list = new ArrayList<>();
        if (json.startsWith("[")) {
            JSONArray array = JSONArray.fromObject(json);
            for (int i = 0; i < array.size(); i++) {
                try {
                    JSONObject jsonObject = array.getJSONObject(i);
                    list.add((T) JSONObject.toBean(jsonObject, type));
                } catch (Exception e) {
                    list.add((T) array.get(i));
                }
            }
        }
        return list;
    }


}


