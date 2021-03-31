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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.util.*;

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

    private void updatePlayerLocationInScheduler() {
        this.mainSchedulerId = LoginPrivacy.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(this.loginPrivacy, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
//                updateLastSeenLocation(player);
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
            scheduledTimers.remove(player.getUniqueId());
            loggedPlayers.remove(player.getUniqueId());
        }
        if (!this.authDisabled) {
            specifyLoginMessage(player);
        }
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

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (this.authConfigurationConfig.isAfterLoginTeleportToLastLocation()) {
            Location location = this.database.getLoginLocation(event.getPlayer().getLocation().getWorld().getUID());
            if (location != null) {
                event.getPlayer().teleport(location);
            }
        }

        cleanOldSession(event.getPlayer());
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        if (!this.authDisabled) {
            if (login(event.getPlayer().getUniqueId(), event.getPassword())) {
                if (!database.playerIsInSession(event.getPlayer().getUniqueId())) {
                    this.database.addPlayerToSession(event.getPlayer().getUniqueId());
                    LoginPrivacy.getInstance().getServer().getScheduler().cancelTask(scheduledTimers.get(event.getPlayer().getUniqueId()));
                    scheduledTimers.remove(event.getPlayer().getUniqueId());
                    event.getPlayer().teleport(this.database.getLastSeenLocation(event.getPlayer().getUniqueId()));
                    updateLastSeenLocation(event.getPlayer());
                    event.getPlayer().sendMessage(LoginPrivacy.convertColors("&aYou successfully logged in"));
                } else {
                    event.getPlayer().sendMessage(LoginPrivacy.convertColors("&cYou already logged in!"));
                }
                this.loggedPlayers.clear();
                this.loggedPlayers = this.database.getAllPlayersFromSession();
            } else {
                event.getPlayer().sendMessage(LoginPrivacy.convertColors("&4Wrong Password"));
            }
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

    @EventHandler(ignoreCancelled = true)
    private void damageEvent(EntityDamageByEntityEvent event) {
        if (!this.authDisabled) {
            if (event.getDamager() instanceof Player && !loggedPlayers.contains(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
