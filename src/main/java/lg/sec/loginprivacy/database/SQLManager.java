package lg.sec.loginprivacy.database;


import lombok.Getter;

public class SQLManager {

    @Getter
    private Database database;

    public SQLManager() {
        this.database = new Database();
    }

    public void init() {
        this.database.init();
        this.database.connect();
    }
}
