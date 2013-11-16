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
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
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

        Map<String, Announcement> sending = Maps.newHashMap();

        // Select our announcements.
        for (String i : AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().keySet()) {
            sending.put(i, selectAnnouncementFor(i));
        }

        for (ProxiedPlayer player : AdvancedBungeeAnnouncer.getPlugin().getProxy().getPlayers()) {
            // Find the server they are on, and give them an announcement.
            for (String line : sending.get(player.getServer().getInfo().getName()).getText()) {
                player.sendMessage(AdvancedBungeeAnnouncer.getConfiguration().getString("prefix", "") +
                        ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }

    private Announcement selectAnnouncementFor(String server) {
        List<Announcement> announcements = ImmutableList.copyOf(AdvancedBungeeAnnouncer.getAnnouncements().values());
        Announcement a;
        if (AdvancedBungeeAnnouncer.getConfiguration().getString("choose-announcement-via", "sequential").equals("sequential")) {
            while (true) {
                a = announcements.get(index.get(server));
                advanced(server);
                if (doesAnnouncementMatch(a, server))
                    return a;
            }
        } else {
            while (true) {
                a = announcements.get(rnd.nextInt(announcements.size()));
                if (doesAnnouncementMatch(a, server))
                    return a;
            }
        }
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
