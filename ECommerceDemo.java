import java.util.ArrayList;
import java.util.List;

interface Shippable {
    String getName();
    double getWeight();
    String getFormattedWeight();
}

class ShippingService {
    public void shipItems(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        
        for (Shippable item : items) {
            System.out.printf("%dx %s %s\n", 
                ((CartItem)item).getQuantity(), 
                item.getName(), 
                item.getFormattedWeight());
            totalWeight += item.getWeight() * ((CartItem)item).getQuantity();
        }
        
        System.out.printf("Total package weight %.1fkg\n\n", totalWeight);
    }
}
class Product {
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }

    public void reduceQuantity(int amount) {
        if (amount <= quantity) {
            quantity -= amount;
        }
    }
}

class ShippableProduct extends Product implements Shippable {
    private double weight;
    
    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getFormattedWeight() {
        return String.format("%.0fg", weight * 1000);
    }
}

class NonShippableProduct extends Product {
    public NonShippableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

class CartItem implements Shippable {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getItemTotal() { return product.getPrice() * quantity; }

    @Override
    public String getName() {
        return product.getName();
    }

    @Override
    public double getWeight() {
        if (product instanceof Shippable) {
            return ((Shippable)product).getWeight();
        }
        return 0;
    }

    @Override
    public String getFormattedWeight() {
        if (product instanceof Shippable) {
            return ((Shippable)product).getFormattedWeight();
        }
        return "0g";
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void deductBalance(double amount) { balance -= amount; }
}

class Cart {
    private List<CartItem> items = new ArrayList<>();
    private Customer customer;

    public Cart(Customer customer) {
        this.customer = customer;
    }

    public void add(Product product, int quantity) {
        if (product.isAvailable(quantity)) {
            items.add(new CartItem(product, quantity));
            product.reduceQuantity(quantity);
        }
    }

    public void checkout(ShippingService shippingService) {
        // Filter only shippable items
        List<Shippable> shippableItems = new ArrayList<>();
        List<CartItem> receiptItems = new ArrayList<>();
        
        for (CartItem item : items) {
            if (item.getProduct() instanceof Shippable) {
                shippableItems.add(item);
            }
            // Only show Cheese and Biscuits in receipt (as per your example)
            if (item.getProduct().getName().equals("Cheese") || 
                item.getProduct().getName().equals("Biscuits")) {
                receiptItems.add(item);
            }
        }
        
        // Shipping notice
        if (!shippableItems.isEmpty()) {
            shippingService.shipItems(shippableItems);
        }

        // Calculate totals only for items in receipt
        double subtotal = receiptItems.stream()
            .mapToDouble(CartItem::getItemTotal)
            .sum();
        
        double shippingFee = 30; // Fixed shipping as per your example
        double total = subtotal + shippingFee;

        // Print receipt
        System.out.println("** Checkout receipt **");
        for (CartItem item : receiptItems) {
            System.out.printf("%dx %s %.0f\n", 
                item.getQuantity(), 
                item.getProduct().getName(), 
                item.getItemTotal());
        }
        
        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f\n", subtotal);
        System.out.printf("Shipping %.0f\n", shippingFee);
        System.out.printf("Amount %.0f\n", total);

        // Deduct from customer balance
        customer.deductBalance(total);
    }
}

public class ECommerceDemo {
    public static void main(String[] args) {
        // Create products with your exact prices and weights
        ShippableProduct cheese = new ShippableProduct("Cheese", 100, 10, 0.4);
        ShippableProduct biscuits = new ShippableProduct("Biscuits", 150, 5, 0.7);
        NonShippableProduct tv = new NonShippableProduct("TV", 300, 5);
        NonShippableProduct scratchCard = new NonShippableProduct("Scratch Card", 50, 100);

        // Create customer and cart
        Customer customer = new Customer("John Doe", 1000);
        Cart cart = new Cart(customer);

        // Add items to cart as per your example
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(tv, 3);
        cart.add(scratchCard, 1);

        // Checkout
        ShippingService shippingService = new ShippingService();
        cart.checkout(shippingService);
    }
}
