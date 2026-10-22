package dev.prestige.client.config;

import java.time.Instant;

/** A named snapshot of module states and keybinds, stored as its own file. */
public record Profile(String name, Instant lastModified) {
}
