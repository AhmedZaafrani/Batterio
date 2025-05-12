package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Timer;

/**
 * @author Alessandro Bugatti 2015
 */
public class mainForm extends javax.swing.JFrame {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelResult;
    private javax.swing.JPanel jPanelTerrain;
    // End of variables declaration//GEN-END:variables
    private LinkedList <javax.swing.JLabel> values;
    private javax.swing.Timer timerUpdateSimulation;
    private javax.swing.Timer timerUpdateFood;
    private javax.swing.Timer timerUpdateResult;
    private LinkedList<Batterio> batteri;
    private HashMap<String, Integer> numeroBatteri;
    private HashMap<String, Color> coloreBatteri;
    private ArrayList<String> nomiBatteri;
    private static final int LARGHEZZA_PANNELLO_LATERALE = 300;
    private static final int ALTEZZA_BORDO = 50;
    private static final int NUMEROBATTERIINIZIALI = 100;

    /**
     * Creates new form mainForm
     *
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws java.io.IOException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.net.URISyntaxException
     */
    public mainForm() throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, IOException, URISyntaxException {
        initComponents();
        Food food = new Food.Builder(1024, 640, Food.Distribution.SQUARE, 1000).build();
        Batterio.setFood(food);
        inizializzaBatteri();
        this.jPanelTerrain.add(new Terrain(
                        batteri,
                        jPanelTerrain.getBackground(),
                        numeroBatteri,
                        coloreBatteri
                )
        );

        //codice eseguito quando viene premuto il pulsante 'start'
        javax.swing.JButton btnStart = new javax.swing.JButton("Start");
        btnStart.addActionListener((ActionEvent e) -> {
            timerUpdateSimulation.start();
            timerUpdateResult.start();
            timerUpdateFood.start();
        });
        this.jPanelResult.add(btnStart);
        //codice eseguito quando viene premuto il pulsante 'stop'
        javax.swing.JButton btnStop = new javax.swing.JButton("Stop");
        btnStop.addActionListener((ActionEvent e) -> {
            timerUpdateSimulation.stop();
            timerUpdateResult.stop();
            timerUpdateFood.stop();
        });
        this.jPanelResult.add(btnStop);
        pack();
        this.setSize(
                Food.getWidth() + LARGHEZZA_PANNELLO_LATERALE,
                Food.getHeight() + ALTEZZA_BORDO
        );
        //Timer per l'aggiornamento della simulazione
        timerUpdateSimulation = new Timer(50, (ActionEvent e) -> {
            //20 aggiornmenti a secondo
            //necessario per evitare che riparta un ciclo
            // di ridisegno del campo gara mentre ne è già in corso uno
            jPanelTerrain.repaint(); //ridisegno del campo di gara
        });
        //Timer per l'aggiunta di cibo
        timerUpdateFood = new Timer(500, (ActionEvent e) -> {
            food.toggle();
        });
        //timerUpdateFood.setRepeats(true);
        //Timer per l'aggiornamento del pannello dei dati
        timerUpdateResult = new Timer(1000, (ActionEvent e) -> {
            for (int i = 0; i < values.size(); i++) {
                if (numeroBatteri.get(nomiBatteri.get(i)) > 0) {
                    values.get(i).setText(nomiBatteri.get(i) + " " + numeroBatteri.get(nomiBatteri.get(i)));
                } else {
                    System.out.println('('+nomiBatteri.get(i)+" is dead) ");
                    numeroBatteri.remove(nomiBatteri.get(i));
                    coloreBatteri.remove(nomiBatteri.get(i));
                    values.get(i).setText(nomiBatteri.get(i) + " 0");
                    values.remove(i); //non aggiorna più la label per non creare problemi grafici
                    nomiBatteri.remove(i);
                    i--;
                    timerUpdateSimulation.stop();
                    timerUpdateResult.stop();
                    timerUpdateFood.stop();
                }
            }
        });
    }

    /**
     * Funzione che recupera il nome di tutti i batteri ereditati che si trovano
     * nel package batteri_figli
     */
    private List<String> recuperaNomi() throws IOException, URISyntaxException {
        List<String> nomi = new ArrayList<>(), files;
        Path path;
        try {
            path = new File(this.getClass().getResource("../children/Dumb.class")
                    .toURI()).getParentFile().toPath();
        } catch (Exception e) {
            System.out.println("Dumb.class doesn't exist (" + e + ')');
            path = Paths.get(new File(".").getCanonicalPath() + "/build/classes/children/");
        }
        files = Files.walk(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(n -> n.endsWith(".class"))
                .collect(Collectors.toList());
        for (String nome : files)
            nomi.add(nome.replace(".class", ""));
        if (nomi.isEmpty()) {
            System.out.println("no classes found in " + path);
        } else {
            System.out.println("Classes found:");
            for (String nome : nomi)
                System.out.println("- "+nome);
        }
        return nomi;
    }

    /**
     * Inizializza la lista dei batteri
     */
    private void inizializzaBatteri() throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, IOException, URISyntaxException {
        batteri = new LinkedList<>();
        numeroBatteri = new HashMap<>();
        coloreBatteri = new HashMap<>();
        nomiBatteri = (ArrayList<String>) recuperaNomi();
        ArrayList<Color> colori = new ArrayList<>();
        colori.add(Color.RED);
        colori.add(Color.BLUE);
        colori.add(new Color(85,104,50));

        colori.add(Color.BLACK);
        colori.add(Color.MAGENTA);
        colori.add(new Color(255,117,20));
        colori.add(Color.CYAN);
        colori.add(Color.GREEN);
        colori.add(Color.YELLOW);


        colori.add(Color.DARK_GRAY);
        colori.add(Color.GRAY);

        colori.add(Color.LIGHT_GRAY);
        //array temporaneo per contenere i tempi di esecuzione dei batteri, per effettuare la media
        int tempiEsecuzione[] = new int[nomiBatteri.size()];
        /* Fa una stima dei tempi di esecuzione e cerca classi non valide (tutte le classi che non ereditano da Batterio)
        appartenenti alla lista nomiBatteri, che quindi vengono eliminate */
        final int ITERATION = 1;
        for (int i = 0; i < NUMEROBATTERIINIZIALI; i++) {
            for (int j = 0; j < nomiBatteri.size(); j++) {
                try {
                    batteri.add((Batterio) Class.forName("children." + nomiBatteri.get(j))
                            .getConstructor()
                            .newInstance());
                    //Recupero dei tempi di esecuzione
                    long start = System.nanoTime();
                    for (int k = 0; k < ITERATION ; k++) {
                        batteri.get(nomiBatteri.size() * i + j).run();
                    }
                    tempiEsecuzione[j] += System.nanoTime() - start;
                } catch (Exception e) {
                    System.out.println(nomiBatteri.get(j) + " has been removed (" + e + ')');
                    nomiBatteri.remove(j);
                    j--;
                }
            }
        }
        //rimuove alcuni batteri se non ci sono colori sufficienti per rappresentarli

        while (nomiBatteri.size() > colori.size()) {
            String nome = nomiBatteri.get(nomiBatteri.size() - 1);
            System.out.println('(' + nome + ") removed for lack of colors");
            for (int i = 0; i < batteri.size(); i++) {
                if (batteri.get(i).getClass().getSimpleName().equals(nome)) {
                    batteri.remove(i--);
                }
            }
            nomiBatteri.remove(nome);
        }
        //inserimento dei dati nelle due hashmap
        for (int i = 0; i < nomiBatteri.size(); i++) {
            coloreBatteri.put(nomiBatteri.get(i), colori.get(i));
            numeroBatteri.put(nomiBatteri.get(i), NUMEROBATTERIINIZIALI);
        }
        System.out.println(numeroBatteri.size() + " bacteria approved: ");
        //stampa il tempo medio (in nanosecondi) di esecuzione di ciascun batterio
        for (int i = 0; i < nomiBatteri.size(); i++) {
            if (tempiEsecuzione[i] / (NUMEROBATTERIINIZIALI * ITERATION) < 4000)
                System.out.println(tempiEsecuzione[i] / (NUMEROBATTERIINIZIALI * ITERATION) + "\tns OK (" + nomiBatteri.get(i) + ')');
            else
                System.out.println(tempiEsecuzione[i] / (NUMEROBATTERIINIZIALI * ITERATION) + "\tns KO (" + nomiBatteri.get(i) + ')');
        }
        values = new LinkedList <> ();
        //Creazione della lista dei nomi ed il numero dei batteri nella barra grafica
        for (int i = 0; i < nomiBatteri.size(); i++) {
            values.add(new javax.swing.JLabel(nomiBatteri.get(i) + " " + numeroBatteri.get(nomiBatteri.get(i))));
            values.get(i).setForeground(coloreBatteri.get(nomiBatteri.get(i)));
            values.get(i).setFont(new Font("Arial", Font.BOLD, 20));
            this.jPanelResult.add(values.get(i));
        }
    }

    /**
     *
     * @return numero dei batteri di ciascuno tipo all'inizio della simulazione
     */
    public static int getNumeroBatteriIniziali() {
        return mainForm.NUMEROBATTERIINIZIALI;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.1111111
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelResult = new javax.swing.JPanel();
        jPanelTerrain = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bacteria");
        setResizable(false);

        jPanelResult.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelResult.setMinimumSize(new java.awt.Dimension(150, 100));
        jPanelResult.setPreferredSize(new java.awt.Dimension(240, 70));
        jPanelResult.setLayout(new javax.swing.BoxLayout(jPanelResult, javax.swing.BoxLayout.Y_AXIS));
        getContentPane().add(jPanelResult, java.awt.BorderLayout.LINE_END);

        jPanelTerrain.setBackground(new java.awt.Color(255, 255, 255));
        jPanelTerrain.setMinimumSize(new java.awt.Dimension(1324, 700));
        jPanelTerrain.setPreferredSize(new java.awt.Dimension(1024, 700));
        jPanelTerrain.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanelTerrain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        /* editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) "
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(mainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new mainForm().setVisible(true);
            } catch (ClassNotFoundException | NoSuchMethodException
                     | InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException
                     | IOException | URISyntaxException ex) {
                Logger.getLogger(mainForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
