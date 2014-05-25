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
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Announcement {
    @NonNull @Getter @Setter private List<String> text = Collections.emptyList();
    @Getter @Setter private List<String> servers = Lists.newArrayList();

    protected Announcement() {
        // Nope, not this time!
    }

    public static Announcement create(@NonNull List<String> text) {
        // Global announcement
        Announcement announcement = new Announcement();
        announcement.setText(new ArrayList<>(text));
        announcement.setServers(Lists.newArrayList("global"));
        return announcement;
    }

    public static Announcement create(@NonNull List<String> text, @NonNull List<String> servers) {
        // Server-specific announcements
        Announcement announcement = create(text);
        announcement.setServers(new ArrayList<>(servers));
        return announcement;
    }

    public static Announcement create(@NonNull String text, @NonNull List<String> servers) {
        return create(Lists.newArrayList(text), servers);
    }

    public void addServer(@NonNull String server) {
        if (AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().containsKey(server)) {
            servers.add(server);
        }
    }

    public void removeServer(@NonNull String server) {
        if (AdvancedBungeeAnnouncer.getPlugin().getProxy().getServers().containsKey(server) &&
                servers.contains(server)) {
            servers.remove(server);
        }
    }

    public void addLine(@NonNull String line) {
        text.add(line);
    }

    public void removeLine(@NonNull String line) {
        text.remove(line);
    }

    public void setLine(int id, @NonNull String line) {
        text.set(id, line);
    }
}
