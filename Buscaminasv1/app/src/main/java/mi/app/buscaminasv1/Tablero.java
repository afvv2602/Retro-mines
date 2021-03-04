package mi.app.buscaminasv1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import hallianinc.opensource.timecounter.StopWatch;

import static mi.app.buscaminasv1.MainActivity.musica;

public class Tablero extends AppCompatActivity {

    static AudioManager audioManager;
    private static final String TAG ="" ;
    static Lienzo fondo;
    static Button reiniciar,salir,sonido,bandera;
    static StopWatch stopwatch;
    static TextView cronometro,mejorCrono;
    static Thread colores;
    static View menuAB,menuA,menuDcho,menuIzq;
    static TextView contadorBombasTV;
    static RelativeLayout layoutJuego;
    static String nivel;
    static boolean swJuego,swCronometro,swAnuncio,swAnuncioCrono,swBanderas,swMinas,swReanudar;
    static int tiempo,contadorMin,contadorBanderas,columnas,filas;
    //Arrays para el funcionamiento del juego
    private static Button[][] buttons;
    private static int[][] tablero;
    private static boolean[][] pulsado;
    private static int[][] banderas;
    private static int minas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablero);
        SharedPreferences preferences = getSharedPreferences("marcas", Context.MODE_PRIVATE);
        mejorCrono = findViewById(R.id.mejorCronoTV);
        String btnPulsado = getIntent().getStringExtra("juego");
        switch (btnPulsado) {
            case "principiante":
                String principiante = preferences.getString("principiante","59:59:0");
                nivel = "principiante";
                mejorCrono.setText(principiante);
                filas = 2;
                columnas = 2;
                minas = 1;
                break;
            case "medio":
                String medio = preferences.getString("medio","59:59:0");
                nivel = "medio";
                mejorCrono.setText(medio);
                filas = 14;
                columnas = 14;
                minas = 30;
                break;
            case "experto":
                String experto = preferences.getString("experto","59:59:0");
                nivel = "experto";
                mejorCrono.setText(experto);
                filas = 18;
                columnas = 18;
                minas = 50;
                break;
        }
        generarRecursos();
        generarAccionesBotones();
        configurarMusica();
        //Hasta que no este creado el layout y no empieza a crear el tablero
        final View marco = findViewById(R.id.Tablero);
        marco.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                marco.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (marco.getHeight() != 0) {
                    controladorJuego();
                    mostrarArray();
                }
            }
        });
    }

    private void configurarMusica() {
        MainActivity.musica.stopAndChange(this,MainActivity.musica.getIndex());
    }

    private void generarAccionesBotones() {
        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                String nivel = getIntent().getStringExtra("juego");
                comunicacion.putExtra("juego", nivel);
                startActivity(comunicacion);
                finish();
            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Jugar.class);
                startActivity(comunicacion);
                finish();
            }
        });
        sonido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = findViewById(R.id.pop);
                popSonido(view);
            }
        });
        bandera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swBanderas){
                    bandera.setBackgroundResource(R.drawable.boton_rojo_ig);
                    for(int i = 0 ; i<filas;i++){
                        for(int j = 0; j<columnas;j++){
                            if(banderas[i][j]==10){
                                buttons[i][j].setOnClickListener(null);
                            }else {
                                buttons[i][j].setOnClickListener(comprobarClick);
                            }
                        }
                    }
                    swBanderas=false;

                }else{
                    bandera.setBackgroundResource(R.drawable.boton_azul_ig);
                    swBanderas=true;
                    for(int i = 0 ; i<filas;i++){
                        for(int j = 0; j<columnas;j++){
                            buttons[i][j].setOnClickListener(clickBanderas);
                        }
                    }
                }
            }
        });
    }

    private void generarRecursos(){
        //Cogemos las views de los menus
        menuA = findViewById(R.id.menuA);
        menuAB = findViewById(R.id.menuAB);
        menuIzq = findViewById(R.id.Tableroizq);
        menuDcho = findViewById(R.id.Tablerodcha);

        //Contador de bombas
        contadorBombasTV = findViewById(R.id.contadorbombs);
        contadorMin = minas;
        contadorBombasTV.setText("" + contadorMin);
        swMinas = true;

        //Cronometro al cual le pasamos el TV en el cual va a reflejar el resultado
        cronometro = findViewById(R.id.cronometroTV);
        stopwatch = new StopWatch(cronometro);
        swCronometro = true;

        //Boolean del anuncio
        swAnuncio=true;
        swAnuncioCrono = true;

        //set de todos los arrays
        tablero = new int[filas][columnas];
        buttons = new Button[filas][columnas];
        pulsado = new boolean[filas][columnas];
        banderas = new int[filas][columnas];

        //Cambio de colores de los menus
        colores = new Menus(menuA,menuAB,menuIzq,menuDcho);
        colores.start();

        //Creacion del lienzo
        layoutJuego = (RelativeLayout) findViewById(R.id.pintar);
        fondo = new Lienzo(this, filas, columnas);
        layoutJuego.addView(fondo);
        swJuego = true;

        //Menu de abajo
        reiniciar  = findViewById(R.id.BTNReiniciar);
        salir = findViewById(R.id.BTNSalir);
        sonido = findViewById(R.id.BTNSonido);
        bandera = findViewById(R.id.BTNBanderas);
        swBanderas = false;
        swReanudar = true;

    }

    private static void mostrarArray() {
        String tableroV = "      Buscaminas \n";
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int valorA = tablero[i][j];
                if (j == filas - 1) {
                    tableroV += "[" + valorA + "] \n";
                } else {
                    tableroV += "[" + valorA + "]";
                }
            }
        }
        System.out.println(tableroV);
    }

    private void controladorJuego() {
        generarMinas();
        generarJuego();
        rellenarBooleans();
        generarBotones();
    }

    private void generarMinas() {
        View view = (View) findViewById(R.id.Tablero);
        RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.pintar);
        contadorBanderas = minas;
        int ancho = view.getWidth();
        int alto = view.getHeight();
        for (int i = 0; i < minas; i++) {
            int fila = (int) (Math.random() * filas);
            int columna = (int) (Math.random() * columnas);
            int revisar = tablero[fila][columna];
            if (revisar == 0) {
                final ImageView imagen = new ImageView(this);
                imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                imagen.setImageResource(R.drawable.bomba);
                layout1.addView(imagen);
                tablero[fila][columna] = 9;
            } else {
                i--;
            }
        }
    }

    private void generarJuego() {
        /*
         * [izqA][midA][dchaA] [izqM][fila,columna][dchaM] [izqB][midB][dchaB]
         */
        View view = (View) findViewById(R.id.Tablero);
        RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.pintar);
        int ancho = view.getWidth();
        int alto = view.getHeight();
        ImageView imagen;
        int contadorBombas = 0;
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                contadorBombas = 0;
                //Esquina izquierda
                if (fila == 0 && columna == 0 && tablero[fila][columna] == 0) {
                    contadorBombas += revisardchaM(fila, columna);
                    contadorBombas += revisardchaB(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Lateral izquierdo
                else if (fila > 0 && fila < filas - 1 && columna == 0 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarmidA(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    contadorBombas += revisardchaA(fila, columna);
                    contadorBombas += revisardchaM(fila, columna);
                    contadorBombas += revisardchaB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                        case 4:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cuatro);
                            layout1.addView(imagen);
                            break;
                        case 5:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cinco);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Esquina izquierda abajo
                else if (fila == filas - 1 && columna == 0 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarmidA(fila, columna);
                    contadorBombas += revisardchaA(fila, columna);
                    contadorBombas += revisardchaM(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;

                    }
                }
                // Parte de abajo
                else if (fila == filas - 1 && columna > 0 && columna < columnas - 1 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarizqA(fila, columna);
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarmidA(fila, columna);
                    contadorBombas += revisardchaA(fila, columna);
                    contadorBombas += revisardchaM(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                        case 4:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cuatro);
                            layout1.addView(imagen);
                            break;
                        case 5:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cinco);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Esquina derecha Abajo
                else if (fila == filas - 1 && columna == columnas - 1 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarizqA(fila, columna);
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarmidA(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Lateral derecho
                else if (fila < filas - 1 && fila > 0 && columna == columnas - 1 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarizqA(fila, columna);
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarizqB(fila, columna);
                    contadorBombas += revisarmidA(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                        case 4:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cuatro);
                            layout1.addView(imagen);
                            break;
                        case 5:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cinco);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Esquina Derecha Arriba
                else if (fila == 0 && columna == columnas - 1 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarizqB(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                    }
                }
                // Parte de arriba
                else if (fila == 0 && columna < columnas - 1 && columna > 0 && tablero[fila][columna] == 0) {
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarizqB(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    contadorBombas += revisardchaM(fila, columna);
                    contadorBombas += revisardchaB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                        case 4:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cuatro);
                            layout1.addView(imagen);
                            break;
                        case 5:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cinco);
                            layout1.addView(imagen);
                            break;
                    }
                    // Resto
                }
                //Revisar que no pinta sobre una bomba
                else if (tablero[fila][columna] == 9) {
                } else {
                    contadorBombas += revisarizqA(fila, columna);
                    contadorBombas += revisarizqM(fila, columna);
                    contadorBombas += revisarizqB(fila, columna);
                    contadorBombas += revisarmidA(fila, columna);
                    contadorBombas += revisarmidB(fila, columna);
                    contadorBombas += revisardchaA(fila, columna);
                    contadorBombas += revisardchaM(fila, columna);
                    contadorBombas += revisardchaB(fila, columna);
                    tablero[fila][columna] = contadorBombas;
                    switch (contadorBombas) {
                        case 0:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            int random = (int) (Math.random() * 4 + 1);
                            switch (random) {
                                case 0:
                                    imagen.setImageResource(R.drawable.vacio1);
                                    break;
                                case 1:
                                    imagen.setImageResource(R.drawable.vacio2);
                                    break;
                                case 2:
                                    imagen.setImageResource(R.drawable.vacio3);
                                    break;
                                case 3:
                                    imagen.setImageResource(R.drawable.vacio4);
                                    break;
                                case 4:
                                    imagen.setImageResource(R.drawable.vacio5);
                                    break;
                            }
                            layout1.addView(imagen);
                            break;
                        case 1:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.uno);
                            layout1.addView(imagen);
                            break;
                        case 2:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.dos);
                            layout1.addView(imagen);
                            break;
                        case 3:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.tres);
                            layout1.addView(imagen);
                            break;
                        case 4:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cuatro);
                            layout1.addView(imagen);
                            break;
                        case 5:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.cinco);
                            layout1.addView(imagen);
                            break;
                        case 6:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.seis);
                            layout1.addView(imagen);
                            break;
                        case 7:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.siete);
                            layout1.addView(imagen);
                            break;
                        case 8:
                            imagen = new ImageView(this);
                            imagen.animate().x(ancho / columnas * columna).y(alto / filas * fila).setDuration(0).start();
                            imagen.setLayoutParams(new RelativeLayout.LayoutParams((ancho / columnas), (alto / filas)));
                            imagen.setImageResource(R.drawable.ocho);
                            layout1.addView(imagen);
                            break;
                    }
                }
            }
        }
    }

    private void rellenarBooleans() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                pulsado[i][j] = false;
            }
        }
    }

    private void generarBotones() {
        View view = (View) findViewById(R.id.Tablero);
        RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.pintar);
        int ancho = view.getWidth();
        int alto = view.getHeight();
        Log.d(TAG, "generarBotones: "+ancho+" "+alto);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final Button boton = new Button(this);
                boton.animate().x(ancho / columnas * j).y(alto / filas * i).setDuration(0).start();
                boton.setHeight(alto / filas);
                boton.setWidth(ancho / columnas);
                boton.setLayoutParams(new RelativeLayout.LayoutParams(ancho / columnas, alto / filas));
                boton.setText("");
                int random = (int) (Math.random() * 4 + 1);
                switch (random) {
                    case 0:
                        boton.setBackgroundResource(R.drawable.boton_azul);
                        break;
                    case 1:
                        boton.setBackgroundResource(R.drawable.boton_rojo);
                        break;
                    case 2:
                        boton.setBackgroundResource(R.drawable.boton_morado);
                        break;
                    case 3:
                        boton.setBackgroundResource(R.drawable.boton_verde);
                        break;
                    case 4:
                        boton.setBackgroundResource(R.drawable.boton_rosa);
                        break;
                }
                boton.setTag(i + "#" + j + "#" + tablero[i][j] + "#");
                boton.setOnClickListener(comprobarClick);
                boton.setOnLongClickListener(colocarBandera);
                buttons[i][j] = boton;
                layout1.addView(boton);
            }

        }
    }

    //colocar banderas
    private View.OnLongClickListener colocarBandera = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (swCronometro) {
                stopwatch.start();
                swCronometro = false;
            }
            if (swJuego) {
                Button boton = (Button) v;
                boton.setOnClickListener(null);
                Object ObjBTN = boton.getTag();
                String pos = ObjBTN.toString();
                String[] partes = pos.split("#");
                String fil = partes[0];
                String col = partes[1];
                int fila = Integer.parseInt(fil);
                int columna = Integer.parseInt(col);
                if (banderas[fila][columna] != 10 && swMinas) {
                    contadorMin--;
                    contadorBanderas--;
                    revisarMinas();
                    contadorBombasTV.setText("" + contadorMin);
                    banderas[fila][columna] = 10;
                    boton.setBackgroundResource(R.drawable.bandera);
                    revisarGanador(v);
                    return true;
                }else if (banderas[fila][columna] == 10) {
                    contadorMin++;
                    contadorBanderas++;
                    revisarMinas();
                    contadorBombasTV.setText("" + contadorMin);
                    banderas[fila][columna] = 0;
                    boton.setOnClickListener(comprobarClick);
                    int random = (int) (Math.random() * 4 + 1);
                    switch (random) {
                        case 0:
                            boton.setBackgroundResource(R.drawable.boton_rosa);
                            break;
                        case 1:
                            boton.setBackgroundResource(R.drawable.boton_azul);
                            break;
                        case 2:
                            boton.setBackgroundResource(R.drawable.boton_rojo);
                            break;
                        case 3:
                            boton.setBackgroundResource(R.drawable.boton_verde);
                            break;
                        case 4:
                            boton.setBackgroundResource(R.drawable.boton_morado);
                            break;
                    }

                }
            }
            return true;
        }

    };

    private void revisarMinas() {
        if(contadorBanderas==0){
            swMinas = false;
        }else {
            swMinas = true;
        }
    }

    private void revisarGanador(View v) {
        int ganador = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (tablero[i][j] == 9 && banderas[i][j] == 10) {
                    ganador++;
                    if (ganador == minas) {
                        stopwatch.pause();
                        registrarTiempoyPuntos();
                        popGanaste(v);
                    }
                }
            }
        }
    }

    private void registrarTiempoyPuntos() {
        int time = stopwatch.getTime();
        String tiempo = ""+time;
        String ms = tiempo.substring(tiempo.length()-1);
        String s = tiempo.substring(0,tiempo.length()-1);
        int segundos = Integer.parseInt(s);
        int minutos = 0;
        while(true){
            if(segundos>60){
                segundos = segundos-60;
                minutos++;
            }else{
                break;
            }
        }
        tiempo = ""+minutos+":"+segundos+":"+ms;
        compararMarcas(tiempo);
    }

    private void compararMarcas(String tiempo){
        SharedPreferences preferences = getSharedPreferences("marcas", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        String tiempoGuardado = preferences.getString(nivel,"59:59:0");
        String tiempoAct[] = tiempo.split(":");
        String tiempoGua[] = tiempoGuardado.split(":");
        int minAct = Integer.parseInt(tiempoAct[0]);
        int segAct = Integer.parseInt(tiempoAct[1]);
        int milAct = Integer.parseInt(tiempoAct[2]);
        int minGua = Integer.parseInt(tiempoGua[0]);
        int segGua = Integer.parseInt(tiempoGua[1]);
        int milGua = Integer.parseInt(tiempoGua[2]);
        if(minAct<minGua){
            //Guardamos la nueva mejor marca
            editor.putString(""+nivel,tiempo);
            editor.commit();
        }else if (minAct == minGua){
            if(segAct<segGua){
                editor.putString(""+nivel,tiempo);
                editor.commit();
            }else if (segAct == segGua){
                if(milAct<milGua){
                    editor.putString(""+nivel,tiempo);
                    editor.commit();
                }
            }
        }

    }

    //Ventanas emergentes
    private void popPerdiste(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popperdiste, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        final Button jugar = popupView.findViewById(R.id.jugar);
        final Button salir = popupView.findViewById(R.id.salir);
        final Button anuncio = popupView.findViewById(R.id.anuncio);
        final Button bombaBTN = (Button) view;
        if(!swAnuncio){
            anuncio.setBackgroundResource(R.drawable.boton_rojo_ig);
            anuncio.setVisibility(View.INVISIBLE);
        }

        jugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                comunicacion.putExtra("juego", nivel);
                startActivity(comunicacion);
                finish();
                popupWindow.dismiss();
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Jugar.class);
                startActivity(comunicacion);
                finish();
                popupWindow.dismiss();
            }
        });
        anuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swAnuncio && contadorMin!=1){
                swJuego = true;
                quitarBomba(bombaBTN);
                popupWindow.dismiss();
                swAnuncio = false;
            }else if(contadorMin==1){
                popupWindow.dismiss();
                registrarTiempoyPuntos();
                popGanaste(bombaBTN);
                }
            }
        });

    }

    private void popGanaste(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popganaste, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        final Button jugar = popupView.findViewById(R.id.jugar);
        final Button salir = popupView.findViewById(R.id.salir);
        final Button siguiente = popupView.findViewById(R.id.siguiente);
        jugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                String nivel = getIntent().getStringExtra("juego");
                comunicacion.putExtra("juego", nivel);
                popupWindow.dismiss();
                startActivity(comunicacion);
                finish();
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Jugar.class);
                popupWindow.dismiss();
                startActivity(comunicacion);
                finish();
            }
        });
        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                switch (nivel) {
                    case "principiante":
                        comunicacion.putExtra("juego", "medio");
                        startActivity(comunicacion);
                        popupWindow.dismiss();
                        finish();
                        break;
                    case "medio":
                        comunicacion.putExtra("juego", "experto");
                        startActivity(comunicacion);
                        popupWindow.dismiss();
                        finish();
                        break;
                    case "experto":
                        Intent volver = new Intent(v.getContext(), Jugar.class);
                        startActivity(volver);
                        popupWindow.dismiss();
                        finish();
                        break;
                }
            }
        });
    }

    private void popSonido(View view) {
        stopwatch.pause();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popsonido, null);
        int width = view.getWidth();
        int height = view.getHeight();
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        SharedPreferences preferences = getSharedPreferences("musica", Context.MODE_PRIVATE);
        String playlist = preferences.getString("playlistAct","KlowBeats");
        TextView playlistTV = popupView.findViewById(R.id.textoPlaylistTV);
        playlistTV.setText(playlist);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        final Button atras = popupView.findViewById(R.id.atras);
        final Button reanudar = popupView.findViewById(R.id.reanudar);
        final Button siguiente = popupView.findViewById(R.id.siguiente);
        final Button salir = popupView.findViewById(R.id.BTNSalir);
        final SeekBar barra = popupView.findViewById(R.id.barra_sonido);

        //Barra sonido
        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            barra.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            barra.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            barra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musica.atras();
            }
        });
        reanudar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("musica", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor= preferences.edit();
                boolean PSBtn = preferences.getBoolean("estado",false);
                if(PSBtn){
                    reanudar.setBackgroundResource(R.drawable.pausa);
                    editor.putBoolean("estado",false);
                    musica.reanudarParar();
                }else{
                    reanudar.setBackgroundResource(R.drawable.play);
                    editor.putBoolean("estado",true);
                    musica.reanudarParar();
                }
            }
        });
        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musica.siguiente();
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void quitarBomba(Button bombaBTN) {
        Object ObjBTN = bombaBTN.getTag();
        String pos = ObjBTN.toString();
        String[] partes = pos.split("#");
        String fil = partes[0];
        String col = partes[1];
        int fila = Integer.parseInt(fil);
        int columna = Integer.parseInt(col);
        banderas[fila][columna]=10;
        contadorBanderas--;
        contadorMin--;
        contadorBombasTV.setText("" + contadorMin);
    }

    //Revisar botones
    private View.OnClickListener comprobarClick = new View.OnClickListener() {
        public void onClick(View v) {
            if (swCronometro) {
                stopwatch.start();
                swCronometro = false;
            }
            if(swAnuncio == false && swAnuncioCrono) {
                stopwatch.resume();
                swAnuncioCrono = false;
            }
            if (swJuego) {
                Button boton = (Button) v;
                Object ObjBTN = boton.getTag();
                String pos = ObjBTN.toString();
                String[] partes = pos.split("#");
                String fil = partes[0];
                String col = partes[1];
                String cas = partes[2];
                int fila = Integer.parseInt(fil);
                int columna = Integer.parseInt(col);
                int casilla = Integer.parseInt(cas);
                pulsado[fila][columna] = true;
                if (casilla == 9) {
                    boton.setEnabled(false);
                    boton.setVisibility(View.INVISIBLE);
                    swJuego = false;
                    stopwatch.pause();
                    popPerdiste(v);
                }
                revisar();
                expandir(fila, columna);
            }
    }};

    private View.OnClickListener clickBanderas = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (swCronometro) {
                stopwatch.start();
                swCronometro = false;
            }
            if (swJuego) {
                Button boton = (Button) v;
                Object ObjBTN = boton.getTag();
                String pos = ObjBTN.toString();
                String[] partes = pos.split("#");
                String fil = partes[0];
                String col = partes[1];
                int fila = Integer.parseInt(fil);
                int columna = Integer.parseInt(col);
                if (banderas[fila][columna] != 10 && swMinas) {
                    contadorMin--;
                    contadorBanderas--;
                    revisarMinas();
                    contadorBombasTV.setText("" + contadorMin);
                    banderas[fila][columna] = 10;
                    boton.setBackgroundResource(R.drawable.bandera);
                    revisarGanador(v);
                }else if(banderas[fila][columna] == 10){
                    contadorMin++;
                    contadorBanderas++;
                    revisarMinas();
                    contadorBombasTV.setText("" + contadorMin);
                    banderas[fila][columna] = 0;
                    int random = (int) (Math.random() * 4 + 1);
                    switch (random) {
                        case 0:
                            boton.setBackgroundResource(R.drawable.boton_rosa);
                            break;
                        case 1:
                            boton.setBackgroundResource(R.drawable.boton_azul);
                            break;
                        case 2:
                            boton.setBackgroundResource(R.drawable.boton_rojo);
                            break;
                        case 3:
                            boton.setBackgroundResource(R.drawable.boton_verde);
                            break;
                        case 4:
                            boton.setBackgroundResource(R.drawable.boton_morado);
                            break;
                    }

                }
            }
        }
    };

    private void pararJuego()  {
        tiempo = stopwatch.getTime();
        colores.interrupt();
    }

    private void revisar() {
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero.length; j++) {
                if (pulsado[i][j]) {
                    expandir(i, j);
                    Button boton = buttons[i][j];
                    boton.setEnabled(false);
                    boton.setVisibility(View.INVISIBLE);
                }
            }
        }
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero.length; j++) {
                if (buttons[i][j].isEnabled() && pulsado[i][j]) {
                    revisar();
                }
            }
        }
    }

    private void expandir(int fila, int columna) {
        if (tablero[fila][columna] == 0 && pulsado[fila][columna] == true) {
            try {
                pulsado[fila - 1][columna - 1] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila][columna - 1] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila + 1][columna - 1] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila - 1][columna] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila + 1][columna] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila - 1][columna + 1] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila][columna + 1] = true;
            } catch (Exception e) {
            }
            try {
                pulsado[fila + 1][columna + 1] = true;
            } catch (Exception e) {
            }
        }
    }


    //Revisar al crear el tablero

    private static int revisarizqA(int fila, int columna) {
        if (tablero[fila - 1][columna - 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisarizqM(int fila, int columna) {
        if (tablero[fila][columna - 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisarizqB(int fila, int columna) {
        if (tablero[fila + 1][columna - 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisarmidA(int fila, int columna) {
        if (tablero[fila - 1][columna] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisarmidB(int fila, int columna) {
        if (tablero[fila + 1][columna] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisardchaA(int fila, int columna) {
        if (tablero[fila - 1][columna + 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisardchaM(int fila, int columna) {
        if (tablero[fila][columna + 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int revisardchaB(int fila, int columna) {
        if (tablero[fila + 1][columna + 1] == 9) {
            return 1;
        } else {
            return 0;
        }
    }
    
    //Full screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}

class Lienzo extends View {

    static int filas ;
    static int columnas;

    public Lienzo(Context context,int filas,int columnas) {
        super(context);
        this.filas = filas;
        this.columnas = columnas;
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRGB(0,0 ,0 );
        int ancho = canvas.getWidth();
        int alto = canvas.getHeight();

        Paint bordeEx = new Paint();
        int random = (int) (Math.random() * 3 + 1);
        bordeEx.setStrokeWidth(30);
        switch (random) {
            default:
                bordeEx.setColor(Color.rgb(0, 0, 0));
                break;
        }
        canvas.drawLine(0, 0, ancho, 0, bordeEx);
        canvas.drawLine(0, 0, 0, alto, bordeEx);
        canvas.drawLine(ancho, 0, ancho, alto, bordeEx);
        canvas.drawLine(0, alto, ancho, alto, bordeEx);
        Paint tablero = new Paint();
        switch (random) {
            default:
                tablero.setColor(Color.rgb(0, 0, 0));
                break;
        }
        //pintamos las celdas
        tablero.setStrokeWidth(0);
        for (int i = 0; i < columnas; i++) { //verticales
            canvas.drawLine(ancho / filas * i, 0, ancho/ filas *i, alto, tablero); //linea
        }

        for (int i = 0; i < filas; i++) { //horizontales
            canvas.drawLine(0, alto / columnas * i, ancho, alto / columnas * i, tablero); //linea
        }
    }
}

