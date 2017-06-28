package com.imaginarycode.minecraft.advancedbungeeannouncer.config;

import com.google.common.base.Joiner;
import com.imaginarycode.minecraft.advancedbungeeannouncer.AdvancedBungeeAnnouncer;
import com.imaginarycode.minecraft.advancedbungeeannouncer.Announcement;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Getter
public class AnnouncementConfig
{
    private final AdvancedBungeeAnnouncer plugin;
    private final Map<String, Announcement> announcements = Collections.synchronizedMap(new LinkedHashMap<String, Announcement>());
    private SelectionMethod method;
    private AnnouncementDisplay display;
    private String prefix;
    private int delay;
    private int actionBarPeriod;
    private TitleDisplay titleDisplay;
    private BarDisplay barDisplay;

    public AnnouncementConfig(AdvancedBungeeAnnouncer plugin)
    {
        this.plugin = plugin;
        reloadConfiguration();
        reloadAnnouncements();
    }

    public void reloadAnnouncements()
    {
        Configuration announcements;

        try
        {
            announcements = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(persistIfRequired("announcements.yml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load announcements", e);
        }

        if (announcements.get("DO_NOT_TOUCH_VERSION") == null)
        {
            plugin.getLogger().info("No version found, so migrating your announcements.yml to a flattened structure (configuration version 1).");
            announcements = announcements.getSection("announcements");
            Collection<String> keys = announcements.getKeys();

            for (String key : keys)
            {
                if (announcements.get(key + ".text") instanceof List)
                {
                    announcements.set(key + ".text", Joiner.on('\n').join(announcements.getStringList(key + ".text")));
                }
            }

            announcements.set("DO_NOT_TOUCH_VERSION", 1);

            File old = new File(plugin.getDataFolder(), "announcements.yml.old");
            File conf = new File(plugin.getDataFolder(), "announcements.yml");

            try
            {
                if (!conf.renameTo(old))
                {
                    throw new IOException("Unable to rename " + conf.getAbsolutePath() + " to " + old.getAbsolutePath());
                }
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(announcements, conf);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not save announcements.yml", e);
            }
        }

        Collection<String> keys = announcements.getKeys();

        this.announcements.clear();

        for (String key : keys)
        {
            Object object = announcements.get(key);
            // API was changed to return Configuration instead of Map in BungeeCord commit 98e3c70
            if (!(object instanceof Map) && !(object instanceof Configuration)) continue;
           
            Announcement announcement = new Announcement(announcements.getString(key + ".text"),announcements.getString(key+".permission"));
            announcement.getServers().addAll(announcements.getStringList(key + ".servers"));
            
            this.announcements.put(key, announcement);
        }
    }

    public void reloadConfiguration()
    {
        Configuration configuration;

        try
        {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(persistIfRequired("config.yml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load config", e);
        }

        delay = configuration.getInt("delay");
        prefix = configuration.getString("prefix");

        try
        {
            method = SelectionMethod.valueOf(configuration.getString("choose-announcement-via").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().info("Invalid selection method " + configuration.getString("choose-announcement-via"));
            method = SelectionMethod.SEQUENTIAL;
        }

        try
        {
            display = AnnouncementDisplay.valueOf(configuration.getString("display").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().info("Invalid display method " + configuration.getString("display"));
            display = AnnouncementDisplay.CHAT;
        }

        actionBarPeriod = configuration.getInt("action-bar-period", 1);
        titleDisplay = TitleDisplay.deserialize(configuration);
        barDisplay = BarDisplay.deserialize(configuration);
    }

    public void saveAnnouncements()
    {
        Configuration configuration = new Configuration();

        for (Map.Entry<String, Announcement> entry : announcements.entrySet())
        {
            configuration.set(entry.getKey() + ".text", entry.getValue().getText());
            configuration.set(entry.getKey() + ".servers", entry.getValue().getServers());
            configuration.set(entry.getKey() + ".permission",entry.getValue().getPerm() );
        }

        configuration.set("DO_NOT_TOUCH_VERSION", 1);

        try
        {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getPlugin().getDataFolder(), "announcements.yml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to save announcements", e);
        }
    }

    private File persistIfRequired(String file) throws IOException
    {
        File cfg = new File(plugin.getDataFolder(), file);

        if (!cfg.exists())
        {
            plugin.getDataFolder().mkdir();
            try (InputStream is = plugin.getResourceAsStream(file))
            {
                Files.copy(is, cfg.toPath());
            }
        }

        return cfg;
    }
}
