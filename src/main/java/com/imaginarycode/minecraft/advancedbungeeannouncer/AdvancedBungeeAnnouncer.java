/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.imaginarycode.minecraft.advancedbungeeannouncer.config.AnnouncementConfig;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class AdvancedBungeeAnnouncer extends Plugin
{
    @Getter
    private static AdvancedBungeeAnnouncer plugin;
    @Getter
    private static AnnouncementConfig configuration;

    @Override
    public void onEnable()
    {
        plugin = this;
        configuration = new AnnouncementConfig(this);

        if (configuration.getAnnouncements().isEmpty())
        {
            getLogger().severe("No announcements are configured.");
        }

        getProxy().getPluginManager().registerCommand(this, new AnnouncerCommand());
        getProxy().getScheduler().schedule(this, new AnnouncingTask(), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable()
    {
        configuration.saveAnnouncements();
    }
}
