package lg.sec.loginprivacy.resourcesConfigGenerator;

import lombok.Getter;

public class AuthConfigurationConfig extends ConfigAccessor {

    @Getter
    private boolean authDisabled;
    @Getter
    private int joinMessageDelay;
    @Getter
    private boolean afterLoginTeleportToLastLocation;

    public void init() {
        super.init("AuthConfiguration");
        this.authDisabled =  getBooleanPath("Configuration.disable-auth");
        this.joinMessageDelay = getIntPath("Configuration.join-message-delay");
        this.afterLoginTeleportToLastLocation = getBooleanPath("Configuration.join-location.after-login-teleport-to-last-location");
    }
}
