package children;
import main.Batterio;
import main.Food;

public class VimOptimized extends main.Batterio {
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
    
    public VimOptimized() {
        super(); // Call parent constructor
        
        // Simplified positioning - direct center placement for best performance
        int width = Food.getWidth();
        int height = Food.getHeight();
        
        // Place at center with small offset
        this.x = width / 2 + (int)(Math.random() * 10) - 5;
        this.y = height / 2 + (int)(Math.random() * 10) - 5;
        
        // Ensure within bounds
        this.x = Math.max(2, Math.min(width - 3, this.x));
        this.y = Math.max(2, Math.min(height - 3, this.y));
        
        // Initialize with random direction
        this.direction = (int)(Math.random() * 4);
        this.targetingFood = false;
        
        // Fixed search radius
        this.searchRadius = BASE_SEARCH_RADIUS;
        this.isOnXPattern = false;
    }
    
    // Simplified method to check if food is abundant
    private boolean isFoodAbundant() {
        return Food.getFoodQuantity() > ABUNDANT_FOOD_max;
    }
    
    @Override
    protected void move() {
        // Quick check if we're already on food
        if (Food.isFood(x, y)) {
            return; // Exit immediately if on food
        }

        // Simple survival mode check
        survivalMode = this.getHealth() < 30;
        
        // Quick food search check
        if (!targetingFood || moveCounter % 5 == 0) {
            searchForFood(BASE_SEARCH_RADIUS);
        }
        
        // Movement strategy
        if (targetingFood) {
            moveTowardsFood();
            
            // Double movement for speed
            if (survivalMode) {
                moveTowardsFood();
            }
        } else {
            spiralMovement();
        }
        
        // Increment counter
        moveCounter++;
        
        // Keep in bounds
        x = Math.max(2, Math.min(Food.getWidth() - 3, x));
        y = Math.max(2, Math.min(Food.getHeight() - 3, y));
    }

    // Highly optimized search method that skips most pixels
    private void searchForFood(int radius) {
        // Check if we're already on food
        if (Food.isFood(x, y)) {
            this.targetingFood = true;
            this.targetX = x;
            this.targetY = y;
            return;
        }
        
        // Fast check of 8 directions at distance 2
        int[] dx = {0, 2, 2, 2, 0, -2, -2, -2};
        int[] dy = {-2, -2, 0, 2, 2, 2, 0, -2};
        
        for (int d = 0; d < 8; d++) {
            int checkX = x + dx[d];
            int checkY = y + dy[d];
            
            if (checkX >= 0 && checkX < Food.getWidth() && 
                checkY >= 0 && checkY < Food.getHeight() &&
                Food.isFood(checkX, checkY)) {
                
                this.targetingFood = true;
                this.targetX = checkX;
                this.targetY = checkY;
                return;
            }
        }

        // Skip factor for large jumps in search pattern
        int skipFactor = Math.max(5, radius / 4);
        
        // Search in expanding squares with large step sizes
        for (int currentRadius = 8; currentRadius <= radius; currentRadius += skipFactor) {
            // Check only the perimeter (edges of square)
            for (int i = -currentRadius; i <= currentRadius; i += skipFactor) {
                // Top and bottom edges
                if (checkFoodAtPoint(x + i, y - currentRadius)) return;
                if (checkFoodAtPoint(x + i, y + currentRadius)) return;
            }
            
            for (int j = -currentRadius + skipFactor; j < currentRadius; j += skipFactor) {
                // Left and right edges
                if (checkFoodAtPoint(x - currentRadius, y + j)) return;
                if (checkFoodAtPoint(x + currentRadius, y + j)) return;
            }
        }
        
        // If no food found, try a few random spots
        for (int i = 0; i < 3; i++) {
            int randomX = (int)(Math.random() * Food.getWidth());
            int randomY = (int)(Math.random() * Food.getHeight());
            
            if (checkFoodAtPoint(randomX, randomY)) return;
        }
    }
    
    // Helper method to check for food at a point
    private boolean checkFoodAtPoint(int checkX, int checkY) {
        if (checkX >= 0 && checkX < Food.getWidth() && 
            checkY >= 0 && checkY < Food.getHeight() && 
            Food.isFood(checkX, checkY)) {
            
            this.targetingFood = true;
            this.targetX = checkX;
            this.targetY = checkY;
            return true;
        }
        return false;
    }
    
    // Fast, fixed radius search
    private void updateSearchRadius() {
        // Fixed search radius for performance
        searchRadius = BASE_SEARCH_RADIUS;
        
        // Only increase for survival mode
        if (survivalMode) {
            searchRadius += 10;
        }
    }
    
    // Simple spiral movement
    private void spiralMovement() {
        // Move in the current direction
        switch (direction) {
            case UP: y -= 1; break;
            case RIGHT: x += 1; break;
            case DOWN: y += 1; break;
            case LEFT: x -= 1; break;
        }
        
        // Change direction occasionally for spiral effect
        if (moveCounter % 10 == 0) {
            direction = (direction + 1) % 4;
        }
        
        // Avoid borders
        if (x < 2) {
            x = 2;
            direction = RIGHT;
        } else if (x > Food.getWidth() - 3) {
            x = Food.getWidth() - 3;
            direction = LEFT;
        }
        
        if (y < 2) {
            y = 2;
            direction = DOWN;
        } else if (y > Food.getHeight() - 3) {
            y = Food.getHeight() - 3;
            direction = UP;
        }
    }
    
    // Fast movement toward food with high speed factor
    private void moveTowardsFood() {
        if (Food.isFood(x, y)) return;
        
        // Calculate distance to food
        int dx = targetX - x;
        int dy = targetY - y;
        
        // Very high movement factor for speed
        int moveFactor = survivalMode ? 10 : 7;
        
        // Fast movement calculation
        if (dx != 0) x += (dx > 0) ? moveFactor : -moveFactor;
        if (dy != 0) y += (dy > 0) ? moveFactor : -moveFactor;
        
        // Keep in bounds
        x = Math.max(0, Math.min(Food.getWidth() - 1, x));
        y = Math.max(0, Math.min(Food.getHeight() - 1, y));
        
        // Reset targeting if we reached the food
        if (Math.abs(x - targetX) <= moveFactor && Math.abs(y - targetY) <= moveFactor) {
            if (!Food.isFood(targetX, targetY)) {
                targetingFood = false;
            } else {
                x = targetX;
                y = targetY;
            }
        }
    }
    
    // Very simplified teleport method
    public boolean teleport(boolean targetedTeleport) {
        if (teleportCooldown > 0) return false;
        
        if (targetedTeleport && targetingFood) {
            // Direct teleport to food
            x = targetX;
            y = targetY;
        } else {
            // Random teleport
            x = 10 + (int)(Math.random() * (Food.getWidth() - 20));
            y = 10 + (int)(Math.random() * (Food.getHeight() - 20));
        }
        
        teleportCooldown = 5;
        return true;
    }
    
    @Override
    public Batterio clone() throws CloneNotSupportedException {
        VimOptimized clone = (VimOptimized) super.clone();
        
        // Simple offset positioning
        clone.x += (Math.random() < 0.5) ? 3 : -3;
        clone.y += (Math.random() < 0.5) ? 3 : -3;
        
        // Keep in bounds
        clone.x = Math.max(2, Math.min(Food.getWidth() - 3, clone.x));
        clone.y = Math.max(2, Math.min(Food.getHeight() - 3, clone.y));
        
        // Copy state
        clone.targetingFood = this.targetingFood;
        clone.searchRadius = this.searchRadius;
        
        return clone;
    }
}
