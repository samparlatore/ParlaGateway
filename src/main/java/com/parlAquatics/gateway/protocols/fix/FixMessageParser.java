package com.parlAquatics.gateway.protocols.fix;

import java.util.HashMap;
import java.util.Map;

public class FixMessageParser {

    public static Map<Integer, String> parse(String raw) {
        Map<Integer, String> tags = new HashMap<>();
        String[] fields = raw.split("\u0001");
        for (String field : fields) {
            String[] kv = field.split("=");
            if (kv.length == 2) {
                try {
                    tags.put(Integer.parseInt(kv[0]), kv[1]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return tags;
    }

}
