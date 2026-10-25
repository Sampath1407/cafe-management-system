
import java.util.Scanner;

public class CafeApp {
    public static void main(String[] args) {
        Scanner sc = CafeData.sc;

        while (true) {
            System.out.println("\n==========================");
            System.out.println("   WELCOME TO OUR CAFE");
            System.out.println("==========================");
            System.out.println("1. Customer");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                Main.main(new String[0]);
            } else if (choice == 2) {
                AdminPanel.main(new String[0]);
            } else if (choice == 3) {
                System.out.println("Thank you for visiting!");
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
