package children;

import java.util.LinkedList;

import main.Batterio;
import main.Food;

/**
 *Batterio cerca il cibo
 */
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
    private final int searchRadius = 15; // dove cerca il cibo
    // tempo della ricerca
    private int moveCounter = 0;
    //per salvare le posizioni del cibo
    private LinkedList<int[]> foodMemory = new LinkedList<>();

    public Dumb() {
        super(); // Chiama il costruttore della classe base
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
        if(!this.targetingFood) {
            searchForFood();
        }
        
        // Se abbiamo trovato del cibo, muoviamoci verso di esso
        if (this.targetingFood) {
            moveTowardsFood();
            
            // Se abbiamo raggiunto il cibo, smettiamo di inseguirlo
            if (this.x == this.targetX && this.y == this.targetY) {
                this.targetingFood = false;
            }        } else {
            //se non ce nessun cibo nelle vicinanze usiamo un movimento a spirale
            spiralMovement();
        }
    }
    
    private void searchForFood() {
        //usiamo questo metodo del cambio di raggio in modo da tener conto del risco di morte del batterio rendendolo piu aggrasivo se sta morendo

        int dynamicRadius;
        if (this.getHealth() < 300) {
            dynamicRadius = 25; // raggio maggiore se la salute è bassa
        } else {
            dynamicRadius = 15; // raggio standard
        }

        int minDistance = Integer.MAX_VALUE;
        boolean found = false;
        
        // Cerca cibo partendo dalle posizioni più vicine (più efficiente)
        for (int radius = 1; radius <= dynamicRadius; radius++) {
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
        //evita di sprecare tempo a controllare il cibo che è già stato controllato
        foodMemory.add(new int[]{checkX, checkY});
        if (!foodMemory.isEmpty()) {
            for (int[] pos : foodMemory) {
                if (pos[0] == checkX && pos[1] == checkY) {
                    return; // Cibo già controllato
                }
            }    
        }
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
    
    private void spiralMovement() {
        
        // Cambia direzione casualmente ogni tanto (movimento a spirale)
        if (Math.random() < 0.07) {
            direction = (direction + 1) % 4;
        }

        // Se siamo troppo vicini ai bordi cambia direzione in modo più aggressivo
        int margin = 3; 
        if (x <= margin || x >= Food.getWidth() - 1 - margin ||
            y <= margin || y >= Food.getHeight() - 1 - margin) {
            
            // simula una curva
            direction = (direction + 1 + (int)(Math.random() * 2)) % 4;
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

        // Rimbalzo ai bordi (rimane come sicurezza)
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
        int steps;
        if (this.getHealth() < 150) {
            steps = 2; // se la salute è bassa, muoviti 2 volte
        } else {
            steps = 1; // altrimenti una sola volta
        }

         while (steps > 0 && (x != targetX || y != targetY)) {
            steps--;

            if (dx != 0) {
                if (dx > 0) {
                    x += 1;
                } else {
                    x -= 1;
                }
            }

            if (dy != 0) {
                if (dy > 0) {
                    y += 1;
                } else {
                    y -= 1;
                }
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