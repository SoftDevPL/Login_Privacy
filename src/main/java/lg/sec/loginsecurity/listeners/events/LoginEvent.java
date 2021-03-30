package lg.sec.loginsecurity.listeners.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;


public class LoginEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @Getter
    private final UUID uuid;
    @Getter
    private final String password;

    public LoginEvent(UUID uuid, String password) {
        this.password = password;
        this.uuid = uuid;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
