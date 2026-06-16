package cn.ggsn.openrxlight.utils.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetUtils {

    private static volatile String cachedHostname;

    /**
     * Get the local hostname efficiently by trying multiple strategies in
     * order of cost: env var → /etc/hostname → InetAddress fallback.
     * The result is cached so subsequent calls are free.
     */
    public static String getHostname() {
        if (cachedHostname != null) {
            return cachedHostname;
        }
        synchronized (NetUtils.class) {
            if (cachedHostname != null) {
                return cachedHostname;
            }
            cachedHostname = resolveHostname();
            return cachedHostname;
        }
    }

    private static String resolveHostname() {
        // 1. Fastest: environment variable
        String env = System.getenv("HOSTNAME");
        if (env != null && !env.isEmpty()) {
            return env;
        }
        // Windows also exposes COMPUTERNAME
        env = System.getenv("COMPUTERNAME");
        if (env != null && !env.isEmpty()) {
            return env;
        }

        // 2. Fast on Unix: read /etc/hostname directly (avoids DNS lookup)
        if (isUnix()) {
            String fromFile = readHostnameFile("/etc/hostname");
            if (fromFile != null) {
                return fromFile;
            }
            // Some distributions use /etc/HOSTNAME
            fromFile = readHostnameFile("/etc/HOSTNAME");
            if (fromFile != null) {
                return fromFile;
            } else {
                String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                int atIndex = jvmName.indexOf('@');
                if (atIndex > 0) {
                    return jvmName.substring(atIndex + 1);
                }
            }
        }

        // 3. Fallback: InetAddress (may trigger DNS, slower)
        return getHostnameViaInetAddress();
    }

    private static String readHostnameFile(String path) {
        File f = new File(path);
        if (!f.isFile() || !f.canRead()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    return line;
                }
            }
        } catch (IOException ignored) {
            // fall through
        }
        return null;
    }

    private static String getHostnameViaInetAddress() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static volatile String cachedHostAddress;

    /**
     * Get the local IP address efficiently with caching.
     * Prefers non-loopback, site-local addresses.
     */
    public static String getHostAddress() {
        if (cachedHostAddress != null) {
            return cachedHostAddress;
        }
        synchronized (NetUtils.class) {
            if (cachedHostAddress != null) {
                return cachedHostAddress;
            }
            cachedHostAddress = resolveHostAddress();
            return cachedHostAddress;
        }
    }

    private static String resolveHostAddress() {
        // 1. Try network interfaces for a non-loopback IPv4 address (no DNS)
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
            // fall through
        }

        // 2. Fallback: InetAddress (may trigger DNS lookup)
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux") || os.contains("mac")
                || os.contains("darwin") || os.contains("freebsd")
                || os.contains("openbsd") || os.contains("sunos")
                || os.contains("nix") || os.contains("aix");
    }
}
