package net.r_developing.rewardads;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Account {
    private final QuickConnect quickConnect;
    private final Messager messager;
    private final AdBits adBits;
    private final Buy buy;
    private final Config config;
    private final Api api;

    public Account(QuickConnect quickConnect, Messager messager, AdBits adBits, Buy buy, Config config, Api api) {
        this.quickConnect = quickConnect;
        this.messager = messager;
        this.adBits = adBits;
        this.buy = buy;
        this.config = config;
        this.api = api;
    }

    public void sendDetails(Player player) {
        if (quickConnect.isConnected(player)) {
            getDetails(player, (username, email) -> {
                player.sendMessage("§8--- §6RewardADs §8[§bMy Account§8] ---");
                player.sendMessage("");
                player.sendMessage("§8§l• §6Username: §7" + username);
                player.sendMessage("§8§l• §6Email: §7" + email);
                player.sendMessage("§8§l• §6AdBits: §7" + adBits.getAdBits(player));
                player.sendMessage("§8§l• §6Purchases: §7" + buy.getBuys(player));
            });
        } else {
            player.sendMessage(messager.get("notConnected"));
        }
    }

    private void getDetails(Player player, BiConsumer<String, String> callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", config.getUserId(player.getUniqueId()));

        api.send("getuser", payload, result -> {
            if (result != null) {
                boolean success = Boolean.parseBoolean((String) result.get("success"));

                if (success) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> user = (Map<String, Object>) result.get("user");

                    String username = user.getOrDefault("name_user",  "Unknown").toString();
                    String email    = user.getOrDefault("email_user", "Unknown").toString();
                    callback.accept(username, email);
                } else {
                    player.sendMessage(messager.get("invalidCode"));
                }
            } else {
                player.sendMessage(ChatColor.RED + "INTERNAL ERROR: No response from server.");
            }
        });
    }
}
