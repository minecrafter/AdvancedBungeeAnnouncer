/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AnnouncingTask implements Runnable {
    private Map<String, Integer> index = new HashMap<>();
    private LoadingCache<String, Pattern> regexCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {
        @Override
        public Pattern load(String s) throws Exception {
            return Pattern.compile(s);
        }
    });
    private int timeSinceLastRun = 0;
    private Random rnd = new Random();
    private Gson gson = new Gson();

    @Override
    public void run() {
        if (timeSinceLastRun + 1 >= AdvancedBungeeAnnouncer.getConfiguration().getInt("delay", 180)) {
            timeSinceLastRun = 0;
        } else {
            timeSinceLastRun++;
            return;
        }

        if (AdvancedBungeeAnnouncer.getAnnouncements().isEmpty())
            return;

        String prefix = ChatColor.translateAlternateColorCodes('&', AdvancedBungeeAnnouncer.getConfiguration().getString("prefix", ""));

        // Select and display our announcements.
        Map<String, Announcement> selectedAnnouncements = new HashMap<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getServer() == null) {
                // No use in giving connecting players an announcement
                continue;
            }

            ServerInfo info = player.getServer().getInfo();

            if (player.hasPermission("advancedbungeeannouncer.ignore") ||
                    player.hasPermission("advancedbungeeannouncer.ignore.server." + info.getName()))
                continue;

            if (!index.containsKey(info.getName()))
                index.put(info.getName(), 0);

            Announcement announcement = selectedAnnouncements.get(info.getName());

            if (announcement == null)
                selectedAnnouncements.put(info.getName(), announcement = selectAnnouncementFor(info.getName()));

            if (announcement == null)
                continue;

            List<BaseComponent[]> components = new ArrayList<>();

            for (String line : announcement.getText()) {
                if (line.startsWith("{")) {
                    try {
                        BaseComponent[] components2 = ComponentSerializer.parse(line);
                        BaseComponent[] prefixComp = TextComponent.fromLegacyText(prefix);

                        if (prefixComp.length != 0)
                            prefixComp[prefixComp.length - 1].setExtra(Arrays.asList(components2));
                        else
                            prefixComp = components2;

                        components.add(prefixComp);
                    } catch (Exception ignored) {
                        components.add(TextComponent.fromLegacyText(prefix + ChatColor.translateAlternateColorCodes('&', line)));
                    }
                } else {
                    components.add(TextComponent.fromLegacyText(prefix + ChatColor.translateAlternateColorCodes('&', line)));
                }
            }

            for (BaseComponent[] component : components) {
                if (AdvancedBungeeAnnouncer.getConfiguration().getString("display", "chat").equals("chat")) {
                    player.sendMessage(component);
                } else {
                    // WTF?!? With 1.8 you have to use legacy text inside chat JSON.
                    if (player.getPendingConnection().getVersion() >= 47) { // 1.8
                        JsonObject object = new JsonObject();
                        object.addProperty("text", TextComponent.toLegacyText(component));
                        player.unsafe().sendPacket(new Chat(gson.toJson(object), (byte) 2));
                    } else {
                        player.sendMessage(component);
                    }
                }
            }
        }
    }

    private Announcement selectAnnouncementFor(String server) {
        List<Announcement> announcements = ImmutableList.copyOf(AdvancedBungeeAnnouncer.getAnnouncements().values());
        Announcement a;
        int tries = 0;
        if (AdvancedBungeeAnnouncer.getConfiguration().getString("choose-announcement-via", "sequential").equals("sequential")) {
            while (tries < 5) {
                int idx = index.get(server);

                if (idx >= announcements.size()) {
                    // Reset the index
                    idx = 0;
                    index.put(server, 0);
                }

                a = announcements.get(idx);
                advanced(server);
                if (doesAnnouncementMatch(a, server))
                    return a;
                tries++;
            }
        } else {
            while (tries < 5) {
                a = announcements.get(rnd.nextInt(announcements.size()));
                if (doesAnnouncementMatch(a, server))
                    return a;
                tries++;
            }
            // Forget it, let's just find one.
            for (Announcement announcement : announcements) {
                if (doesAnnouncementMatch(announcement, server))
                    return announcement;
            }
        }
        return null;
    }

    private void advanced(String key) {
        int val = index.get(key);

        if (val + 1 >= AdvancedBungeeAnnouncer.getAnnouncements().size())
            index.put(key, 0);
        else
            index.put(key, val + 1);
    }

    private List<Pattern> producePatterns(List<String> patterns) {
        List<Pattern> patterns1 = new ArrayList<>();
        for (String pattern : patterns) {
            patterns1.add(regexCache.getUnchecked(pattern));
        }
        return patterns1;
    }

    private boolean doesAnnouncementMatch(Announcement announcement, String server) {
        if (announcement.getServers().contains(server) || announcement.getServers().contains("global")) {
            return true;
        }
        for (Pattern pattern : producePatterns(announcement.getServers())) {
            if (pattern.matcher(server).find()) {
                return true;
            }
        }
        return false;
    }
}
