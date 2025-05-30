import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/lab6db",
                    "postgres",
                    "qwe123"
            );

            User user = new User(1, "Alice", "alice@gmail.com");

            DbRepositoryImpl<User, Integer> repo = new DbRepositoryImpl<>(
                    connection,
                    "users",
                    rs -> {
                        try {
                            return new User(
                                    rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getString("email")
                            );
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to read from ResultSet", e);
                        }
                    },
                    (stmt, u) -> {
                        stmt.setInt(1, u.id);
                        stmt.setString(2, u.name);
                        stmt.setString(3, u.email);
                    },
                    (stmt, u) -> {
                        stmt.setString(1, u.name);
                        stmt.setString(2, u.email);
                        stmt.setInt(3, u.id);
                    }
            );

            repo.save(user);

            for (User u : repo.findAll()) {
                System.out.println(u);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
