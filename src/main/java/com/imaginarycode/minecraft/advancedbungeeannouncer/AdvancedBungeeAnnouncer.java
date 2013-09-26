/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AdvancedBungeeAnnouncer extends Plugin {

    @Getter
    private static AdvancedBungeeAnnouncer plugin;
    private static Map<String, Announcement> announcements = Maps.newHashMap();
    @Getter private static YamlConfiguration configuration;
    @Getter private static YamlConfiguration announcementConfiguration;

    public static Map<String, Announcement> getAnnouncements() {
        return ImmutableMap.copyOf(announcements);
    }

    @Override
    public void onEnable() {
        plugin = this;

        reloadConfiguration();

        if (announcements.size() == 0) {
            getLogger().severe("No announcements are configured :(");
            return;
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
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream("defaultconfig.yml")));
                BufferedWriter bw = new BufferedWriter(new FileWriter(cfg));
                String line;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
                br.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "I/O error while reading or writing config files!", e);
            }
        }

        configuration = YamlConfiguration.loadConfiguration(cfg);

        // Load and parse the announcements
        // Load the configuration (non-announcements)
        File annFile = new File(getDataFolder(), "announcements.yml");
        if (!annFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream("defaultannouncements.yml")));
                BufferedWriter bw = new BufferedWriter(new FileWriter(annFile));
                String line;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
                br.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "I/O error while reading or writing config files!", e);
            }
        }

        announcementConfiguration = YamlConfiguration.loadConfiguration(annFile);
        announcements.clear();

        for (String key : announcementConfiguration.getConfigurationSection("announcements").getKeys(false)) {
            if (announcementConfiguration.isList("announcements." + key + ".text")) {
                if (announcementConfiguration.isList("announcements." + key + ".servers")) {
                    announcements.put(key, Announcement.create(announcementConfiguration.getStringList
                            ("announcements." + key + ".text"), announcementConfiguration.getStringList
                            ("announcements." + key + ".servers")));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for (String key : announcementConfiguration.getConfigurationSection("announcements").getKeys(false)) {
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
            announcementConfiguration.save(new File(getDataFolder(), "announcements.yml"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "I/O error while writing announcements!", e);
        }
    }
}
