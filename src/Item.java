/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * Represents an item in the marketplace, tracking its name, price, owner,
 * and whether it's listed for sale.
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Item implements ItemInterface {
    /** The display name of this item. */
    private String name;
    /** The monetary cost of this item in dollars. */
    private double cost;
    /** Username of the current owner or seller of this item. */
    private String seller;
    /** Flag indicating if the item is currently listed for sale. */
    private boolean sellable;  // new: true if listed for sale

    /**
     * Constructs a new Item with the given name, cost, and seller.
     * Initially, items are not listed for sale.
     *
     * @param name    the display name of the item
     * @param cost    the initial price of the item
     * @param seller  the username of the item's owner
     */
    public Item(String name, double cost, String seller) {
        this.name = name;
        this.cost = cost;
        this.seller = seller;
        this.sellable = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public String getSeller() {
        return seller;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public void setSeller(String seller) {
        this.seller = seller;
    }

    /**
     * @return whether this item is currently listed for sale.
     */
    public boolean isSellable() {
        return sellable;
    }

    /**
     * Mark the item as listed (true) or unlisted (false).
     *
     * @param sellable new listed status of the item
     */
    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    @Override
    public String toString() {
        return name;
    }
}
