/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.advancedbungeeannouncer.config.SelectionMethod;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.*;
import java.util.regex.Pattern;

public class AnnouncingTask implements Runnable
{
    private final Map<String, Integer> index = new HashMap<>();
    private int timeSinceLastRun = 0;
    private final Random rnd = new Random();
    private List<Announcement> announcements;

    @Override
    public void run()
    {
        if (timeSinceLastRun + 1 >= AdvancedBungeeAnnouncer.getConfiguration().getDelay())
        {
            timeSinceLastRun = 0;
        } else
        {
            timeSinceLastRun++;
            return;
        }

        announcements = ImmutableList.copyOf(AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().values());

        if (announcements.isEmpty())
            return;

        String prefix = ChatColor.translateAlternateColorCodes('&', AdvancedBungeeAnnouncer.getConfiguration().getPrefix());

        // Select and display our announcements.
        Map<String, Announcement> serverAnnouncements = new HashMap<>();
        Map<String, BaseComponent[]> perPlayerAnnouncements = new HashMap<>();

        Collection<String> online = new HashSet<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers())
        {
            if (player.getServer() == null)
            {
                // No use in giving connecting players an announcement
                continue;
            }

            online.add(player.getName());

            ServerInfo info = player.getServer().getInfo();

            if (player.hasPermission("advancedbungeeannouncer.ignore") ||
                    player.hasPermission("advancedbungeeannouncer.ignore.server." + info.getName()))
                continue;

            Announcement announcement = serverAnnouncements.get(info.getName());

            if (announcement == null)
                serverAnnouncements.put(info.getName(), announcement = selectAnnouncementFor(info.getName()));

            if (announcement == null)
                continue;

            BaseComponent[] components;

            String line = announcement.getText();

            if (line.startsWith("{") || line.startsWith("["))
            {
                try
                {
                    BaseComponent[] components2 = ComponentSerializer.parse(line);
                    BaseComponent[] prefixComp = TextComponent.fromLegacyText(prefix);

                    if (prefixComp.length != 0)
                        prefixComp[prefixComp.length - 1].setExtra(Arrays.asList(components2));
                    else
                        prefixComp = components2;

                    components = prefixComp;
                }
                catch (Exception ignored)
                {
                    components = TextComponent.fromLegacyText(prefix + ChatColor.translateAlternateColorCodes('&', line));
                }
            } else
            {
                components = TextComponent.fromLegacyText(prefix + ChatColor.translateAlternateColorCodes('&', line));
            }

            perPlayerAnnouncements.put(player.getName(), components);
        }

        switch (AdvancedBungeeAnnouncer.getConfiguration().getDisplay())
        {
            case CHAT:
                for (Map.Entry<String, BaseComponent[]> entry : perPlayerAnnouncements.entrySet())
                {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(entry.getKey());

                    if (player != null)
                    {
                        player.sendMessage(entry.getValue());
                    }
                }
                break;
            case ACTION:
                // We aren't able to do much better than this
                new ActionBarRepeatingTask(perPlayerAnnouncements, AdvancedBungeeAnnouncer.getConfiguration().getActionBarPeriod()).start();
                break;
            case TITLE:
                for (Map.Entry<String, BaseComponent[]> entry : perPlayerAnnouncements.entrySet())
                {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(entry.getKey());

                    if (player != null)
                    {
                        ProxyServer.getInstance().createTitle()
                                .fadeIn(AdvancedBungeeAnnouncer.getConfiguration().getTitleDisplay().getFadeIn())
                                .fadeOut(AdvancedBungeeAnnouncer.getConfiguration().getTitleDisplay().getFadeOut())
                                .stay(AdvancedBungeeAnnouncer.getConfiguration().getTitleDisplay().getStay())
                                .title(AdvancedBungeeAnnouncer.getConfiguration().getTitleDisplay().getTitle())
                                .subTitle(entry.getValue())
                                .send(player);
                    }
                }
                break;
        }

        // Clean up the index map afterwards
        index.keySet().retainAll(online);
    }

    private Announcement selectAnnouncementFor(String server)
    {
        Announcement a;
        int tries = 0;
        if (AdvancedBungeeAnnouncer.getConfiguration().getMethod() == SelectionMethod.SEQUENTIAL)
        {
            while (tries < 5)
            {
                Integer idx = index.get(server);

                if (idx == null || idx >= announcements.size())
                {
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
        } else
        {
            while (tries < 5)
            {
                a = announcements.get(rnd.nextInt(announcements.size()));
                if (doesAnnouncementMatch(a, server))
                    return a;
                tries++;
            }
            // Forget it, let's just find one.
            for (Announcement announcement : announcements)
            {
                if (doesAnnouncementMatch(announcement, server))
                    return announcement;
            }
        }
        return null;
    }

    private void advanced(String key)
    {
        int val = index.get(key);

        if (val + 1 >= AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().size())
            index.put(key, 0);
        else
            index.put(key, val + 1);
    }

    private boolean doesAnnouncementMatch(Announcement announcement, String server)
    {
        if (announcement.getServers().contains(server) || announcement.getServers().contains("global"))
        {
            return true;
        }

        for (String s : announcement.getServers())
        {
            if (Pattern.compile(s).matcher(server).find())
            {
                return true;
            }
        }

        return false;
    }
}
