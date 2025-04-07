/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Item implements ItemInterface {
    private String name;
    private double cost;
    private String seller;

    public Item(String name, double cost, String seller) {
        this.name = name;
        this.cost = cost;
        this.seller = seller;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public String getSeller() {
        return seller;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String toString() {
        return name;
    }
}
