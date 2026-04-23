package net.r_developing.rewardads;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    private final RewardsGUI rewardsGUI;
    private final Messager messager;
    private final Config config;
    private final Version version;
    private final QuickConnect quickConnect;
    private final AdBits adBits;
    private final Platform platform;
    private final Fetcher fetcher;
    private final Buy buy;
    private final Account account;

    public Commands(RewardsGUI rewardsGUI, Messager messager, Config config, Version version, QuickConnect quickConnect, AdBits adBits, Platform platform, Fetcher fetcher, Buy buy, Account account) {
        this.rewardsGUI = rewardsGUI;
        this.messager = messager;
        this.config = config;
        this.version = version;
        this.quickConnect = quickConnect;
        this.adBits = adBits;
        this.platform = platform;
        this.fetcher = fetcher;
        this.buy = buy;
        this.account = account;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            if(platform.isProxy())
                sendMinimalHelp(sender);
            else
                sendHelp(sender);
        } else {
            switch(args[0]) {
                case "reload":
                case "rel":
                    if(sender.isOp() || sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.reload")) {
                        config.reloadConfigs();
                        platform.checkAndStart(fetcher, rewardsGUI, messager, version, quickConnect, adBits, buy, account);
                        //messager.reload();
                        sender.sendMessage(messager.get("reload"));
                    } else {
                        sender.sendMessage(messager.get("noPermission"));
                    }
                    break;
                case "version":
                case "ver":
                    if(sender.isOp() || sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.version")) {
                        sender.sendMessage((String.format(messager.get("currentVersion"), version.currentVersion())));
                        version.checkVersion(sender);
                    } else {
                        sender.sendMessage(messager.get("noPermission"));
                    }
                    break;
                case "buy":
                case "purchase":
                    platform.isValid(valid -> {
                        boolean isProxy = platform.isProxy();
                        if(!valid || isProxy) {
                            sender.sendMessage(messager.get("platformNotReady"));
                            return;
                        }

                        if(sender instanceof Player) {
                            Player player = (Player) sender;

                            if(quickConnect.isConnected(player)) {
                                if(args.length > 1) {
                                    buy.send(player, args[1]);
                                } else {
                                    rewardsGUI.open(player);
                                }
                            } else {
                                sender.sendMessage(messager.get("notConnected"));
                            }
                        } else {
                            sender.sendMessage(messager.get("onlyConsole"));
                        }
                    });
                    break;
                case "quickconnect":
                case "connect":
                    platform.isValid(valid -> {
                        if(!valid || platform.isProxy()) {
                            sender.sendMessage(messager.get("platformNotReady"));
                            return;
                        }
                        if(args.length > 1) {
                            quickConnect.connect(sender, args[1]);
                        } else {
                            quickConnect.sendInsertCode((Player) sender);
                        }
                    });
                    break;
                case "disconnect":
                case "quickdisconnect":
                    platform.isValid(valid -> {
                        if(!valid || platform.isProxy()) {
                            sender.sendMessage(messager.get("platformNotReady"));
                            return;
                        }
                        quickConnect.disconnect(sender);
                    });
                    break;
                case "myaccount":
                case "account":
                    platform.isValid(valid -> {
                        if(!valid || platform.isProxy()) {
                            sender.sendMessage(messager.get("platformNotReady"));
                            return;
                        }
                        account.sendDetails((Player) sender);
                    });
                    break;
                default:
                    if(platform.isProxy() || !(sender instanceof Player)) {
                        sendMinimalHelp(sender);
                    } else {
                        sendHelp(sender);
                    }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("reload", "version", "buy", "connect", "disconnect", "account", "help"));
        }

        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8--- §6RewardADs Help §8[§bBukkit§8] ---");
        sender.sendMessage("");
        sender.sendMessage("§8§l• §b/rewardads buy §f[name]§7- Open rewards gui");
        sender.sendMessage("§8§l• §b/rewardads connect §f<code> §8- §7Connect to your account");
        sender.sendMessage("§8§l• §b/rewardads disconnect §8- §7Disconnect from your account");
        sender.sendMessage("§8§l• §b/rewardads reload §8- §7Reload the plugin config");
        sender.sendMessage("§8§l• §b/rewardads version §8- §7Show plugin version");
    }

    private void sendMinimalHelp(CommandSender sender) {
        sender.sendMessage("§8--- §6RewardADs Help §8[§cProxied§8] ---");
        sender.sendMessage("");
        sender.sendMessage("§8§l• §b/rewardads reload §7- Reload the plugin config");
        sender.sendMessage("§8§l• §b/rewardads version §7- Show plugin version");
    }
}
