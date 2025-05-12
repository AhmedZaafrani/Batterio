package children;

import main.Batterio;
import main.Food;

/**
 *Batterio cerca il cibo
 */
public class Dumb extends Batterio {
    // Costanti delle direzioni
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    
    // Stato corrente e parametri di comportamento
    private int direction;
    private boolean targetingFood;
    private int targetX;
    private int targetY;
    private final int searchRadius = 15; // Raggio di ricerca maggiore rispetto alla classe Smart
    
    // Parametro per ottimizzare la ricerca del cibo
    private int moveCounter = 0;
    
    /**
     * Costruttore: posiziona il batterio in una posizione strategica e imposta direzione casuale
     */
    public Dumb() {
        super(); // Chiama il costruttore della classe base
        // Posiziona il batterio in una zona con alta probabilità di trovare cibo
        // (nel centro con piccola variazione casuale)
        this.x = Food.getWidth() / 2 + (int)(Math.random() * 10) - 5;
        this.y = Food.getHeight() / 2 + (int)(Math.random() * 10) - 5;
          // Inizializza con direzione casuale
        this.direction = (int)(Math.random() * 4);
        this.targetingFood = false;
    }
    
    @Override
    protected void move() {
        // Incrementa il contatore di movimenti
        this.moveCounter++;
        
        // Cerca cibo ogni 3 movimenti per ottimizzare le prestazioni
        if (this.moveCounter % 3 == 0) {
            if (!this.targetingFood) {
                searchForFood();
            }
        }
        
        // Se abbiamo trovato del cibo, muoviamoci verso di esso
        if (this.targetingFood) {
            moveTowardsFood();
            
            // Se abbiamo raggiunto il cibo, smettiamo di inseguirlo
            if (this.x == this.targetX && this.y == this.targetY) {
                this.targetingFood = false;
            }        } else {
            // Nessun cibo trovato nelle vicinanze, usa una strategia di esplorazione a spirale
            // che è più efficiente rispetto al movimento casuale
            spiralMovement();
        }
    }
    
    /**
     * Cerca cibo all'interno del raggio di ricerca con strategia ottimizzata
     */
    private void searchForFood() {
        int minDistance = Integer.MAX_VALUE;
        boolean found = false;
        
        // Cerca cibo partendo dalle posizioni più vicine (più efficiente)
        for (int radius = 1; radius <= this.searchRadius; radius++) {
            // Controlla solo il perimetro di ogni raggio per ottimizzare
            for (int i = -radius; i <= radius; i++) {
                // Verifica i bordi superiore e inferiore del perimetro
                checkFoodPosition(this.x + i, this.y - radius, minDistance);
                checkFoodPosition(this.x + i, this.y + radius, minDistance);
                
                // Verifica i bordi laterali esclusi gli angoli (già controllati sopra)
                if (i > -radius && i < radius) {
                    checkFoodPosition(this.x - radius, this.y + i, minDistance);
                    checkFoodPosition(this.x + radius, this.y + i, minDistance);
                }
            }
            
            // Se abbiamo trovato cibo in questo raggio, interrompi per efficienza
            if (this.targetingFood) break;
        }
    }
    
    /**
     * Controlla se c'è cibo in una posizione specifica e aggiorna il target se necessario
     */
    private void checkFoodPosition(int checkX, int checkY, int minDistance) {
        // Verifica che la posizione sia dentro i limiti dell'arena
        if (checkX >= 0 && checkX < Food.getWidth() && 
            checkY >= 0 && checkY < Food.getHeight() && 
            Food.isFood(checkX, checkY)) {
            
            // Calcola distanza Manhattan
            int distance = Math.abs(checkX - x) + Math.abs(checkY - y);
            
            if (distance < minDistance) {
                minDistance = distance;
                targetX = checkX;
                targetY = checkY;
                targetingFood = true;
            }
        }
    }
    
    /**
     * Movimento a spirale per esplorare l'area in modo efficiente
     */
    private void spiralMovement() {
        // Periodicamente cambia direzione per creare un movimento a spirale
        if (Math.random() < 0.07) {
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
        
        // Rimbalza se raggiungiamo i bordi e cambia direzione
        if (x <= 0) {
            x = 0;
            direction = RIGHT;
        } else if (x >= Food.getWidth() - 1) {
            x = Food.getWidth() - 1;
            direction = LEFT;
        }
        
        if (y <= 0) {
            y = 0;
            direction = DOWN;
        } else if (y >= Food.getHeight() - 1) {
            y = Food.getHeight() - 1;
            direction = UP;
        }
    }
    
    /**
     * Muove il batterio verso il cibo bersaglio usando un algoritmo ottimizzato
     */
    private void moveTowardsFood() {
        // Calcola le distanze
        int dx = targetX - x;
        int dy = targetY - y;
        
        // Determina se muoversi diagonalmente quando possibile (ottimizzazione del percorso)
        if (dx != 0 && dy != 0) {
            // Muoviti in diagonale
            if (x > 0) {
                x += 1;
            } else {
                x -= 1;
            }
            if (y > 0) {
                y += 1;
            } else {
                y -= 1;
                
            }
            
        } else if (dx != 0) {
            // Muoviti orizzontalmente
            if (dx > 0) {
                x++;
            } else {
                x--;
                
            }
        } else if (dy != 0) {
            // Muoviti verticalmente
            if (dy > 0) {
                y++;
            } else {
                y--;
                
            }
        }
    }
    
    @Override
    public Batterio clone() throws CloneNotSupportedException {
        Dumb clone = (Dumb)super.clone();
        
        // Strategia di clonazione: posiziona il clone in una direzione diversa
        // per massimizzare la copertura dell'arena
        clone.direction = (direction + 2) % 4;  // Direzione opposta
        clone.targetingFood = false;
        
        // Piccola variazione nella posizione del clone per evitare sovraffollamento
        if (Math.random() < 0.5) {
            if (Math.random() < 0.5) {
                clone.x += (Math.random() < 0.5) ? 1 : -1;
                if (Math.random() < 0.5) {
                    clone.x += 1;
                } else {
                    clone.x += -1;
                }
            } else {
                if (Math.random() < 0.5) {
                    clone.y += 1;
                } else {
                    clone.y += -1;
                    
                }
            }
            if (Math.random() < 0.5) {
                clone.x += 1;
            } else {
                clone.x += -1;
                
            }
        } else {
            if (Math.random() < 0.5) {
                clone.y += 1;
            } else {
                clone.y += -1;
                
            }
        }
        
        // Assicurati che il clone rimanga nei limiti
        clone.x = Math.max(0, Math.min(Food.getWidth() - 1, clone.x));
        clone.y = Math.max(0, Math.min(Food.getHeight() - 1, clone.y));
        
        return clone;
    }
}
