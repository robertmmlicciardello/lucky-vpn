package app.lovable.luckyvpnmaster.adapters;

/** Tiny date formatter for ISO-8601 server timestamps. Not public API. */
final class DateFmt {
    private DateFmt() {}

    /** "2026-10-12T08:30:00.000Z" -> "2026-10-12 08:30:00". Falls back to raw input. */
    static String fmt(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String s = raw.trim().replace('T', ' ');
        int cut = s.length();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.' || c == '+' || c == 'Z' || c == 'z') { cut = i; break; }
        }
        return s.substring(0, cut).trim();
    }
}
