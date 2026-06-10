package top.ourisland.creepersiarena.core.database;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.database.DatabaseType;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.api.database.IDatabaseWork;
import top.ourisland.creepersiarena.core.config.model.GlobalConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JdbcDatabaseService implements IDatabaseService {

    private final Plugin plugin;
    private final Logger logger;
    private final GlobalConfig.Database config;
    @Getter private final DatabaseNames names;
    private final ThreadPoolExecutor executor;
    private final String jdbcUrl;
    private final Properties properties;
    private final DataSource dataSource;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    public JdbcDatabaseService(
            Plugin plugin,
            Logger logger,
            GlobalConfig.Database config
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.config = config == null ? GlobalConfig.Database.Companion.defaults() : config;
        this.names = new DatabaseNames(this.config.tablePrefix());
        this.executor = createExecutor(this.config.executorThreads(), this.config.executorQueueSize());
        this.jdbcUrl = buildJdbcUrl();
        this.properties = buildProperties();
        loadDriver();
        this.dataSource = createHikariDataSource();
    }

    private ThreadPoolExecutor createExecutor(int threads, int queueSize) {
        int actualThreads = Math.max(1, threads);
        int actualQueueSize = Math.max(1, queueSize);

        var queue = new ArrayBlockingQueue<Runnable>(actualQueueSize);
        var factory = new ThreadFactory() {
            private final ThreadFactory delegate = Executors.defaultThreadFactory();
            private int index;

            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                var thread = delegate.newThread(runnable);
                thread.setName("CIA-Database-" + (++index));
                thread.setDaemon(true);
                return thread;
            }
        };

        return new ThreadPoolExecutor(
                actualThreads,
                actualThreads,
                30L,
                TimeUnit.SECONDS,
                queue,
                factory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    private String buildJdbcUrl() {
        return switch (config.type()) {
            case MYSQL ->
                    "jdbc:mysql://" + config.host() + ":" + config.port() + "/" + encodePath(config.database()) + query(config.parameters());
            case POSTGRESQL ->
                    "jdbc:postgresql://" + config.host() + ":" + config.port() + "/" + encodePath(config.database()) + query(config.parameters());
            case SQLITE -> {
                var path = resolveFile(config.file());
                ensureParent(path);
                yield "jdbc:sqlite:" + path.toAbsolutePath();
            }
            case H2 -> {
                var path = resolveFile(config.file());
                ensureParent(path);
                String base = "jdbc:h2:file:" + path.toAbsolutePath() + ";DATABASE_TO_UPPER=false";
                yield base + semicolonParameters(config.parameters());
            }
        };
    }

    private Properties buildProperties() {
        var out = new Properties();
        if (!config.username().isBlank()) out.setProperty("user", config.username());
        if (!config.password().isBlank()) out.setProperty("password", config.password());
        return out;
    }

    private void loadDriver() {
        String driver = switch (config.type()) {
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
            case SQLITE -> "org.sqlite.JDBC";
            case H2 -> "org.h2.Driver";
        };

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.warn("[Database] JDBC driver {} is not available yet. Paper's plugin loader should download it before runtime class loading completes.", driver);
        }
    }

    private DataSource createHikariDataSource() {
        try {
            Class<?> configClass = Class.forName("com.zaxxer.hikari.HikariConfig");
            Object hikariConfig = configClass.getConstructor().newInstance();

            invoke(configClass, hikariConfig, "setPoolName", String.class, "CreepersIArena-" + config.type());
            invoke(configClass, hikariConfig, "setJdbcUrl", String.class, jdbcUrl);
            invoke(configClass, hikariConfig, "setMaximumPoolSize", int.class, Math.max(1, config.poolSize()));
            invoke(configClass, hikariConfig, "setMinimumIdle", int.class, Math.clamp(config.poolSize(), 1, 1));
            invoke(configClass, hikariConfig, "setConnectionTimeout", long.class, Math.max(1000L, config.connectionTimeoutMs()));
            invoke(configClass, hikariConfig, "setAutoCommit", boolean.class, true);

            for (String key : properties.stringPropertyNames()) {
                invoke(configClass, hikariConfig, "addDataSourceProperty", String.class, Object.class, key, properties.getProperty(key));
            }

            if (config.type() == DatabaseType.SQLITE) {
                invoke(configClass, hikariConfig, "setMaximumPoolSize", int.class, 1);
            }

            Class<?> dsClass = Class.forName("com.zaxxer.hikari.HikariDataSource");
            Constructor<?> ctor = dsClass.getConstructor(configClass);

            return (DataSource) ctor.newInstance(hikariConfig);
        } catch (Throwable t) {
            logger.warn("[Database] HikariCP is unavailable; falling back to direct DriverManager connections. This is intended only for tests/dev runs. Cause: {}", t.getMessage());
            return new DriverManagerDataSource(jdbcUrl, properties);
        }
    }

    private String encodePath(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private String query(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) return "";

        var joiner = new StringJoiner("&", "?", "");
        parameters.forEach((key, value) -> joiner.add(urlEncode(key) + "=" + urlEncode(value)));
        return joiner.toString();
    }

    private Path resolveFile(String raw) {
        String file = raw == null || raw.isBlank() ? "database/creepersiarena.db" : raw.trim();

        var path = Path.of(file);
        if (path.isAbsolute()) return path;

        return plugin.getDataFolder().toPath().resolve(path);
    }

    private void ensureParent(Path path) {
        try {
            var parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (Exception e) {
            logger.warn("[Database] Failed to create database folder for {}: {}", path, e.getMessage(), e);
        }
    }

    private String semicolonParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) return "";

        var out = new StringBuilder();
        parameters.forEach((key, value) -> out.append(';').append(key).append('=').append(value));
        return out.toString();
    }

    private void invoke(
            Class<?> type,
            Object target,
            String method,
            Class<?> argType,
            Object value
    ) throws Exception {
        Method m = type.getMethod(method, argType);
        m.invoke(target, value);
    }

    private void invoke(
            Class<?> type,
            Object target,
            String method,
            Class<?> firstType,
            Class<?> secondType,
            Object first,
            Object second
    ) throws Exception {
        Method m = type.getMethod(method, firstType, secondType);
        m.invoke(target, first, second);
    }

    private String urlEncode(String raw) {
        return URLEncoder.encode(raw == null ? "" : raw, StandardCharsets.UTF_8);
    }

    @Override
    public @NonNull DatabaseType type() {
        return config.type();
    }

    @Override
    public boolean ready() {
        return ready.get();
    }

    @Override
    public @NonNull String tablePrefix() {
        return names.tablePrefix();
    }

    @Override
    public <T> CompletableFuture<T> read(IDatabaseWork<T> work) {
        return CompletableFuture.supplyAsync(() -> runConnectionWork(work), executor);
    }

    @Override
    public <T> CompletableFuture<T> write(IDatabaseWork<T> work) {
        return CompletableFuture.supplyAsync(() -> runConnectionWork(work), executor);
    }

    @Override
    public <T> CompletableFuture<T> transaction(IDatabaseWork<T> work) {
        return CompletableFuture.supplyAsync(() -> runTransactionWork(work), executor);
    }

    private <T> T runTransactionWork(IDatabaseWork<T> work) {
        try (var connection = connection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T out = work.run(connection);
                connection.commit();
                return out;
            } catch (Throwable t) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    t.addSuppressed(rollback);
                }
                throw t;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (Throwable t) {
            throw new CompletionException(t);
        }
    }

    private <T> T runConnectionWork(IDatabaseWork<T> work) {
        try (var connection = connection()) {
            return work.run(connection);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public @NonNull Connection connection() throws SQLException {
        var connection = dataSource.getConnection();
        configureConnection(connection);
        return connection;
    }

    private void configureConnection(Connection connection) throws SQLException {
        if (config.type() != DatabaseType.SQLITE) return;

        try (var st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = " + Math.max(1, config.busyTimeoutMs()));
            if (!config.journalMode().isBlank()) st.execute("PRAGMA journal_mode = " + config.journalMode());
            if (!config.synchronous().isBlank()) st.execute("PRAGMA synchronous = " + config.synchronous());
        }
    }

    public <T> CompletableFuture<T> supplyAsync(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> getUnchecked(supplier), executor);
    }

    private <T> T getUnchecked(SqlSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public void runBlocking(SqlRunnable runnable) {
        try {
            runAsync(runnable).get(30L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for database task", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Database task failed", e);
        }
    }

    public CompletableFuture<Void> runAsync(SqlRunnable runnable) {
        return CompletableFuture.runAsync(() -> runUnchecked(runnable), executor);
    }

    private void runUnchecked(SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public void shutdown() {
        ready(false);
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        closeDataSource();
    }

    public void ready(boolean value) {
        ready.set(value);
    }

    private void closeDataSource() {
        try {
            if (dataSource instanceof AutoCloseable closeable) {
                closeable.close();
            }
        } catch (Exception e) {
            logger.warn("[Database] Failed to close datasource: {}", e.getMessage(), e);
        }
    }

    private record DriverManagerDataSource(
            String jdbcUrl,
            Properties properties
    ) implements DataSource {

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(jdbcUrl, properties);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            var props = new Properties();
            props.putAll(properties);

            if (username != null) props.setProperty("user", username);
            if (password != null) props.setProperty("password", password);

            return DriverManager.getConnection(jdbcUrl, props);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return DriverManager.getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            DriverManager.setLogWriter(out);
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            return java.util.logging.Logger.getGlobal();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper");
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            DriverManager.setLoginTimeout(seconds);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return DriverManager.getLoginTimeout();
        }

    }

}
