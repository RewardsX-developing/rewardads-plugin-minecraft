package net.R_Developing.rewardads;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;

public class Commands implements SimpleCommand {
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

    public Commands(Messager messager, Config config,
                    Version version, QuickConnect quickConnect, AdBits adBits, Platform platform,
                    Fetcher fetcher, Buy buy, ProxySender proxySender, Account account) {
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
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if(args.length == 0) {
            sendHelp(sender);
        } else {
            switch(args[0].toLowerCase()) {
                case "reload":
                case "rel":
                    if(sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.version")) {
                        config.reloadConfigs();
                        messager.reload();
                        platform.checkAndStart(fetcher, messager, version, quickConnect, adBits, buy, account, platform);
                        sender.sendMessage(messager.getMessage("reload"));
                    } else {
                        sender.sendMessage(messager.getMessage("noPermission"));
                    }
                    break;
                case "version":
                case "ver":
                    if(sender.hasPermission("rewardads.admin") || sender.hasPermission("rewardads.version")) {
                        sender.sendMessage(messager.custom(String.format(messager.get("currentVersion"), version.currentVersion())));
                        if(sender instanceof Player) {
                            version.checkVersion((Player) sender);
                        } else {
                            version.checkVersion(null);
                        }
                    } else {
                        sender.sendMessage(messager.getMessage("noPermission"));
                    }
                    break;
                case "purchase":
                case "buy":
                    platform.isValid(valid -> {
                        if(!valid) {
                            sender.sendMessage(messager.getMessage("platformNotReady"));
                            return;
                        }
                        if(sender instanceof Player player) {
                            if(config.getUserId(player.getUniqueId()) != null) {
                                if(args.length > 1) {
                                    buy.send(player, args[1]);
                                } else {
                                    player.getCurrentServer().ifPresent(serverConnection -> {
                                        String server = serverConnection.getServerInfo().getName();
                                        proxySender.sendCommand(player, server, "OPENGUI", "");
                                    });
                                }
                            } else {
                                sender.sendMessage(messager.getMessage("notConnected"));
                            }
                        } else {
                            sender.sendMessage(messager.getMessage("onlyConsole"));
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
                    if(sender instanceof Player) {
                        account.sendDetails((Player) sender);
                    } else {
                        sender.sendMessage(messager.getMessage("onlyConsole"));
                    }
                    break;
                default:
                    if(sender instanceof Player) {
                        sendHelp(sender);
                    } else {
                        sendMinimalHelp(sender);
                    }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if(args.length <= 1)
            return Arrays.asList("reload", "version", "buy", "account", "connect", "disconnect", "help");

        return List.of();
    }

    private void sendHelp(CommandSource sender) {
        // Header line
        sender.sendMessage(
                Component.text("--- ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("RewardADs Help ", NamedTextColor.GOLD))
                        .append(Component.text("[", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Velocity", NamedTextColor.BLUE))
                        .append(Component.text("] ---", NamedTextColor.DARK_GRAY))
        );

        sender.sendMessage(Component.empty());

        // Individual help lines
        sender.sendMessage(
                Component.text("/rewardads buy ", NamedTextColor.AQUA)
                        .append(Component.text("<name> ", NamedTextColor.WHITE))
                        .append(Component.text("- Open rewards gui", NamedTextColor.GRAY))
        );

        sender.sendMessage(
                Component.text("/rewardads connect ", NamedTextColor.AQUA)
                        .append(Component.text("<code> ", NamedTextColor.WHITE))
                        .append(Component.text("- Connect to your account", NamedTextColor.GRAY))
        );

        sender.sendMessage(
                Component.text("/rewardads disconnect ", NamedTextColor.AQUA)
                        .append(Component.text("- Disconnect from your account", NamedTextColor.GRAY))
        );

        sender.sendMessage(
                Component.text("/rewardads reload ", NamedTextColor.AQUA)
                        .append(Component.text("- Reload the plugin config", NamedTextColor.GRAY))
        );

        sender.sendMessage(
                Component.text("/rewardads version ", NamedTextColor.AQUA)
                        .append(Component.text("- Show plugin version", NamedTextColor.GRAY))
        );
    }

    private void sendMinimalHelp(CommandSource sender) {
        // Header line
        sender.sendMessage(
                Component.text("--- ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("RewardADs Help ", NamedTextColor.GOLD))
                        .append(Component.text("[", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Velocity", NamedTextColor.RED))
                        .append(Component.text("] ---", NamedTextColor.DARK_GRAY))
        );

        sender.sendMessage(Component.empty());

        // Help lines
        sender.sendMessage(
                Component.text("/rewardads reload ", NamedTextColor.AQUA)
                        .append(Component.text("- Reload the plugin config", NamedTextColor.GRAY))
        );

        sender.sendMessage(
                Component.text("/rewardads version ", NamedTextColor.AQUA)
                        .append(Component.text("- Show plugin version", NamedTextColor.GRAY))
        );
    }
}
