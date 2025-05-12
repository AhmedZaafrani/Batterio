package children;
import main.Batterio;
import main.Food;

public class Dumb  extends Batterio {
    // Costanti delle direzioni
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    
    // Stato corrente
    private int direction;
    private boolean targetingFood;
    //possibile posizione del cibo
    private int targetX; 
    private int targetY;      
    // Raggio base e dinamico per la ricerca del cibo
    private final int BASE_SEARCH_RADIUS = 45;
    private int searchRadius; // Raggio di ricerca dinamico
    // tempo della ricerca
    private int moveCounter = 0;
    // Soglia per considerare il cibo abbondante
    private static final int ABUNDANT_FOOD_max = 300; // Ulteriormente ridotto per stabilizzare la popolazione
    // Soglia per considerare il cibo scarso e attivare strategie di conservazione
    private static final int SCARCE_FOOD_min = 100;
    // Traccia quando è stato trovato cibo abbondante
    private boolean hadAbundantFood = false;
    // Traccia il tempo dall'ultima volta che è stato trovato cibo
    private int sinceLastFood = 0;
    // Usato per l'esplorazione quando non si trova cibo
    private boolean isExploring = false;
    // Modalità di sopravvivenza quando la salute è molto bassa
    private boolean survivalMode = false;
    // Modalità di conservazione della massa dei batteri
    private boolean conservationMode = false;    public Dumb() {
        super(); // Chiama il costruttore della classe base
        this.x = Food.getWidth() / 2 + (int)(Math.random() * 10) - 5;
        this.y = Food.getHeight() / 2 + (int)(Math.random() * 10) - 5;
        // Inizializza con direzione casuale
        this.direction = (int)(Math.random() * 4);
        this.targetingFood = false;
        
        // Inizializza il raggio di ricerca con un valore alto per i batteri appena nati
        this.searchRadius = BASE_SEARCH_RADIUS * 2; // Raggio doppio all'inizio
    }
    
    // Controlla se la quantità di cibo è abbondante
    private boolean isFoodAbundant() {
        return Food.getFoodQuantity() > ABUNDANT_FOOD_max;
    }    

    @Override
    protected void move() {
        // Incrementa il contatore di movimenti
        this.moveCounter++;
        
        // Attiva la modalità di sopravvivenza se la salute è molto bassa
        if (this.getHealth() < 30) {
            survivalMode = true;
        } else if (this.getHealth() > 80) {
            survivalMode = false;
        }
          // Verifica se attivare la modalità di conservazione 
        // basata sulla quantità di cibo disponibile
        conservationMode = Food.getFoodQuantity() < SCARCE_FOOD_min;
        
        // Aggiorna il raggio di ricerca in base alla quantità di cibo nell'arena
        updateSearchRadius();
        
        // Cerca cibo ogni 3 movimenti o immediatamente in modalità sopravvivenza
        if (moveCounter % 3 == 0 || !targetingFood || survivalMode) {
            // Se non abbiamo trovato cibo per un po', aumenta il raggio di ricerca
            int effectiveRadius = searchRadius;
            if (sinceLastFood > 20 || survivalMode) {
                effectiveRadius = searchRadius * 2;
            }
            searchForFood(effectiveRadius);
        }
        
        // Modalità cibo abbondante: comportamento potenziato
        if (isFoodAbundant()) {
            // Movimento accelerato verso il cibo
            if (this.targetingFood) {
                moveTowardsFood();
                moveTowardsFood(); // Doppio movimento
                sinceLastFood = 0; // Reset contatore
            } else {
                // Movimento esplorativo più ampio
                isExploring = true;
                spiralMovement();
            }
            // Impostiamo il flag per migliorare la clonazione
            hadAbundantFood = true;
        } 
        else { // Comportamento normale
            // Risparmio energetico solo se siamo a salute media e non stiamo inseguendo cibo
            // NON risparmiare energia se siamo in modalità sopravvivenza
            if (this.getHealth() < 60 && this.getHealth() > 30 && !targetingFood && !survivalMode) {
                sinceLastFood++;
                return; // Risparmia energia
            }
            
            // Se abbiamo trovato del cibo, muoviamoci verso di esso
            if (this.targetingFood) {
                moveTowardsFood();
                // Se in modalità sopravvivenza, muoviti più velocemente verso il cibo
                if (survivalMode) {
                    moveTowardsFood();
                }
                sinceLastFood = 0; // Reset contatore
                
                // Se abbiamo raggiunto il cibo, smettiamo di inseguirlo
                if (this.x == this.targetX && this.y == this.targetY) {
                    this.targetingFood = false;
                }
            } else {
                // Incrementa il tempo da quando non troviamo cibo
                sinceLastFood++;
                
                // Se non abbiamo trovato cibo per troppo tempo, esplora con movimento più ampio
                if (sinceLastFood > 30 || survivalMode) {
                    isExploring = true;
                }
                
                // Se non c'è nessun cibo nelle vicinanze usiamo un movimento a spirale
                spiralMovement();
            }
        }
        
        // Mantenimento preventivo lontano dai bordi
        avoidBorders();
    }
    
    // Nuovo metodo per evitare i bordi
    private void avoidBorders() {
        if (x <= 2) {
            x = 3;
            direction = RIGHT;
        }
        else if (x >= Food.getWidth() - 3) {
            x = Food.getWidth() - 4;
            direction = LEFT;
        }
        
        if (y <= 2) {
            y = 3;
            direction = DOWN;
        }
        else if (y >= Food.getHeight() - 3) {
            y = Food.getHeight() - 4;
            direction = UP;
        }
    }    

    /* Non più utilizzato direttamente ma mantenuto come riferimento
    private void searchForFood() {
        // Usa il raggio base
        searchForFood(searchRadius);
    }
    */
    
    private void searchForFood(int radius) {
        int minDistance = Integer.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;

        for (int currentRadius = 1; currentRadius <= radius; currentRadius++) {
            for (int i = -currentRadius; i <= currentRadius; i++) {
                int[] positions = {
                    this.x + i, this.y - currentRadius,
                    this.x + i, this.y + currentRadius,
                    this.x - currentRadius, this.y + i,
                    this.x + currentRadius, this.y + i
                };

                for (int j = 0; j < positions.length; j += 2) {
                    int checkX = positions[j];
                    int checkY = positions[j + 1];

                    if (checkX >= 0 && checkX < Food.getWidth() &&
                        checkY >= 0 && checkY < Food.getHeight() &&
                        Food.isFood(checkX, checkY)) {

                        int distance = Math.abs(checkX - x) + Math.abs(checkY - y);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestX = checkX;
                            bestY = checkY;
                        }
                    }
                }
            }
        }

        if (minDistance < Integer.MAX_VALUE) {
            this.targetingFood = true;
            this.targetX = bestX;
            this.targetY = bestY;
        }
    }
    
    // Metodo per aggiornare il raggio di ricerca in base alla quantità di cibo
    private void updateSearchRadius() {
        int foodQuantity = Food.getFoodQuantity();
        
        // Più cibo c'è, minore è il raggio di ricerca
        if (foodQuantity > ABUNDANT_FOOD_max) {
            // Con cibo abbondante, gradualmente ritorna al raggio base
            if (searchRadius > BASE_SEARCH_RADIUS) {
                searchRadius--;
            }
        } 
        // Con cibo scarso, mantiene un raggio alto o lo aumenta gradualmente
        else if (foodQuantity < SCARCE_FOOD_min) {
            // Limite superiore per evitare raggi eccessivi
            if (searchRadius < BASE_SEARCH_RADIUS * 2) {
                searchRadius++;
            }
        }
        // Con cibo moderato, mantiene un raggio intermedio
        else {
            // Se il raggio è troppo basso, lo aumenta
            if (searchRadius < BASE_SEARCH_RADIUS * 1.5) {
                searchRadius++;
            }
            // Se è troppo alto, lo diminuisce
            else if (searchRadius > BASE_SEARCH_RADIUS * 1.5) {
                searchRadius--;
            }
        }
        
        // Il raggio non può mai scendere sotto il valore minimo
        if (searchRadius < BASE_SEARCH_RADIUS) {
            searchRadius = BASE_SEARCH_RADIUS;
        }
    }

    private void spiralMovement() {
        int arenaWidth = Food.getWidth();
        int arenaHeight = Food.getHeight();

        int margin = 3; // distanza minima dai bordi

        // Cambio direzione sensato se vicino ai bordi
        boolean nearLeft = x <= margin;
        boolean nearRight = x >= arenaWidth - 1 - margin;
        boolean nearTop = y <= margin;
        boolean nearBottom = y >= arenaHeight - 1 - margin;

        // Se vicino a un bordo, cambia direzione in senso orario
        if (nearLeft && direction == LEFT) {
            direction = UP;
        } 
        else if (nearTop && direction == UP) {
            direction = RIGHT;
        } 
        else if (nearRight && direction == RIGHT) {
            direction = DOWN;
        } 
        else if (nearBottom && direction == DOWN) {
            direction = LEFT;
        }

        // Frequenza di cambio direzione basata su modalità esplorazione
        int turnFrequency = isExploring ? 5 : 10;
        
        // Cambia direzione per simulare "spirale"
        if (moveCounter % turnFrequency == 0) {
            direction = (direction + 1) % 4;
        }

        // Esegui il movimento nella direzione corrente
        switch (direction) {
            case UP:
                y--;
                break;
            case RIGHT:
                x++;
                break;
            case DOWN:
                y++;
                break;
            case LEFT:
                x--;
                break;
        }

        // Rimbalzo ai bordi come ultima sicurezza
        if (x <= 0) {
            x = 0;
            direction = RIGHT;
        } 
        else if (x >= arenaWidth - 1) {
            x = arenaWidth - 1;
            direction = LEFT;
        }

        if (y <= 0) {
            y = 0;
            direction = DOWN;
        } 
        else if (y >= arenaHeight - 1) {
            y = arenaHeight - 1;
            direction = UP;
        }
    }

    private void moveTowardsFood() {
        // Calcola le distanze
        int dx = targetX - x;
        int dy = targetY - y;
        
        // Movimento ottimizzato verso il cibo
        if (dx != 0) {
            if (dx > 0) {
                x++;
            } 
            else {
                x--;
            }
        }
        if (dy != 0) {
            if (dy > 0) {
                y++;
            } 
            else {
                y--;
            }
        }
        
        // Mantenimento entro i bordi
        x = Math.max(0, Math.min(Food.getWidth() - 1, x));
        y = Math.max(0, Math.min(Food.getHeight() - 1, y));
    }
    
    @Override
    public Batterio clone() throws CloneNotSupportedException {
        Dumb clone = (Dumb) super.clone();

        // Clonazione potenziata in caso di cibo abbondante o se abbiamo individuato cibo
        // Il valore di base verrà impostato automaticamente dal clone() nella classe padre
        
        // Strategia di posizionamento intelligente in base alle circostanze
        int variationX = 0;
        int variationY = 0;
        
        // Se abbiamo un target di cibo, posiziona il clone verso quella direzione
        if (this.targetingFood) {
            // Calcola direzione verso il cibo
            int dx = targetX - x;
            int dy = targetY - y;
              // Imposta una variazione che punti verso il cibo
            if (dx != 0) {
                if (dx > 0) {
                    variationX = 1;
                } else {
                    variationX = -1;
                }
            } else {
                variationX = 0;
            }
            
            if (dy != 0) {
                if (dy > 0) {
                    variationY = 1;
                } else {
                    variationY = -1;
                }
            } else {
                variationY = 0;
            }
        }        
        // Altrimenti, se il cibo è abbondante, posiziona strategicamente intorno al genitore
        else if (hadAbundantFood || isFoodAbundant()) {
            // Usa una dispersione più ampia in caso di cibo abbondante
            if (Math.random() < 0.5) {
                variationX = 2;
            } else {
                variationX = -2;
            }
            
            if (Math.random() < 0.5) {
                variationY = 2;
            } else {
                variationY = -2;
            }
        }
        // Strategia standard per condizioni normali
        else {
            // Usa una strategia a rotazione basata sul contatore per massimizzare l'esplorazione
            switch (moveCounter % 4) {
                case 0:
                    variationX = 1;
                    variationY = 0;
                    break;
                case 1:
                    variationX = 0;
                    variationY = 1;
                    break;
                case 2:
                    variationX = -1;
                    variationY = 0;
                    break;
                case 3:
                    variationX = 0;
                    variationY = -1;
                    break;
            }
        }
        
        clone.x += variationX;
        clone.y += variationY;

        // Assicurati che il clone rimanga nei limiti dell'arena        
        clone.x = Math.max(2, Math.min(Food.getWidth() - 3, clone.x));
        clone.y = Math.max(2, Math.min(Food.getHeight() - 3, clone.y));
          // Trasferisci le informazioni strategiche al clone
        clone.hadAbundantFood = this.hadAbundantFood;
        clone.sinceLastFood = 0; // Il clone inizia fresco
        clone.conservationMode = this.conservationMode; // Condividi lo stato di conservazione
        
        // I cloni iniziano con un raggio di ricerca leggermente più alto rispetto al genitore
        clone.searchRadius = Math.min(this.searchRadius + 10, BASE_SEARCH_RADIUS * 2);

        return clone;
    }
}