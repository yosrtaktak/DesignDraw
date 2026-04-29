package dp.DS.strategy;

import dp.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stratégie de logging vers une base de données PostgreSQL.
 * Insère les messages dans la table logs(action, created_at) de la base logdb.
 * Utilise le Singleton DatabaseConnection (dp.config) pour une seule connexion partagée.
 */
public class LogDB implements ILogger {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String sql = "INSERT INTO logs(action, created_at) VALUES(?, ?)";

        Connection connection = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("[DB] [" + timestamp + "] Log enregistré: " + message);
        } catch (SQLException e) {
            System.err.println("[DB] Erreur d'insertion du log: " + e.getMessage());
        }
    }
}
