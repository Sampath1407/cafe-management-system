
import java.util.Scanner;

public class AdminTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin();

        System.out.print("Enter username: ");
        String user = sc.nextLine();

        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        if (admin.login(user, pass)) {
            System.out.println("Login successful! Welcome, Admin.");
        } else {
            System.out.println("Invalid username or password.");
        }

        sc.close();
    }
}
