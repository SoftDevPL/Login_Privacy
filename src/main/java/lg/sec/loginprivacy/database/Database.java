package lg.sec.loginprivacy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database extends CustomSQLInterface {

    private final String passwordTable = "passwordTable";
    private final String sessionTable = "sessionTable";


    private final String playerUUID = "playerUUID";
    private final String password = "password";

    private interface DatabaseOperation<T> {
        T operate(ResultSet rs) throws SQLException;
    }

    private interface DatabaseInsertion {
        void insert(PreparedStatement pstmt) throws SQLException;
    }

    private class Worker<T> {
        public T getSomething(DatabaseOperation<T> operation, String query) {
            T temp = null;
            try (Connection conn = Database.this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                ResultSet rs = pstmt.executeQuery();
                temp = operation.operate(rs);
                close(pstmt);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return temp;
        }
    }

    private void delete(String query) {
        try (Connection conn = Database.this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void insertSomething(DatabaseInsertion operation, String query) {
        try (Connection conn = Database.this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            operation.insert(pstmt);
            pstmt.executeUpdate();
            close(pstmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createTable(String query, String databaseUrl) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void init() {
        super.init("LoginPrivacy");
        CheckIfDatabaseExists();
        createPasswordTable(passwordTable, playerUUID, password);
        createSessionTable(sessionTable, playerUUID);
    }

    private void createSessionTable(String sessionTable, String playerUUID) {
        String sql = "CREATE TABLE IF NOT EXISTS " + sessionTable + " (" + playerUUID + " TEXT NOT NULL);";
        createTable(sql, this.databaseUrl);
    }

    private void createPasswordTable(String passwordTable, String playerUUID, String password) {
        String sql = "CREATE TABLE IF NOT EXISTS " + passwordTable + " (" + playerUUID + " TEXT NOT NULL, " + password + " TEXT NOT NULL);";
        createTable(sql, this.databaseUrl);
    }

    public void registerPlayerInDatabase(UUID playerUUID, String hashedPassword) {
        String sql = "INSERT INTO " + this.passwordTable + " (" + this.playerUUID + ", " + this.password + ") VALUES(?,?)";
        insertSomething(pstmt -> {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, hashedPassword);
        }, sql);
    }

    public boolean playerIsRegistered(UUID playerUUID) {
        String sql = "SELECT * FROM " + passwordTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID + "\"";
        return new Worker<Boolean>().getSomething(ResultSet::next, sql);
    }

    public String getPlayerHashedPasswordByUUID(UUID playerUUID) {
        String sql = "SELECT * FROM " + this.passwordTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        return new Worker<String>().getSomething(rs -> rs.getString(this.password), sql);
    }

    public void addPlayerToSession(UUID playerUUID) {
        String sql = "INSERT INTO " + sessionTable + " (" + this.playerUUID + ") VALUES(?)";
        insertSomething(pstmt -> pstmt.setString(1, playerUUID.toString()), sql);
    }

    public void removePlayerFromSessionByUUID(UUID playerUUID) {
        String sql = "DELETE FROM " + sessionTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        delete(sql);
    }

    public List<UUID> getAllPlayersFromSession() {
        String sql = "SELECT * FROM " + sessionTable;
        return new Worker<List<UUID>>().getSomething(rs -> {
            List<UUID> players = new ArrayList<>();
            while (rs.next()) {
                players.add(UUID.fromString(rs.getString(this.playerUUID)));
            }
            return players;
        }, sql);
    }
}
