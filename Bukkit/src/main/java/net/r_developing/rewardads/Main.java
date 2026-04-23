package net.r_developing.rewardads;

import lombok.Getter;

import net.r_developing.rewardads.Configs.MainConfig;
import net.r_developing.rewardads.Configs.MessagesConfig;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class Main extends JavaPlugin {
    @Getter
    public static Main instance;

    @Getter
    private Api api;

    @Getter
    private Platform platform;

    @Override
    public void onEnable() {
        instance = this;

        if(!getDataFolder().exists())
            getDataFolder().mkdirs();

        Path dataPath = getDataFolder().toPath();
        MessagesConfig messagesConfig = new MessagesConfig();
        MainConfig mainConfig = new MainConfig();
        Config config = new Config(this, mainConfig, messagesConfig, dataPath);

        api = new Api();
        platform = new Platform(this, config);
        api.setPlatform(platform);

        Translator translator = new Translator(api);
        Messager messager = new Messager(config, platform, translator, this);
        Fetcher fetcher = new Fetcher(this, api, config, platform, null, 60);
        ProxySender proxySender = new ProxySender(this);
        AdBits adBits = new AdBits(fetcher, config, messager);
        Buy buy = new Buy(this, fetcher, api, platform, config, messager, proxySender, adBits);
        fetcher.setBuy(buy);

        Version version = new Version(this, fetcher, messager);
        RewardsGUI rewardsGUI = new RewardsGUI(fetcher, messager, buy);
        QuickConnect quickConnect = new QuickConnect(messager, api, config);
        Account account = new Account(quickConnect, messager, adBits, buy, config, api);

        ProxyListener proxy = new ProxyListener(this, rewardsGUI, buy);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "rewardads:command", proxy);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "rewardads:command");

        try {
            new PApi(version, fetcher, this, messager, config).register();
        } catch(Exception ignored) {
            getLogger().info("PlaceholderAPI was not found. Some features may not work as expected");
        }

        platform.checkAndStart(fetcher, rewardsGUI, messager, version, quickConnect, adBits, buy, account);
    }

    @Override
    public void onDisable() {
        getLogger().info("RewardADs has been disabled.");
    }
    public String getVersion() {
        return getDescription().getVersion();
    }
}

