package net.R_Developing.rewardads;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import com.google.inject.Inject;
import java.nio.file.Path;

import net.R_Developing.rewardads.Configs.MainConfig;
import net.R_Developing.rewardads.Configs.MessagesConfig;

@Plugin(
    id = "rewardads",
    name = "RewardADs",
    version = "2026.04.30",
    description = "RewardADs plugin for Velocity",
    authors = {"R_Developing"}
)

public final class Main {
    @Getter
    private static Main instance;
    @Getter
    private final ProxyServer proxy;
    @Getter
    private final Logger logger;

    private final Path dataDirectory;

    @Inject
    public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        try {
            MessagesConfig messagesConfig = new MessagesConfig();
            MainConfig mainConfig = new MainConfig();
            Config config = new Config(this, mainConfig, messagesConfig, dataDirectory);

            Api api = new Api();
            Messager messager = new Messager(config);
            ProxySender proxySender = new ProxySender(this);
            Platform platform = new Platform(this, config, api, proxySender);
            api.setPlatform(platform);

            Fetcher fetcher = new Fetcher(this, api, config, platform, null, 60);
            Version version = new Version(this, fetcher, messager);
            QuickConnect quickConnect = new QuickConnect(messager, api, config);
            AdBits adBits = new AdBits(fetcher, config, messager);
            Buy buy = new Buy(this.getProxy(), logger, fetcher, api, platform, config, messager, proxySender, adBits);
            Account account = new Account(quickConnect, messager, adBits, buy, config, api);
            fetcher.setBuy(buy);

            ChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.create("rewardads", "command");
            proxy.getChannelRegistrar().register(channelIdentifier);
            proxy.getEventManager().register(this, new ProxyListener(this, buy));

            platform.checkAndStart(fetcher, messager, version, quickConnect, adBits, buy, account, platform);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("RewardADs Velocity plugin disabled.");
    }
}
