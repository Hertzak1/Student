import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyDataBase {
    public static String url = "jdbc:mysql://localhost/college";
    public static String user = "root";
    public static String password = "";

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void distributeVariantsToAllStudents() {
        Connection connection = null;
        try {
            connection = openConnection();

            String studentQuery = "SELECT id_student, surname FROM Student WHERE id_student NOT IN (SELECT id_student FROM Variants)";
            PreparedStatement studentStmt = connection.prepareStatement(studentQuery);
            ResultSet studentResult = studentStmt.executeQuery();

            Map<Integer, Integer> variantCount = new HashMap<>();
            for (int i = 1; i <= 10; i++) {
                variantCount.put(i, 0);
            }

            String variantQuery = "SELECT variantions, COUNT(*) AS count FROM Variants GROUP BY variantions";
            PreparedStatement variantStmt = connection.prepareStatement(variantQuery);
            ResultSet variantResult = variantStmt.executeQuery();
            while (variantResult.next()) {
                int variant = variantResult.getInt("variantions");
                int count = variantResult.getInt("count");
                variantCount.put(variant, count);
            }

            Random random = new Random();
            while (studentResult.next()) {
                int studentId = studentResult.getInt("id_student");
                String surname = studentResult.getString("surname");
                int variant = random.nextInt(10) + 1;

                while (variantCount.get(variant) >= 4) {
                    variant = random.nextInt(10) + 1;
                }

                variantCount.put(variant, variantCount.get(variant) + 1);

                String insertVariant = "INSERT INTO Variants (id_var, id_student, variantions) VALUES (NULL, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertVariant);
                insertStmt.setInt(1, studentId);
                insertStmt.setInt(2, variant);
                insertStmt.executeUpdate();

                System.out.println("Студенту " + surname + " назначен вариант " + variant);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void showSortedStudentsWithVariants() {
        Connection connection = null;
        try {
            connection = openConnection();
            String query = "SELECT Student.surname, Student.name, Student.lastname, Variants.variantions " +
                    "FROM Student INNER JOIN Variants ON Student.id_student = Variants.id_student " +
                    "ORDER BY Student.surname";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String surname = resultSet.getString("surname");
                String name = resultSet.getString("name");
                String lastname = resultSet.getString("lastname");
                int variant = resultSet.getInt("variantions");

                System.out.println(surname + " " + name + " " + lastname + " - Вариант: " + variant);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void showStudentsGroupedByVariant() {
        Connection connection = null;
        try {
            connection = openConnection();

            String query = "SELECT s.surname, s.name, s.lastname, v.variantions " +
                    "FROM Student s " +
                    "JOIN Variants v ON s.id_student = v.id_student " +
                    "ORDER BY v.variantions, s.surname";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            int currentVariant = -1;
            while (resultSet.next()) {
                int variant = resultSet.getInt("variantions");
                String surname = resultSet.getString("surname");
                String name = resultSet.getString("name");
                String lastname = resultSet.getString("lastname");

                if (variant != currentVariant) {
                    currentVariant = variant;
                    System.out.println("\n=== Вариант " + currentVariant + " ===");
                }

                System.out.println(surname + " " + name + " " + lastname);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void showVariantBySurname(String surname) {
        Connection connection = null;
        try {
            connection = openConnection();
            String query = "SELECT Variants.variantions FROM Student INNER JOIN Variants " +
                    "ON Student.id_student = Variants.id_student WHERE Student.surname = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, surname);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int variant = resultSet.getInt("variantions");
                System.out.println("Вариант для студента " + surname + ": " + variant);
            } else {
                System.out.println("Студент с фамилией " + surname + " не найден.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void showStudentsByVariant(int variant) {
        Connection connection = null;
        try {
            connection = openConnection();
            String query = "SELECT Student.surname, Student.name, Student.lastname FROM Student " +
                    "INNER JOIN Variants ON Student.id_student = Variants.id_student WHERE Variants.variantions = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, variant);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String surname = resultSet.getString("surname");
                String name = resultSet.getString("name");
                String lastname = resultSet.getString("lastname");

                System.out.println(surname + " " + name + " " + lastname);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void showCountOfStudentsPerVariant() {
        Connection connection = null;
        try {
            connection = openConnection();
            String query = "SELECT Variants.variantions, COUNT(*) AS count FROM Variants GROUP BY Variants.variantions";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int variant = resultSet.getInt("variantions");
                int count = resultSet.getInt("count");

                System.out.println("Вариант " + variant + ": " + count + " студентов");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void addStudent(String surname, String name, String lastname, int id_group) {
        Connection connection = null;
        try {
            connection = openConnection();
            String query = "INSERT INTO Student (id_student, surname, name, lastname, id_group) " +
                    "VALUES (NULL, ? , ? , ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, surname);
            statement.setString(2, name);
            statement.setString(3, lastname);
            statement.setInt(4, id_group);
            int rowsTrue = statement.executeUpdate();
            if (rowsTrue > 0) System.out.println("Студент добавлен.");
            else System.out.println("Ошибка при добавлении студента.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }
}
