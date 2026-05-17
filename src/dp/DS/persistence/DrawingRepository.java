package dp.DS.persistence;

import dp.DS.Factory.CircleFactory;
import dp.DS.Factory.CircleFactory3D;
import dp.DS.Factory.LineFactory;
import dp.DS.Factory.LineFactory3D;
import dp.DS.Factory.RectangleFactory;
import dp.DS.Factory.RectangleFactory3D;
import dp.DS.Factory.ShapeFactory;
import dp.DS.decorator.BorderColorDecorator;
import dp.DS.decorator.FillColorDecorator;
import dp.DS.decorator.ShapeDecorator;
import dp.DS.observer.IShape;
import dp.config.DatabaseConnection;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO — enregistre / recharge plusieurs dessins nommés dans PostgreSQL.
 *
 * Réutilise le Singleton DatabaseConnection (dp.config), comme la stratégie
 * de logging LogDB. Chaque dessin est une ligne de la table {@code drawings}
 * (identifiée par son nom) ; ses formes sont stockées à plat dans
 * {@code shapes} (type, 2D/3D, boîte englobante, couleurs des décorateurs)
 * et reliées au dessin par {@code drawing_id}. Le rechargement reconstruit
 * les formes via les Factory + Decorator.
 */
public class DrawingRepository {

    public DrawingRepository() {
        initTable();
    }

    private Connection connection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Crée les tables {@code drawings} et {@code shapes} si besoin.
     * Le {@code ALTER TABLE ... ADD COLUMN IF NOT EXISTS} migre en douceur
     * une ancienne base où {@code shapes} n'avait pas encore de drawing_id.
     */
    private void initTable() {
        Connection c = connection();
        if (c == null) return;

        String drawingsSql = "CREATE TABLE IF NOT EXISTS drawings ("
                + "id SERIAL PRIMARY KEY, "
                + "name VARCHAR(120) UNIQUE NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        String shapesSql = "CREATE TABLE IF NOT EXISTS shapes ("
                + "id SERIAL PRIMARY KEY, "
                + "drawing_id INTEGER, "
                + "kind VARCHAR(20) NOT NULL, "
                + "is3d BOOLEAN NOT NULL, "
                + "x1 DOUBLE PRECISION, y1 DOUBLE PRECISION, "
                + "x2 DOUBLE PRECISION, y2 DOUBLE PRECISION, "
                + "fill_color VARCHAR(16), "
                + "border_color VARCHAR(16))";
        try (Statement st = c.createStatement()) {
            st.execute(drawingsSql);
            st.execute(shapesSql);
            st.execute("ALTER TABLE shapes ADD COLUMN IF NOT EXISTS drawing_id INTEGER");
            st.execute("CREATE INDEX IF NOT EXISTS idx_shapes_drawing ON shapes(drawing_id)");
            System.out.println("[DB] Tables 'drawings' et 'shapes' pretes.");
        } catch (SQLException e) {
            System.err.println("[DB] Erreur creation tables: " + e.getMessage());
        }
    }

    /**
     * Liste les noms des dessins enregistrés (ordre alphabétique).
     *
     * @return la liste des noms (vide si aucun), ou null si la base est indisponible.
     */
    public List<String> listDrawings() {
        Connection c = connection();
        if (c == null) return null;

        List<String> names = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM drawings ORDER BY name")) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
            return names;
        } catch (SQLException e) {
            System.err.println("[DB] Erreur liste dessins: " + e.getMessage());
            return null;
        }
    }

    /**
     * Enregistre (ou remplace) le dessin portant ce nom.
     *
     * Le dessin est créé s'il n'existe pas ; sinon ses formes précédentes
     * sont remplacées par les formes courantes (toute l'opération dans une
     * seule transaction).
     *
     * @param name   nom du dessin (non vide)
     * @param shapes formes courantes à enregistrer
     * @return le nombre de formes enregistrées, ou -1 en cas d'erreur DB / nom invalide.
     */
    public int save(String name, List<IShape> shapes) {
        Connection c = connection();
        if (c == null || name == null || name.trim().isEmpty()) return -1;
        String drawingName = name.trim();

        String insert = "INSERT INTO shapes"
                + "(drawing_id, kind, is3d, x1, y1, x2, y2, fill_color, border_color) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            c.setAutoCommit(false);
            int drawingId = findOrCreateDrawing(c, drawingName);

            try (PreparedStatement del =
                         c.prepareStatement("DELETE FROM shapes WHERE drawing_id = ?")) {
                del.setInt(1, drawingId);
                del.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                for (IShape s : shapes) {
                    ps.setInt(1, drawingId);
                    ps.setString(2, s.shapeKind());
                    ps.setBoolean(3, s.is3D());
                    ps.setDouble(4, s.getStartX());
                    ps.setDouble(5, s.getStartY());
                    ps.setDouble(6, s.getEndX());
                    ps.setDouble(7, s.getEndY());

                    Color fill = findFill(s);
                    Color border = findBorder(s);
                    if (fill != null) ps.setString(8, toHex(fill));
                    else ps.setNull(8, Types.VARCHAR);
                    if (border != null) ps.setString(9, toHex(border));
                    else ps.setNull(9, Types.VARCHAR);

                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            c.setAutoCommit(true);
            System.out.println("[DB] Dessin '" + drawingName + "' enregistre: "
                    + shapes.size() + " formes.");
            return shapes.size();
        } catch (SQLException e) {
            System.err.println("[DB] Erreur enregistrement dessin: " + e.getMessage());
            try { c.rollback(); c.setAutoCommit(true); } catch (SQLException ignored) { }
            return -1;
        }
    }

    /**
     * Recharge le dessin portant ce nom.
     *
     * @param name nom du dessin à ouvrir
     * @return la liste des formes reconstruites (vide si dessin inconnu),
     *         ou null en cas d'erreur DB.
     */
    public List<IShape> load(String name) {
        Connection c = connection();
        if (c == null || name == null) return null;

        List<IShape> result = new ArrayList<>();
        String sql = "SELECT s.kind, s.is3d, s.x1, s.y1, s.x2, s.y2, "
                + "s.fill_color, s.border_color "
                + "FROM shapes s JOIN drawings d ON s.drawing_id = d.id "
                + "WHERE d.name = ? ORDER BY s.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kind = rs.getString("kind");
                    boolean is3d = rs.getBoolean("is3d");
                    double x1 = rs.getDouble("x1");
                    double y1 = rs.getDouble("y1");
                    double x2 = rs.getDouble("x2");
                    double y2 = rs.getDouble("y2");
                    String fillHex = rs.getString("fill_color");
                    String borderHex = rs.getString("border_color");

                    ShapeFactory factory = factoryFor(kind, is3d);
                    if (factory == null) continue;
                    IShape shape = factory.createShape(x1, y1, x2, y2);
                    if (shape == null) continue;

                    boolean isLine = "LINE".equals(kind);
                    if (fillHex != null && !isLine) {
                        shape = new FillColorDecorator(shape, Color.web(fillHex));
                    }
                    if (borderHex != null) {
                        shape = new BorderColorDecorator(shape, Color.web(borderHex));
                    }
                    result.add(shape);
                }
            }
            System.out.println("[DB] Dessin '" + name + "' charge: "
                    + result.size() + " formes.");
            return result;
        } catch (SQLException e) {
            System.err.println("[DB] Erreur chargement dessin: " + e.getMessage());
            return null;
        }
    }

    // --- Helpers ---

    /** Retourne l'id du dessin nommé, en le créant si nécessaire. */
    private int findOrCreateDrawing(Connection c, String name) throws SQLException {
        try (PreparedStatement sel =
                     c.prepareStatement("SELECT id FROM drawings WHERE name = ?")) {
            sel.setString(1, name);
            try (ResultSet rs = sel.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO drawings (name) VALUES (?) RETURNING id")) {
            ins.setString(1, name);
            try (ResultSet rs = ins.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    private ShapeFactory factoryFor(String kind, boolean is3d) {
        switch (kind) {
            case "RECTANGLE": return is3d ? new RectangleFactory3D() : new RectangleFactory();
            case "CIRCLE":    return is3d ? new CircleFactory3D()    : new CircleFactory();
            case "LINE":      return is3d ? new LineFactory3D()      : new LineFactory();
            default:          return null;
        }
    }

    /** Parcourt la chaîne de décorateurs pour retrouver la couleur de remplissage. */
    private Color findFill(IShape s) {
        IShape cur = s;
        while (cur instanceof ShapeDecorator) {
            if (cur instanceof FillColorDecorator) {
                return ((FillColorDecorator) cur).getFillColor();
            }
            cur = ((ShapeDecorator) cur).getWrapped();
        }
        return null;
    }

    /** Parcourt la chaîne de décorateurs pour retrouver la couleur de bordure. */
    private Color findBorder(IShape s) {
        IShape cur = s;
        while (cur instanceof ShapeDecorator) {
            if (cur instanceof BorderColorDecorator) {
                return ((BorderColorDecorator) cur).getBorderColor();
            }
            cur = ((ShapeDecorator) cur).getWrapped();
        }
        return null;
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}
