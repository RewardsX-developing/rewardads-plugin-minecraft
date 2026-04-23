package net.R_Developing.rewardads;

import lombok.Getter;
import net.R_Developing.rewardads.Configs.MainConfig;
import net.R_Developing.rewardads.Configs.MessagesConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class Main extends Plugin {
    @Getter
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        File dataFolder = getDataFolder();

        try {
            MessagesConfig messagesConfig = new MessagesConfig();
            MainConfig mainConfig = new MainConfig();
            Config config = new Config(this, mainConfig, messagesConfig, dataFolder);

            Api api = new Api();
            Messager messager = new Messager(config);
            ProxySender proxySender = new ProxySender();
            Platform platform = new Platform(this, config, api, proxySender);
            api.setPlatform(platform);

            Fetcher fetcher = new Fetcher(this, api, config, platform, null, 60);
            Version version = new Version(this, fetcher, messager);
            QuickConnect quickConnect = new QuickConnect(messager, api, config);
            AdBits adBits = new AdBits(fetcher, config, messager);
            Buy buy = new Buy(this, fetcher, api, platform, config, messager, proxySender, adBits);
            Account account = new Account(quickConnect, messager, adBits, buy, config, api);
            fetcher.setBuy(buy);

            platform.checkAndStart(fetcher, messager, version, quickConnect, adBits, buy, platform, account);
            ProxyServer.getInstance().registerChannel("rewardads:command");
            ProxyServer.getInstance().getPluginManager().registerListener(this, new ProxyListener(this, buy));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("RewardADs BungeeCord plugin disabled.");
    }
}
