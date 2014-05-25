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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class AnnouncingTask implements Runnable {
    private Map<String, Integer> index = Maps.newHashMap();
    private LoadingCache<String, Pattern> regexCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {
        @Override
        public Pattern load(String s) throws Exception {
            return Pattern.compile(s);
        }
    });
    private int timeSinceLastRun = 0;
    private Random rnd = new Random();

    public AnnouncingTask() {
        for (String i : AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().keySet()) {
            index.put(i, 0);
        }
    }

    @Override
    public void run() {
        if (timeSinceLastRun >= AdvancedBungeeAnnouncer.getConfiguration().getInt("delay", 180)) {
            timeSinceLastRun = 0;
        } else {
            timeSinceLastRun++;
            return;
        }

        // Select and display our announcements.
        for (Map.Entry<String, ServerInfo> entry : AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().entrySet()) {
            Announcement announcement = selectAnnouncementFor(entry.getKey());

            if (announcement == null)
                continue;

            List<BaseComponent[]> components = new ArrayList<>();

            for (String line : announcement.getText()) {
                components.add(TextComponent.fromLegacyText(AdvancedBungeeAnnouncer.getConfiguration().getString("prefix", "") +
                        ChatColor.translateAlternateColorCodes('&', line)));
            }

            for (ProxiedPlayer player : entry.getValue().getPlayers()) {
                for (BaseComponent[] component : components) {
                    player.sendMessage(component);
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
                a = announcements.get(index.get(server));
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
        index.put(key, index.get(key) + 1);
        if (index.get(key) == AdvancedBungeeAnnouncer.getAnnouncements().size()) {
            index.put(key, 0);
        }
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
