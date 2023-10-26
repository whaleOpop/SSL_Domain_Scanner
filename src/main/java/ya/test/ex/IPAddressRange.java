package ya.test.ex;

import java.util.ArrayList;
import java.util.List;

public class IPAddressRange {

    public IPAddressRange(String ips_mass) {
        this.ipAddressWithMask = ips_mass;
    }

    public String getIps_mass() {
        return ipAddressWithMask;
    }

    public void setIps_mass(String ips_mass) {
        this.ipAddressWithMask = ips_mass;
    }

    private String ipAddressWithMask;

    public String[] calculateIPRange() {
        String[] ipAddresses;

        String[] parts = ipAddressWithMask.split("/");
        String ipAddress = parts[0];
        int mask = Integer.parseInt(parts[1]);

        String[] ipParts = ipAddress.split("\\.");

        int[] ip = new int[4];
        for (int i = 0; i < 4; i++) {
            ip[i] = Integer.parseInt(ipParts[i]);
        }

        int numberOfAddresses = (int) Math.pow(2, 32 - mask);
        ipAddresses = new String[numberOfAddresses];

        for (int i = 0; i < numberOfAddresses; i++) {
            ipAddresses[i] = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
            incrementIP(ip);
        }

        return ipAddresses;
    }

    private static void incrementIP(int[] ip) {
        int i = 3;

        while (i >= 0 && ip[i] == 255) {
            ip[i] = 0;
            i--;
        }

        if (i >= 0) {
            ip[i]++;
        }
    }
}
