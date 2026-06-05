package pe.edu.uni.fc.cc.citizenvoice.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parser/escritor minimo para no depender de librerias externas. */
public final class SimpleJson {
    private SimpleJson() {}

    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);
        int i = 0;
        while (i < s.length()) {
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) i++;
            if (i >= s.length()) break;
            if (s.charAt(i) != '"') break;
            int keyEnd = findStringEnd(s, i + 1);
            String key = unescape(s.substring(i + 1, keyEnd));
            i = keyEnd + 1;
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ':')) i++;
            String value;
            if (i < s.length() && s.charAt(i) == '"') {
                int valEnd = findStringEnd(s, i + 1);
                value = unescape(s.substring(i + 1, valEnd));
                i = valEnd + 1;
            } else {
                int valEnd = i;
                while (valEnd < s.length() && s.charAt(valEnd) != ',') valEnd++;
                value = s.substring(i, valEnd).trim();
                i = valEnd;
            }
            map.put(key, value);
        }
        return map;
    }

    private static int findStringEnd(String s, int start) {
        boolean esc = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') return i;
        }
        return s.length();
    }

    public static String obj(Object... kv) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(escape(String.valueOf(kv[i]))).append('"').append(':');
            Object value = kv[i + 1];
            if (value instanceof Number || value instanceof Boolean) sb.append(value);
            else sb.append('"').append(escape(value == null ? "" : String.valueOf(value))).append('"');
        }
        return sb.append('}').toString();
    }

    public static String quote(String s) { return '"' + escape(s) + '"'; }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}

