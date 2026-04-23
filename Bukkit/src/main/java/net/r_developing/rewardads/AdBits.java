package net.r_developing.rewardads;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdBits {
    private final Fetcher fetcher;
    private final Config config;
    private final Messager messager;

    public AdBits(Fetcher fetcher, Config config, Messager messager) {
        this.fetcher = fetcher;
        this.config = config;
        this.messager = messager;
    }

    public int getAdBits(Player player) {
        String userId = config.getUserId(player.getUniqueId());
        if(userId == null) return 0;
        return fetcher.adbitsList.getOrDefault(userId, 0);
    }

    public void removeAdBits(Player player, int amount) {
        String userId = config.getUserId(player.getUniqueId());
        if(userId == null || amount <= 0) return;
        int currentBalance = fetcher.adbitsList.getOrDefault(userId, 0);
        int newBalance = currentBalance - amount;
        fetcher.adbitsList.put(userId, newBalance);
    }
}
