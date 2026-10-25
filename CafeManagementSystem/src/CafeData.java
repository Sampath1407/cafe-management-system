
import java.util.ArrayList;
import java.util.Scanner;

public class CafeData {
    public static final ArrayList<MenuItem> menu = new ArrayList<>();
    public static final ArrayList<Order> orders = new ArrayList<>();
    public static final Scanner sc = new Scanner(System.in);

    static {
        menu.add(new MenuItem("Cappuccino", 120, "Coffee"));
        menu.add(new MenuItem("Latte", 140, "Coffee"));
        menu.add(new MenuItem("Cold Coffee", 100, "Beverage"));
        menu.add(new MenuItem("Chocolate Cake", 150, "Dessert"));
        menu.add(new MenuItem("Brownie", 90, "Dessert"));
    }
}
