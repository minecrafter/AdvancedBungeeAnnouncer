package com.imaginarycode.minecraft.advancedbungeeannouncer.config;

import com.imaginarycode.minecraft.advancedbungeeannouncer.AdvancedBungeeAnnouncer;
import com.imaginarycode.minecraft.advancedbungeeannouncer.bossbar.BarColor;
import com.imaginarycode.minecraft.advancedbungeeannouncer.bossbar.BarDivision;
import lombok.Value;
import net.md_5.bungee.config.Configuration;

@Value
public class BarDisplay {
    private final BarColor color;
    private final BarDivision style;

    static BarDisplay deserialize(Configuration configuration)
    {
        BarColor color;
        try
        {
            color = BarColor.valueOf(configuration.getString("bar-display.color", "WHITE").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            AdvancedBungeeAnnouncer.getPlugin().getLogger().info("Invalid bar color " +
                    configuration.getString("color"));
            color = BarColor.WHITE;
        }

        BarDivision style;
        try
        {
            style = BarDivision.valueOf(configuration.getString("bar-display.division", "NONE").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            AdvancedBungeeAnnouncer.getPlugin().getLogger().info("Invalid bar style " +
                    configuration.getString("style"));
            style = BarDivision.NONE;
        }

        return new BarDisplay(color, style);
    }
}
