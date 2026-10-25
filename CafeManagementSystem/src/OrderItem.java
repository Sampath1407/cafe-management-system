
public class OrderItem {
    private MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return item.getPrice() * quantity;
    }

    public void displayOrderItem() {
        System.out.printf("%-18s %4d %10.2f %10.2f%n",
                item.getName(),
                quantity,
                item.getPrice(),
                getTotal());
    }
}
