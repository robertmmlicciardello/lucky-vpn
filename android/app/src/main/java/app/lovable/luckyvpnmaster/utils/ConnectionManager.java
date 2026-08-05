
package app.lovable.luckyvpnmaster.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.annotation.NonNull;
import java.util.HashSet;
import java.util.Set;

public class ConnectionManager {
    private static final String TAG = "ConnectionManager";
    
    public interface NetworkCallback {
        void onNetworkAvailable();
        void onNetworkLost();
        void onNetworkCapabilitiesChanged(boolean isWifi, boolean isMobile);
    }
    
    private final ConnectivityManager connectivityManager;
    private final Set<NetworkCallback> callbacks = new HashSet<>();
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isNetworkAvailable = false;
    
    public ConnectionManager(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        initializeNetworkCallback();
        registerNetworkCallback();
        checkInitialNetworkState();
    }
    
    private void initializeNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (!isNetworkAvailable) {
                    isNetworkAvailable = true;
                    notifyNetworkAvailable();
                }
            }
            
            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // Check if we still have other networks available
                if (!hasActiveNetwork()) {
                    isNetworkAvailable = false;
                    notifyNetworkLost();
                }
            }
            
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                super.onCapabilitiesChanged(network, capabilities);
                boolean isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                boolean isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                notifyCapabilitiesChanged(isWifi, isMobile);
            }
        };
    }
    
    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }
    }
    
    private void checkInitialNetworkState() {
        isNetworkAvailable = hasActiveNetwork();
    }
    
    private boolean hasActiveNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null && 
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
    
    public boolean isNetworkAvailable() {
        return isNetworkAvailable && hasActiveNetwork();
    }
    
    public boolean isWifiConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            android.net.NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
    }
    
    public void addCallback(NetworkCallback callback) {
        callbacks.add(callback);
    }
    
    public void removeCallback(NetworkCallback callback) {
        callbacks.remove(callback);
    }
    
    private void notifyNetworkAvailable() {
        for (NetworkCallback callback : callbacks) {
            callback.onNetworkAvailable();
        }
    }
    
    private void notifyNetworkLost() {
        for (NetworkCallback callback : callbacks) {
            callback.onNetworkLost();
        }
    }
    
    private void notifyCapabilitiesChanged(boolean isWifi, boolean isMobile) {
        for (NetworkCallback callback : callbacks) {
            callback.onNetworkCapabilitiesChanged(isWifi, isMobile);
        }
    }
    
    public void destroy() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        callbacks.clear();
    }
}
