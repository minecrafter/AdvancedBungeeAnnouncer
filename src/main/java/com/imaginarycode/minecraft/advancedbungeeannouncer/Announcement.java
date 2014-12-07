/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Announcement
{
    @Getter
    private List<String> text = new ArrayList<>();
    @Getter
    private List<String> servers = new ArrayList<>();

    protected Announcement()
    {
        // Nope, not this time!
    }

    public static Announcement create(@NonNull List<String> text)
    {
        // Global announcement
        Announcement announcement = new Announcement();
        announcement.getText().addAll(text);
        announcement.getServers().add("global");
        return announcement;
    }

    public static Announcement create(@NonNull List<String> text, @NonNull List<String> servers)
    {
        // Server-specific announcements
        Announcement announcement = new Announcement();
        announcement.getText().addAll(text);
        announcement.getServers().addAll(servers);
        return announcement;
    }

    public static Announcement create(@NonNull String text, @NonNull List<String> servers)
    {
        return create(Lists.newArrayList(text), servers);
    }

    public void addServer(@NonNull String server)
    {
        if (AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().containsKey(server))
        {
            servers.add(server);
        }
    }

    public void removeServer(@NonNull String server)
    {
        if (AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().containsKey(server) &&
                servers.contains(server))
        {
            servers.remove(server);
        }
    }
}
