package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 * Classe che rappresenta il terreno di gara
 *
 * @author Alessandro Bugatti
 * @version 0.1
 */
public class Terrain extends JPanel {

    private final Color sfondo;
    private final LinkedList<Batterio> batteri;
    private final HashMap<String, Integer> numeroBatteri;
    private final HashMap<String, Color> coloreBatteri;

    public Terrain(
            LinkedList<Batterio> l,
            Color s,
            HashMap<String, Integer> numeroBatteri,
            HashMap<String, Color> coloreBatteri
    ) {
        batteri = l;
        sfondo = s;
        this.numeroBatteri = numeroBatteri;
        this.coloreBatteri = coloreBatteri;
    }

    @Override
    public void paintComponent(Graphics g) {
        //colorare lo sfondo
        g.setColor(sfondo);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        LinkedList<Batterio> babies = new LinkedList<>(); //lista di batteri nati durante questo ciclo
        for (Iterator<Batterio> i = batteri.iterator(); i.hasNext();) {
            Batterio batterio = i.next();
            g.setColor(sfondo);
            g.fillRect(batterio.getX(), batterio.getY(), 5, 5);
            batterio.run();
            String bacteriaType = batterio.getClass().getSimpleName();
            if (batterio.isDead()) {
                numeroBatteri.put(bacteriaType, numeroBatteri.get(bacteriaType) - 1);
                i.remove();
            } else if (batterio.isReadyForCloning()) {
                try {
                    int xp = batterio.x, yp = batterio.y;
                    Batterio clone = (Batterio) batterio.clone();
                    //recupera il tipo del figlio perché non è obbligatorio creare figli dello stesso tipo del padre
                    String cloneType = clone.getClass().getSimpleName();
                    //evitare che il padre si muova durante la fase di clonazione senza consumare salute
                    batterio.x = xp; batterio.y = yp;
                    //il figlio possiederà le stesse coordinate del padre
                    clone.x = xp; clone.y = yp;
                    babies.add(clone);
                    numeroBatteri.put(cloneType, numeroBatteri.get(cloneType) + 1);
                } catch (Exception e) {
                    System.out.println(e + " ("+ bacteriaType + " during cloning)");
                }
            } else {
                //Ridisegnare il batterio
                g.setColor(coloreBatteri.get(bacteriaType));
                g.fillRect(batterio.getX(), batterio.getY(), 3, 3);
            }
        }
        batteri.addAll(babies);
        //Ridisegna il cibo a ogni ciclo
        g.setColor(Color.GREEN);
        for (int i = 0; i < Food.getWidth(); i++) {
            for (int j = 0; j < Food.getHeight(); j++) {
                if (Food.isFood(i, j)) {
                    g.fillRect(i, j, 2, 2);
                }
            }
        }
    }
}
