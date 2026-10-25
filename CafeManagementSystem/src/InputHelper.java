
import java.util.Scanner;

public class InputHelper {
    public static int readInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);

            if (sc.hasNextInt()) {
                int value = sc.nextInt();
                sc.nextLine();
                return value;
            }

            System.out.println("Invalid input. Please enter a whole number.");
            sc.nextLine();
        }
    }

    public static double readDouble(Scanner sc, String message) {
        while (true) {
            System.out.print(message);

            if (sc.hasNextDouble()) {
                double value = sc.nextDouble();
                sc.nextLine();
                return value;
            }

            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine();
        }
    }
}
