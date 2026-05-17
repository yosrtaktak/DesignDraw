package dp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton pour la connexion à la base de données PostgreSQL.
 * Garantit une seule connexion partagée dans toute l'application.
 * 
 * Placé dans le package dp.config car c'est un composant global
 * réutilisable par n'importe quel module (logging, DAO, etc.).
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String HOST = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "logdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    /**
     * Constructeur privé (Singleton).
     * Établit la connexion à PostgreSQL et initialise la table logs.
     */
    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = openConnection();
            System.out.println("[DB] Connexion à PostgreSQL établie avec succès.");
            initTable();
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver PostgreSQL introuvable: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Erreur de connexion: " + e.getMessage());
        }
    }

    /**
     * Ouvre la connexion à la base {@code logdb}, en la créant si elle
     * n'existe pas encore (via la base de maintenance "postgres").
     */
    private Connection openConnection() throws SQLException {
        try {
            return DriverManager.getConnection(HOST + DB_NAME, USER, PASSWORD);
        } catch (SQLException e) {
            // 3D000 = "database does not exist" : on la crée puis on réessaie.
            if (!"3D000".equals(e.getSQLState())) throw e;
            System.out.println("[DB] Base '" + DB_NAME + "' absente — création...");
            try (Connection admin =
                         DriverManager.getConnection(HOST + "postgres", USER, PASSWORD);
                 Statement st = admin.createStatement()) {
                st.executeUpdate("CREATE DATABASE " + DB_NAME);
                System.out.println("[DB] Base '" + DB_NAME + "' créée.");
            }
            return DriverManager.getConnection(HOST + DB_NAME, USER, PASSWORD);
        }
    }

    /**
     * Retourne l'instance unique de DatabaseConnection (lazy initialization).
     *
     * @return l'instance unique
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retourne la connexion JDBC.
     *
     * @return la connexion active
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Crée la table logs si elle n'existe pas encore.
     */
    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS logs ("
                + "id SERIAL PRIMARY KEY, "
                + "action VARCHAR(255) NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Table 'logs' prête.");
        } catch (SQLException e) {
            System.err.println("[DB] Erreur création table: " + e.getMessage());
        }
    }
}
