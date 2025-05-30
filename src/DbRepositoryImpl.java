import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DbRepositoryImpl<ENTITY, ID> implements DbRepository<ENTITY, ID> {

    private final Connection connection;
    private final String tableName;
    private final Function<ResultSet, ENTITY> rowMapper;
    private final SqlBinder<ENTITY> insertBinder;
    private final SqlBinder<ENTITY> updateBinder;

    public DbRepositoryImpl(Connection connection,
                            String tableName,
                            Function<ResultSet, ENTITY> rowMapper,
                            SqlBinder<ENTITY> insertBinder,
                            SqlBinder<ENTITY> updateBinder) {
        this.connection = connection;
        this.tableName = tableName;
        this.rowMapper = rowMapper;
        this.insertBinder = insertBinder;
        this.updateBinder = updateBinder;
    }

    @Override
    public ENTITY findById(ID id) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE id = ?")) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rowMapper.apply(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ENTITY> findAll() {
        List<ENTITY> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tableName)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rowMapper.apply(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public ENTITY save(ENTITY entity) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?)")) {
            insertBinder.bind(stmt, entity);
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ENTITY update(ENTITY entity) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE " + tableName + " SET name = ?, email = ? WHERE id = ?")) {
            updateBinder.bind(stmt, entity);
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(ID id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tableName + " WHERE id = ?")) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface SqlBinder<T> {
        void bind(PreparedStatement stmt, T entity) throws SQLException;
    }
}
