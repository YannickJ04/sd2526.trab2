package sd2526.trab.impl.utils;

public class ServerSecret {

    public static final String SECRET_HEADER = "X-Server-Secret";

    private static String secret = "default-secret"; // fallback default

    public static void set(String s) {
        secret = s;
    }

    public static String get() {
        return secret;
    }

    public static boolean isValid(String incoming) {
        return secret != null && secret.equals(incoming);
    }
}