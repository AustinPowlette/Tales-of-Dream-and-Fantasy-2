package Game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Filter / sort / limit spec shared by the terminal `list` command and the GUI browser, so both behave the same.
 *  Syntax (any order): a bare number = limit, `sort=<key>` (optionally `:desc`), `desc`/`asc`, `key=text` (contains), `key!=text`,
 *  `key>=n` `key<=n` `key>n` `key<n` `key=n` for numbers, and bare words = name filter. Example: `list shop 25 type=weapon lvl>=30 price<=5000 sort=price` */
public class ListQuery {
    static class Filter {
        final String key, op, val;
        Filter(String key, String op, String val) { this.key = key; this.op = op; this.val = val; }
        public String toString() { return key + op + val; }
    }

    static final int MAX_LIMIT = 500;
    private static final Pattern FILTER = Pattern.compile("^([A-Za-z_]+)(>=|<=|!=|=|>|<)(.+)$");

    int limit = -1;              // -1 = use the character's default (`limit <n>`)
    String sortKey = null;
    boolean desc = false;
    boolean descGiven = false;
    final List<Filter> filters = new ArrayList<>();
    String error = null;

    /** Parses tokens[from..]. */
    static ListQuery parse(String[] tokens, int from) {
        ListQuery q = new ListQuery();
        for (int i = from; i < tokens.length; i++) {
            String t = tokens[i].trim();
            if (t.isEmpty()) continue;
            String low = t.toLowerCase();
            if (low.equals("desc") || low.equals("descending")) { q.desc = true; q.descGiven = true; continue; }
            if (low.equals("asc") || low.equals("ascending")) { q.desc = false; q.descGiven = true; continue; }
            if (low.equals("limit") && i + 1 < tokens.length && tokens[i + 1].trim().matches("\\d+")) { q.limit = clamp(TermFormat.parseInt(tokens[++i].trim(), MAX_LIMIT)); continue; } // 'people limit 3' = 'people 3'
            if (t.matches("\\d+")) { q.limit = clamp(TermFormat.parseInt(t, MAX_LIMIT)); continue; } // 'list people 99999999999' used to throw
            if (low.startsWith("sort=") || low.startsWith("sort:") || low.startsWith("by=")) {
                int eq = t.indexOf('=') >= 0 ? t.indexOf('=') : t.indexOf(':');
                String v = t.substring(eq + 1);
                int c = v.indexOf(':');
                if (c >= 0) { String dir = v.substring(c + 1).toLowerCase(); v = v.substring(0, c); q.desc = dir.startsWith("d"); q.descGiven = true; }
                q.sortKey = alias(v.toLowerCase());
                continue;
            }
            if (low.startsWith("limit=")) {
                try { q.limit = clamp(Integer.parseInt(t.substring(6).trim())); } catch (NumberFormatException e) { q.error = "limit must be a number"; }
                continue;
            }
            Matcher m = FILTER.matcher(t);
            if (m.matches()) { q.filters.add(new Filter(alias(m.group(1).toLowerCase()), m.group(2), m.group(3).trim())); continue; }
            q.filters.add(new Filter("name", "=", t)); // a plain word filters by name
        }
        return q;
    }

    static int clamp(int n) { return Math.max(1, Math.min(MAX_LIMIT, n)); }

    static String alias(String k) {
        switch (k) {
            case "level": case "l": return "lvl";
            case "cost": case "p": return "price";
            case "kind": return "type";
            case "affiliation": case "alignment": return "aff";
            case "relationship": return "rel";
            case "distance": case "d": return "dist";
            case "quantity": return "qty";
            default: return k;
        }
    }

    /** Returns an error message if a key is unknown for this list, else null. */
    String validate(Set<String> keys) {
        if (error != null) return error;
        for (Filter f : filters) if (!keys.contains(f.key)) return "Unknown filter '" + f.key + "'. Keys: " + keys;
        if (sortKey != null && !keys.contains(sortKey)) return "Unknown sort key '" + sortKey + "'. Keys: " + keys;
        return null;
    }

    boolean test(Map<String, Object> fields) {
        for (Filter f : filters) {
            Object v = fields.get(f.key);
            if (v == null) return false;
            if (v instanceof Number) {
                long have = ((Number) v).longValue(), want;
                try { want = parseNumber(f.val); } catch (NumberFormatException e) { return false; }
                switch (f.op) {
                    case ">=": if (!(have >= want)) return false; break;
                    case "<=": if (!(have <= want)) return false; break;
                    case ">":  if (!(have > want)) return false; break;
                    case "<":  if (!(have < want)) return false; break;
                    case "!=": if (have == want) return false; break;
                    default:   if (have != want) return false; break;
                }
            } else {
                String have = v.toString().toLowerCase(), want = f.val.toLowerCase();
                switch (f.op) {
                    case "!=": if (have.contains(want)) return false; break;
                    case ">": case ">=": case "<": case "<=": return false;
                    default: if (!have.contains(want)) return false; break;
                }
            }
        }
        return true;
    }

    // 1500, 2k, 3m, 4b (thousand / million / billion)
    static long parseNumber(String s) {
        s = s.trim().toLowerCase().replace(",", "").replace("_", "");
        long mult = 1;
        if (s.endsWith("k")) { mult = 1_000L; s = s.substring(0, s.length() - 1); }
        else if (s.endsWith("m")) { mult = 1_000_000L; s = s.substring(0, s.length() - 1); }
        else if (s.endsWith("b")) { mult = 1_000_000_000L; s = s.substring(0, s.length() - 1); }
        return Long.parseLong(s) * mult;
    }

    /** Filters, sorts (default order unless a sort key was given) and caps a list. `total` receives the size before the cap. */
    <T> List<T> apply(List<T> src, Function<T, Map<String, Object>> fields, Comparator<T> defaultOrder, int defaultLimit, int[] total) {
        List<T> out = new ArrayList<>();
        for (T t : src) if (test(fields.apply(t))) out.add(t);
        if (sortKey != null) {
            final String k = sortKey;
            final boolean d = desc;
            out.sort((a, b) -> {
                Object x = fields.apply(a).get(k), y = fields.apply(b).get(k);
                int c;
                if (x instanceof Number && y instanceof Number) c = Long.compare(((Number) x).longValue(), ((Number) y).longValue());
                else c = String.valueOf(x).compareToIgnoreCase(String.valueOf(y));
                return d ? -c : c;
            });
        } else if (defaultOrder != null) {
            out.sort(descGiven && desc ? defaultOrder.reversed() : defaultOrder);
        }
        if (total != null && total.length > 0) total[0] = out.size();
        int cap = limit > 0 ? limit : defaultLimit;
        return out.size() > cap ? new ArrayList<>(out.subList(0, cap)) : out;
    }
}
