# Minecraft Server Wake-on-LAN Plugin

## How it Works

When a player connects to your proxy server, this plugin sends a ping to your target host (defined in the config file) and waits for a response. If a response comes back, it is assumed that your Minecraft server is online (or in the process of starting) and will do nothing. If the target host is offline, however, it will send a Wake-on-LAN packet to the target host (once again, defined in the config file). This process works best when you have set up a fallback server (such as a [Limbo server](https://www.spigotmc.org/resources/limbo-standalone-server-lightweight-solution-for-afk-or-waiting-rooms-in-your-server-network.82468/)) for your players to wait in. It's important to note that this plugin does not redirect your players to the host once it's come back online. You'll want a separate plugin for that.

## Setup Notes

Obviously, you'll need your system to both support Wake-on-LAN and have it actively turned on, both through your BIOS and your operating system, if necessary. The port in the config file will be used for both the status check of the target host and sending the WoL packet. That port must be actively listening, and you will probably need to define a new Inbound Rule on your firewall for that specific port. For my testing, I used Server Message Block (SMB) port 445.
