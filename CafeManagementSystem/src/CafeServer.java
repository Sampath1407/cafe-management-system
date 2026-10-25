
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CafeServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080), 0);

        server.createContext("/api/status", exchange -> {
            if (!allowMethod(exchange, "GET")) return;

            sendJson(exchange, 200,
                    "{\"message\":\"Cafe Java server is running!\"}");
        });

        server.createContext("/api/menu", exchange -> {
            if (!allowMethod(exchange, "GET")) return;
            sendJson(exchange, 200, getMenuJson());
        });

        server.createContext("/api/checkout", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(
                    exchange.getRequestMethod())) {
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!allowMethod(exchange, "POST")) return;

            try {
                String body = readBody(exchange);

                if (!"Cash".equals(getString(body, "paymentMethod"))) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Cash payment is required.\"}");
                    return;
                }

                Matcher matcher = Pattern.compile(
                        "\\{\"name\":\"((?:\\\\.|[^\"\\\\])*)\","
                      + "\"quantity\":(\\d+)\\}")
                        .matcher(body);

                Order order = new Order();
                int count = 0;

                while (matcher.find()) {
                    String name = unescapeJson(matcher.group(1));
                    int quantity = Integer.parseInt(matcher.group(2));

                    if (quantity < 1 || quantity > 100) {
                        sendJson(exchange, 400,
                                "{\"error\":\"Invalid quantity.\"}");
                        return;
                    }

                    MenuItem found = findItem(name);

                    if (found == null) {
                        sendJson(exchange, 400,
                                "{\"error\":\"Unknown menu item.\"}");
                        return;
                    }

                    order.addItem(found, quantity);
                    count++;
                }

                if (count == 0) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Your cart is empty.\"}");
                    return;
                }

                order.confirmPayment();
                CafeData.orders.add(order);
                order.displayBill();

                sendJson(exchange, 200,
                        "{\"message\":\"Order placed successfully!\","
                      + "\"orderId\":" + order.getOrderId() + ","
                      + "\"total\":" + order.getTotal() + "}");

            } catch (Exception e) {
                sendJson(exchange, 400,
                        "{\"error\":\"Could not process the order.\"}");
            }
        });

        // Admin: add a menu item
        server.createContext("/api/admin/menu/add", exchange -> {
            if (!allowMethod(exchange, "POST")) return;

            try {
                String body = readBody(exchange);
                String name = getString(body, "name");
                String category = getString(body, "category");
                double price = getNumber(body, "price");

                if (name == null || name.trim().isEmpty()
                        || category == null || category.trim().isEmpty()
                        || !Double.isFinite(price) || price <= 0) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Invalid menu item details.\"}");
                    return;
                }

                if (findItem(name) != null) {
                    sendJson(exchange, 409,
                            "{\"error\":\"That menu item already exists.\"}");
                    return;
                }

                CafeData.menu.add(new MenuItem(name, price, category));

                sendJson(exchange, 200,
                        "{\"message\":\"Menu item added successfully.\"}");

            } catch (Exception e) {
                sendJson(exchange, 400,
                        "{\"error\":\"Invalid request.\"}");
            }
        });

        // Admin: update a menu item
        server.createContext("/api/admin/menu/update", exchange -> {
            if (!allowMethod(exchange, "POST")) return;

            try {
                String body = readBody(exchange);
                String oldName = getString(body, "oldName");
                String name = getString(body, "name");
                String category = getString(body, "category");
                double price = getNumber(body, "price");

                if (oldName == null || name == null
                        || name.trim().isEmpty()
                        || category == null || category.trim().isEmpty()
                        || !Double.isFinite(price) || price <= 0) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Invalid menu item details.\"}");
                    return;
                }

                MenuItem item = findItem(oldName);

                if (item == null) {
                    sendJson(exchange, 404,
                            "{\"error\":\"Menu item not found.\"}");
                    return;
                }

                if (!oldName.equals(name) && findItem(name) != null) {
                    sendJson(exchange, 409,
                            "{\"error\":\"That item name already exists.\"}");
                    return;
                }

                item.setName(name);
                item.setPrice(price);
                item.setCategory(category);

                sendJson(exchange, 200,
                        "{\"message\":\"Menu item updated successfully.\"}");

            } catch (Exception e) {
                sendJson(exchange, 400,
                        "{\"error\":\"Invalid request.\"}");
            }
        });

        // Admin: remove a menu item
        server.createContext("/api/admin/menu/delete", exchange -> {
            if (!allowMethod(exchange, "POST")) return;

            try {
                String body = readBody(exchange);
                String name = getString(body, "name");
                MenuItem item = name == null ? null : findItem(name);

                if (item == null) {
                    sendJson(exchange, 404,
                            "{\"error\":\"Menu item not found.\"}");
                    return;
                }

                CafeData.menu.remove(item);

                sendJson(exchange, 200,
                        "{\"message\":\"Menu item removed successfully.\"}");

            } catch (Exception e) {
                sendJson(exchange, 400,
                        "{\"error\":\"Invalid request.\"}");
            }
        });

        // Admin: view website order history
        server.createContext("/api/admin/orders", exchange -> {
            if (!allowMethod(exchange, "GET")) return;

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (Order order : CafeData.orders) {
                if (!first) json.append(",");

                json.append("{")
                    .append("\"orderId\":").append(order.getOrderId())
                    .append(",\"total\":").append(order.getTotal())
                    .append(",\"paid\":").append(order.isPaid())
                    .append("}");

                first = false;
            }

            json.append("]");
            sendJson(exchange, 200, json.toString());
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Cafe server started!");
        System.out.println("Menu: http://localhost:8080/api/menu");
        System.out.println("Admin APIs are ready.");
        System.out.println("Keep this terminal running.");
    }

    private static MenuItem findItem(String name) {
        for (MenuItem item : CafeData.menu) {
            if (item.getName().equals(name)) {
                return item;
            }
        }

        return null;
    }

    private static String getMenuJson() {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (MenuItem item : CafeData.menu) {
            if (!first) json.append(",");

            json.append("{")
                .append("\"name\":\"")
                .append(escapeJson(item.getName()))
                .append("\",\"price\":")
                .append(item.getPrice())
                .append(",\"category\":\"")
                .append(escapeJson(item.getCategory()))
                .append("\"}");

            first = false;
        }

        return json.append("]").toString();
    }

    private static String readBody(HttpExchange exchange)
            throws IOException {
        return new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    private static String getString(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(json);

        return matcher.find() ? unescapeJson(matcher.group(1)) : null;
    }

    private static double getNumber(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) return Double.NaN;

        return Double.parseDouble(matcher.group(1));
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

    private static boolean allowMethod(
            HttpExchange exchange, String method) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return false;
        }

        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Method not allowed.\"}");
            return false;
        }

        return true;
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(
            HttpExchange exchange, int status, String response)
            throws IOException {

        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(status, data.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }
}
