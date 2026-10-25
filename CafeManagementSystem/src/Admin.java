
public class Admin {
    private String username = "admin";
    private String password = "cafe123";

    public boolean login(String user, String pass) {
        return username.equals(user) && password.equals(pass);
    }
}
