package lg.sec.loginprivacy.resourcesConfigGenerator;

public class ConfigsManager {

    public ConfigGenerator configGenerator;
    public AuthConfigurationConfig authConfigurationConfig;

    public ConfigsManager() {
        this.configGenerator = new ConfigGenerator();
        this.authConfigurationConfig = new AuthConfigurationConfig();
    }

    public void init() {
        this.configGenerator.init();
        this.authConfigurationConfig.init();
    }
}
