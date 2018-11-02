public class OnStackDemo {
    public static class User {
        public int id = 0;
	public String name = "";

    }

    public static void alloc() {
        User user = new User();
        user.id = 1;
        user.name = "lee";
    }

    public static void main(String[] args) {
        long a = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            alloc();
        }
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }
}
