## Overview

This application supports user accounts, item listings, direct messaging between users, and basic payment processing. It's a simple marketplace where users can buy, sell, and communicate about items.

## Features

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
