package main;

/**
 * Test class for StaffService functionality.
 * Tests book addition and removal operations.
 */
public class StaffBackendTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing StaffService...");
        
        StaffService staffService = new StaffService();
        
        // Test 1: Add a book
        System.out.println("\n📚 Test 1: Adding a book");
        boolean addResult = staffService.addBook("Test Book", "Test Genre", 299.99, 10);
        System.out.println("Add book result: " + (addResult ? "✅ SUCCESS" : "❌ FAILED"));
        
        // Test 2: Get book details (we'll need to know the book ID)
        System.out.println("\n📖 Test 2: Getting book details");
        // Note: In a real scenario, you'd get the book ID from the add operation
        // For now, we'll test with a known book ID (assuming book ID 1 exists)
        StaffService.BookDetails bookDetails = staffService.getBookDetails(1);
        if (bookDetails != null) {
            System.out.println("✅ Book found: " + bookDetails.title + " - " + bookDetails.genre);
        } else {
            System.out.println("❌ Book not found or error occurred");
        }
        
        // Test 3: Remove a book (we'll test with book ID 1)
        System.out.println("\n🗑️ Test 3: Removing a book");
        boolean removeResult = staffService.removeBook(1);
        System.out.println("Remove book result: " + (removeResult ? "✅ SUCCESS" : "❌ FAILED"));
        
        // Test 4: Input validation
        System.out.println("\n🔍 Test 4: Input validation");
        try {
            staffService.addBook("", "Valid Genre", 100.0, 5); // Empty title
            System.out.println("❌ Should have thrown exception for empty title");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Correctly caught empty title: " + e.getMessage());
        }
        
        try {
            staffService.addBook("Valid Title", "", 100.0, 5); // Empty genre
            System.out.println("❌ Should have thrown exception for empty genre");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Correctly caught empty genre: " + e.getMessage());
        }
        
        try {
            staffService.addBook("Valid Title", "Valid Genre", -100.0, 5); // Negative price
            System.out.println("❌ Should have thrown exception for negative price");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Correctly caught negative price: " + e.getMessage());
        }
        
        try {
            staffService.addBook("Valid Title", "Valid Genre", 100.0, -5); // Negative stock
            System.out.println("❌ Should have thrown exception for negative stock");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Correctly caught negative stock: " + e.getMessage());
        }
        
        System.out.println("\n🎉 StaffService testing completed!");
    }
} 