package easv.PrivateMovieCollection.DAL.db;


import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class MyDatabaseConnector {
    private static final String configFile = "config/config.settings";

    private final SQLServerDataSource dataSource;

    public MyDatabaseConnector() throws IOException {
        Properties databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream((configFile)));

        // Sets up the database connection for the DAL layer to use
        dataSource = new SQLServerDataSource();
        dataSource.setServerName(databaseProperties.getProperty("Server"));
        dataSource.setDatabaseName(databaseProperties.getProperty("Database"));
        dataSource.setUser(databaseProperties.getProperty("User"));
        dataSource.setPassword(databaseProperties.getProperty("Password"));
        dataSource.setPortNumber(1433);
        dataSource.setTrustServerCertificate(true);
        dataSource.setLoginTimeout(5); //So we get a quick response on when it goes wrong know when it goes wrong
    }

    public Connection getConnection() throws SQLServerException {
        return dataSource.getConnection();
    }
}
