
package app.lovable.luckyvpnmaster;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class VPNService extends VpnService {
    private static final String TAG = "VPNService";
    private static final String CHANNEL_ID = "VPN_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private boolean isConnected = false;
    private String serverIP;
    private int serverPort;
    private PointsManager pointsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        pointsManager = new PointsManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if ("connect".equals(action)) {
                serverIP = intent.getStringExtra("server_ip");
                serverPort = intent.getIntExtra("server_port", 1194);
                startVPN();
            } else if ("disconnect".equals(action)) {
                stopVPN();
            }
        }
        return START_STICKY;
    }

    private void startVPN() {
        if (isConnected) return;

        try {
            // Create VPN interface
            Builder builder = new Builder();
            builder.setMtu(1400);
            builder.addAddress("10.0.0.2", 24);
            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("8.8.4.4");
            builder.addRoute("0.0.0.0", 0);
            builder.setSession("Lucky VPN");

            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                return;
            }

            isConnected = true;
            
            // Start VPN thread
            vpnThread = new Thread(this::runVPN);
            vpnThread.start();

            // Show notification
            showNotification("Connected", "VPN is active");
            
            // Add connection points
            pointsManager.addPoints(10, "VPN Connection");
            
            Log.d(TAG, "VPN started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
            stopVPN();
        }
    }

    private void runVPN() {
        try {
            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            
            DatagramChannel tunnel = DatagramChannel.open();
            tunnel.connect(new InetSocketAddress(serverIP, serverPort));
            
            ByteBuffer packet = ByteBuffer.allocate(32767);
            
            while (isConnected && !Thread.interrupted()) {
                // Read from VPN interface
                int length = in.available();
                if (length > 0) {
                    packet.clear();
                    length = in.read(packet.array(), 0, Math.min(length, packet.capacity()));
                    if (length > 0) {
                        packet.limit(length);
                        tunnel.write(packet);
                    }
                }

                // Read from tunnel
                packet.clear();
                int receivedLength = tunnel.read(packet);
                if (receivedLength > 0) {
                    out.write(packet.array(), 0, receivedLength);
                }

                Thread.sleep(10);
            }

        } catch (Exception e) {
            Log.e(TAG, "VPN thread error", e);
        }
    }

    private void stopVPN() {
        isConnected = false;
        
        if (vpnThread != null) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }
        
        stopForeground(true);
        stopSelf();
        
        Log.d(TAG, "VPN stopped");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("VPN connection status");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        stopVPN();
        super.onDestroy();
    }
}
