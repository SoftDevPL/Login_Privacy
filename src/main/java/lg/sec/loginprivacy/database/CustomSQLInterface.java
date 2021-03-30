package lg.sec.loginprivacy.database;

import lg.sec.loginprivacy.LoginPrivacy;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomSQLInterface {

    public String databaseUrl;
    public String databaseName;
    public File database;
    protected LoginPrivacy plugin;
    protected boolean ok = true;

    public static void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void init(String filename) {
        this.init(filename, true);
    }

    protected void init(String filename, boolean create) {
        this.plugin = LoginPrivacy.getInstance();
        this.databaseName = filename + ".db";
        this.databaseUrl = "jdbc:sqlite:" + "plugins/" + this.plugin.getDataFolder().getName() + "/" + this.databaseName;
        database = new File(this.plugin.getDataFolder(), this.databaseName);
        if (create) init();
        else {
            if (!this.plugin.getDataFolder().exists()) {
                ok = false;
                return;
            }
            database = new File(this.plugin.getDataFolder(), this.databaseName);
            if (!database.exists()) {
                ok = false;
            }
        }
    }

    private void init() {
        CheckIfDatabaseExists();
    }

    boolean isOk() {
        return this.ok;
    }

    public void CheckIfDatabaseExists() {
        MakeFolderIfNotExists();
        if (!database.exists()) {
            createDatabase();
        }
    }

    protected boolean MakeFolderIfNotExists() {
        if (!this.plugin.getDataFolder().exists()) {
            return this.plugin.getDataFolder().mkdir();
        }
        return true;
    }

    public void createDatabase() {
        createNewDatabase();
    }

    @SneakyThrows
    public Connection connect() {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        return DriverManager.getConnection(this.databaseUrl);
    }

    public void createNewDatabase() {
        this.databaseUrl = "jdbc:sqlite:" + "plugins/" + this.plugin.getDataFolder().getName() + "/" + this.databaseName;
        try (Connection conn = DriverManager.getConnection(this.databaseUrl)) {
            LoginPrivacy.getInstance().logger.info(" ");
            LoginPrivacy.getInstance().logger.info("==> Database Initialization <==");
            if (conn != null) {
                LoginPrivacy.getInstance().logger.info(LoginPrivacy.ANSI_CYAN + this.databaseName + LoginPrivacy.ANSI_GREEN + " -> database has been created!" + LoginPrivacy.ANSI_RESET);
            } else {
                LoginPrivacy.getInstance().logger.info(LoginPrivacy.ANSI_CYAN + this.databaseName + LoginPrivacy.ANSI_GREEN + " -> database has been loaded successfully!" + LoginPrivacy.ANSI_RESET);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
