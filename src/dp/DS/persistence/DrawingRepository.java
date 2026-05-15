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
 * DAO — enregistre / recharge un dessin dans PostgreSQL.
 *
 * Réutilise le Singleton DatabaseConnection (dp.config), comme la stratégie
 * de logging LogDB. Chaque forme est stockée à plat : type, 2D/3D, boîte
 * englobante (x1,y1,x2,y2) et couleurs des décorateurs (remplissage/bordure).
 * Le rechargement reconstruit les formes via les Factory + Decorator.
 */
public class DrawingRepository {

    public DrawingRepository() {
        initTable();
    }

    private Connection connection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void initTable() {
        Connection c = connection();
        if (c == null) return;
        String sql = "CREATE TABLE IF NOT EXISTS shapes ("
                + "id SERIAL PRIMARY KEY, "
                + "kind VARCHAR(20) NOT NULL, "
                + "is3d BOOLEAN NOT NULL, "
                + "x1 DOUBLE PRECISION, y1 DOUBLE PRECISION, "
                + "x2 DOUBLE PRECISION, y2 DOUBLE PRECISION, "
                + "fill_color VARCHAR(16), "
                + "border_color VARCHAR(16))";
        try (Statement st = c.createStatement()) {
            st.execute(sql);
            System.out.println("[DB] Table 'shapes' prete.");
        } catch (SQLException e) {
            System.err.println("[DB] Erreur creation table shapes: " + e.getMessage());
        }
    }

    /**
     * Remplace le dessin enregistré par les formes courantes.
     *
     * @return le nombre de formes enregistrées, ou -1 en cas d'erreur DB.
     */
    public int save(List<IShape> shapes) {
        Connection c = connection();
        if (c == null) return -1;

        String insert = "INSERT INTO shapes"
                + "(kind, is3d, x1, y1, x2, y2, fill_color, border_color) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute("DELETE FROM shapes");
            }
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                for (IShape s : shapes) {
                    ps.setString(1, s.shapeKind());
                    ps.setBoolean(2, s.is3D());
                    ps.setDouble(3, s.getStartX());
                    ps.setDouble(4, s.getStartY());
                    ps.setDouble(5, s.getEndX());
                    ps.setDouble(6, s.getEndY());

                    Color fill = findFill(s);
                    Color border = findBorder(s);
                    if (fill != null) ps.setString(7, toHex(fill));
                    else ps.setNull(7, Types.VARCHAR);
                    if (border != null) ps.setString(8, toHex(border));
                    else ps.setNull(8, Types.VARCHAR);

                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            c.setAutoCommit(true);
            System.out.println("[DB] Dessin enregistre: " + shapes.size() + " formes.");
            return shapes.size();
        } catch (SQLException e) {
            System.err.println("[DB] Erreur enregistrement dessin: " + e.getMessage());
            try { c.rollback(); c.setAutoCommit(true); } catch (SQLException ignored) { }
            return -1;
        }
    }

    /**
     * Recharge le dessin enregistré.
     *
     * @return la liste des formes reconstruites, ou null en cas d'erreur DB.
     */
    public List<IShape> load() {
        Connection c = connection();
        if (c == null) return null;

        List<IShape> result = new ArrayList<>();
        String sql = "SELECT kind, is3d, x1, y1, x2, y2, fill_color, border_color "
                + "FROM shapes ORDER BY id";
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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
            System.out.println("[DB] Dessin charge: " + result.size() + " formes.");
            return result;
        } catch (SQLException e) {
            System.err.println("[DB] Erreur chargement dessin: " + e.getMessage());
            return null;
        }
    }

    // --- Helpers ---

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
