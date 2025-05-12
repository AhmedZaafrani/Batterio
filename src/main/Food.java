package main;

import java.util.Random;

/**
 * Classe che rappresenta la distribuzione di cibo
 *
 * @author Alessandro Bugatti
 */
public class Food {

    /**
     * Matrice del cibo
     */
    private static boolean food[][];
    /**
     * Larghezza della matrice
     */
    private static int width;
    /**
     * Altezza della matrice
     */
    private static int height;
    /**
     * is used to generate a stream of pseudorandom numbers
     */
    private static Random random;
    /**
     * Tipo di distribuzione da effettuare
     */
    private static Distribution distributionType;
    /**
     * Quantità di cibo prodotta, poi distribuita seguendo le regole di {distributionType}
     */
    private static int foodQuantity;
    /**
     * Grandezza in pixel dei quadrati di cibo
     * utilizzato solo per la distribuzione square e corner
     */
    private static int foodDimension;

    /**
     * Vari tipi di distribuzione del cibo
     */
    public static enum Distribution {
        SQUARE, CORNER, RANDOM
    }
    
    private Runnable distributionMethod;
    
    /**
     * @param w Larghezza della matrice
     * @param h Altezza della matrice
     */
    private Food(int w, int h, Distribution dt, int foodQuantity) {
        width = w;
        height = h;
        food = new boolean[w][h];
        random = new Random();
        Food.distributionType = dt;
        switch (dt) {
            case SQUARE:
                distributionMethod = Food::squareDistribution;
                break;
            case CORNER:
                distributionMethod = Food::cornerDistribution;
                break;
            case RANDOM:
                distributionMethod = Food::randomDistribution;
                break;
            default:
                distributionMethod = Food::squareDistribution;
        }
        Food.foodQuantity = foodQuantity;
        Food.foodDimension = 50;
    }

    /**
     * Distribuisce il cibo secondo una distribuzione quadrata
     */
    private static void squareDistribution() {
        int randx = random.nextInt(width - foodDimension);
        int randy = random.nextInt(height - foodDimension);
        for (int i = 0; i < foodQuantity; i++) {
            food[random.nextInt(foodDimension) + randx][random.nextInt(foodDimension) + randy] = true;
        }
    }

    /**
     * Distribuisce il cibo secondo una distribuzione casuale
     */
    private static void randomDistribution() {
        for (int i = 0; i < foodQuantity; i++) {
            food[random.nextInt(width - 1)][random.nextInt(height - 1)] = true;
        }
    }

    /**
     * Distribuisce il cibo secondo una distribuzione che distribuisce solo
     * negli angoli
    */
    private static void cornerDistribution() {
        int x = 0, y = 0, dx = 1, dy = 1;
        int corner = random.nextInt(4);
        switch (corner) {
            case 0:
                x = 0;
                y = 0;
                dx = 1;
                dy = 1;
                break;
            case 1:
                x = width - 1;
                y = 0;
                dx = -1;
                dy = 1;
                break;
            case 2:
                x = 0;
                y = height - 1;
                dx = 1;
                dy = -1;
                break;
            case 3:
                x = width - 1;
                y = height - 1;
                dx = -1;
                dy = -1;
                break;
        }
        for (int i = 0; i < foodQuantity; i++) {
            food[x + dx * random.nextInt(foodDimension)][y + dy * random.nextInt(foodDimension)] = true;
        }
    }

    /**
     * distribuisce il cibo
     */
    public void toggle() {
        distributionMethod.run();
    }

    /**
     *
     * @return il tipo di distribuzione
     */
    public static Distribution getDistributionType() {
        return Food.distributionType;
    }

    /**
     * Controlla se c'è cibo in posizione x,y
     *
     * @param x Coordinata x da controllare
     * @param y Coordinata y da controllare
     * @return True se c'è cibo in x,y, false altrimenti
     * <strong> Se x e y non sono valori validi per la matrice ritorna false,
     * evitando di sollevare un'eccezione</strong>
     */
    public static boolean isFood(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return food[x][y];
    }

    /**
     * Mangia il cibo in posizione x,y
     *
     * @param x Coordinata x
     * @param y Coordinata y
     * @return 
     */
    public boolean eatFood(int x, int y) {
        if (isFood(x, y)) {
            food[x][y] = false;
            return true;
        }
        return false;
    }

    /**
     * @return Larghezza
     */
    public static int getWidth() {
        return width;
    }

    /**
     * @return Altezza
     */
    public static int getHeight() {
        return height;
    }

    /**
     * 
     * @return 
     */
    
    public static int getFoodQuantity() {
        return Food.foodQuantity;
    }
    
    /**
     * 
     * @return 
     */
    
    public static int getFoodDimension() {
        return Food.foodDimension;
    }
    
    /**
     * Classe utilizzata per creare oggetti di tipo Food in modo tale che si
     * possa creare solo un oggetto di questo tipo e si possa richiedere il
     * riferimento a quell'oggetto una volta soltanto
     */
    public static final class Builder {

        /**
         * Larghezza della matrice
         */
        private static int width;
        /**
         * Altezza della matrice
         */
        private static int height;
        /**
         * Tipo di distribuzione da effettuare
         */
        private static Distribution distributionType;
        /**
         * Quantità di cibo prodotta, nei modi definiti in {distributionType}
         */
        private static int foodQuantity;
        /**
         * flag, true se l'oggetto è già stato istanziato
         */
        private static boolean istanziato = false;

        public Builder(int w, int h, Distribution dt, int fq) {
            Builder.width = w;
            Builder.height = h;
            Builder.distributionType = dt;
            Builder.foodQuantity = fq;
        }

        /**
         * crea il riferimento al cibo e lo ritorna, può essere chiamato una sola volta
         * (utilizzato nel main form per creare il cibo)
         *
         * @return riferimento al cibo
         * @throws NullPointerException
         */
        synchronized public Food build() throws NullPointerException {
            if (!Builder.istanziato) {
                Builder.istanziato = true;
                return new Food(width, height, distributionType, foodQuantity);
            }
            return null;
        }
    }
}
