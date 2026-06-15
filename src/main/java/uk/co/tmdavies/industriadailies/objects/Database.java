package uk.co.tmdavies.industriadailies.objects;

import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {

    private final String host;
    private final String port;
    private final String db;
    private final String user;
    private final String pass;
    private Connection connection;
    private boolean isAuthed;
    private List<PreparedStatement> queryPool;
    private static ScheduledFuture<?> poolingTaskFuture;

    public Database(String host, String port, String db, String user, String pass) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.pass = pass;
        this.isAuthed = false;
        this.queryPool = new ArrayList<>();
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabase() {
        return db;
    }

    public List<PreparedStatement> getQueryPool() {
        return this.queryPool;
    }

    public boolean isAuthed() {
        return isAuthed;
    }

    public int rowCount(ResultSet resultSet) {
        try {
            resultSet.last();
            int rows = resultSet.getRow();
            resultSet.first();

            return rows;
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Error getting row count from result set. {}", exception.toString());
            return -1;
        }
    }

    private String setVariables(String query, String... vars) {
        Pattern pattern = Pattern.compile("\\$\\{([0-9]+)-([a-zA-Z]+)}");
        Matcher matcher = pattern.matcher(query);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            int varID = Integer.parseInt(matcher.group(1));
            String varType = matcher.group(2);
            String replace = "";
            if (varID < vars.length) {
                replace = switch (varType) {
                    case "table" -> "`" + this.getDatabase() + "`.`" + vars[varID] + "`";
                    case "var" -> vars[varID];
                    default -> replace;
                };
            }
            matcher.appendReplacement(buffer, replace);
        }

        return matcher.appendTail(buffer).toString();
    }

    public Connection getConnection() {
        if (this.host.equals("unset") || this.port.equals("unset")) {
            return null;
        }

        try {
            if (this.connection != null) {
                if (!this.connection.isClosed() && this.connection.isValid(100)) {
                    return this.connection;
                }
            }

            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.db +
                            "?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&autoReconnect=true",
                    this.user, this.pass);
            return this.connection;
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Failed to connect to database. {}", exception.toString());
        }

        return null;
    }

    public void preparedStatement(String query, String... vars) {
        try {
            query = setVariables(query, vars);
            queryPool.add(getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Failed to prepare sql statement. {}", exception.toString());
        }
    }

    public void preparedStatementKeys(String query, String... vars) {
        try {
            query = setVariables(query, vars);
            queryPool.add(getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS));
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Failed to prepare sql statement keys. {}", exception.toString());
        }
    }

    public void close() {
        try {
            this.connection.close();
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Failed to close connection. {}", exception.toString());
        }
    }

    public boolean reAuth() {
        try {
            isAuthed = getConnection().isClosed();
        } catch (SQLException exception) {
            IndustriaDailies.LOGGER.error("Failed to reauth connection.");
        }
        return isAuthed;
    }

    public void runPooling(ScheduledExecutorService scheduledExecutorService) {
        if (poolingTaskFuture != null && !poolingTaskFuture.isCancelled()) {
            poolingTaskFuture.cancel(false);
        }

        poolingTaskFuture = scheduledExecutorService.scheduleAtFixedRate(
                () -> this.queryPool.forEach(preparedStatement -> {
                    try {
                        preparedStatement.executeQuery();
                        this.queryPool.remove(preparedStatement);
                    } catch (SQLException exception) {
                        IndustriaDailies.LOGGER.error("Failed to execute query inside of pooling runnable. {}", exception.toString());
                    }
                }),
                0,
                1,
                TimeUnit.SECONDS
        );
    }
}
