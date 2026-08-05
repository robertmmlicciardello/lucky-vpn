
package app.lovable.luckyvpnmaster;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimizedVPNService extends VpnService {
    private static final String TAG = "OptimizedVPNService";
    private static final String CHANNEL_ID = "VPN_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    private static final int IDLE_TIMEOUT = 300000; // 5 minutes
    private static final int BUFFER_SIZE = 32767;
    
    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executorService;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isIdle = new AtomicBoolean(false);
    
    private String serverIp;
    private String serverPort;
    private String serverConfig;
    
    private PowerManager.WakeLock wakeLock;
    private Handler mainHandler;
    private long lastActivity;
    
    // Performance monitoring
    private long bytesTransmitted = 0;
    private long bytesReceived = 0;
    private int connectionAttempts = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        // Initialize thread pool for efficient task management
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Acquire wake lock for stable connection
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + ":VPNWakeLock");
        
        Log.i(TAG, "OptimizedVPNService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            serverIp = intent.getStringExtra("server_ip");
            serverPort = intent.getStringExtra("server_port");
            serverConfig = intent.getStringExtra("server_config");
            
            String action = intent.getStringExtra("action");
            if ("disconnect".equals(action)) {
                stopVPN();
                return START_NOT_STICKY;
            } else {
                startVPN();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopVPN();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        super.onDestroy();
        Log.i(TAG, "OptimizedVPNService destroyed");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Monetize VPN Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("VPN connection status and data usage");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startVPN() {
        if (isRunning.get()) {
            Log.w(TAG, "VPN already running");
            return;
        }

        try {
            connectionAttempts++;
            Log.i(TAG, "Starting VPN connection (attempt " + connectionAttempts + ")");
            
            // Create optimized VPN interface
            Builder builder = new Builder()
                .setSession("Monetize VPN")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                .setMtu(1400) // Optimized MTU for mobile networks
                .setBlocking(false); // Non-blocking I/O for better performance

            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                notifyConnectionError();
                return;
            }

            isRunning.set(true);
            lastActivity = System.currentTimeMillis();
            
            // Acquire wake lock
            if (!wakeLock.isHeld()) {
                wakeLock.acquire(10 * 60 * 1000L); // 10 minutes max
            }
            
            // Start foreground service
            startForeground(NOTIFICATION_ID, createNotification("Connecting...", "Establishing secure connection"));
            
            // Start VPN processing in background thread
            executorService.submit(this::runVPNLoop);
            
            // Start idle monitoring
            executorService.submit(this::monitorIdleState);
            
            Log.i(TAG, "VPN started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
            stopVPN();
            notifyConnectionError();
        }
    }

    private void runVPNLoop() {
        try {
            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            
            DatagramChannel tunnel = DatagramChannel.open();
            tunnel.connect(new InetSocketAddress(serverIp, Integer.parseInt(serverPort)));
            tunnel.configureBlocking(false); // Non-blocking for better performance
            
            // Protect the tunnel socket
            protect(tunnel.socket());
            
            ByteBuffer packet = ByteBuffer.allocateDirect(BUFFER_SIZE); // Direct buffer for better performance
            byte[] packetArray = new byte[BUFFER_SIZE];
            
            // Update notification
            updateNotification("Connected", "Secure connection established");
            
            while (isRunning.get() && !Thread.interrupted()) {
                try {
                    // Handle idle state for battery optimization
                    if (isIdle.get()) {
                        Thread.sleep(100); // Reduce CPU usage when idle
                        continue;
                    }
                    
                    boolean hasActivity = false;
                    
                    // Read from VPN interface (non-blocking)
                    int length = in.available();
                    if (length > 0) {
                        length = Math.min(length, BUFFER_SIZE);
                        int bytesRead = in.read(packetArray, 0, length);
                        
                        if (bytesRead > 0) {
                            packet.clear();
                            packet.put(packetArray, 0, bytesRead);
                            packet.flip();
                            
                            // Forward to tunnel
                            int sent = tunnel.write(packet);
                            if (sent > 0) {
                                bytesTransmitted += sent;
                                hasActivity = true;
                            }
                        }
                    }
                    
                    // Read from tunnel (non-blocking)
                    packet.clear();
                    int received = tunnel.read(packet);
                    if (received > 0) {
                        out.write(packet.array(), 0, received);
                        bytesReceived += received;
                        hasActivity = true;
                    }
                    
                    if (hasActivity) {
                        lastActivity = System.currentTimeMillis();
                        
                        // Update notification with data usage periodically
                        if ((bytesTransmitted + bytesReceived) % (1024 * 1024) == 0) { // Every MB
                            updateDataUsageNotification();
                        }
                    }
                    
                    // Small delay to prevent excessive CPU usage
                    Thread.sleep(1);
                    
                } catch (InterruptedException e) {
                    Log.i(TAG, "VPN thread interrupted");
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error in VPN loop", e);
                    // Continue running unless it's a critical error
                    if (e instanceof java.nio.channels.ClosedChannelException) {
                        break;
                    }
                }
            }
            
            tunnel.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in VPN thread", e);
        } finally {
            Log.i(TAG, "VPN loop ended");
        }
    }

    private void monitorIdleState() {
        while (isRunning.get()) {
            try {
                Thread.sleep(30000); // Check every 30 seconds
                
                long idleTime = System.currentTimeMillis() - lastActivity;
                boolean shouldBeIdle = idleTime > IDLE_TIMEOUT;
                
                if (shouldBeIdle != isIdle.get()) {
                    isIdle.set(shouldBeIdle);
                    
                    if (shouldBeIdle) {
                        Log.i(TAG, "Entering idle state for battery optimization");
                        updateNotification("Connected (Idle)", "VPN active, optimizing battery");
                        
                        // Release wake lock during idle
                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                    } else {
                        Log.i(TAG, "Exiting idle state");
                        updateNotification("Connected", "Secure connection active");
                        
                        // Re-acquire wake lock
                        if (!wakeLock.isHeld()) {
                            wakeLock.acquire(10 * 60 * 1000L);
                        }
                    }
                }
                
            } catch (InterruptedException e) {
                Log.i(TAG, "Idle monitor interrupted");
                break;
            } catch (Exception e) {
                Log.e(TAG, "Error in idle monitor", e);
            }
        }
    }

    private void stopVPN() {
        Log.i(TAG, "Stopping VPN");
        
        isRunning.set(false);
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }
        
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        stopForeground(true);
        stopSelf();
        
        // Log final statistics
        Log.i(TAG, String.format("VPN stopped. Data: %d bytes sent, %d bytes received, %d connection attempts", 
            bytesTransmitted, bytesReceived, connectionAttempts));
    }

    private Notification createNotification(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_vpn_connected)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build();
    }

    private void updateNotification(String title, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(title, content));
        }
    }

    private void updateDataUsageNotification() {
        String dataUsage = formatBytes(bytesTransmitted + bytesReceived);
        updateNotification("Connected", "Data used: " + dataUsage);
    }

    private void notifyConnectionError() {
        mainHandler.post(() -> {
            // Notify UI about connection error
            Intent errorIntent = new Intent("VPN_CONNECTION_ERROR");
            sendBroadcast(errorIntent);
        });
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
