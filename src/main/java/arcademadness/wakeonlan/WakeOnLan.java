package arcademadness.wakeonlan;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WakeOnLan extends Plugin implements Listener{

    private String serverIP;
    private int serverPort;
    private String serverMAC;
    private int timeout;

    @Override
    public void onEnable() {
        loadConfig();
        getProxy().getPluginManager().registerListener(this, this);
    }

    public void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            createDefaultConfig(configFile);
        }

        try (InputStream input = Files.newInputStream(configFile.toPath())) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(input);
            if (data instanceof Map) {
                Map<String, Object> config = (Map<String, Object>) data;
                Map<String, Object> server = (Map<String, Object>) config.get("server");
                if (server != null) {
                    serverIP = (String) server.get("ip");

                    Object port = server.get("port");
                    if (port instanceof Number) {
                        serverPort = ((Number) port).intValue();
                    }

                    serverMAC = (String) server.get("mac");

                    Object timeoutms = server.get("timeout");
                    if (timeoutms instanceof Number) {
                        timeout = ((Number) timeoutms).intValue();
                    }
                }
            }

            getLogger().info("Configuration loaded successfully.");

        } catch (IOException e) {
            getLogger().severe("Error loading configuration: " + e.getMessage());
        } catch (ClassCastException e) {
            getLogger().severe("Invalid data type in config.yml: " + e.getMessage());
        }
    }


    private void createDefaultConfig(File configFile) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setIndent(2);
            options.setPrettyFlow(true);

            StringBuilder yamlBuilder = new StringBuilder();
            yamlBuilder.append("server:\n");
            yamlBuilder.append("  ip: 192.168.1.100\n");
            yamlBuilder.append("  ports: 8\n");
            yamlBuilder.append("  mac: 00:1A:2B:3C:4D:5E");
            yamlBuilder.append("  timeout: 5000");

            try (OutputStream output = Files.newOutputStream(configFile.toPath())) {
                output.write(yamlBuilder.toString().getBytes());
                getLogger().info("Default config.yml created. You'll want to go modify that.");
            }

        } catch (IOException e) {
            getLogger().severe("Failed to create default config.yml: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {

            if (!isServerOnline(serverIP, serverPort)) {
                try {
                    sendWakeOnLanPacket(serverMAC, serverIP, serverPort);
                } catch (Exception e) {
                    //nothing
                }
            }
        });
    }

    private boolean isServerOnline(String ip, int port) {
        getLogger().info("Sending Ping Request to " + ip);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            getLogger().info("Host is reachable on port " + port);
            return true;
        } catch (Exception e) {
            getLogger().warning("Cannot reach host via TCP: " + e.getMessage());
            return false;
        }
    }

    private void sendWakeOnLanPacket(String macStr, String ipStr, int PORT) {
        try {
            final String[] hex = validateMac(macStr);

            final byte[] macBytes = new byte[6];
            for(int i=0; i<6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }

            final byte[] bytes = new byte[102];

            for(int i=0; i<6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for(int i=6; i<bytes.length; i+=macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            final InetAddress address = InetAddress.getByName(ipStr);
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            final DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.send(packet);
            socket.close();
            getLogger().info("Wake-on-LAN packet sent");

        } catch (Exception e) {
            getLogger().warning("Could not send Wake-on-LAN packet: " + e.getMessage());
        }
    }

    private static String[] validateMac(String mac) throws IllegalArgumentException
    {
        // error handle semi colons
        mac = mac.replace(";", ":");

        // attempt to assist the user a little
        String newMac = "";

        if(mac.matches("([a-zA-Z0-9]){12}")) {
            // expand 12 chars into a valid mac address
            for(int i=0; i<mac.length(); i++){
                if((i > 1) && (i % 2 == 0)) {
                    newMac += ":";
                }
                newMac += mac.charAt(i);
            }
        }else{
            newMac = mac;
        }

        // regexp pattern match a valid MAC address
        final Pattern pat = Pattern.compile("((([0-9a-fA-F]){2}[-:]){5}([0-9a-fA-F]){2})");
        final Matcher m = pat.matcher(newMac);

        if(m.find()) {
            String result = m.group();
            return result.split("(\\:|\\-)");
        }else{
            throw new IllegalArgumentException("Invalid MAC address");
        }
    }

}


