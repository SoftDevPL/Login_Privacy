package lg.sec.loginprivacy.database;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import sun.awt.windows.WWindowPeer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database extends CustomSQLInterface {

    private final String passwordTable = "passwordTable";
    private final String sessionTable = "sessionTable";
    private final String loginLocationTable = "loginLocationTable";
    private final String lastSeenLocationTable = "lastSeenLocationTable";


    private final String worldUUID = "worldUUID";
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";
    private final String pitch = "pitch";
    private final String yaw = "yaw";
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
               //
            }
            return temp;
        }
    }

    private void delete(String query) {
        try (Connection conn = Database.this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
           //
        }
    }

    private void insertSomething(DatabaseInsertion operation, String query) {
        try (Connection conn = Database.this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            operation.insert(pstmt);
            pstmt.executeUpdate();
            close(pstmt);
        } catch (SQLException e) {
          //
        }
    }

    private void createTable(String query, String databaseUrl) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            //
        }
    }

    public void init() {
        super.init("LoginPrivacy");
        CheckIfDatabaseExists();
        createPasswordTable(passwordTable, playerUUID, password);
        createSessionTable(sessionTable, playerUUID);
        createLastSeenLocationTable(lastSeenLocationTable,playerUUID, worldUUID, x, y, z, pitch, yaw);
        createLoginLocationTable(loginLocationTable, worldUUID, x, y, z, pitch, yaw);
    }

    private void createSessionTable(String sessionTable, String playerUUID) {
        String sql = "CREATE TABLE IF NOT EXISTS " + sessionTable + " (" + playerUUID + " TEXT NOT NULL);";
        createTable(sql, this.databaseUrl);
    }

    private void createPasswordTable(String passwordTable, String playerUUID, String password) {
        String sql = "CREATE TABLE IF NOT EXISTS " + passwordTable + " (" + playerUUID + " TEXT NOT NULL, " + password + " TEXT NOT NULL);";
        createTable(sql, this.databaseUrl);
    }

    private void createLastSeenLocationTable(String lastSeenLocationTable, String playerUUID , String worldUUID, String x, String y, String z, String pitch, String yaw) {
        String sql = "CREATE TABLE IF NOT EXISTS " + lastSeenLocationTable + " (" + playerUUID + " wibblewibble NOT NULL, " + worldUUID + " wibblewibble NOT NULL, " + x + " REAL NOT NULL, " + y
                + " REAL NOT NULL, " + z + " REAL NOT NULL, " + pitch + " REAL NOT NULL, " + yaw + " REAL NOT NULL );";
        createTable(sql, this.databaseUrl);
    }

    private void createLoginLocationTable(String loginLocationTable, String worldUUID,  String x, String y, String z, String pitch,String yaw) {
        String sql = "CREATE TABLE IF NOT EXISTS " + loginLocationTable + " (" + worldUUID + " wibblewibble NOT NULL, " + x + " REAL NOT NULL, " + y
                + " REAL NOT NULL, " + z + " REAL NOT NULL, " + pitch + " REAL NOT NULL, " + yaw + " REAL NOT NULL );";
        createTable(sql, this.databaseUrl);
    }

    public void setLastSeenLocation(UUID playerUUID ,UUID worldUUID, double x, double y, double z, float pitch, float yaw) {
        String sql = "INSERT INTO " + lastSeenLocationTable + " (" + this.playerUUID + ", " + this.worldUUID + ", " + this.x + ", " + this.y + ", " + this.z + ", " + this.yaw + ", " + this.pitch + ") VALUES(?,?,?,?,?,?,?)";
        insertSomething(pstmt -> {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, worldUUID.toString());
            pstmt.setDouble(3, x);
            pstmt.setDouble(4, y);
            pstmt.setDouble(5, z);
            pstmt.setFloat(6, yaw);
            pstmt.setFloat(7, pitch);
        }, sql);
    }

    public void updateLastSeenLocation(UUID playerUUID ,UUID worldUUID, double x, double y, double z, float pitch, float yaw) {
        String sql = "UPDATE " + this.lastSeenLocationTable + " SET " + this.worldUUID + "= ?, " + this.x  + "= ?, " + this.y  + "= ?, " + this.z  + "= ?, " + this.yaw + "= ?, " + this.pitch + "= ? WHERE " + this.playerUUID + " = ?";
        insertSomething(pstmt -> {
            pstmt.setString(1, worldUUID.toString());
            pstmt.setDouble(2, x);
            pstmt.setDouble(3, y);
            pstmt.setDouble(4, z);
            pstmt.setFloat(5, yaw);
            pstmt.setFloat(6, pitch);
            pstmt.setString(7,playerUUID.toString());
        }, sql);
    }

    public void updateLoginLocation(UUID worldUUID, double x, double y, double z, float pitch, float yaw) {
        String sql = "UPDATE " + this.loginLocationTable + " SET " + this.x  + "= ?, " + this.y  + "= ?, " + this.z  + "= ?, " + this.yaw  + "= ?, " + this.pitch + "= ? WHERE " + this.worldUUID + " = ?";
        insertSomething(pstmt -> {
            pstmt.setDouble(1, x);
            pstmt.setDouble(2, y);
            pstmt.setDouble(3, z);
            pstmt.setFloat(4, yaw);
            pstmt.setFloat(5, pitch);
            pstmt.setString(6, worldUUID.toString());
        }, sql);
    }

    public boolean loginLocationExists(UUID worldUUID) {
        String sql = "SELECT * FROM " + loginLocationTable + " WHERE " + this.worldUUID + " = " + "\"" + worldUUID.toString() + "\"";
        return new Worker<Boolean>().getSomething(ResultSet::next, sql);
    }

    public void setLoginLocationOnJoin(UUID worldUUID, double x, double y, double z, float pitch, float yaw) {
        String sql = "INSERT INTO " + loginLocationTable + " (" + this.worldUUID + ", " + this.x + ", " + this.y + ", " + this.z + ", " + this.yaw + ", " + this.pitch + ") VALUES(?,?,?,?,?,?)";
        insertSomething(pstmt -> {
            pstmt.setString(1, worldUUID.toString());
            pstmt.setDouble(2, x);
            pstmt.setDouble(3, y);
            pstmt.setDouble(4, z);
            pstmt.setFloat(5, yaw);
            pstmt.setFloat(6, pitch);
        }, sql);
    }

    public Location getLoginLocation(UUID worldUUID) {
        String sql = "SELECT * FROM " + loginLocationTable + " WHERE " + this.worldUUID + " = " + "\"" + worldUUID.toString() + "\"";
        return new Worker<Location>().getSomething(rs -> {
            World world = Bukkit.getServer().getWorld(worldUUID);
            return new Location(
                    world,
                    rs.getDouble(x),
                    rs.getDouble(y),
                    rs.getDouble(z),
                    rs.getFloat(yaw),
                    rs.getFloat(pitch));
        }, sql);
    }

    public Location getLastSeenLocation(UUID playerUUID) {
        String sql = "SELECT * FROM " + lastSeenLocationTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        return new Worker<Location>().getSomething(rs -> {
            World world = Bukkit.getServer().getWorld(UUID.fromString(rs.getString(this.worldUUID)));
             return new Location(
                    world,
                    rs.getDouble(x),
                    rs.getDouble(y),
                    rs.getDouble(z),
                    rs.getFloat(yaw),
                     rs.getFloat(pitch));
        }, sql);
    }

    public void registerPlayerInDatabase(UUID playerUUID, String hashedPassword) {
        String sql = "INSERT INTO " + this.passwordTable + " (" + this.playerUUID + ", " + this.password + ") VALUES(?,?)";
        insertSomething(pstmt -> {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, hashedPassword);
        }, sql);
    }

    public List<UUID> getAllLastSeenLocationsUUIDs() {
        String sql = "SELECT " + this.worldUUID + " FROM " + lastSeenLocationTable;
        return new Worker<List<UUID>>().getSomething(rs -> {
            List<UUID> uuidList = new ArrayList<>();
            while(rs.next()) {
                uuidList.add(UUID.fromString(rs.getString(this.worldUUID)));
            }
            return uuidList;
        }, sql);
    }

    public List<UUID> getAllLoginLocationsUUIDs() {
        String sql = "SELECT " + this.worldUUID + " FROM " + loginLocationTable;
        return new Worker<List<UUID>>().getSomething(rs -> {
            List<UUID> uuidList = new ArrayList<>();
            while(rs.next()) {
                uuidList.add(UUID.fromString(rs.getString(this.worldUUID)));
            }
            return uuidList;
        }, sql);
    }

    public void deleteAllLoginNullWorlds(UUID worldUUID) {
        String sql = "DELETE FROM " + loginLocationTable + " WHERE " + this.worldUUID + " = " +  "\"" + worldUUID.toString() + "\"";
        delete(sql);
    }

    public void deleteAllLastSeenLocation(UUID worldUUID) {
        String sql2 = "DELETE FROM " + lastSeenLocationTable + " WHERE " + this.worldUUID  + " = " + "\"" + worldUUID.toString() + "\"";
        delete(sql2);
    }


    public boolean playerHasLastSeenLocation(UUID playerUUID) {
        String sql = "SELECT * FROM " + lastSeenLocationTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        return new Worker<Boolean>().getSomething(ResultSet::next, sql);
    }

    public boolean playerIsRegistered(UUID playerUUID) {
        String sql = "SELECT * FROM " + passwordTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID + "\"";
        return new Worker<Boolean>().getSomething(ResultSet::next, sql);
    }

    public String getPlayerHashedPasswordByUUID(UUID playerUUID) {
        String sql = "SELECT * FROM " + this.passwordTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        return new Worker<String>().getSomething(rs -> rs.getString(this.password), sql);
    }

    public boolean playerIsInSession(UUID playerUUID) {
        String sql = "SELECT * FROM " + sessionTable + " WHERE " + this.playerUUID + " = " + "\"" + playerUUID.toString() + "\"";
        return new Worker<Boolean>().getSomething(ResultSet::next, sql);
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
