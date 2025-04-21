## Overview

This application supports user accounts, item listings, direct messaging between users, and basic payment processing. It's a simple marketplace where users can buy, sell, and communicate about items.

## Features

Phase 1:

- **User Account Management**
  - User registration
  - Password-protected login
  - Account deletion with data cleanup

- **Item Management**
  - Sellers can create item listings
  - Buyers can purchase items
  - Owned items are tracked per user
  - Marketplace item search
  - Item deletion from marketplace and user inventory

- **Messaging System**
  - Users can send direct messages to each other
  - Messages are stored and viewable by participants
  - Conversations can be retrieved between two users

- **Transactions**
  - Buyers can purchase items if balance is sufficient
  - Seller receives funds
  - Buyer receives the item
  - Item is removed from marketplace and seller inventory

- **Testing**
  - JUnit 5 testing that validates all major features

Phase 2

**Overview**

Phase 2 introduces network I/O: a single server process manages all data (users, inventory, listings, messaging, balances), and two console‐based clients (`Client` and `Client2`) connect for multi‐client testing.


**Run Application in IntelliJ**

1. **Open `Server.java`** to start the server.
2. **Open `Client.java`** and run it for client #1.
3. **Open `Client2.java`** and run it for client #2.

Each client connects to the same local server at `localhost:12345`.

---

**Client Commands**

```text
register <user> <pass> <balance>   Create account
login <user> <pass>                Authenticate
logout                              Sign out
additem <name> <cost>              Add new item to inventory
sellitem <name>                    List an owned item for sale
unsellitem <name>                  Remove a listing, return to inventory
listitems                          Show all items currently listed
myitems                            Show items currently in your inventory
searchitem <name>                  Find a specific listed item
buy <name>                         Purchase a listed item
getbalance                         Display your current balance
deleteitem <name>                  Permanently delete your own listing
sendmessage <user> <message>       Send a direct message
viewuserlist                       Show users you’ve messaged
viewconversation <user>            Display DM thread with a user
exit                               Disconnect from server
```

---

**Manual Test Walkthrough** (in IntelliJ)

1. **Server:** Run `Server.java`

2. **Client #1 (Alice):** Run `Client.java`
   ```text
   register alice pw 100
   login alice pw
   additem widget 10.00
   sellitem widget
   listitems
   logout
   ```

3. **Client #2 (Bob):** Run `Client2.java`
   ```text
   register bob pw2 50
   login bob pw2
   listitems
   buy widget
   getbalance
   viewconversation alice
   ```

4. **Back to Alice (Client #1)**
   ```text
   login alice pw
   viewconversation bob
   ```

All of the tests should cover all server logic, command parsing, and data operations.