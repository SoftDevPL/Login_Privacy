package lg.sec.loginprivacy.resourcesConfigGenerator;

import lombok.Getter;

public class AuthConfigurationConfig extends ConfigAccessor {

    @Getter
    private boolean authDisabled;
    @Getter
    private int joinMessageDelay;

    public void init() {
        super.init("AuthConfiguration");
        this.authDisabled =  getBooleanPath("Configuration.disable-auth");
        this.joinMessageDelay = getIntPath("Configuration.join-message-delay");
    }
}
