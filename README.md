#  Wake-on-LAN Plugin for BungeeCord

## Why do this?

The intended purpose of this plugin is save on electrical usage. To run some type of automatic shutdown program/plugin on the host so that it doesn't use much power in times of frequent inactivity. While it's down, the proxy server will be run on something much more lightweight such as raspberry pi 4~5. Then once a player joins the proxy, this plugin will send a Wake-on-LAN packet the host server, hopefully turning it back on. If your server is being used 24/7, this project will do practically nothing. 

## How it Works

When a player connects to your proxy server, this plugin sends a ping to your target host (defined in the config file) and waits for a response. If a response comes back, it is assumed that your Minecraft server is online (or in the process of starting) and will do nothing. If the target host is offline, however, it will send a Wake-on-LAN packet to the target host (once again, defined in the config file). This process works best when you have set up a fallback server (such as [Limbo](https://www.spigotmc.org/resources/limbo-standalone-server-lightweight-solution-for-afk-or-waiting-rooms-in-your-server-network.82468/)) for your players to wait in. It's important to note that this plugin does not redirect your players to the host once it's come back online. You'll want a separate plugin for that... Such as the one i just made, [AutoTransfer](https://github.com/TrademarkTHIS/BungeeAutoTransfer)!  

## Setup Notes

Obviously, you'll need your system to both support Wake-on-LAN and have it actively turned on, both through your BIOS and your operating system if necessary. The port in the config file will be used for both the status check of the target host and sending the WoL packet. That port must be actively listening, and you will probably need to define a new Inbound Rule on your firewall for that specific port. For my testing, I used Server Message Block (SMB) port 445. I tested this project with two computers, both running their operating systems on SSDs. If you're not running an SSD, you will probably be dissapointed by how long it takes to actually connect to the host...


## Useful links

[How to setup Wake-on-LAN](https://www.asus.com/support/faq/1045950/) Official ASUS guide for setting up Wake-on-LAN

[Limbo fallback server](https://www.spigotmc.org/resources/limbo-standalone-server-lightweight-solution-for-afk-or-waiting-rooms-in-your-server-network.82468/) deliberately avoids handling as many packets as possible to decrease power and memory usage (perfect for raspberry pi).

[Hibernate Plugin](https://www.spigotmc.org/resources/hibernate.4441/) lowers the CPU usage of the host server when there is no player activity.

[AutoTransfer](https://github.com/TrademarkTHIS/BungeeAutoTransfer) Moves you from the fallback server to the default once it becomes online.
