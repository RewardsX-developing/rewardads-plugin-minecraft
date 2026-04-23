package net.R_Developing.rewardads;

import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Fetcher {
    private final Plugin plugin;
    private final Api api;
    private final long intervalSeconds; // seconds, not ticks
    private final Config config;
    private final Platform platform;

    @Setter
    private Buy buy;

    public final Map<String, Integer> adbitsList = new HashMap<>();
    public final Map<String, Integer> buysList = new HashMap<>();
    public String latestVersion = "";

    public Fetcher(Plugin plugin, Api api, Config config, Platform platform, Buy buy, int intervalSeconds) {
        this.plugin = plugin;
        this.api = api;
        this.intervalSeconds = intervalSeconds;
        this.config = config;
        this.platform = platform;
        this.buy = buy;
    }

    public void start(Consumer<List<Map<String, String>>> callback) {
        ProxyServer.getInstance().getScheduler().schedule(
                plugin,
                () -> {
                    // Version check
                    try (InputStream in = new URL("https://api.spiget.org/v2/resources/121867/versions/latest").openStream();
                         Scanner scanner = new Scanner(in)) {

                        String json = scanner.useDelimiter("\\A").next();
                        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                        String version = obj.get("name").getAsString();
                        latestVersion = version.split(" ")[0];
                    } catch (Exception e) {
                        if (platform.isDebug()) {
                            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                        }
                    }

                    // getmultiplebuys
                    Map<String, Object> buysPayload = new HashMap<>();
                    List<String> userIds = config.getAllUserIds();
                    buysPayload.put("ids", userIds);
                    api.send("getmultiplebuys", buysPayload, result -> {
                        buysList.clear();
                        if (result != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("buys");
                            if (list != null) {
                                for (Map<String, Object> o : list) {
                                    String idPlayer = (String) o.get("id_player");
                                    int count = ((Number) o.get("buys_count")).intValue();
                                    buysList.put(idPlayer, count);
                                }
                            }
                        }
                    });

                    // getmultipleadbits
                    Map<String, Object> adbitsPayload = new HashMap<>();
                    adbitsPayload.put("ids", userIds);
                    api.send("getmultipleadbits", adbitsPayload, result -> {
                        adbitsList.clear();
                        if (result != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("adbits");
                            if (list != null) {
                                for (Map<String, Object> o : list) {
                                    String idPlayer = (String) o.get("id_player");
                                    int adbits = ((Number) o.get("adbits_player")).intValue();
                                    adbitsList.put(idPlayer, adbits);
                                }
                            }
                        }
                    });

                    // getsuccessbuys
                    Map<String, Object> successBuysPayload = new HashMap<>();
                    successBuysPayload.put("platform", platform.getId());
                    api.send("getsuccessbuys", successBuysPayload, result -> {
                        if (result != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("successbuys");

                            if (list != null) {
                                for (Map<String, Object> o : list) {
                                    String userId = (String) o.get("userId");
                                    String rewardId = (String) o.get("rewardId");
                                    String username = (String) o.get("username");

                                    if (username != null) {
                                        buy.confirm(userId, rewardId, username);
                                    } else {
                                        buy.confirm(userId, rewardId);
                                    }
                                }
                            }
                        }
                    });
                },
                0L,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }
}