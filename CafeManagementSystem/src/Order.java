
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private static int nextOrderId = 1001;
    private int orderId;
    private ArrayList<OrderItem> items = new ArrayList<>();
    private boolean paid = false;

    public Order() {
        orderId = nextOrderId++;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(MenuItem item, int quantity) {
        if (item == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid order item.");
        }

        items.add(new OrderItem(item, quantity));
    }

    public double getTotal() {
        double total = 0;

        for (OrderItem item : items) {
            total += item.getTotal();
        }

        return total;
    }

    public boolean isPaid() {
        return paid;
    }

    public void confirmPayment() {
        paid = true;
    }

    public void displayBill() {
        System.out.println("\n==============================================");
        System.out.println("              CAFE RECEIPT");
        System.out.println("==============================================");
        System.out.println("Order ID: " + orderId);
        System.out.println("----------------------------------------------");
        System.out.printf("%-18s %4s %10s %10s%n",
                "Item", "Qty", "Price", "Subtotal");
        System.out.println("----------------------------------------------");

        for (OrderItem item : items) {
            item.displayOrderItem();
        }

        System.out.println("----------------------------------------------");
        System.out.printf("%-32s Rs. %10.2f%n",
                "TOTAL AMOUNT:", getTotal());
        System.out.println("Payment Method: Cash");
        System.out.println("==============================================");
    }
}
