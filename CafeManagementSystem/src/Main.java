
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = CafeData.sc;
        ArrayList<MenuItem> menu = CafeData.menu;
        Order order = new Order();

        System.out.println("\n======= OUR CAFE MENU =======");

        for (int i = 0; i < menu.size(); i++) {
            System.out.print((i + 1) + ". ");
            menu.get(i).displayItem();
        }

        if (menu.isEmpty()) {
            System.out.println("The menu is currently empty.");
            return;
        }

        while (true) {
            int choice = InputHelper.readInt(sc,
                    "\nEnter item number (0 to finish): ");

            if (choice == 0) {
                break;
            }

            if (choice < 1 || choice > menu.size()) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            int quantity = InputHelper.readInt(sc, "Enter quantity: ");

            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                continue;
            }

            order.addItem(menu.get(choice - 1), quantity);
            System.out.println("Item added!");
        }

        if (order.getTotal() == 0) {
            System.out.println("No items ordered.");
            return;
        }

        order.displayBill();

        int payment = InputHelper.readInt(sc,
                "Confirm cash payment? (1 = Yes, 0 = Cancel): ");

        if (payment == 1) {
            order.confirmPayment();
            CafeData.orders.add(order);
            System.out.println("Cash payment received!");
            System.out.println("Order placed successfully. Thank you!");
        } else if (payment == 0) {
            System.out.println("Payment cancelled. Order not added to history.");
        } else {
            System.out.println("Invalid payment choice. Order not completed.");
        }
    }
}
