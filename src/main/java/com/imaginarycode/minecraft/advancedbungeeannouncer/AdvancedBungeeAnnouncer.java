/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AdvancedBungeeAnnouncer extends Plugin {

    @Getter
    private static AdvancedBungeeAnnouncer plugin;
    @Getter
    private static Map<String, Announcement> announcements = new ConcurrentHashMap<>(10, 0.75f, 1);
    @Getter private static Configuration configuration;
    @Getter private static Configuration announcementConfiguration;

    @Override
    public void onEnable() {
        plugin = this;

        reloadConfiguration();

        if (announcements.size() == 0) {
            getLogger().severe("No announcements are configured.");
        }

        getProxy().getPluginManager().registerCommand(this, new AnnouncerCommand());
        getProxy().getScheduler().schedule(this, new AnnouncingTask(), 1, 1, TimeUnit.SECONDS);
    }

    public void addAnnouncement(String id, Announcement a) {
        announcements.put(id, a);
    }

    public void removeAnnouncement(String id) {
        announcements.remove(id);
    }

    protected void reloadConfiguration() {
        // Load the configuration (non-announcements)
        File cfg = new File(getDataFolder(), "config.yml");
        if (!cfg.exists()) {
            getDataFolder().mkdir();
            try (InputStream is = getResourceAsStream("defaultconfig.yml");
                 FileOutputStream fos = new FileOutputStream(cfg)) {
                ByteStreams.copy(is, fos);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "I/O error while reading or writing config files!", e);
            }
        }

        try
        {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cfg);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Could not load config.yml", e);
        }

        // Load and parse the announcements
        // Load the configuration (non-announcements)
        File annFile = new File(getDataFolder(), "announcements.yml");
        if (!annFile.exists()) {
            try (InputStream is = getResourceAsStream("defaultannouncements.yml");
                 FileOutputStream fos = new FileOutputStream(annFile)) {
                ByteStreams.copy(is, fos);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "I/O error while reading or writing config files!", e);
            }
        }

        try
        {
            announcementConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(annFile);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Could not load announcements.yml", e);
        }

        Collection<String> keys = announcementConfiguration.getSection("announcements").getKeys();

        for (String key : ImmutableSet.copyOf(announcements.keySet())) {
            if (!keys.contains(key))
                announcements.remove(key);
        }

        for (Object key : keys) {
            if (announcementConfiguration.get("announcements." + key + ".text") instanceof List) {
                if (announcementConfiguration.get("announcements." + key + ".servers") instanceof List) {
                    announcements.put((String)key, Announcement.create(announcementConfiguration.getStringList
                            ("announcements." + key + ".text"), announcementConfiguration.getStringList
                            ("announcements." + key + ".servers")));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for (String key : announcementConfiguration.getSection("announcements").getKeys()) {
            if (!announcements.containsKey(key)) {
                announcementConfiguration.set("announcements." + key + ".text", null);
                announcementConfiguration.set("announcements." + key + ".servers", null);
                announcementConfiguration.set("announcements." + key, null);
            }
        }
        for (String key : announcements.keySet()) {
            announcementConfiguration.set("announcements." + key + ".text", announcements.get(key).getText());
            announcementConfiguration.set("announcements." + key + ".servers", announcements.get(key).getServers());
        }
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(announcementConfiguration, new File(getDataFolder(), "announcements.yml"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "I/O error while writing announcements!", e);
        }
    }
}
