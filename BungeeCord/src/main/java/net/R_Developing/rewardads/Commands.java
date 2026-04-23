package net.R_Developing.rewardads;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands extends Command implements TabExecutor {

    private final Messager messager;
    private final Config config;
    private final Version version;
    private final QuickConnect quickConnect;
    private final AdBits adBits;
    private final Platform platform;
    private final Fetcher fetcher;
    private final Buy buy;
    private final ProxySender proxySender;
    private final Account account;

    public Commands(String name, Messager messager, Config config,
                    Version version, QuickConnect quickConnect, AdBits adBits, Platform platform,
                    Fetcher fetcher, Buy buy, ProxySender proxySender, Account account) {
        super(name);
        this.messager = messager;
        this.config = config;
        this.version = version;
        this.quickConnect = quickConnect;
        this.adBits = adBits;
        this.platform = platform;
        this.fetcher = fetcher;
        this.buy = buy;
        this.proxySender = proxySender;
        this.account = account;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sendHelp(sender);
        } else {
            switch(args[0].toLowerCase()) {
                case "reload":
                case "rel":
                    if(sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.version")) {
                        config.reloadConfigs();
                        messager.reload();
                        platform.checkAndStart(fetcher, messager, version, quickConnect, adBits, buy, platform, account);
                        sender.sendMessage(messager.custom(messager.get("reload")));
                    } else {
                        sender.sendMessage(messager.custom(messager.get("noPermission")));
                    }
                    break;
                case "version":
                case "ver":
                    if(sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.version")) {
                        sender.sendMessage(messager.custom(String.format(messager.get("currentVersion"), version.currentVersion())));
                        if(sender instanceof ProxiedPlayer) {
                            version.checkVersion((ProxiedPlayer) sender);
                        } else {
                            version.checkVersion(null);
                        }
                    } else {
                        sender.sendMessage(messager.custom(messager.get("noPermission")));
                    }
                    break;
                case "purchase":
                case "buy":
                    platform.isValid(valid -> {
                        if(!valid) {
                            sender.sendMessage(messager.custom(messager.get("platformNotReady")));
                            return;
                        }
                        if(sender instanceof ProxiedPlayer) {
                            ProxiedPlayer player = (ProxiedPlayer) sender;
                            if(config.getUserId(player.getUniqueId()) != null) {
                                if(args.length > 1) {
                                    buy.send(player, args[1]);
                                } else {
                                    String server = player.getServer().getInfo().getName();
                                    proxySender.sendCommand(player, server, "OPENGUI", "");
                                }
                            } else {
                                sender.sendMessage(messager.custom(messager.get("notLogin")));
                            }
                        } else {
                            sender.sendMessage(messager.custom(messager.get("onlyConsole")));
                        }
                    });
                    break;
                case "quickconnect":
                case "connect":
                    platform.isValid(valid -> {
                        if(!valid) {
                            sender.sendMessage(messager.getMessage("platformNotReady"));
                            return;
                        }
                        if(args.length > 1) {
                            quickConnect.connect(sender, args[1]);
                        } else {
                            sender.sendMessage(messager.getMessage("insertCode"));
                        }
                    });
                    break;
                case "quickdisconnect":
                case "disconnect":
                    platform.isValid(valid -> {
                        if(!valid) {
                            sender.sendMessage(messager.getMessage("platformNotReady"));
                            return;
                        }
                        quickConnect.disconnect(sender);
                    });
                    break;
                case "myaccount":
                case "account":
                    if(sender instanceof ProxiedPlayer) {
                        account.sendDetails((ProxiedPlayer) sender);
                    } else {
                        sender.sendMessage(messager.getMessage("onlyConsole"));
                    }
                    break;
                default:
                    if(sender instanceof ProxiedPlayer) {
                        sendHelp(sender);
                    } else {
                        sendMinimalHelp(sender);
                    }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("reload", "version", "buy", "connect", "disconnect", "account", "help"));
        }

        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8--- §6RewardADs Help §8[§bBukkit§8] ---")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads buy §f[name]§7- Open rewards gui")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads connect §f<code> §8- §7Connect to your account")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads disconnect §8- §7Disconnect from your account")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads reload §8- §7Reload the plugin config")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads version §8- §7Show plugin version")));
    }

    private void sendMinimalHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8--- §6RewardADs Help §8[§cProxied§8] ---")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads reload §7- Reload the plugin config")));
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "§8§l• §b/rewardads version §7- Show plugin version")));
    }
}
