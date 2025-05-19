package children;
import main.Batterio;
import main.Food;

public class Vim extends Batterio {
    
    private static int arenaWidth = -1;
    private static int arenaHeight = -1;
    
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    
    private int direction;
    private boolean targetingFood;
    private int targetX;
    private int targetY;
    private final int SEARCH_RADIUS = 80; 
    private int moveCounter = 0;
    
    public Vim() {
        super(); 
        
        this.direction = (int)(Math.random() * 4); 
        this.targetingFood = false;
        
        if (arenaWidth == -1) {
            arenaWidth = Food.getWidth();
            arenaHeight = Food.getHeight();
        }
    }
    
    @Override
    protected void move() {
       
        moveCounter++;
        
        if (Food.isFood(x, y)) {
            scanAndEatAdjacentFood();
            return;
        }
        
        if (!targetingFood || moveCounter % 5 == 0) {
            searchForFood();
        }
        
        if (targetingFood) {
            moveTowardsFood();
            
            if (getHealth() < 30) {
                moveTowardsFood();
            }
        } else {
            spiralMovement();
        }
    }
    
    private void searchForFood() {
        if (Food.isFood(x, y)) {
            targetingFood = true;
            targetX = x;
            targetY = y;
            return;
        }
        
        int bestX = -1;
        int bestY = -1;
        int minDistance = Integer.MAX_VALUE;
        
        final int[] xOffsets = {0, 1, 0, -1};
        final int[] yOffsets = {-1, 0, 1, 0};
        
        for (int dir = 0; dir < 4; dir++) {
            int dx = xOffsets[dir];
            int dy = yOffsets[dir];
            
            for (int i = 1; i <= SEARCH_RADIUS; i += 3) {
                int checkX = x + (dx * i);
                int checkY = y + (dy * i);
                
                if (checkX >= 0 && checkX < arenaWidth && 
                    checkY >= 0 && checkY < arenaHeight) {
                    
                    if (Food.isFood(checkX, checkY)) {
                        int distance = Math.abs(checkX - x) + Math.abs(checkY - y);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestX = checkX;
                            bestY = checkY;
                        }
                        if (distance < 10) break;
                    }
                }
            }
        }
        
        if (bestX != -1) {
            targetingFood = true;
            targetX = bestX;
            targetY = bestY;
        } else {
            targetingFood = false;
        }
    }
    
    private void spiralMovement() {
        int margin = 3;
        boolean nearLeft = x <= margin;
        boolean nearRight = x >= arenaWidth - 1 - margin;
        boolean nearTop = y <= margin;
        boolean nearBottom = y >= arenaHeight - 1 - margin;
        
        if (nearLeft && direction == LEFT) {
            direction = UP;
        } else if (nearTop && direction == UP) {
            direction = RIGHT;
        } else if (nearRight && direction == RIGHT) {
            direction = DOWN;
        } else if (nearBottom && direction == DOWN) {
            direction = LEFT;
        }
        
        if (moveCounter % 8 == 0) {
            direction = (direction + 1) % 4;
        }
        
        switch (direction) {
            case UP: y--; break;
            case RIGHT: x++; break;
            case DOWN: y++; break;
            case LEFT: x--; break;
        }
        
        if (x < 0) { x = 0; direction = RIGHT; }
        else if (x >= arenaWidth) { x = arenaWidth - 1; direction = LEFT; }
        
        if (y < 0) { y = 0; direction = DOWN; }
        else if (y >= arenaHeight) { y = arenaHeight - 1; direction = UP; }
    }
    
    private void moveTowardsFood() {
        if (Food.isFood(x, y)) {
            scanAndEatAdjacentFood();
            return;
        }
        
        int dx = targetX - x;
        int dy = targetY - y;
        
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
        
        if (Math.abs(x - targetX) <= 1 && Math.abs(y - targetY) <= 1) {
            if (Food.isFood(targetX, targetY)) {
                x = targetX;
                y = targetY;
                scanAndEatAdjacentFood();
            } else {
                targetingFood = false;
                searchForFood();
            }
        }
    }
    
    private void scanAndEatAdjacentFood() {
        
        final int[] xOffsets = {0, 1, 0, -1, 1, -1, 1, -1};
        final int[] yOffsets = {-1, 0, 1, 0, -1, -1, 1, 1};
        
        int closestX = -1;
        int closestY = -1;
        int minDist = Integer.MAX_VALUE;
        
        for (int i = 0; i < 8; i++) {
            int nx = x + xOffsets[i];
            int ny = y + yOffsets[i];
            
            if (nx >= 0 && nx < arenaWidth && ny >= 0 && ny < arenaHeight && Food.isFood(nx, ny)) {
                int dist = Math.abs(xOffsets[i]) + Math.abs(yOffsets[i]);
                if (dist < minDist) {
                    minDist = dist;
                    closestX = nx;
                    closestY = ny;
                }
            }
        }
        
        if (closestX != -1) {
            targetX = closestX;
            targetY = closestY;
        } else {
            targetingFood = false;
            searchForFood();
        }
    }
   
    @Override
    public Batterio clone() throws CloneNotSupportedException {
        Vim clone = (Vim) super.clone();
        
        clone.direction = this.direction;
        clone.targetingFood = false;
        
        int offset = moveCounter % 4;
        switch (offset) {
            case 0: clone.x++; break;
            case 1: clone.y++; break;
            case 2: clone.x--; break;
            case 3: clone.y--; break;
        }
        
        clone.x = Math.max(0, Math.min(arenaWidth - 1, clone.x));
        clone.y = Math.max(0, Math.min(arenaHeight - 1, clone.y));
        
        return clone;
    }
}