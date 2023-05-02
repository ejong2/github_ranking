package kr.tenth.ranking.common;

import java.util.HashMap;
import java.util.Map;

public class Result {
    private Map<String, Object> data;

    public Result() {
        data = new HashMap<>();
    }

    public void addItem(String key, Object value) {
        data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
