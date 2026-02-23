package com.example.meta_backend.service.generator.base;

/**
 * Utility class for handling app name conversions.
 * Java doesn't support hyphens in package names or class names,
 * so we need to convert them appropriately.
 */
public class AppNameUtils {

    /**
     * Converts app name to a Java-safe class name format.
     * Examples:
     * - "my-app" → "MyApp"
     * - "my_app" → "MyApp"
     * - "MyApp" → "MyApp"
     * - "MY-APP" → "MyApp"
     */
    public static String toClassName(String appName) {
        if (appName == null || appName.isEmpty()) {
            return appName;
        }
        
        String[] parts = appName.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                sb.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Converts app name to a safe package name format (still with dots).
     * Example: "my-app" → "com.metagen.backend.generated.myapp"
     */
    public static String toPackageName(String appName) {
        if (appName == null || appName.isEmpty()) {
            return appName;
        }
        
        // First convert to class name format, then lowercase for package
        return toClassName(appName).toLowerCase();
    }

    /**
     * Returns the app name as-is for directory paths.
     * Example: "my-app" → "my-app"
     */
    public static String toDirectoryName(String appName) {
        return appName;
    }
}
