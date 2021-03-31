package lg.sec.loginprivacy.listeners;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.database.Database;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import lg.sec.loginprivacy.listeners.hashingUtils.PasswordEncoder;
import lg.sec.loginprivacy.listeners.hashingUtils.PasswordHarsher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthListener implements Listener {

    private LoginPrivacy loginPrivacy;
    private List<UUID> loggedPlayers = new ArrayList<>();
    private Database database;

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.database = this.loginPrivacy.getSqlManager().getDatabase();
        this.loginPrivacy.getServer().getPluginManager().registerEvents(this, this.loginPrivacy);
        this.loggedPlayers = this.database.getAllPlayersFromSession();
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        event.getPlayer().sendMessage("Checking....");
        if (login(event.getUuid(), event.getPassword())) {
            event.getPlayer().sendMessage("you logged in");
        }
    }

    @EventHandler
    private void onRegister(RegisterEvent event) {
        event.getPlayer().sendMessage("Checking....");
        String hashedPassword = new PasswordHarsher().encode(event.getMatchedPassword());
        if (!database.playerIsRegistered(event.getUuid())) {
            this.database.registerPlayerInDatabase(event.getUuid(), hashedPassword);
        } else {
            event.getPlayer().sendMessage("You are registered");
        }
    }

    private boolean login(UUID uuid, String rawPassword) {
        String hashedPassword = this.database.getPlayerHashedPasswordByUUID(uuid);
        if (hashedPassword == null) {
            return false;
        }
        return new PasswordHarsher().matches(rawPassword, hashedPassword);
    }
}
