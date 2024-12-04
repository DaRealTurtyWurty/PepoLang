package dev.turtywurty.pepolang.logging;

public class LogManager {
    public static void report(int pos, String where, String message) {
        System.err.println("[pos " + pos + "] Error" + where + ": " + message);
    }
}
