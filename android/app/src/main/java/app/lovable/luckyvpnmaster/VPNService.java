
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
    private boolean isRunning = false;
    
    private String serverIp;
    private String serverPort;
    private String serverConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            serverIp = intent.getStringExtra("server_ip");
            serverPort = intent.getStringExtra("server_port");
            serverConfig = intent.getStringExtra("server_config");
            
            startVPN();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopVPN();
        super.onDestroy();
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
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startVPN() {
        if (isRunning) return;

        try {
            // Create VPN interface
            Builder builder = new Builder()
                .setSession("Lucky VPN Master")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .addRoute("0.0.0.0", 0);

            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                return;
            }

            isRunning = true;
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start VPN thread
            vpnThread = new Thread(this::runVPN);
            vpnThread.start();
            
            Log.d(TAG, "VPN started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
            stopVPN();
        }
    }

    private void stopVPN() {
        isRunning = false;
        
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

    private void runVPN() {
        try {
            // Set up data channels
            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            
            // Create UDP channel for server communication
            DatagramChannel tunnel = DatagramChannel.open();
            tunnel.connect(new InetSocketAddress(serverIp, Integer.parseInt(serverPort)));
            
            // Protect the tunnel socket
            protect(tunnel.socket());
            
            // Packet processing loop
            ByteBuffer packet = ByteBuffer.allocate(32767);
            
            while (isRunning && !Thread.interrupted()) {
                // Read from VPN interface
                int length = in.read(packet.array());
                if (length > 0) {
                    packet.limit(length);
                    
                    // Process and forward packet
                    processPacket(packet, tunnel, out);
                    
                    packet.clear();
                }
            }
            
            tunnel.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in VPN thread", e);
        }
    }

    private void processPacket(ByteBuffer packet, DatagramChannel tunnel, FileOutputStream out) {
        try {
            // Simple packet forwarding
            // In a real VPN, you would implement proper encryption and protocol handling
            
            // Forward to server
            tunnel.write(packet);
            packet.rewind();
            
            // Read response from server
            ByteBuffer response = ByteBuffer.allocate(32767);
            int responseLength = tunnel.read(response);
            
            if (responseLength > 0) {
                // Forward response back to VPN interface
                out.write(response.array(), 0, responseLength);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing packet", e);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lucky VPN Master")
            .setContentText("VPN Connected - " + serverIp)
            .setSmallIcon(R.drawable.ic_vpn_connected)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
}
