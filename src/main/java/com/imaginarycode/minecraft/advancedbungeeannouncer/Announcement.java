/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class Announcement
{
    private final String text;
    private final int expiryTimestamp;
    private final List<String> servers = new ArrayList<>();
}
