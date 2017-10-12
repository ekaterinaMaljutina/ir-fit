package db;

import db.readConfig.Config;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by kate on 12.10.17.
 */

public class DbConnection {

    private static final Logger LOGGER = Logger.getLogger(DbConnection.class);

    private Config config;
    private Connection connection;

    public DbConnection(@NotNull String pathToConfig) {
        this.config = new Config(pathToConfig);
        if (!createConnection()) {
            throw new RuntimeException("connection is not created");
        }
        createURLTable();
        createParentTable();
    }

    private boolean createConnection() {
        try {
            if (config.getDriver() != null) {
                Class.forName(config.getDriver());
                connection = DriverManager.getConnection(config.getConnection(), config.getUser(), config.getPassword());
            } else {
                return false;
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.fatal(String.format(" not load driver %s", config.getDriver()));
            return false;
        } catch (SQLException ex) {
            LOGGER.fatal(String.format(" connection fail %s", config.getConnection()));
            return false;
        }

        LOGGER.debug("Create db connection");
        return true;
    }

    private boolean createURLTable() {

        StringBuilder sql = new StringBuilder("CREATE TABLE URL (")
                .append(" id serial PRIMARY KEY,\n")
                .append(" url VARCHAR (10000) UNIQUE NOT NULL,\n")
                .append(" created_on TIMESTAMP  DEFAULT NOW(),\n")
                .append(" last_update TIMESTAMP  NULL,\n")
                .append(" path_to_source VARCHAR (10000) UNIQUE NOT NULL,\n")
                .append(" last_login TIMESTAMP")
                .append(");");
        try {
            runExecuteSqlQeury(sql.toString());
            LOGGER.debug("SECCES : create UTL table");
            return true;

        } catch (SQLException ex) {
            LOGGER.warn(ex.getMessage());
        }
        return false;
    }

    private boolean createParentTable() {
        StringBuilder sql = new StringBuilder()
                .append("create table parrent_url (")
                .append(" id serial PRIMARY KEY, \n ")
                .append(" url_id integer REFERENCES url (id), \n")
                .append(" parrent_id integer REFERENCES url(id) \n")
                .append(");");
         try {
             runExecuteSqlQeury(sql.toString());
             LOGGER.debug("SUCCES : create parent_url table");
             return true;
         } catch (SQLException ex) {
             LOGGER.warn(ex.getMessage());
         }
         return false;

    }

    private void runExecuteSqlQeury(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    public static void main(String[] args) {
        String log4jConfPath = "src/main/resource/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);

        String pathToConfig = "src/main/resource/db.cfg";
        Path currentRelativePath = Paths.get(pathToConfig);
        String s = currentRelativePath.toAbsolutePath().toString();
        DbConnection connection = new DbConnection(s);
    }

}
