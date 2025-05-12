package main;

/**
 * Classe astratta genitore della gerarchia dei batteri.
 * Ogni tipo diverso di batterio eredita da questa classe
 * @author Alessandro Bugatti
 */
abstract public class Batterio implements Cloneable {
    /**
     * L'età iniziale del batterio è composta da INITIAL_LIFE sommato 
     * ad un numero casuale compreso tra zero e RANDOM_INITIAL_LIFE
     */
    private final static int RANDOM_INITIAL_LIFE = 1500;
    private final static int INITIAL_LIFE = 500;
    /**
     * La salute iniziale del batterio è composta da INITIAL_HEALTH sommato 
     * ad un numero casuale compreso tra zero e RANDOM_INITIAL_HEALTH
     */
    private final static int RANDOM_INITIAL_HEALTH = 600;
    private final static int INITIAL_HEALTH = 200;
    /**
     * Salute minima necessaria per la riproduzione
     */
    private final static int GOOD_HEALTH = 200;
    /**
     * Incremento della salute ogni qualvolta mangia il cibo
     */
    private final static int INCREASE_HEALTH = 100;
    /**
     * I cicli riproduttivi iniziali del batterio sono composti da
     * INITIAL_REPRODUCTIVE_LOOPS sommato ad un numero casuale compreso tra zero
     * e RANDOM_REPRODUCTIVE_LOOPS
     */
    private final static int INITIAL_REPRODUCTIVE_LOOPS = 500;
    private final static int RANDOM_REPRODUCTIVE_LOOPS = 100;
    /**
     * Cicli riproduttivi del batterio dalla prima clonazione in poi
     */
    private final static int REPRODUCTIVE_LOOPS = 200;

    /**
     * Contiene la vita del batterio, arrivata a zero il batterio muore
     */
    private int age;
    /**
     * Contiene la salute del batterio, arrivata a zero il batterio muore
     */
    private int health;
    /**
     * Contatore che indica qunti cicli mancano prima della sua clonazione
     */
    private int loopsForCloning;
    /**
    * Posizione x del batterio sul terreno
    */
    protected int x;
    /**
    * Posizione y del batterio sul terreno
    */
    protected int y;
    /**
     * riferimento al cibo
     */
    private static Food food = null;
    
    public Batterio() {
        this.x = (int) (Math.random() * Food.getWidth());
        this.y = (int) (Math.random() * Food.getHeight());
        this.age = (int) (Math.random() * RANDOM_INITIAL_LIFE) + INITIAL_LIFE;
        this.health = (int) (Math.random() * RANDOM_INITIAL_HEALTH) + INITIAL_HEALTH;
        this.loopsForCloning = INITIAL_REPRODUCTIVE_LOOPS + (int) (Math.random() * RANDOM_REPRODUCTIVE_LOOPS);
    }
    /**
     * Inserisce il riferimento al cibo
     * @param food
     */
    public final static void setFood(Food food) {
        if (Batterio.food == null)
            Batterio.food = food;
    }
  
    /**
     * Sposta il batterio sul terreno di gioco
     * Deve essere ridefinita nelle classi ereditate per dar loro un comportamento diverso
     * @throws java.lang.Exception
     */
    protected abstract void move() throws Exception;
    /**
     * Se nella posizione occupata dal batterio c'è del cibo lo mangia 
     * e incrementa la sua salute di INCREASE_HEALTH
     */
    private void eat() {
        if (food.eatFood(x, y)) {
            health+=INCREASE_HEALTH;
        }
    }
    /**
     * Controlla se un batterio è fecondo
     * @return True se è fecondo, false altrimenti
     */
    public final boolean isReadyForCloning() {
        if (loopsForCloning == 0 && health > GOOD_HEALTH) {
			loopsForCloning = REPRODUCTIVE_LOOPS;
			return true;
		}
        return false;
    }
    /**
     * Controlla se un batterio è morto o perchè troppo vecchio o perchè non ha abbastanza salute
     * @return True se è morto, false altrimenti
     */
    public final boolean isDead() {
        if (health < 1 || age < 1)
            return true;
        else
            return false;
    }
    /**
     * Esegue il batterio
     */
    public final void run() {
        if (this.isDead())
            return;
        int xPrevious = getX(), yPrevious = getY();
        try {
            this.move(); //Calcolo le nuove coordinate del batterio
        } catch (Exception e) {
            System.out.println(e + "(from " + this.getClass().getSimpleName() + " while moving)");
            this.health = 0;
            return;
        }
        this.eat(); //Mangia l'eventuale cibo
        this.age--; //Il batterio invecchia
        //Diminuisce la sua salute in funzione dello spostamento effettuato secondo una metrica Manhattan
        //https://it.wikipedia.org/wiki/Geometria_del_taxi
        int effort = Math.abs(getX() - xPrevious) + Math.abs(getY() - yPrevious);
        health -= effort;
        //Diminuisce il tempo per la riproduzione, solo se si è mosso, altrimenti no
        if (effort != 0) {
            loopsForCloning--;
        }
    }
    /**
     * @return x
     */
    public final int getX() {
        return x;
    }
    /**
     * @return y
     */
    public final int getY() {
        return y;
    }
    /**
     * @return età
     */
    public final int getAge() {
        return this.age;
    }
    /**
     * @return salute
     */
    public final int getHealth() {
        return this.health;
    }
    /**
     * @return quanti cicli mancano alla clonazione
     */
    public final int getLoopsForCloning() {
        return this.loopsForCloning;
    }

    /**
     * Clona il batterio in senso biologico
     *
     * @return Un nuovo batterio
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    protected Batterio clone() throws CloneNotSupportedException {
        Batterio b = (Batterio) super.clone();
        b.age = (int) (Math.random() * RANDOM_INITIAL_LIFE) + INITIAL_LIFE;
        b.health = (int) (Math.random() * RANDOM_INITIAL_HEALTH) + INITIAL_HEALTH;
        b.loopsForCloning = INITIAL_REPRODUCTIVE_LOOPS + (int) (Math.random() * RANDOM_REPRODUCTIVE_LOOPS);
        return b;
    }
}
