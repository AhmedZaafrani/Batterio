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
    private final int BASE_SEARCH_RADIUS = 100;
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

    // Flag per tracciare se il batterio è stato posto su un pattern X
    private boolean isOnXPattern = false;
    
    // Variabile per limitare l'uso del teletrasporto
    private int teleportCooldown = 0;
    private static final int TELEPORT_COOLDOWN_MAX = 30; // Attesa tra teletrasporti
    private static final int TELEPORT_ENERGY_COST = 25; // Costo energetico del teletrasporto
    // Soglia critica di salute per attivare il teletrasporto d'emergenza
    private static final int EMERGENCY_TELEPORT_THRESHOLD = 15;
    // Distanza minima per considerare il teletrasporto al cibo
    private static final int TELEPORT_DISTANCE_THRESHOLD = 90;    
    public Vim() {
        super(); // Chiama il costruttore della classe base
        
        // Determina se questo batterio deve apparire al centro o vicino ai bordi (ottimale per trovare cibo)
        boolean spawnNearEdge = Math.random() < 0.5; // 50% di probabilità
        if (spawnNearEdge) {
            // Spawna negli angoli con pattern X
            arrangeInXPatternFromCorner();
        } else {
            // Spawna al centro sotto forma di X
            arrangeInXPatternFromCenter();
        }
        
        // Inizializza con direzione casuale
        this.direction = (int)(Math.random() * 4);
        this.targetingFood = false;

        // Inizializza il raggio di ricerca con un valore alto per i batteri appena nati
        this.searchRadius = BASE_SEARCH_RADIUS * 2; // Raggio doppio all'inizio
    }
    
    /**
     * Posiziona il batterio in un pattern X centrato nell'arena
     */
    private void arrangeInXPatternFromCenter() {
        int centerX = Food.getWidth() / 2;
        int centerY = Food.getHeight() / 2;
        int maxDistance = 250; // Lunghezza massima di ciascun braccio della X
        
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
        
        isOnXPattern = true;
    }
    
    /**
     * Posiziona il batterio in un pattern X centrato in uno degli angoli dell'arena
     */
    private void arrangeInXPatternFromCorner() {
        // Seleziona un angolo casuale
        int corner = (int)(Math.random() * 4);
        // Distanza dal bordo per trovare il centro dell'angolo
        int spaziatura = 140;
        // Dimensione del pattern X negli angoli
        int maxDistance = 200;
        
        int centerX, centerY;
        
        // Determina le coordinate del centro dell'angolo
        switch (corner) {
            case 0: // Angolo in alto a sinistra
                centerX = spaziatura;
                centerY = spaziatura;
                break;
            case 1: // Angolo in alto a destra
                centerX = Food.getWidth() - spaziatura;
                centerY = spaziatura;
                break;
            case 2: // Angolo in basso a sinistra
                centerX = spaziatura;
                centerY = Food.getHeight() - spaziatura;
                break;
            case 3: // Angolo in basso a destra
                centerX = Food.getWidth() - spaziatura;
                centerY = Food.getHeight() - spaziatura;
                break;
            default:
                centerX = spaziatura;
                centerY = spaziatura;
        }
        
        // Ora crea il pattern X centrato nell'angolo selezionato
        double t = Math.random(); // Posizione casuale sul braccio della X
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
        
        // Assicurati che la posizione sia all'interno dell'arena
        this.x = Math.max(2, Math.min(Food.getWidth() - 3, this.x));
        this.y = Math.max(2, Math.min(Food.getHeight() - 3, this.y));
        
        isOnXPattern = true;
    }

    // Controlla se la quantità di cibo è abbondante
    private boolean isFoodAbundant() {
        return Food.getFoodQuantity() > ABUNDANT_FOOD_max;
    }    @Override
    protected void move() {
        // Decrementa il cooldown del teletrasporto se necessario
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        // Incrementa il contatore di movimenti
        this.moveCounter++;

        // Controlla prima se siamo già su del cibo
        if (Food.isFood(x, y)) {
            // Se siamo già su del cibo, controlla se c'è altro cibo nelle vicinanze
            scanAndEatAdjacentFood();
            return; // Abbiamo consumato cibo, termina qui il movimento
        }

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
          // Se non stiamo già inseguendo cibo, o siamo in modalità sopravvivenza, cerca cibo immediatamente
        if (!targetingFood || survivalMode) {
            int effectiveRadius = searchRadius;
            // Raggio maggiore se non abbiamo trovato cibo da tempo o in modalità sopravvivenza
            if (sinceLastFood > 15 || survivalMode) {
                effectiveRadius = searchRadius * 2;
            }
            searchForFood(effectiveRadius);
            
            // Se stiamo cercando cibo attivamente ma non lo troviamo, riorganizziamo in pattern X
            if (!targetingFood && !isOnXPattern) {
                if (Math.random() < 0.3) { // 30% di probabilità di riorganizzarsi ad X
                    // Scegli casualmente tra pattern X centrale o negli angoli
                    if (Math.random() < 0.5) {
                        arrangeInXPatternFromCenter();
                    } else {
                        arrangeInXPatternFromCorner();
                    }
                }
            }
        }
        // Altrimenti, cerca cibo periodicamente per trovare fonti migliori
        else if (moveCounter % 5 == 0) {
            // Cerca cibo ogni 5 movimenti in modalità normale
            searchForFood(BASE_SEARCH_RADIUS);
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
                // Quando troviamo cibo, non siamo più sul pattern X
                isOnXPattern = false;
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
    }      private void searchForFood(int radius) {
        // Prima controlla se c'è cibo nella posizione attuale
        if (Food.isFood(x, y)) {
            this.targetingFood = true;
            this.targetX = x;
            this.targetY = y;
            this.isOnXPattern = false;
            return;
        }
        
        // Poi controlla se c'è cibo nelle immediate vicinanze
        int smallRadius = 5; // Controllo ravvicinato prima di fare una ricerca più ampia
        boolean foundNearbyFood = searchInArea(smallRadius);
        if (foundNearbyFood) {
            return; // Se abbiamo trovato cibo nelle immediate vicinanze, non serve cercare oltre
        }

        int minDistance = Integer.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;
        
        // Controlla se ci sono gruppi di cibo invece che singole unità
        // Utilizziamo una mappa per tenere traccia della densità di cibo nelle diverse aree
        int[][] foodDensity = new int[5][5]; // Dividiamo l'arena in una griglia 5x5
        int gridWidth = Food.getWidth() / 5;
        int gridHeight = Food.getHeight() / 5;
        
        // Ottimizzazione: ricerca più rapida con incremento dinamico
        // Utilizza incremento minore per raggi più piccoli per maggiore precisione
        for (int currentRadius = 1; currentRadius <= radius; currentRadius += Math.max(2, currentRadius/10)) {
            // Ricerca più intensiva sui raggi inferiori per trovare cibo vicino più rapidamente
            int step = Math.max(2, currentRadius/8); // Incremento dinamico
            
            for (int i = -currentRadius; i <= currentRadius; i += step) {
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
                            checkY >= 0 && checkY < Food.getHeight()) {
                        
                        if (Food.isFood(checkX, checkY)) {
                            // Aggiorniamo la mappa della densità
                            int gridX = Math.min(checkX / gridWidth, 4);
                            int gridY = Math.min(checkY / gridHeight, 4);
                            foodDensity[gridX][gridY]++;
                            
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
            
            // Ottimizzazione: se troviamo cibo vicino, interrompi la ricerca
            // per concentrarsi sul cibo più facilmente raggiungibile
            if (minDistance < currentRadius * 2 && bestX != -1) {
                break;
            }
        }
        
        // Controlla se ci sono aree con alta densità di cibo
        int maxDensity = 0;
        int bestGridX = -1;
        int bestGridY = -1;
        
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (foodDensity[i][j] > maxDensity) {
                    maxDensity = foodDensity[i][j];
                    bestGridX = i;
                    bestGridY = j;
                }
            }
        }
        
        // Se troviamo un'area con molta densità di cibo, dirigiamoci lì invece che verso un singolo punto
        if (maxDensity > 5) {
            // Troviamo il centro dell'area ad alta densità
            int centerX = bestGridX * gridWidth + gridWidth / 2;
            int centerY = bestGridY * gridHeight + gridHeight / 2;
            
            // Verifichiamo che ci sia davvero del cibo in quell'area
            boolean foundFoodInDenseArea = false;
            int searchArea = 15; // Raggio di ricerca nell'area densa
            for (int i = -searchArea; i <= searchArea && !foundFoodInDenseArea; i += 3) {
                for (int j = -searchArea; j <= searchArea && !foundFoodInDenseArea; j += 3) {
                    int checkX = centerX + i;
                    int checkY = centerY + j;
                    
                    if (checkX >= 0 && checkX < Food.getWidth() &&
                        checkY >= 0 && checkY < Food.getHeight() &&
                        Food.isFood(checkX, checkY)) {
                        bestX = checkX;
                        bestY = checkY;
                        foundFoodInDenseArea = true;
                    }
                }
            }
        }

        if (minDistance < Integer.MAX_VALUE) {
            this.targetingFood = true;
            this.targetX = bestX;
            this.targetY = bestY;
            this.isOnXPattern = false; // Quando troviamo cibo, non siamo più sul pattern X
        } else {
            // Se non abbiamo trovato cibo, c'è una probabilità di organizzarsi nuovamente in pattern X
            if (!isOnXPattern && Math.random() < 0.2) { // 20% di probabilità
                // Se non si trova cibo, posiziona i batteri nuovamente ad X
                if (Math.random() < 0.5) {
                    arrangeInXPatternFromCenter();
                } else {
                    arrangeInXPatternFromCorner();
                }
            }
        }
    }
    
    /**
     * Cerca cibo in un'area ristretta attorno alla posizione attuale
     * @param range il raggio di ricerca
     * @return true se è stato trovato del cibo
     */
    private boolean searchInArea(int range) {
        // Scansiona un'area quadrata attorno alla posizione attuale
        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                int checkX = x + i;
                int checkY = y + j;
                
                if (checkX >= 0 && checkX < Food.getWidth() &&
                    checkY >= 0 && checkY < Food.getHeight() &&
                    Food.isFood(checkX, checkY)) {
                    
                    // Abbiamo trovato del cibo nelle vicinanze
                    this.targetingFood = true;
                    this.targetX = checkX;
                    this.targetY = checkY;
                    this.isOnXPattern = false;
                    return true;
                }
            }
        }
        return false;
    }
    // Metodo per aggiornare il raggio di ricerca in base alla quantità di cibo
    private void updateSearchRadius() {
        int foodQuantity = Food.getFoodQuantity();
        
        // Adattamento dinamico del raggio di ricerca basato sulla salute attuale
        int healthFactor = this.getHealth() < 50 ? 2 : 1; // Raggio maggiore con poca salute
        
        // Più cibo c'è, minore è il raggio di ricerca
        if (foodQuantity > ABUNDANT_FOOD_max) {
            // Con cibo abbondante, gradualmente ritorna al raggio base
            if (searchRadius > BASE_SEARCH_RADIUS) {
                searchRadius -= 2; // Riduzione più veloce con cibo abbondante
            }
        }
        // Con cibo scarso, tecniche aggressive con raggio di ricerca molto più ampio
        else if (foodQuantity < SCARCE_FOOD_min) {
            // Aumento più rapido e aggressivo del raggio di ricerca
            if (searchRadius < BASE_SEARCH_RADIUS * 3 * healthFactor) {
                searchRadius += 3; // Incremento ancora più rapido
            }
        }
        // Con cibo moderato, mantiene un raggio intermedio
        else {
            // Se il raggio è troppo basso, lo aumenta
            if (searchRadius < BASE_SEARCH_RADIUS * 1.5 * healthFactor) {
                searchRadius += 2;
            }
            // Se è troppo alto, lo diminuisce
            else if (searchRadius > BASE_SEARCH_RADIUS * 1.5 * healthFactor) {
                searchRadius--;
            }
        }

        // Il raggio non può mai scendere sotto il valore minimo
        if (searchRadius < BASE_SEARCH_RADIUS) {
            searchRadius = BASE_SEARCH_RADIUS;
        }
        
        // Limita il raggio massimo per evitare spreco di calcoli
        int maxRadius = BASE_SEARCH_RADIUS * 4;
        if (searchRadius > maxRadius) {
            searchRadius = maxRadius;
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
    }    private void moveTowardsFood() {
        // Prima verifica se c'è cibo nella posizione attuale
        if (Food.isFood(x, y)) {
            // Siamo già su del cibo, controlliamo se c'è altro cibo adiacente
            scanAndEatAdjacentFood();
            return; // Abbiamo consumato cibo, termina qui il movimento
        }
        
        // Calcola le distanze
        int dx = targetX - x;
        int dy = targetY - y;
        
        int moveFactor = 1; // Fattore di movimento base
        
        // Movimento più veloce quando la fame è alta o in sopravvivenza
        if (survivalMode || this.getHealth() < 30) {
            moveFactor = 2; // Movimento più rapido in situazioni critiche
        }
        
        // Movimento ottimizzato diagonalmente verso il cibo
        if (dx != 0 && dy != 0) {
            // Movimento diagonale (ottimizzato)
            if (dx > 0) {
                x += moveFactor;
            } else {
                x -= moveFactor;
            }
            
            if (dy > 0) {
                y += moveFactor;
            } else {
                y -= moveFactor;
            }
        } else {
            // Movimento ortogonale (quando già allineati su un asse)
            if (dx != 0) {
                if (dx > 0) {
                    x += moveFactor;
                } else {
                    x -= moveFactor;
                }
            }
            if (dy != 0) {
                if (dy > 0) {
                    y += moveFactor;
                } else {
                    y -= moveFactor;
                }
            }
        }

        // Mantenimento entro i bordi
        x = Math.max(0, Math.min(Food.getWidth() - 1, x));
        y = Math.max(0, Math.min(Food.getHeight() - 1, y));
        
        // Verifica se c'è cibo sulla posizione attuale dopo il movimento
        if (Food.isFood(x, y)) {
            // Se c'è cibo nella nuova posizione, controlla anche le celle adiacenti
            scanAndEatAdjacentFood();
        }
        // Altrimenti continua verso il target originale
        else if (Math.abs(x - targetX) <= moveFactor && Math.abs(y - targetY) <= moveFactor) {
            // Se siamo vicini alla destinazione, controllare se il cibo esiste ancora
            if (!Food.isFood(targetX, targetY)) {
                targetingFood = false;
                // Cerca immediatamente altro cibo nelle vicinanze
                searchForFood(BASE_SEARCH_RADIUS);
            } else {
                // Se il cibo esiste ancora, posizionati esattamente su di esso
                x = targetX;
                y = targetY;
                // E cerca anche nelle celle adiacenti
                scanAndEatAdjacentFood();
            }
        }
    }
    
    /**
     * Scansione e consumo del cibo nelle celle adiacenti
     * Permette al batterio di mangiare tutto il cibo presente in un'area
     */
    private void scanAndEatAdjacentFood() {
        // Mantieni traccia se hai trovato altro cibo nelle vicinanze
        boolean foundMoreFood = false;
        int scanRange = 3; // Raggio di scansione attorno alla posizione attuale
        int closestFoodX = -1;
        int closestFoodY = -1;
        int minDistance = Integer.MAX_VALUE;
        
        // Scansiona un'area quadrata intorno alla posizione attuale
        for (int i = -scanRange; i <= scanRange; i++) {
            for (int j = -scanRange; j <= scanRange; j++) {
                int checkX = x + i;
                int checkY = y + j;
                
                // Verifica che la posizione sia valida e contenga cibo
                if (checkX >= 0 && checkX < Food.getWidth() && 
                    checkY >= 0 && checkY < Food.getHeight() && 
                    Food.isFood(checkX, checkY)) {
                    
                    // Calcola la distanza
                    int distance = Math.abs(i) + Math.abs(j);
                    
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestFoodX = checkX;
                        closestFoodY = checkY;
                        foundMoreFood = true;
                    }
                }
            }
        }
        
        // Se trovato altro cibo nelle vicinanze, aggiorna il target
        if (foundMoreFood) {
            targetX = closestFoodX;
            targetY = closestFoodY;
        } else {
            // Se non c'è più cibo nelle immediate vicinanze, cerca in un'area più ampia
            targetingFood = false;
            searchForFood(BASE_SEARCH_RADIUS);
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
            // In situazioni di emergenza, teletrasporta direttamente sul cibo
            if (this.getHealth() < EMERGENCY_TELEPORT_THRESHOLD) {
                // Teletrasporto diretto sul cibo in caso di emergenza
                x = targetX;
                y = targetY;
            } else {
                // Calcola una posizione molto vicina al cibo (quasi sopra)
                int distanceFromFood = 2; // Distanza ridotta per arrivare più vicino
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
        } 
        // Teletrasporto intelligente in cerca di cibo se non è specificato un obiettivo
        else {
            boolean foundFood = false;
            // Prima cerca di trovare cibo in zone casuali ma potenzialmente ricche di cibo
            
            // Se la quantità di cibo è scarsa, tentiamo di trovare cibo negli angoli
            if (Food.getFoodQuantity() < SCARCE_FOOD_min) {
                // Seleziona un angolo a caso
                int corner = (int)(Math.random() * 4);
                int margin = 40; // Margine dall'angolo
                
                switch (corner) {
                    case 0: // Angolo in alto a sinistra
                        x = margin + (int)(Math.random() * margin);
                        y = margin + (int)(Math.random() * margin);
                        break;
                    case 1: // Angolo in alto a destra
                        x = Food.getWidth() - margin - (int)(Math.random() * margin);
                        y = margin + (int)(Math.random() * margin);
                        break;
                    case 2: // Angolo in basso a sinistra
                        x = margin + (int)(Math.random() * margin);
                        y = Food.getHeight() - margin - (int)(Math.random() * margin);
                        break;
                    case 3: // Angolo in basso a destra
                        x = Food.getWidth() - margin - (int)(Math.random() * margin);
                        y = Food.getHeight() - margin - (int)(Math.random() * margin);
                        break;
                }
                
                // Cerca cibo nella nuova posizione
                searchForFood(BASE_SEARCH_RADIUS);
                if (targetingFood) {
                    foundFood = true;
                    // Ulteriore teletrasporto verso il cibo trovato
                    teleport(true);
                }
            }
            
            // Se non abbiamo ancora trovato cibo, teletrasporto casuale standard
            if (!foundFood) {
                // Per il teletrasporto casuale, evita i bordi dell'arena
                int margin = 20;
                x = margin + (int)(Math.random() * (Food.getWidth() - 2 * margin));
                y = margin + (int)(Math.random() * (Food.getHeight() - 2 * margin));
                
                // Cerca immediatamente cibo nella nuova posizione
                searchForFood(BASE_SEARCH_RADIUS * 2);
            }
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
        clone.y = Math.max(2, Math.min(Food.getHeight() - 3, clone.y));        // Trasferisci le informazioni strategiche al clone
        clone.hadAbundantFood = this.hadAbundantFood;
        clone.sinceLastFood = 0; // Il clone inizia fresco
        clone.conservationMode = this.conservationMode; // Condividi lo stato di conservazione
        clone.isOnXPattern = this.isOnXPattern; // Trasferisci lo stato del pattern X

        // I cloni iniziano con un raggio di ricerca leggermente più alto rispetto al genitore
        clone.searchRadius = Math.min(this.searchRadius + 10, BASE_SEARCH_RADIUS * 2);

        return clone;
    }
}