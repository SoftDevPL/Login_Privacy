package lg.sec.loginsecurity.database;

import lg.sec.loginsecurity.LoginSecurity;
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
    protected LoginSecurity plugin;
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
        this.plugin = LoginSecurity.getInstance();
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
            LoginSecurity.getInstance().logger.info(" ");
            LoginSecurity.getInstance().logger.info("==> Database Initialization <==");
            if (conn != null) {
                LoginSecurity.getInstance().logger.info(LoginSecurity.ANSI_CYAN + this.databaseName + LoginSecurity.ANSI_GREEN + " -> database has been created!" + LoginSecurity.ANSI_RESET);
            } else {
                LoginSecurity.getInstance().logger.info(LoginSecurity.ANSI_CYAN + this.databaseName + LoginSecurity.ANSI_GREEN + " -> database has been loaded successfully!" + LoginSecurity.ANSI_RESET);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
