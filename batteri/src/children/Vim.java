package children;
import main.Batterio;
import main.Food;

public class Vim extends main.Batterio{
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
    private final int BASE_SEARCH_RADIUS = 60;
    private int searchRadius; // Raggio di ricerca dinamico
    // tempo della ricerca
    private int moveCounter = 0;
    // Soglia per considerare il cibo abbondante
    private static final int ABUNDANT_FOOD_max = 250; //stabilizzare la popolazione
    // Soglia per considerare il cibo scarso e attivare le strategie di preservazione vitaaòe
    private static final int SCARCE_FOOD_min = 200;
    // Traccia quando è stato trovato cibo abbondante
    private boolean hadAbundantFood = false;
    // Traccia il tempo dall'ultima volta che è stato trovato cibo
    private int sinceLastFood = 0;
    // Usato per l'esplorazione quando non si trova cibo
    private boolean isExploring = false;
    // Modalità di sopravvivenza quando la salute è molto bassa
    private boolean survivalMode = false;
    // Modalità di conservazione della massa dei batteri
    private boolean conservationMode = false;

    // Variabile per limitare l'uso del teletrasporto
    private int teleportCooldown = 0;
    private static final int TELEPORT_COOLDOWN_MAX = 50; // Attesa tra teletrasporti
    private static final int TELEPORT_ENERGY_COST = 25; // Costo energetico del teletrasporto
    // Soglia critica di salute per attivare il teletrasporto d'emergenza
    private static final int EMERGENCY_TELEPORT_THRESHOLD = 15;
    // Distanza minima per considerare il teletrasporto al cibo
    private static final int TELEPORT_DISTANCE_THRESHOLD = 80;

    public Vim() {
        super(); // Chiama il costruttore della classe base
        
        // Determina se questo batterio deve apparire al centro o vicino ai bordi (ottimale per trovare cibo)
        boolean spawnNearEdge = Math.random() < 0.5; // 50% di probabilità
          if (spawnNearEdge) {
            // Spawna al centro degli angoli (ottimale per trovare cibo)
            int corner = (int)(Math.random() * 4);
            int variazione = 20; // Variazione casuale attorno al centro dell'angolo
            int spaziatura = 100; // Distanza dal bordo per trovare il centro dell'angolo
            
            switch (corner) {
                case 0: // Angolo in alto a sinistra
                    this.x = spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    this.y = spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    break;
                case 1: // Angolo in alto a destra
                    this.x = Food.getWidth() - spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    this.y = spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    break;
                case 2: // Angolo in basso a sinistra
                    this.x = spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    this.y = Food.getHeight() - spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    break;
                case 3: // Angolo in basso a destra
                    this.x = Food.getWidth() - spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    this.y = Food.getHeight() - spaziatura + (int)(Math.random() * variazione) - variazione/2;
                    break;
            }        } else {
            // Spawna al centro sotto forma di X
            int centerX = Food.getWidth() / 2;
            int centerY = Food.getHeight() / 2;
            int maxDistance = 60; // Lunghezza massima di ciascun braccio della X
            
            // Sceglie un punto casuale su uno dei bracci della X
            double t = Math.random(); // Parametro per determinare la posizione sul braccio (0.0-1.0)
            boolean diagonalePrincipale = Math.random() < 0.5; // Scelta casuale tra le due diagonali
            
            if (diagonalePrincipale) {
                // Diagonale principale (alto-sx a basso-dx)
                int offsetX = (int)(maxDistance * t) - maxDistance/2;
                int offsetY = offsetX; // Stessa direzione per creare la diagonale principale
                this.x = centerX + offsetX;
                this.y = centerY + offsetY;
            } else {
                // Diagonale secondaria (alto-dx a basso-sx)
                int offsetX = (int)(maxDistance * t) - maxDistance/2;
                int offsetY = -offsetX; // Direzione opposta per creare la diagonale secondaria
                this.x = centerX + offsetX;
                this.y = centerY + offsetY;
            }
        }
        
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
        // Decrementa il cooldown del teletrasporto se necessario
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

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
        boolean previousMode = conservationMode;
        conservationMode = Food.getFoodQuantity() < SCARCE_FOOD_min;
        
        // Se entriamo in modalità conservazione, aumenta immediatamente il raggio di ricerca
        if (conservationMode && !previousMode) {
            // Tecniche aggressive: aumenta drasticamente il raggio di ricerca
            searchRadius = BASE_SEARCH_RADIUS * 3;
        }

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

        // Gestione del teletrasporto in situazioni critiche o strategiche
        if (this.getHealth() <= EMERGENCY_TELEPORT_THRESHOLD && teleportCooldown <= 0) {
            // Teletrasporto d'emergenza verso del cibo se possibile
            if (targetingFood) {
                if (teleport(true)) {
                    return; // Se il teletrasporto è riuscito, termina il movimento
                }
            } else {
                // Teletrasporto casuale d'emergenza
                if (teleport(false)) {
                    return; // Se il teletrasporto è riuscito, termina il movimento
                }
            }
        }
        // Teletrasporto strategico quando il cibo è molto lontano
        else if (targetingFood && teleportCooldown <= 0 && this.getHealth() > TELEPORT_ENERGY_COST*2) {
            int distToFood = Math.abs(targetX - x) + Math.abs(targetY - y);
            if (distToFood > TELEPORT_DISTANCE_THRESHOLD) {
                if (teleport(true)) {
                    return; // Se il teletrasporto è riuscito, termina il movimento
                }
            }
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
                
                // Tecniche aggressive con cibo scarso: movimento più rapido
                if (conservationMode) {
                    // Triplo movimento verso il cibo quando è scarso (tecnica aggressiva)
                    moveTowardsFood();
                    moveTowardsFood();
                } 
                // Se in modalità sopravvivenza, muoviti più velocemente verso il cibo
                else if (survivalMode) {
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

                // Tecniche aggressive per esplorare più rapidamente con cibo scarso
                if (conservationMode) {
                    isExploring = true;
                    // Se non c'è cibo nelle vicinanze con cibo scarso, esecuzione di doppio movimento a spirale
                    spiralMovement();
                    spiralMovement();
                }
                // Se non abbiamo trovato cibo per troppo tempo, esplora con movimento più ampio
                else if (sinceLastFood > 30 || survivalMode) {
                    isExploring = true;
                    // Movimento a spirale standard
                    spiralMovement();
                } else {
                    // Movimento a spirale standard
                    spiralMovement();
                }
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

    
    private void searchForFood(int radius) {
        int minDistance = Integer.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;

        // Incremento di 4 pixel per ogni iterazione del raggio di ricerca
        for (int currentRadius = 1; currentRadius <= radius; currentRadius += 4) {
            // Incremento di 4 pixel per ogni punto lungo il perimetro del raggio corrente
            for (int i = -currentRadius; i <= currentRadius; i += 4) {
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
        // Con cibo scarso, tecniche aggressive con raggio di ricerca molto più ampio
        else if (foodQuantity < SCARCE_FOOD_min) {
            // Aumento più rapido e aggressivo del raggio di ricerca
            if (searchRadius < BASE_SEARCH_RADIUS * 3) {
                searchRadius += 2; // Incremento più rapido
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
        
        // Verifica se il cibo è ancora presente nella posizione target
        // Se non è più presente, smetti di inseguirlo
        if (x == targetX && y == targetY && !Food.isFood(x, y)) {
            targetingFood = false;
        }
    }

    /**
     * Riduce la salute del batterio di un valore specificato
     * Utilizza riflessione per accedere al campo privato health nella classe parent
     * @param amount la quantità di salute da diminuire
     */
    private void decreaseHealth(int amount) {
        // Accediamo direttamente alla proprietà health nella classe genitore
        try {
            // Otteniamo il campo 'health' dalla superclasse
            java.lang.reflect.Field healthField = main.Batterio.class.getDeclaredField("health");
            // Rendiamo accessibile il campo privato
            healthField.setAccessible(true);
            // Otteniamo il valore corrente
            int currentHealth = (int) healthField.get(this);
            // Aggiorniamo il valore
            healthField.set(this, currentHealth - amount);
            // Ripristiniamo l'accesso
            healthField.setAccessible(false);
        } catch (Exception e) {
            // Se non riusciamo ad accedere direttamente, non facciamo nulla
            // La salute verrà comunque decrementata dal movimento normale
        }
    }

    /**
     * Permette al batterio di teletrasportarsi in un'altra posizione dell'arena
     * Ha un costo energetico e un tempo di ricarica
     * @param targetedTeleport se true, tenta di teletrasportarsi verso del cibo rilevato
     * @return true se il teletrasporto è avvenuto con successo, false altrimenti
     */
    public boolean teleport(boolean targetedTeleport) {
        // Verifica se il batterio può teletrasportarsi (cooldown e salute sufficienti)
        if (teleportCooldown > 0 || this.getHealth() < TELEPORT_ENERGY_COST + 10) {
            return false;
        }
        
        // Teletrasporto verso il cibo se richiesto e se è stato rilevato un obiettivo
        if (targetedTeleport && targetingFood) {
            // Calcola una posizione vicina al cibo (non esattamente sopra per essere realistico)
            int distanceFromFood = 5; // Distanza dal cibo dopo il teletrasporto
            int dx = targetX - x;
            int dy = targetY - y;
            
            // Calcola la direzione verso il cibo
            double angle = Math.atan2(dy, dx);
            
            // Calcola la nuova posizione vicino al cibo
            int newX = targetX - (int)(Math.cos(angle) * distanceFromFood);
            int newY = targetY - (int)(Math.sin(angle) * distanceFromFood);
            
            // Assicurati che la posizione sia all'interno dell'arena
            x = Math.max(2, Math.min(Food.getWidth() - 3, newX));
            y = Math.max(2, Math.min(Food.getHeight() - 3, newY));
        } 
        // Teletrasporto casuale se non è specificato un obiettivo o non ne è stato trovato uno
        else {
            // Per il teletrasporto casuale, evita i bordi dell'arena
            int margin = 20;
            x = margin + (int)(Math.random() * (Food.getWidth() - 2 * margin));
            y = margin + (int)(Math.random() * (Food.getHeight() - 2 * margin));
        }
        
        // Applica il costo energetico e il cooldown
        this.decreaseHealth(TELEPORT_ENERGY_COST);
        teleportCooldown = TELEPORT_COOLDOWN_MAX;
        
        return true;
    }

    @Override
    public Batterio clone() throws CloneNotSupportedException {
        Vim clone = (Vim) super.clone();

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
        // Con conservationMode attiva (cibo scarso), strategie di clonazione aggressive
        else if (conservationMode) {
            // Quando il cibo è scarso, i cloni si muovono in direzioni diametralmente opposte per massimizzare l'area di ricerca
            int direction = moveCounter % 4;
            switch (direction) {
                case 0:
                    variationX = 5; // Movimento più ampio a destra
                    variationY = 0;
                    break;
                case 1:
                    variationX = 0;
                    variationY = 5; // Movimento più ampio in basso
                    break;
                case 2:
                    variationX = -5; // Movimento più ampio a sinistra
                    variationY = 0;
                    break;
                case 3:
                    variationX = 0;
                    variationY = -5; // Movimento più ampio in alto
                    break;
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