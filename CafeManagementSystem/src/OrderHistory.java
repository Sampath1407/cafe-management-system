
import java.util.ArrayList;

public class OrderHistory {
    public static void displayHistory() {
        ArrayList<Order> orders = CafeData.orders;

        System.out.println("\n========== ORDER HISTORY ==========");

        if (orders.isEmpty()) {
            System.out.println("No completed orders found.");
            return;
        }

        System.out.printf("%-12s %15s %18s%n",
                "Order ID", "Total (Rs.)", "Payment Status");
        System.out.println("-----------------------------------------------");

        for (Order order : orders) {
            String status = order.isPaid() ? "Paid (Cash)" : "Unpaid";

            System.out.printf("%-12d %15.2f %18s%n",
                    order.getOrderId(),
                    order.getTotal(),
                    status);
        }

        System.out.println("===============================================");
    }
}
