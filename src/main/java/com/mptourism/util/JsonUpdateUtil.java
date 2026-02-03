package com.mptourism.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUpdateUtil {
    
    public static void updateJsonValue(
        ObjectNode root,
        String path, 
        JsonNode newValue
    ) {
        String[] keys = path.split("\\.");
        ObjectNode current = root;

        for(int i = 0; i < keys.length -1; i++) {
            JsonNode next = current.get(keys[i]);
            if(next == null || !next.isObject()) {
                throw new RuntimeException("Invalid path:" + path);
            }
            current = (ObjectNode) next;
        }

        current.set(keys[keys.length - 1], newValue);
    }
}
