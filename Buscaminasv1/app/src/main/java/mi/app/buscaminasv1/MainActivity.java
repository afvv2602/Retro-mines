package mi.app.buscaminasv1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import static mi.app.buscaminasv1.Tablero.audioManager;

public class MainActivity extends AppCompatActivity {
    static Musica musica = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);
        if(musica!=null){
            musica.stopAndChange(this,musica.getIndex());
        }else{
            musica = new Musica(this);
        }
        Button salir = findViewById(R.id.salir);
        Button jugar = findViewById(R.id.jugar);
        Button configuracion = findViewById(R.id.configuracion);

        jugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comunicacion = new Intent(v.getContext(), Jugar.class);
                startActivity(comunicacion);
            }
        });
        configuracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = findViewById(R.id.pop);
                popSonido(view);
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void popSonido(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popsonido, null);
        int width = view.getWidth();
        int height = view.getHeight();
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        SharedPreferences preferences = getSharedPreferences("musica", Context.MODE_PRIVATE);
        String playlist = preferences.getString("playlistAct", "KlowBeats");
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
                SharedPreferences.Editor editor = preferences.edit();
                boolean PSBtn = preferences.getBoolean("estado", false);
                if (PSBtn) {
                    reanudar.setBackgroundResource(R.drawable.pausa);
                    editor.putBoolean("estado", false);
                } else {
                    reanudar.setBackgroundResource(R.drawable.play);
                    editor.putBoolean("estado", true);
                }
                musica.reanudarParar();

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

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}