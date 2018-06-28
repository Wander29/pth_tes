package fx1.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Scanner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class Chart {
/**********************************************/
/*      DICHIARAZIONE VARIABILI               */
/**********************************************/
//Layout, contenitori
    StackPane layout;
//Grafica-componenti
    Axes axes;
    Plot base;
    final int XYMAX = 20;
    final int ZOOM = 3;
    final int X_START = 0, Y_START = 0, X_END = 6, Y_END = 3;
//File Input e relativi
    File fileInput;
    Scanner sc;
    String[] inp_temp;
//Logica-rappresentazione
    int cnt = 0; //contatore per i cicli della transizione del percorso (serve globale)
    public static boolean base_exists = false;
    //Triangoli
        ArrayList<Polygon> polys;
    //Inizio-fine
        Punto start = new Punto();
        Punto end = new Punto();
        Circle c_start = new Circle();
        Circle c_end = new Circle();
        int sX, sY, eX, eY, temp_x, temp_y;
//Percorso
    ArrayList<Punto> points;
    Path path;
//Algortimo
    final float COSTO_STRAIGHT = (float) 0.5;
    final float COSTO_DIAGONALE = (float) pitagora(COSTO_STRAIGHT, COSTO_STRAIGHT);
//Piano Cartesiano logico
    Grafo grafo;
    Nodo logic_start, logic_end, current;
    Triangolo tri_temp;
    PriorityQueue<Entry> frontier;
    Map<Nodo, Nodo> came_from = new HashMap(); //dict per ricostruire ogni percorso
    Map<Nodo, Float> costo_finora = new HashMap(); //dict per salvare i costi e confrontare i vantaggi dei percorsi
    int cnt_pr = 0; //contatore priorità (sarebbe p2) usato per la doppia priorità in modo da evitare stati di uguaglianza nella coda
    float new_cost, pr_temp;
    ArrayList<Nodo> pathfound = new ArrayList();
                            
/**********************************************/
/*      FUNZIONI ALGORITMO                    */
/**********************************************/
    public float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public float pitagora(int a, int b){
        return round((float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)), 5);
    }
    //Overload
    public float pitagora(float a, float b){
        return round((float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)), 5);
    }

    public float euristica(Nodo end, Nodo nodo){//funzione di calcolo distanza Euristica (istintiva)
        return pitagora((end.getX() - nodo.getX()), (end.getY() - nodo.getY()));
    }    
    
    public float doZoom (float n){
        return n * ZOOM;
    }
    //Overload
    public int doZoom (int n){
        return n * ZOOM;
    }
    
    public float remZoom (float n){
        return n / ZOOM;
    }
    //Overload
    public float remZoom (int n){
        return (float) n / (float) ZOOM;
    }
    
    private void puntoOutOfChartDialog(String punto){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERRORE");
        alert.setHeaderText("Il punto '" + punto + "'" + " non è utilizzabile");
        alert.setContentText("Probabilmente il punto inserito è contenuto in un "
                + "ostacolo oppure si sovrappone ad un altro punto.\n\n"
                + "Prova ad utilizzare altre coordinate");
        Optional<ButtonType> result = alert.showAndWait();
    }
    
//Costruttore
    public Chart(StackPane sp){
        this.layout = sp;
        initPiano();
    }

/**********************************************/
/*      FUNZIONI CHART                        */
/**********************************************/
    private void initPiano(){
        axes = new Axes( 
                410, 410, //width, height of the plane
                0, XYMAX, 1, // start X (lowerBound), end X (upperBound), tickUnit
                0, XYMAX, 1  // Y
        );
        axes.setStyle("-fx-background-color: rgb(35, 39, 50);");
        layout.setPadding(new Insets(40));
        layout.getChildren().add(axes);
        
        try{
            fileInput = new File("_files\\input_pathfinding.txt");
            sc = new Scanner(fileInput);
        } catch (Exception e){ }
        
        grafo = new Grafo( doZoom(XYMAX) ); //1680 Nodi
        base_exists = false;
    }
    
    public void preparaAlg(int sX, int sY, int eX, int eY){ 
        //Prepara i triangoli ed i punti di inizio e fine, ma NON il percorso
        this.sX = sX;
        this.sY = sY;
        this.eX = eX;
        this.eY = eY;
        
        try{
            if (base_exists){
                layout.getChildren().remove(base);
                base.updatePlot();
            } else {
                base = new Plot(axes);
            }
            layout.getChildren().add(base);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void startAlg(){
        base.find_path();
    }
    
    public void refresh(){
        if ( cnt_pr != 0) {
            try{
                layout.getChildren().remove(base);
                base.getChildren().remove(path);
                svuotaVar();
                layout.getChildren().add(base);
            } catch (Exception e) { }       
        }
    }
    
    private void svuotaVar() throws IndexOutOfBoundsException {
        path = new Path();
        points.clear();
        cnt = cnt_pr = 0;
        new_cost = pr_temp = 0;
        frontier.clear();
        came_from.clear();
        costo_finora.clear();
        pathfound.clear();
    }

/**********************************************/
/*      CLASSE AXES                           */
/**********************************************/
    class Axes extends Pane {
        private NumberAxis xAxis;
        private NumberAxis yAxis;

        public Axes(
                int width, int height,
                int xLow, int xHi, int xTickUnit,
                int yLow, int yHi, int yTickUnit
        ) {
            this.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
            this.setPrefSize(width, height);
            this.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

            xAxis = new NumberAxis(xLow, xHi, xTickUnit); //lowerBound, UpperBound, tickUnit
            xAxis.setSide(Side.BOTTOM);
            xAxis.setMinorTickVisible(true);
            xAxis.setPrefWidth(width);
            xAxis.setLayoutY(height);

            yAxis = new NumberAxis(yLow, yHi, yTickUnit);
            yAxis.setSide(Side.LEFT);
            yAxis.setMinorTickVisible(true);
            yAxis.setPrefHeight(height);
            yAxis.layoutXProperty().bind(Bindings.subtract(0, yAxis.widthProperty()));

            this.getChildren().setAll(xAxis, yAxis);
        }

        public NumberAxis getXAxis() {
            return xAxis;
        }

        public NumberAxis getYAxis() {
            return yAxis;
        }
    }
    
/**********************************************/
/*      CLASSE PLOT (crea grafica)            */
/**********************************************/
    class Plot extends Pane {
        public Plot(Axes axes) {
        //Inizializzazioni
            this.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
            this.setPrefSize(axes.getPrefWidth(), axes.getPrefHeight());
            this.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
            
            c_start.setRadius(3);
            c_start.setFill(Color.RED);
            c_end.setRadius(3);
            c_end.setFill(Color.WHITE);
            
            //Punti di Inizio e Fine inizialmente fissi e preimpostati
                start = new Punto(mapX(X_START, axes), 
                            mapY(Y_START, axes));
                logic_start = grafo.findNodo( doZoom(X_START), doZoom(Y_START) );
                end = new Punto(mapX(X_END, axes), 
                            mapY(Y_END, axes));
                logic_end = grafo.findNodo( doZoom(X_END), doZoom(Y_END) );

                c_start.setCenterX(start.getX());
                c_start.setCenterY(start.getY());
                c_end.setCenterX(end.getX());
                c_end.setCenterY(end.getY());

            //TRIANGLES
                polys = new ArrayList();
                while (sc.hasNextLine()){
                    inp_temp = sc.nextLine().split(" ");
                    Polygon poly = new Polygon();
                    poly.getPoints().addAll(new Double[]{
                        mapX(Integer.parseInt(inp_temp[0]), axes), mapY(Integer.parseInt(inp_temp[1]), axes),
                        mapX(Integer.parseInt(inp_temp[2]), axes), mapY(Integer.parseInt(inp_temp[3]), axes),
                        mapX(Integer.parseInt(inp_temp[4]), axes), mapY(Integer.parseInt(inp_temp[5]), axes)
                    });
                    poly.setFill(Color.web("#ff006a"));
                    polys.add(poly);
                    tri_temp = new Triangolo(
                            grafo.findNodo( doZoom(Integer.parseInt(inp_temp[0])), doZoom(Integer.parseInt(inp_temp[1])) ),
                            grafo.findNodo( doZoom(Integer.parseInt(inp_temp[2])), doZoom(Integer.parseInt(inp_temp[3])) ),
                            grafo.findNodo( doZoom(Integer.parseInt(inp_temp[4])), doZoom(Integer.parseInt(inp_temp[5])) )
                    );
                    grafo.addTriangolo(tri_temp);
                }
            
        //Inserimento elementi dentro lo StackPane
            for(int i = 0; i < polys.size(); i++){
                this.getChildren().add(polys.get(i));
            }
                
            this.getChildren().add(c_start);
            this.getChildren().add(c_end);
            base_exists = true;
            grafo.removeTriangoli();
        }//FINE Costruttore PLOT

        private void updatePlot(){
            if(sX >= 0 && sY >= 0){
                if ( grafo.findNodo( doZoom(sX), doZoom(sY) ) == null 
                    || ( doZoom(sX) == logic_end.getX() && doZoom(sY) == logic_end.getY() )
                    || ( doZoom(sX) == logic_start.getX() && doZoom(sY) == logic_start.getY() ) ){
                    puntoOutOfChartDialog("START");
                } else {
                    start = new Punto(mapX(sX, axes), mapY(sY, axes));
                    logic_start = grafo.findNodo( doZoom(sX), doZoom(sY) );
                    c_start.setCenterX(start.getX());
                    c_start.setCenterY(start.getY());
                }
            }
            if(eX >= 0 && eY >= 0){
                if ( grafo.findNodo( doZoom(eX), doZoom(eY) ) == null 
                    || ( doZoom(eX) == logic_start.getX() && doZoom(eY) == logic_start.getY() ) 
                    || ( doZoom(eX) == logic_end.getX() && doZoom(eY) == logic_end.getY() ) ){
                    puntoOutOfChartDialog("END");
                } else {
                    end = new Punto(mapX(eX, axes), mapY(eY, axes));
                    logic_end = grafo.findNodo( doZoom(eX), doZoom(eY) );
                    c_end.setCenterX(end.getX());
                    c_end.setCenterY(end.getY());
                }
            }
        }
        
        private void find_path(){
        /**********************************************/
        /*       ALGORITMO A*                         */
        /**********************************************/
        
            frontier = new PriorityQueue<>(); //coda a priorità, prendo prima quello con priorità minore
            frontier.add(new Entry(0, 0, logic_start)); //punto iniziale (START), priorità1, pr2, nodo
            came_from.put(logic_start, logic_start);
            costo_finora.put(logic_start, new Float(0));
            cnt_pr = 0;
        //Piano Grafico
            points = new ArrayList(); 
            path = new Path();
            //Path
            path.setStroke(Color.ORANGE.deriveColor(0, 1, 1, 0.6));
            path.setStrokeWidth(2);
            path.setClip(
                    new Rectangle(
                            0, 0, 
                            axes.getPrefWidth(), 
                            axes.getPrefHeight()
                    )
            );
            path.getElements().add(
                    new MoveTo( //mi sposto all'inizio
                            start.getX(), start.getY()
                    )
            );
            this.getChildren().add(path);
            
        //Algoritmo
            while (! frontier.isEmpty() ){
                current = frontier.poll().getNode(); //prende l'elemento nella coda con priorità minore, 2 perchè dopo priorità e cnt
                if (current == logic_end) { //se arrivati al punto d'arrivo
                    break;
                }
                grafo.findAdjacents(current); //cerca i vicini solo del punto estratto dalla coda, non di tutti
                for ( Nodo next : current.get_adjacents() ) {
                    new_cost = costo_finora.get(current) + grafo.costoMovimento(next, current);
                    if ( !costo_finora.containsKey(next) || new_cost < costo_finora.get(next) ){
                        costo_finora.put(next, new_cost);
                        pr_temp = new_cost + euristica(logic_end, next);
                        frontier.add( new Entry(pr_temp, cnt_pr, next) );
                        came_from.put(next, current);
                        cnt_pr ++;
                    }
                }
            }
        /**********************************************/
        /*      FINE algoritmo A*                     */
        /**********************************************/
            System.out.println( "#FROM Start[" + remZoom(logic_start.getX()) + ',' + remZoom(logic_start.getY()) +
                    "] | TO End[" + remZoom(logic_end.getX()) + ',' + remZoom(logic_end.getY()) + "]" );
            current = logic_end;
            try {
                while (current != logic_start) {
                    pathfound.add(current);
                    current = came_from.get(current);
                }
                Collections.reverse(pathfound);
                for (Nodo next : pathfound) {
                    points.add(new Punto( mapX(remZoom(next.getX()), axes), mapY(remZoom(next.getY()), axes)) );
                    System.out.println("X:" + remZoom(next.getX()) + "Y:" + remZoom(next.getY()) );
                }
                System.out.println("#COSTO: " + round(costo_finora.get(logic_end), 4) );
            } catch (Exception e) { 
                System.out.println("IMPOSSIBILE trovare il percorso. You lost");
            }
        //Inserimento Percorso sul piano, scheduled                
            try{
                Timeline timeline = new Timeline(
                    new KeyFrame(
                      Duration.ZERO,
                      new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent actionEvent) {  
                        path.getElements().add(
                            new LineTo(
                                    points.get(cnt).getX(), points.get(cnt).getY()
                            )
                        );
                        cnt++;
                        }
                      }
                    ),
                    new KeyFrame(
                      Duration.millis(50)
                    )
                );
                timeline.setCycleCount(points.size());
                timeline.play();
            } catch (Exception e) { }
            //svuotaVar();
        }
        
/**********************************************/
/*           FUNZIONI                         */
/**********************************************/  
    //MAPPATURA PUNTI X e Y, relativamente al contenitore (0,0 in alto a SX dello StackPane)
        private double mapX(int x, Axes axes) {
            double sx = axes.getPrefWidth() / 
               (axes.getXAxis().getUpperBound() - 
                axes.getXAxis().getLowerBound());
            return x * sx;
        }
        //Overload
        private double mapX(double x, Axes axes) {
            double sx = axes.getPrefWidth() / 
               (axes.getXAxis().getUpperBound() - 
                axes.getXAxis().getLowerBound());
            return x * sx;
        }

        private double mapY(int y, Axes axes) {
            double sy = axes.getPrefHeight() / 
                (axes.getYAxis().getUpperBound() - 
                 axes.getYAxis().getLowerBound());
            return axes.getPrefHeight() - (y * sy);
        }
        //Overload
        private double mapY(double y, Axes axes) {
            double sy = axes.getPrefHeight() / 
                (axes.getYAxis().getUpperBound() - 
                 axes.getYAxis().getLowerBound());
            return axes.getPrefHeight() - (y * sy);
        }
    }
    
/**********************************************/
/*      CLASSE PUNTO (supporto, grafica)      */
/**********************************************/
    class Punto { //classe ridondante ottimizzabile, ma Double, usata solo graficamente / Nodo
        private double x, y;
        
        public Punto(double x, double y){
            this.x = x;
            this.y = y;
        }
        
        public Punto(){ }
        
        public double getX(){
            return this.x;
        }
        
        public double getY(){
            return this.y;
        }
    }
/**********************************************/
/*      CLASSE GRAFO                          */
/**********************************************/
    class Grafo{
    /*Corrisponde alla griglia del piano cartesiano, i nodi sono i Vertici;
    è l'oggetto griglia che contiene tutti i nodi e li gestisce*/
        private ArrayList<Nodo> _nodes = new ArrayList();
        private ArrayList<Nodo> nodes_to_delete = new ArrayList();
        //private Map<Nodo, Integer> _indices = new HashMap();
        private ArrayList<Triangolo> _triangoli = new ArrayList();
        private ArrayList<Direzione> dirs = new ArrayList();
        Nodo neighbor, nodoTemp;
        int x_current, y_current, rip_int, cnt_rip_int, rip_est, cnt_rip_est;
        float y_max, y_min, dif_current, dif_max;
    
        public Grafo(int num){
            for (int rig = 0; rig <= num; rig++){
                for (int col = 0; col <= num; col++){
                    this.addNode(new Nodo(rig, col));
                }
            }
            findDirections();
        }
        
        private void addNode(Nodo node){
            if (!this._nodes.contains(node)){
                this._nodes.add(node);
            }
        }
        
        private void addTriangolo(Triangolo tr){
            if (!this._triangoli.contains(tr)){
                this._triangoli.add(tr);
            }
        }
        
        private void findAdjacents(Nodo n){
            for (int u = 0; u < dirs.size(); u++){
                neighbor = grafo.findNodo( 
                        (n.getX() + dirs.get(u).getX()),
                        (n.getY() + dirs.get(u).getY()) 
                );
                if(_nodes.contains(neighbor))
                    addEdge(n, neighbor);
            }
        }
        
        private void findDirections(){
            dirs.add(new Direzione(-1, 0));
            dirs.add(new Direzione(-1, -1));
            dirs.add(new Direzione(0, -1));
            dirs.add(new Direzione(1, -1));
            dirs.add(new Direzione(1, 0));
            dirs.add(new Direzione(1, 1));
            dirs.add(new Direzione(0, 1));
            dirs.add(new Direzione(-1, 1));
        }
        
        private void addEdge(Nodo v1, Nodo v2){
            //Java passa gli oggetti per referenza (passa il valore della referenza)
            v1.addAdjacent(v2);
            v2.addAdjacent(v1);
        }
        
        public ArrayList<Nodo> getNodes(){
            return _nodes;
        }
        
        public ArrayList<Triangolo> getTriangoli(){
            return _triangoli;
        }
        
    //Rimozione OSTACOLI
        private void removeTriangoli(){
            for (Triangolo tri : this._triangoli) {
                        /*_1]A a sx di B
                  _2]A.x = B.x - > Niente su A
                  _3]A a dx di B */
                if ( tri.getVertici().get(0).getX() < tri.getVertici().get(1).getX() ){
                    //inizio con A rispetto a B, 3 casi. Interazioni tra rette AB e AC
                    condemnNodo(tri.getVertici().get(0)); //condanno A
                    this.rip_est = tri.getVertici().get(1).getX() - 
                             tri.getVertici().get(0).getX();
                    if ( tri.getVertici().get(2).getX() <  tri.getVertici().get(1).getX() ){
                        //SE C a sx di B
                        this.rip_est -= ( tri.getVertici().get(1).getX() - tri.getVertici().get(2).getX() );
                    }
                    cnt_rip_est = 1;
                    funcTriangolo(tri, rip_est, cnt_rip_est, 0, "AC", "AB");

                    /*arrivati a B, altri 3 casi
                        _1.1]B a sx di C
                    _1.2]B.x = C.x -> Niente
                    _1.3]B a dx di C */
                    if ( tri.getVertici().get(1).getX() <  tri.getVertici().get(2).getX() ){
                        condemnNodo( tri.getVertici().get(2) ); //condanno C
                        rip_est = tri.getVertici().get(2).getX() - 
                                tri.getVertici().get(1).getX() - 1; //la colonna di B già l'ho tolta
                        if (rip_est >= 1){
                            cnt_rip_est = 1;
                            funcTriangolo(tri, rip_est, cnt_rip_est, 1, "AC", "BC");
                        }
                    } else {
                        // 1.3]_B a dx di C
                        if ( tri.getVertici().get(1).getX() > tri.getVertici().get(2).getX() ){
                            condemnNodo(tri.getVertici().get(1)); //condanno B
                            rip_est = tri.getVertici().get(1).getX() - tri.getVertici().get(2).getX() - 1;
                            if (rip_est >= 1){
                                cnt_rip_est = 1;
                                funcTriangolo(tri, rip_est, cnt_rip_est, 2, "BC", "AB");
                            }
                        }
                    }
                } else {
                    // 2]_A.x = B.x
                    if ( tri.getVertici().get(0).getX() == tri.getVertici().get(1).getX() ) {
                        /*vado a B, solo UN caso
                            _B a sx di C */
                        condemnNodo(tri.getVertici().get(2)); //condanno C
                        rip_est = tri.getVertici().get(2).getX() - tri.getVertici().get(1).getX();
                        cnt_rip_est = 0;
                        funcTriangolo(tri, (rip_est - 1), cnt_rip_est, 1, "AC", "BC");
                    } else {
                        //_3]A a dx di B
                        condemnNodo(tri.getVertici().get(1)); //condanno B
                        rip_est = tri.getVertici().get(0).getX() - tri.getVertici().get(1).getX();
                        if (tri.getVertici().get(2).getX() < tri.getVertici().get(0).getX() ){
                            //SE C a sx di A
                            rip_est -= tri.getVertici().get(0).getX() - tri.getVertici().get(2).getX();
                        }
                        if (rip_est >= 1){
                            cnt_rip_est = 1;
                            funcTriangolo(tri, rip_est, cnt_rip_est, 1, "AB", "BC");
                        }
                        
                        /*arrivati a C, 3 casi
                            _3.1]A a dx di C
                        _3.2]A.x = C.x -> Niente
                        _3.3]A a s x di C */
                        if (tri.getVertici().get(2).getX() < tri.getVertici().get(0).getX() ){
                            condemnNodo(tri.getVertici().get(0));//condanno A
                            rip_est = tri.getVertici().get(0).getX() - tri.getVertici().get(2).getX() - 1;
                            
                            if (rip_est >= 1){
                                cnt_rip_est = 1;
                                funcTriangolo(tri, rip_est, cnt_rip_est, 2, "AB", "AC");
                            }
                        } else {
                            //_3.3]A a sx di C
                            if ( tri.getVertici().get(2).getX() > tri.getVertici().get(0).getX() ) {
                                condemnNodo(tri.getVertici().get(2));
                                rip_est = tri.getVertici().get(2).getX() - tri.getVertici().get(0).getX() -1;
                                
                                if (rip_est >= 1){
                                    cnt_rip_est = 1;
                                    funcTriangolo(tri, rip_est, cnt_rip_est, 0, "AC", "BC");
                                }
                            }
                        }
                    }
                }//FINE if, A rispetto a B
            } //FINE for
            removeNodiCondemnded();
        } //FINE removeTriangoli()
    
        private void funcTriangolo(
                Triangolo tri, int rip_est, int cnt_rip_est,
                int vert_x_start, String retta1, String retta2
        ){
            while ( cnt_rip_est <= rip_est ){//in Orizzontale
                this.x_current = tri.getVertici().get(vert_x_start).getX() + cnt_rip_est;
                y_max = tri.getRette().get(retta1).calcolaY(x_current);
                y_min = tri.getRette().get(retta2).calcolaY(x_current);
                y_current = (int) Math.floor(y_max);
                dif_current = y_max - y_current;
                dif_max = y_max - y_min;
                if ( dif_current <= dif_max ){
                    condemnNodo(x_current, y_current);
                    rip_int = y_current - (int) Math.ceil(y_min);
                    
                    if(rip_int >= 1){
                        cnt_rip_int = 1;
                        while ( cnt_rip_int <= rip_int ){ //in Verticale
                            condemnNodo(x_current, (y_current - cnt_rip_int) );
                            cnt_rip_int++;
                        }
                    }
                }
                cnt_rip_est ++;
            }
        }
        
        private void condemnNodo(int x, int y){
            this.nodoTemp = findNodo(x, y);
            if (!this.nodes_to_delete.contains(this.nodoTemp)) {
                this.nodes_to_delete.add(this.nodoTemp);
            }
        }
        //Overload
        private void condemnNodo(Nodo nodo){
            if (!this.nodes_to_delete.contains(nodo)) {
                this.nodes_to_delete.add(nodo);
            }
        }
        
        private void removeNodiCondemnded(){
            //non li cancella dalla lista dei nodi ma ne annulla solo i valori
            for (Nodo node : nodes_to_delete) {
                System.out.println("X:" + node.getX() + " | Y:" + node.getY());
                node.setX(-1);
                node.setY(-1);
            }
            this.nodes_to_delete.clear();
        }
        
    //Supporto
        private Nodo findNodo(int x, int y){
            nodoTemp = new Nodo(x, y);
            for (Nodo node  : _nodes) {
                if (nodoTemp.compareTo(node) == 0){
                    return node;
                }
            }
            return null;
        }
        
        private float costoMovimento(Nodo p1, Nodo p2){
            if ( Math.abs(p1.getX() - p2.getX()) == 1 &&
                   Math.abs(p1.getY() - p2.getY()) == 1 ) {
                return COSTO_DIAGONALE;
            }
            return COSTO_STRAIGHT;
        }
    }
    
/**********************************************/
/*      CLASSE DIREZIONE                      */
/**********************************************/
    class Direzione{
        private int x, y;
        
        public Direzione(int x, int y){
            this.x = x;
            this.y = y;
        }
        
        public int getX(){
            return this.x;
        }
        
        public int getY(){
            return this.y;
        }
    }
/**********************************************/
/*      CLASSE NODO                           */
/**********************************************/
    class Nodo implements Comparable<Nodo> {
    /* Contiente solamente le coordinate e la lista dei vicini che inizialmente è vuota e viene
    riempita solo se il nodo è di interesse al percorso che si sta cercando
    ridefinisce i 'compare' tra le sue istanze in modo da poterli confrontare*/
        private int x, y;
        private ArrayList<Nodo> _adjacent = new ArrayList();
        
        public Nodo(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void addAdjacent(Nodo a){
            if(!_adjacent.contains(a))
                this._adjacent.add(a);
        }
        
        public ArrayList<Nodo> get_adjacents(){
            return this._adjacent;
        }
        
        public int getX(){
            return this.x;
        }
        
        public int getY(){
            return this.y;
        }
        
        public void setX(int x){
             this.x = x;
        }
        
        public void setY(int y){
             this.y = y;
        }

        @Override
        public int compareTo(Nodo other) {
            if (this.getX() == other.getX()){
                if (this.getY() == other.getY()){
                    return 0;
                }
            }
            return -1;
        }
    }
/**********************************************/
/*      CLASSE TRIANGOLO                      */
/**********************************************/
    class Triangolo{
        private ArrayList<Nodo> vertici = new ArrayList();
        private Map<String, Retta> rette = new HashMap();
        private Nodo tempNodo;
        private Retta rettaAB, rettaAC, rettaBC;
        
        public Triangolo(Nodo v1, Nodo v2, Nodo v3){
            vertici.add(v1);
            vertici.add(v2);
            vertici.add(v3);
            
            ordinaVertici();
            trovaRette();
        }
        
        private void ordinaVertici(){
            for (int h = 1; h < 3; h++){
                //System.out.println(vertici);
                //System.out.println(getVertici().get(0).getX());
                //System.out.println(this.vertici.get(0).getX());
                if ( getVertici().get(0).getY() < getVertici().get(h).getY() ) {
                    tempNodo = this.vertici.get(0);
                    this.vertici.set(0, this.vertici.get(h));
                    this.vertici.set(h, tempNodo);
                } else {
                    if (this.vertici.get(0).getY() == this.vertici.get(h).getY()) {
                        if ( this.vertici.get(0).getX() > this.vertici.get(h).getX() ) {
                            tempNodo = this.vertici.get(0);
                            this.vertici.set(0, this.vertici.get(h));
                            this.vertici.set(h, tempNodo);
                        }
                    }
                }
            }
            
            if ( this.vertici.get(1).getX() < this.vertici.get(2).getX() ) {
                if ( this.vertici.get(1).getY() > this.vertici.get(2).getY() &&
                        this.vertici.get(2).getX() < this.vertici.get(0).getX() ){
                    tempNodo = this.vertici.get(1);
                    this.vertici.set(1, this.vertici.get(2));
                    this.vertici.set(2, tempNodo);
                }
            } else {
                if ( this.vertici.get(1).getX() == this.vertici.get(2).getX() ) {
                    if ( this.vertici.get(1).getY() > this.vertici.get(2).getY() ) {
                        tempNodo = this.vertici.get(1);
                        this.vertici.set(1, this.vertici.get(2));
                        this.vertici.set(2, tempNodo);
                    }
                } else {
                        tempNodo = this.vertici.get(1);
                        this.vertici.set(1, this.vertici.get(2));
                        this.vertici.set(2, tempNodo);
                }
            } 
        }
        
        private void trovaRette(){
          /*A = AB, AC
            B = AB, BC
            C = AC, BC
            sono 3 rette, che si ripetono */

            rettaAB = new Retta(this.vertici.get(0), this.vertici.get(1));
            rettaAC = new Retta(this.vertici.get(0), this.vertici.get(2));
            rettaBC = new Retta(this.vertici.get(1), this.vertici.get(2));
            this.rette.put("AB", rettaAB);
            this.rette.put("AC", rettaAC);
            this.rette.put("BC", rettaBC);
        }
        
        public ArrayList<Nodo> getVertici(){
            return this.vertici;
        }
        
        public Map<String, Retta> getRette(){
            return this.rette;
        }
    }
/**********************************************/
/*      CLASSE RETTA                          */
/**********************************************/
    class Retta{
        private float m, q;
        private String equazione = "";
        private int x_verticale = -1;
        
        public Retta(Nodo p1, Nodo p2){
            this.m = this.q = 0;
            this.x_verticale = -1;
            trovaEquazione(p1, p2);
        }
        
        private void trovaEquazione(Nodo p1, Nodo p2){
            //SE punti sulla stessa retta orizzontale
            if (p1.getY() == p2.getY()){
                this.q = p1.getY();
                this.equazione = "y = " + (this.q);
            } else {
                //SE punti sulla stessa retta verticale
                if (p1.getX() == p2.getX()){
                    this.x_verticale = p1.getX();
                    this.equazione = "x = " + x_verticale;
                } else {
                    this.m = round(( ((float)p2.getY() - (float)p1.getY()) / ((float)p2.getX() - (float)p1.getX()) ), 5);
                    this.q = ( (float)p1.getY() - ((float)p1.getX()*this.m) );
                    this.equazione = "y = " + this.m + "x + " + this.q;
                }
            }
        }
        
        public float calcolaY(int x){
            if (this.x_verticale >= 0){
                return -1;
            } else {
                return (this.m * x + this.q);
            }
        }

        public float calcolaX(int y){
            if (x_verticale >= 0){
                return x_verticale;
            } else {
                return ( (y - this.q)/this.m );
            }
        }
    }
/**********************************************/
/*      CLASSE ENTRY (per la PriorityQueue)   */
/**********************************************/
    class Entry implements Comparable<Entry> {
        
        private Nodo node;
        private int pr2;
        private float pr1;

        public Entry(float pr1, int pr2, Nodo node) {
            this.node = node;
            this.pr1 = pr1;
            this.pr2 = pr2;
        }

        public Nodo getNode(){
            return this.node;
        }
        
        private float getPr1(){
            return this.pr1;
        }
        
        private int getPr2(){
            return this.pr2;
        }

        @Override
        public int compareTo(Entry other) {
            if (this.pr1 > other.getPr1()){
                return 1;
            } else {
                if(this.pr1 == other.getPr1()){
                    if(this.pr2 > other.getPr2()){
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        }
    }
}