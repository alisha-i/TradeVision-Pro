import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class Test {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:tradevision.db");
        ResultSet rs = c.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
        while(rs.next()){
            System.out.println("Table: " + rs.getString("name"));
        }
        c.close();
    }
}
