/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Item implements ItemInterface {
    private String name;
    private double cost;
    private String seller;
    private boolean sellable;  // new: true if listed for sale

    /** Items constructed via addItem/start listed for sale by default. */
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
     */
    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    @Override
    public String toString() {
        return name;
    }
}
