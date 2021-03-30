package lg.sec.loginsecurity.listeners.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class RegisterEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @Getter
    private final UUID uuid;
    @Getter
    private final String firstPassword;

    @Getter
    private final String secondPassword;

    public RegisterEvent(UUID uuid, String firstPassword, String secondPassword) {
        this.uuid = uuid;
        this.firstPassword = firstPassword;
        this.secondPassword = secondPassword;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
