package app.lovable.luckyvpnmaster.vpn;

/**
 * Sanitizes raw .ovpn configs (e.g. from VPNGate) for the embedded
 * OpenVPN3 engine (tim06 library).
 *
 * OpenVPN3 rejects legacy data-channel ciphers like AES-128-CBC that
 * VPNGate configs still ship -> without this the tunnel loops forever
 * on reconnect. We strip cipher directives and inject modern ones,
 * plus block IPv6 and force DNS so Android sees a working network.
 */
public class OvpnConfigSanitizer {

    public static String sanitize(String rawConfig) {
        if (rawConfig == null) return "";
        StringBuilder out = new StringBuilder();
        String[] lines = rawConfig.split("\n");
        for (String line : lines) {
            String t = line.trim();
            // Drop legacy/unsupported cipher directives (case-insensitive)
            if (t.regionMatches(true, 0, "cipher ", 0, 7)) continue;
            if (t.regionMatches(true, 0, "data-ciphers ", 0, 13)) continue;
            if (t.regionMatches(true, 0, "keysize ", 0, 8)) continue;
            // Drop 'comp-lzo' (unsupported in OpenVPN3, causes warnings/loops)
            if (t.regionMatches(true, 0, "comp-lzo", 0, 8)) continue;
            out.append(line).append('\n');
        }
        // Inject modern ciphers
        out.append("cipher CHACHA20-POLY1305\n");
        out.append("data-ciphers CHACHA20-POLY1305:AES-256-GCM:AES-128-GCM\n");
        // Avoid IPv6 leaks / routing stalls on networks without IPv6
        out.append("block-ipv6\n");
        // Ensure DNS works even if the server pushes none
        out.append("dhcp-option DNS 1.1.1.1\n");
        out.append("dhcp-option DNS 8.8.8.8\n");
        return out.toString();
    }
}
