
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminPanel {
    private static final String API = "http://127.0.0.1:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin();

        System.out.print("Username: ");
        String user = sc.nextLine();

        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (!admin.login(user, pass)) {
            System.out.println("Invalid login!");
            return;
        }

        System.out.println("\nWelcome to the Admin Panel!");

        while (true) {
            System.out.println("\n======= ADMIN PANEL =======");
            System.out.println("1. View Menu");
            System.out.println("2. Add Item");
            System.out.println("3. Remove Item");
            System.out.println("4. Edit Item");
            System.out.println("5. Exit");
            System.out.println("6. View Website Order History");

            int choice = InputHelper.readInt(sc, "Choose an option: ");

            try {
                switch (choice) {
                    case 1:
                        showMenu();
                        break;
                    case 2:
                        addItem(sc);
                        break;
                    case 3:
                        removeItem(sc);
                        break;
                    case 4:
                        editItem(sc);
                        break;
                    case 5:
                        System.out.println("Exiting Admin Panel. Goodbye!");
                        return;
                    case 6:
                        showOrderHistory();
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Could not connect to the cafe server.");
                System.out.println("Make sure CafeServer is running on port 8080.");
            }
        }
    }

    private static String request(
            String path, String method, String body) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API + path))
                .header("Content-Type", "application/json");

        if (method.equals("GET")) {
            builder.GET();
        } else {
            builder.method(method,
                    HttpRequest.BodyPublishers.ofString(body));
        }

        HttpResponse<String> response = client.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            Matcher matcher = Pattern.compile(
                    "\"error\":\"((?:\\\\.|[^\"\\\\])*)\"")
                    .matcher(response.body());

            if (matcher.find()) {
                throw new IllegalArgumentException(
                        matcher.group(1));
            }

            throw new IllegalStateException(
                    "Server returned status " + response.statusCode());
        }

        return response.body();
    }

    private static ArrayList<String[]> getMenu() throws Exception {
        String json = request("/api/menu", "GET", "");

        ArrayList<String[]> items = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "\\{\"name\":\"((?:\\\\.|[^\"\\\\])*)\","
                + "\"price\":([0-9.]+),"
                + "\"category\":\"((?:\\\\.|[^\"\\\\])*)\"\\}");

        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            items.add(new String[] {
                    unescapeJson(matcher.group(1)),
                    matcher.group(2),
                    unescapeJson(matcher.group(3))
            });
        }

        return items;
    }

    private static void showMenu() throws Exception {
        ArrayList<String[]> menu = getMenu();

        System.out.println("\n========== CAFE MENU ==========");

        if (menu.isEmpty()) {
            System.out.println("No menu items available.");
            return;
        }

        System.out.printf("%-5s %-22s %-12s %-15s%n",
                "No.", "Name", "Price", "Category");

        for (int i = 0; i < menu.size(); i++) {
            String[] item = menu.get(i);

            System.out.printf("%-5d %-22s Rs. %-8s %-15s%n",
                    i + 1, item[0], item[1], item[2]);
        }
    }

    private static void addItem(Scanner sc) throws Exception {
        System.out.print("Enter item name: ");
        String name = sc.nextLine().trim();

        double price = InputHelper.readDouble(sc, "Enter price: ");

        System.out.print("Enter category: ");
        String category = sc.nextLine().trim();

        if (name.isEmpty() || category.isEmpty()
                || !Double.isFinite(price) || price <= 0) {
            System.out.println("Invalid details.");
            return;
        }

        String body = "{\"name\":\"" + escapeJson(name)
                + "\",\"price\":" + price
                + ",\"category\":\"" + escapeJson(category) + "\"}";

        System.out.println(request("/api/admin/menu/add", "POST", body));
    }

    private static void removeItem(Scanner sc) throws Exception {
        showMenu();

        ArrayList<String[]> menu = getMenu();

        if (menu.isEmpty()) return;

        int itemNo = InputHelper.readInt(
                sc, "Enter item number to remove: ");

        if (itemNo < 1 || itemNo > menu.size()) {
            System.out.println("Invalid item number.");
            return;
        }

        String name = menu.get(itemNo - 1)[0];

        String body = "{\"name\":\"" + escapeJson(name) + "\"}";

        System.out.println(request(
                "/api/admin/menu/delete", "POST", body));
    }

    private static void editItem(Scanner sc) throws Exception {
        showMenu();

        ArrayList<String[]> menu = getMenu();

        if (menu.isEmpty()) return;

        int itemNo = InputHelper.readInt(
                sc, "Enter item number to edit: ");

        if (itemNo < 1 || itemNo > menu.size()) {
            System.out.println("Invalid item number.");
            return;
        }

        String oldName = menu.get(itemNo - 1)[0];

        System.out.print("Enter new name: ");
        String name = sc.nextLine().trim();

        double price = InputHelper.readDouble(
                sc, "Enter new price: ");

        System.out.print("Enter new category: ");
        String category = sc.nextLine().trim();

        if (name.isEmpty() || category.isEmpty()
                || !Double.isFinite(price) || price <= 0) {
            System.out.println("Invalid details.");
            return;
        }

        String body = "{\"oldName\":\"" + escapeJson(oldName)
                + "\",\"name\":\"" + escapeJson(name)
                + "\",\"price\":" + price
                + ",\"category\":\"" + escapeJson(category) + "\"}";

        System.out.println(request(
                "/api/admin/menu/update", "POST", body));
    }

    private static void showOrderHistory() throws Exception {
        String json = request("/api/admin/orders", "GET", "");

        System.out.println("\n========== WEBSITE ORDER HISTORY ==========");

        Pattern pattern = Pattern.compile(
                "\\{\"orderId\":(\\d+),"
                + "\"total\":([0-9.]+),"
                + "\"paid\":(true|false)\\}");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            System.out.println("No website orders found.");
            return;
        }

        System.out.printf("%-12s %-15s %-15s%n",
                "Order ID", "Total (Rs.)", "Payment Status");

        matcher.reset();

        while (matcher.find()) {
            String status = Boolean.parseBoolean(matcher.group(3))
                    ? "Paid (Cash)" : "Unpaid";

            System.out.printf("%-12s %-15s %-15s%n",
                    matcher.group(1), matcher.group(2), status);
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private static String unescapeJson(String value) {
        return value.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r");
    }
}
