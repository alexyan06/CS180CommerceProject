# CS180CommerceProject

Command Example
register Alice password123 100.0 true
register Bob securepass 50.0 false
login Alice password123
add-item Laptop 500.0 Alice
list-items
send-msg Alice Bob Hi Bob, interested in your headphones?
view-msgs


Ideas needed:
- ProcessTransaction in Database
- Changing Test Cases in main
- Making messages to each user

Updates 4/3
- The Java marketplace and messaging system was refactored to improve modularity, flexibility, and testability. Interfaces were introduced for the User, Item, Message, and Database1 classes. The User class was updated to use a single ownedItems list, removing the need for a fixed buyer/seller distinction and allowing dynamic role behavior. New backend features were added, including login() for password validation, searchItem() for item lookup, and an enhanced deleteUser() method that removes associated messages and items. The manual test suite was fully converted into a JUnit 5-based test class, providing automated validation of core features such as account creation, login, messaging, item management, transactions, and data cleanup upon user deletion.
