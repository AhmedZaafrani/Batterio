package children;
import main.Food;
public class Dumb extends main.Batterio{
    private boolean goDown;
    public Dumb() {
        goDown = true;
    }
    @Override
    protected void move() {
        if (goDown) {
            y++;
            if (y > Food.getHeight()) {
                goDown = false;
                y = Food.getHeight() - 1;
            }
        } else {
            y--;
            if (y < 0) {
                goDown = true;
                y = 0;
            }
        }
        
    }
    @Override
    public main.Batterio clone() throws CloneNotSupportedException {
        Dumb clone = (Dumb)super.clone();
        clone.goDown = !goDown;
        return clone;
    }
}