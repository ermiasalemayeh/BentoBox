package world.bentobox.bentobox.listeners.flags.worldsettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.FlagListener;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.util.Util;

/**
 * Handles respawning back on island
 * @author tastybento
 *
 */
public class IslandRespawnListener extends FlagListener {

    private final Map<UUID, UUID> respawn = new HashMap<>();

    /**
     * Tag players who die in island space and have an island
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        World world = Util.getWorld(e.getEntity().getWorld());
        if (world == null || !getIWM().inWorld(world)) {
            return; // not in the island world
        }
        if (!Flags.ISLAND_RESPAWN.isSetForWorld(world)) {
            return; // world doesn't have the island respawn flag
        }
        if (!getIslands().hasIsland(world, e.getEntity().getUniqueId()) && !getIslands().inTeam(world, e.getEntity().getUniqueId())) {
            return; // doesn't have an island in this world
        }

        respawn.put(e.getEntity().getUniqueId(), world.getUID());
    }

    /**
     * Place players back on their island if respawn on island is true and active
     * @param e - event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        final UUID worldUUID = respawn.remove(e.getPlayer().getUniqueId());
        if (worldUUID == null) {
            return; // no respawn world set
        }

        final World world = e.getPlayer().getServer().getWorld(worldUUID);
        if (world == null) {
            return; // world no longer available
        }
        World w = Util.getWorld(world);
        String ownerName = e.getPlayer().getName();
        if (w != null) {
            final Location respawnLocation = getIslands().getSafeHomeLocation(w, User.getInstance(e.getPlayer().getUniqueId()), "");
            if (respawnLocation != null) {
                e.setRespawnLocation(respawnLocation);
                // Get the island owner name
                Island island = BentoBox.getInstance().getIslands().getIsland(w, User.getInstance(e.getPlayer()));
                if (island != null) {
                    ownerName = BentoBox.getInstance().getPlayers().getName(island.getOwner());
                }
            }
        }
        // Run respawn commands, if any
        Util.runCommands(User.getInstance(e.getPlayer()), ownerName, getIWM().getOnRespawnCommands(world), "respawn");
    }

}
