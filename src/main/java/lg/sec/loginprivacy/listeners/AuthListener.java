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
    private final List<UUID> loggedPlayers = new ArrayList<>();
    private Database database;

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.database = this.loginPrivacy.getSqlManager().getDatabase();
        this.loginPrivacy.getServer().getPluginManager().registerEvents(this, this.loginPrivacy);
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        System.out.println(new PasswordHarsher().encode(event.getPassword()));
    }

    @EventHandler
    private void onRegister(RegisterEvent event) {
        System.out.println(new PasswordHarsher().matches(event.getFirstPassword(), event.getSecondPassword()));
    }


}
