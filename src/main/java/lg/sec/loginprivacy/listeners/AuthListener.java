package lg.sec.loginprivacy.listeners;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.database.Database;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import lg.sec.loginprivacy.listeners.events.SetLoginLocationEvent;
import lg.sec.loginprivacy.listeners.hashingUtils.PasswordHarsher;
import lg.sec.loginprivacy.resourcesConfigGenerator.AuthConfigurationConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class AuthListener implements Listener {

    private final Map<UUID, Integer> scheduledTimers = new HashMap<>();
    public AuthConfigurationConfig authConfigurationConfig;
    private int mainSchedulerId;
    private boolean authDisabled;
    private int joinMessageDelay;
    private LoginPrivacy loginPrivacy;
    private List<UUID> loggedPlayers = new ArrayList<>();
    private Database database;

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.authConfigurationConfig = this.loginPrivacy.getConfigsManager().authConfigurationConfig;
        this.joinMessageDelay = this.authConfigurationConfig.getJoinMessageDelay();
        this.authDisabled = this.authConfigurationConfig.isAuthDisabled();
        this.database = this.loginPrivacy.getSqlManager().getDatabase();
        this.loginPrivacy.getServer().getPluginManager().registerEvents(this, this.loginPrivacy);
        this.loggedPlayers = this.database.getAllPlayersFromSession();
        deleteAllNotExistingWorlds();
        addSchedulersToAllOnlinePlayersOnReload();
        updatePlayerLocationInScheduler();
    }


    private void addSchedulersToAllOnlinePlayersOnReload() {
        if (!this.authDisabled) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!database.playerIsInSession(player.getUniqueId())) {
                    specifyLoginMessage(player);
                }
            }
        }
    }

    private List<UUID> returnRetailedList(List<UUID> mainList, List<UUID> listToRetail) {
        return listToRetail.stream().filter(item -> !mainList.contains(item)).collect(Collectors.toList());
    }

    private void deleteAllNotExistingWorlds() {
        List<UUID> lastSeenLocationsWorldsUUIDs = this.database.getAllLastSeenLocationsUUIDs();
        List<UUID> loginLocationWorldsUUIDs = this.database.getAllLoginLocationsUUIDs();

        for (UUID uuid : returnRetailedList(Bukkit.getWorlds().stream().map(World::getUID).collect(Collectors.toList()), lastSeenLocationsWorldsUUIDs)) {
            this.database.deleteAllLastSeenLocation(uuid);
        }

        for (UUID uuid : returnRetailedList(Bukkit.getWorlds().stream().map(World::getUID).collect(Collectors.toList()), loginLocationWorldsUUIDs)) {
            this.database.deleteAllLoginNullWorlds(uuid);
        }
    }

    private void updatePlayerLocationInScheduler() {
        this.mainSchedulerId = LoginPrivacy.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(this.loginPrivacy, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateLastSeenLocation(player);
            }
        }, 0, 18000);
    }

    private void updateLastSeenLocation(Player player) {
        if (!database.playerHasLastSeenLocation(player.getUniqueId())) {
            database.setLastSeenLocation(
                    player.getUniqueId(),
                    player.getLocation().getWorld().getUID(),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getLocation().getPitch(),
                    player.getLocation().getYaw());
        } else {
            this.database.updateLastSeenLocation(
                    player.getUniqueId(),
                    player.getLocation().getWorld().getUID(),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getLocation().getPitch(),
                    player.getLocation().getYaw());
        }
    }

    private boolean login(UUID uuid, String rawPassword) {
        String hashedPassword = this.database.getPlayerHashedPasswordByUUID(uuid);
        if (hashedPassword == null) {
            return false;
        }
        return new PasswordHarsher().matches(rawPassword, hashedPassword);
    }

    private void specifyLoginMessage(Player player) {
        boolean isRegistered = this.database.playerIsRegistered(player.getUniqueId());
        int id = LoginPrivacy.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(this.loginPrivacy, () -> {
            if (isRegistered) {
                player.sendMessage(LoginPrivacy.convertColors("&fYou need to login: &e&l/login [password]"));
            } else {
                player.sendMessage(LoginPrivacy.convertColors("&fYou need to register first: \n &e&l/register [password] [password]"));
            }

        }, 0, this.joinMessageDelay);
        scheduledTimers.put(player.getUniqueId(), id);
    }

    private void cleanOldSession(Player player) {
        if (this.database.playerIsInSession(player.getUniqueId())) {
            this.database.removePlayerFromSessionByUUID(player.getUniqueId());
            scheduledTimers.remove(player.getUniqueId());
            loggedPlayers.remove(player.getUniqueId());
        }
        if (!this.authDisabled) {
            specifyLoginMessage(player);
        }
    }

    private void removeAllNegativePotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFireTicks(0);
    }

    @EventHandler
    private void onSetLoginLocation(SetLoginLocationEvent event) {
        if (!this.database.loginLocationExists(event.getNewLocation().getWorld().getUID())) {
            this.database.setLoginLocationOnJoin(
                    event.getNewLocation().getWorld().getUID(),
                    event.getNewLocation().getX(),
                    event.getNewLocation().getY(),
                    event.getNewLocation().getZ(),
                    event.getNewLocation().getPitch(),
                    event.getNewLocation().getYaw()
            );
        } else {
            this.database.updateLoginLocation(
                    event.getNewLocation().getWorld().getUID(),
                    event.getNewLocation().getX(),
                    event.getNewLocation().getY(),
                    event.getNewLocation().getZ(),
                    event.getNewLocation().getPitch(),
                    event.getNewLocation().getYaw()
            );
        }
    }

    private void teleportPlayerToLoginLocation(Player player) {
        if (this.authConfigurationConfig.isAfterLoginTeleportToLastLocation()) {
            Location location = this.database.getLoginLocation(player.getLocation().getWorld().getUID());
            if (location != null && location.getWorld() != null) {
                player.setInvulnerable(true);
                player.teleport(location);
            }
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        teleportPlayerToLoginLocation(event.getPlayer());
        cleanOldSession(event.getPlayer());
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        if (!this.authDisabled) {
            authenticatePlayer(event.getPlayer(), event.getPassword());
        } else {
            event.getPlayer().sendMessage(LoginPrivacy.convertColors("&cAuth is Disabled"));
        }
    }

    private void updateLastSeenLocationOnQuit(Player player) {
        this.database.updateLastSeenLocation(
                player.getUniqueId(),
                player.getLocation().getWorld().getUID(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getLocation().getPitch(),
                player.getLocation().getYaw());
    }

    private void authenticatePlayer(Player player, String password) {
        if (!database.playerIsInSession(player.getUniqueId())) {
            if (login(player.getUniqueId(), password)) {
                this.database.addPlayerToSession(player.getUniqueId());
                LoginPrivacy.getInstance().getServer().getScheduler().cancelTask(scheduledTimers.get(player.getUniqueId()));
                scheduledTimers.remove(player.getUniqueId());
                if (this.authConfigurationConfig.isAfterLoginTeleportToLastLocation()) {
                    Location location = this.database.getLastSeenLocation(player.getUniqueId());
                    if (location != null && location.getWorld() != null) {
                        player.teleport(location);
                        player.sendMessage(LoginPrivacy.convertColors("&cYou are Invulnerable for &f5 sec"));
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.loginPrivacy, () -> {
                            player.sendMessage(LoginPrivacy.convertColors("&cYou are no more Invulnerable"));
                            player.setInvulnerable(false);
                            removeAllNegativePotionEffects(player);
                        }, 100);
                    }
                }
                updateLastSeenLocation(player);
                player.sendMessage(LoginPrivacy.convertColors("&aYou successfully logged in"));
            } else {
                player.sendMessage(LoginPrivacy.convertColors("&4Wrong Password"));
            }
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
        } else {
            player.sendMessage(LoginPrivacy.convertColors("&cYou already logged in!"));

        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        updateLastSeenLocationOnQuit(event.getPlayer());
        if (database.playerIsInSession(event.getPlayer().getUniqueId())) {
            this.database.removePlayerFromSessionByUUID(event.getPlayer().getUniqueId());
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
        }
    }

    @EventHandler
    private void onRegister(RegisterEvent event) {
        if (!this.authDisabled) {
            String hashedPassword = new PasswordHarsher().encode(event.getMatchedPassword());
            if (!database.playerIsRegistered(event.getPlayer().getUniqueId())) {
                this.database.registerPlayerInDatabase(event.getPlayer().getUniqueId(), hashedPassword);
                this.database.addPlayerToSession(event.getPlayer().getUniqueId());
                this.loginPrivacy.getServer().getScheduler().cancelTask(scheduledTimers.get(event.getPlayer().getUniqueId()));
                scheduledTimers.remove(event.getPlayer().getUniqueId());
                this.loggedPlayers.clear();
                this.loggedPlayers = this.database.getAllPlayersFromSession();
                event.getPlayer().sendMessage(LoginPrivacy.convertColors("&aYou successfully registered"));
            } else {
                event.getPlayer().sendMessage(LoginPrivacy.convertColors("&eYou are already registered"));
            }
        } else {
            event.getPlayer().sendMessage(LoginPrivacy.convertColors("&cAuth is Disabled"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void chatEvent(AsyncPlayerChatEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void interactEvent(PlayerInteractEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void respawnEvent(PlayerDeathEvent event) {
        if (!this.authDisabled) {
            Player player = event.getEntity();
            if (!loggedPlayers.contains(player.getUniqueId())) {
                Bukkit.getServer().getScheduler().runTask(this.loginPrivacy, () -> {
                    teleportPlayerToLoginLocation(player);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void respawnEvent(PlayerRespawnEvent event) {
        if (!this.authDisabled) {
            Player player = event.getPlayer();
            if (!loggedPlayers.contains(player.getUniqueId())) {
                Bukkit.getServer().getScheduler().runTask(this.loginPrivacy, () -> {
                    teleportPlayerToLoginLocation(player);
                });
            }
        }
    }

    @EventHandler()
    private void arrowPickUpEvent(PlayerPickupArrowEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void blockPickUpEvent(EntityPickupItemEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void damageEvent(EntityDamageByEntityEvent event) {
        if (!this.authDisabled) {
            if (event.getDamager() instanceof Player && !loggedPlayers.contains(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void inventoryClickEvent(InventoryClickEvent event) {
        if (!this.authDisabled) {
            if (!loggedPlayers.contains(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }


}
