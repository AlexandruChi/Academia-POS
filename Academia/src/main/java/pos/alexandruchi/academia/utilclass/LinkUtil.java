package pos.alexandruchi.academia.utilclass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinkUtil {
    public static Map<String, Object> createLink(String link, String method, Map<String, Object> query) {
        if (link == null) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("href", link);

        if (method != null) {
            map.put("type", method);
        }

        if (query != null) {
            map.put("query", query);
        }

        return map;
    }
}
