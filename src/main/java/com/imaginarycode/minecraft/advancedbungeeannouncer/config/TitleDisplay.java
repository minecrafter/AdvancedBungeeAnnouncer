package com.imaginarycode.minecraft.advancedbungeeannouncer.config;

import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

@Data
public class TitleDisplay {
    private final BaseComponent[] title;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    static TitleDisplay deserialize(Configuration configuration) {
        BaseComponent[] title = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                configuration.getString("title.title")));
        int fadeIn = configuration.getInt("title.fade-in", 40);
        int stay = configuration.getInt("title.stay", 80);
        int fadeOut = configuration.getInt("title.fade-out", 40);
        return new TitleDisplay(title, fadeIn, stay, fadeOut);
    }
}
