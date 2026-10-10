package app.lovable.luckyvpnmaster.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple internet speed test using Cloudflare's speed-test endpoints.
 * Runs entirely off the main thread via an ExecutorService.
 * No new dependencies — plain HttpURLConnection.
 */
public class SpeedTestHelper {

    private static final int TIMEOUT_MS = 15000;
    private static final String PING_URL = "https://www.cloudflare.com";
    private static final String DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=25000000";
    private static final String UPLOAD_URL = "https://speed.cloudflare.com/__up";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface SpeedTestListener {
        void onPingResult(long ms);
        void onDownloadResult(double mbps);
        void onProgress(String phase, double mbps);
        void onComplete(double downMbps, double upMbps, long pingMs);
        void onError(String msg);
    }

    public void start(SpeedTestListener listener) {
        executor.submit(() -> {
            try {
                listener.onProgress("ping", 0);
                long pingMs = measurePing();
                listener.onPingResult(pingMs);

                listener.onProgress("download", 0);
                double downMbps = measureDownload(listener);
                listener.onDownloadResult(downMbps);

                listener.onProgress("upload", 0);
                double upMbps = measureUpload(listener);

                listener.onComplete(downMbps, upMbps, pingMs);
            } catch (Exception e) {
                listener.onError(e.getMessage() != null ? e.getMessage() : "Speed test failed");
            }
        });
    }

    public void cancel() {
        executor.shutdownNow();
    }

    /** Ping via HTTP HEAD to cloudflare.com, best-of-3, rounded to ms. */
    private long measurePing() throws Exception {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            HttpURLConnection conn = null;
            try {
                long t0 = System.nanoTime();
                conn = (HttpURLConnection) new URL(PING_URL).openConnection();
                conn.setRequestMethod("HEAD");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.connect();
                conn.getResponseCode();
                long ms = (System.nanoTime() - t0) / 1_000_000L;
                if (ms < best) best = ms;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        return best == Long.MAX_VALUE ? -1 : best;
    }

    /** Download 25 MB from Cloudflare, report live Mbps via listener. */
    private double measureDownload(SpeedTestListener listener) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.connect();
            long totalBytes = 0;
            long t0 = System.nanoTime();
            byte[] buf = new byte[64 * 1024];
            try (InputStream in = conn.getInputStream()) {
                int n;
                while ((n = in.read(buf)) != -1) {
                    totalBytes += n;
                    long elapsedNs = System.nanoTime() - t0;
                    if (elapsedNs > 0) {
                        double mbps = (totalBytes * 8.0) / (elapsedNs / 1e9) / 1e6;
                        listener.onProgress("download", mbps);
                    }
                }
            }
            long elapsedNs = System.nanoTime() - t0;
            double seconds = elapsedNs / 1e9;
            return seconds > 0 ? (totalBytes * 8.0) / seconds / 1e6 : 0;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** POST 2 MB of random bytes to Cloudflare's upload endpoint. */
    private double measureUpload(SpeedTestListener listener) throws Exception {
        HttpURLConnection conn = null;
        try {
            byte[] payload = new byte[2 * 1024 * 1024];
            new Random().nextBytes(payload);
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setFixedLengthStreamingMode(payload.length);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            long t0 = System.nanoTime();
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
                out.flush();
            }
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new Exception("Upload failed (HTTP " + code + ")");
            }
            long elapsedNs = System.nanoTime() - t0;
            double seconds = elapsedNs / 1e9;
            double mbps = seconds > 0 ? (payload.length * 8.0) / seconds / 1e6 : 0;
            listener.onProgress("upload", mbps);
            return mbps;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
