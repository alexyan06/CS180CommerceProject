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

# Phase 2

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
logout                             Sign out
additem <name> <cost>              Add new item to inventory
sellitem <name>                    List an owned item for sale
unsellitem <name>                  Remove a listing, return to inventory
changeitemprice <name> <price>     Changes an items price if the user owns it
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

# Phase 3 

**Overview**

Phase 3 adds a full Swing‐based GUI (`MarketplaceGUI.java`) and a reusable `ClientConnection` helper. All client interactions now occur through the GUI, which communicates with the server via `ClientConnection`, handling async updates and user actions.

---

## Features

- **Swing GUI**: Single‐window application with three main views—Items, Inventory, Messages—navigated via a top bar.
- **ClientConnection**: Encapsulates Socket I/O and listener thread; dispatches each server line to a callback for seamless async updates.
- **Responsive Layout**: Panels resize with the window. Custom colors and fonts provide a clean, Purdue‐inspired theme.
- **GUI Commands** (buttons and dialogs replace text commands):
  - **Add Item**: Popup dialog for name + price
  - **Sell / Unsell**: Contextual buttons in inventory and items views
  - **Change Price**: Inline text field + button updates a listing’s price
  - **Buy Item**: Purchase or remove own listing via confirmation dialog
  - **Messaging**: Select a user, view a clean DM thread, send new messages
  - **Balance & Greeting**: Live balance display and personalized welcome

---

## Setup & Run


  - **Server**: `Server.main` → Run
  - **GUI Client**: `MarketplaceGUI.main` → Run
  - **Second GUI Client**: `MarketplaceGUI2.main` > Run

The GUI will connect automatically to `localhost:12345`.

---

## Manual GUI Walkthrough

1. **Launch Server**
2. **Run** the GUI:
  - Register with a username, password, and starting balance
  - Login to enter the marketplace
3. **Items Tab**:
  - View all listed items
  - Search by name
  - Buy items (or remove your own via “Unsell”)
  - Add item
  - Change your item’s price directly in the list
4. **Inventory Tab**:
  - See your owned items
  - Sell selected inventory items
  - Delete or change price for your own inventory
5. **Messages Tab**:
  - See users you’ve messaged
  - Select a user to view a threaded, de‐duplicated DM history
  - Send new messages; scroll shows live updates
6. **Balance & Logout**:
  - Balance label updates after every action
  - Logout returns you to the login screen

---

**Testing**

All previous JUnit tests for server logic and command parsing remain valid. GUI‐listener logic (e.g. sending `changeitemprice`) can be unit‐tested by mocking `ClientConnection` and invoking button actions directly.