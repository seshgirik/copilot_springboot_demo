import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class test_password {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test the hash from data.sql
        String storedHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        String password = "password123";
        
        System.out.println("Testing password: " + password);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Hash matches: " + encoder.matches(password, storedHash));
        
        // Generate a new hash for comparison
        String newHash = encoder.encode(password);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(password, newHash));
    }
} 