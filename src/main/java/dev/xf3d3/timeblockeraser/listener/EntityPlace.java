package dev.xf3d3.timeblockeraser.listener;

import dev.xf3d3.timeblockeraser.TimeBlockEraser;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityPlace implements Listener {
    private final TimeBlockEraser plugin;

    public EntityPlace(@NotNull TimeBlockEraser plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onEntityPlace(EntityPlaceEvent event) {
        final List<String> worlds = plugin.getSettings().getWorlds();

        // Check if the world is enabled
        if (!worlds.contains(event.getEntity().getWorld().getName())) {
            return;
        }

        Entity entity = event.getEntity();
        Player player = event.getPlayer();

        for (String key : plugin.getSettings().getEntities().keySet()) {
            EntityType type = EntityType.valueOf(key);

            // Check if the entity should be removed
            if (entity.getType() != type || player == null) {
                continue;
            }

            // Check if the player has the bypass permission
            if (plugin.getSettings().enableBypass() && player.hasPermission("timeblockeraser.bypass")) {
                continue;
            }

            plugin.runLater(entity::remove, plugin.getSettings().getEntities().get(key));
        }
    }
}
