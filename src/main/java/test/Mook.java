package test;

import entity.User;
import java.util.ArrayList;
import java.util.List;

public class Mook {
    
    public static List<User> createMockUsers() {
        List<User> users = new ArrayList<>();
        
        users.add(new User("root_admin", "ROOT", 1001L));
        
        users.add(new User("admin_zhang", "ADMIN", 2001L));
        users.add(new User("admin_li", "ADMIN", 2002L));
        users.add(new User("admin_wang", "ADMIN", 2003L));
        
        users.add(new User("user_zhao", "USER", 3001L));
        users.add(new User("user_qian", "USER", 3002L));
        users.add(new User("user_sun", "USER", 3003L));
        users.add(new User("user_zhou", "USER", 3004L));
        users.add(new User("user_wu", "USER", 3005L));
        users.add(new User("user_zheng", "USER", 3006L));
        
        return users;
    }
    
    public static void printAllUsers() {
        List<User> users = createMockUsers();
        System.out.println("=== Mock User List ===");
        System.out.println("Total: " + users.size() + " users\n");
        
        for (User user : users) {
            System.out.println(user);
        }
    }
    
    public static void main(String[] args) {
        printAllUsers();
    }
}
