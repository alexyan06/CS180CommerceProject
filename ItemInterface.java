/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public interface ItemInterface {
    String getName();
    double getCost();
    String getSeller();
    void setName(String name);
    void setCost(double cost);
    void setSeller(String seller);
}
