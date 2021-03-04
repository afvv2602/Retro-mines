package mi.app.buscaminasv1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Jugar extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jugar);
        Button volver = findViewById(R.id.BTNVolver);
        Button salir = findViewById(R.id.BTNSalir);
        Button principante = findViewById(R.id.principiante);
        Button medio = findViewById(R.id.medio);
        Button experto = findViewById(R.id.experto);

        principante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                comunicacion.putExtra("juego", "principiante");
                startActivity(comunicacion);
                finish();
            }
        });

        medio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                comunicacion.putExtra("juego", "medio");
                startActivity(comunicacion);
                finish();
            }
        });

        experto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Tablero.class);
                comunicacion.putExtra("juego", "experto");
                startActivity(comunicacion);
                finish();
            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), MainActivity.class);
                startActivity(comunicacion);
                finish();
            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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

