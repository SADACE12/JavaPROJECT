import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuizManager {
    private static final String DB_URL = "jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:6543/postgres?user=postgres.tfikxlxhtoknokpgdbtw&password=FCCVOGOWWC2005";

    // ===================== АУТЕНТИФИКАЦИЯ =====================

    public boolean register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String login(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ===================== ВОПРОСЫ =====================

    public void addQuestion(Question q) {
        String sql = "INSERT INTO questions (question_text, opt1, opt2, opt3, opt4, correct_index) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getText());
            pstmt.setString(2, q.getOptions().get(0));
            pstmt.setString(3, q.getOptions().get(1));
            pstmt.setString(4, q.getOptions().get(2));
            pstmt.setString(5, q.getOptions().get(3));
            pstmt.setInt(6, q.getCorrectAnswer());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateQuestion(Question q) {
        String sql = "UPDATE questions SET question_text=?, opt1=?, opt2=?, opt3=?, opt4=?, correct_index=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getText());
            pstmt.setString(2, q.getOptions().get(0));
            pstmt.setString(3, q.getOptions().get(1));
            pstmt.setString(4, q.getOptions().get(2));
            pstmt.setString(5, q.getOptions().get(3));
            pstmt.setInt(6, q.getCorrectAnswer());
            pstmt.setInt(7, q.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Question> loadQuiz() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, question_text, opt1, opt2, opt3, opt4, correct_index FROM questions ORDER BY id";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"),
                        rs.getString("question_text"),
                        Arrays.asList(rs.getString("opt1"), rs.getString("opt2"), rs.getString("opt3"), rs.getString("opt4")),
                        rs.getInt("correct_index")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return questions;
    }

    // ===================== КОМНАТЫ =====================

    /**
     * Создаёт новую комнату и возвращает уникальный код.
     * @param roomName   название теста
     * @param teacherName имя преподавателя
     * @return код комнаты (6 символов) или null при ошибке
     */
    public String createRoom(String roomName, String teacherName) {
        String code = generateRoomCode();
        String sql = "INSERT INTO rooms (code, name, created_by, is_active) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, roomName);
            pstmt.setString(3, teacherName);
            pstmt.executeUpdate();
            return code;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Проверяет, существует ли активная комната с данным кодом.
     * @return название комнаты или null, если не найдена / закрыта
     */
    public String validateRoomCode(String code) {
        String sql = "SELECT name FROM rooms WHERE code = ? AND is_active = TRUE";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Закрывает комнату (преподаватель завершает сессию).
     */
    public void closeRoom(String code) {
        String sql = "UPDATE rooms SET is_active = FALSE WHERE code = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.toUpperCase());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Возвращает список активных комнат, созданных данным преподавателем.
     */
    public List<String[]> getActiveRooms(String teacherName) {
        List<String[]> rooms = new ArrayList<>();
        String sql = "SELECT code, name, created_at FROM rooms WHERE created_by = ? AND is_active = TRUE ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(new String[]{rs.getString("code"), rs.getString("name"), rs.getString("created_at")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    // ===================== РЕЗУЛЬТАТЫ =====================

    /**
     * Сохраняет результат студента после прохождения теста.
     */
    public void saveResult(String roomCode, String studentName, int score, int total) {
        double percentage = total > 0 ? (double) score / total * 100 : 0;
        String grade = calcGrade(percentage);
        String sql = "INSERT INTO results (room_code, student_name, score, total, percentage, grade) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomCode.toUpperCase());
            pstmt.setString(2, studentName);
            pstmt.setInt(3, score);
            pstmt.setInt(4, total);
            pstmt.setDouble(5, percentage);
            pstmt.setString(6, grade);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Загружает все результаты для конкретной комнаты (для просмотра преподавателем).
     * @return список строк: [имя студента, счёт, всего, процент, оценка, время]
     */
    public List<String[]> getResultsForRoom(String roomCode) {
        List<String[]> results = new ArrayList<>();
        String sql = "SELECT student_name, score, total, percentage, grade, submitted_at " +
                "FROM results WHERE room_code = ? ORDER BY percentage DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomCode.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new String[]{
                        rs.getString("student_name"),
                        String.valueOf(rs.getInt("score")),
                        String.valueOf(rs.getInt("total")),
                        String.format("%.1f%%", rs.getDouble("percentage")),
                        rs.getString("grade"),
                        rs.getString("submitted_at").substring(0, 16) // убираем секунды
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // ===================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====================

    /** Генерирует случайный 6-символьный код (только буквы и цифры, верхний регистр). */
    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // исключены похожие символы O,0,1,I
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String calcGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
}