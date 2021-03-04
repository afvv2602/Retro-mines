package mi.app.buscaminasv1;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

class Musica extends AppCompatActivity {

    static MediaPlayer media [];
    static int KlowBeats [] ;
    int index;
    Context context;
    boolean swMedia;
    boolean estado;
    long tiempoAct = 0;

    public Musica (Context context){
        this.estado = false;
        this.swMedia = true;
        this.context = context;
        this.KlowBeats =new int[] {R.raw.ashes,R.raw.better_days,R.raw.feelings,R.raw.foggy_night,R.raw.gameboy,R.raw.intergalactic,
                R.raw.kokiri,R.raw.los_angeles,R.raw.rhodes,R.raw.road,R.raw.soul,R.raw.summertime,R.raw.unwind};
        this.index = 0;
        cargarMedia();
    }

    public int getIndex(){
        return index;
    }

    //Metodos

    public void cargarMedia(){
        if(swMedia){
            media = new MediaPlayer[KlowBeats.length];
            swMedia = false;
        }
        for (int i = 0;i<KlowBeats.length;i++){
            media[i] = MediaPlayer.create(context,KlowBeats[i]);
        }
    }
    public void siguiente(){
        if(media[index].isPlaying() && index<KlowBeats.length-1){
            media[index].stop();
            index++;
            media[index].start();
        }else if (!media[index].isPlaying()){
            reanudarParar();
        }else if (media[index].isPlaying() && index==KlowBeats.length-1){
            media[index].stop();
            cargarMedia();
            index = 0;
            media[index].start();
        }
    }

    public void atras(){
        if(media[index].isPlaying() && index>0){
            media[index].stop();
            cargarMedia();
            index--;
            media[index].start();
        }else if (!media[index].isPlaying()){
            reanudarParar();
        }else if (media[index].isPlaying() && index==0){
            media[index].stop();
            cargarMedia();
            index = KlowBeats.length-1;
            media[index].start();
        }
    }

    public void reanudarParar(){
        if(media[index].isPlaying()){
            media[index].stop();
        }else{
            media[index].start();
        }
    }
    public void stopAndChange(Context context,int index){
        if(!estado) {
            media[index].getCurrentPosition();
            media[index].stop();
            this.tiempoAct = media[index].getCurrentPosition();
            media[index].release();
            this.context = context;
            this.index = index;
            cargarMedia();
            reanudarTiempo();
            SharedPreferences preferences = context.getSharedPreferences("musica", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= preferences.edit();
            editor.putBoolean("estado",true);
            this.estado = true;
        }else{
            this.context = context;
            this.index = index;
            cargarMedia();
            SharedPreferences preferences = context.getSharedPreferences("musica", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= preferences.edit();
            editor.putBoolean("estado",false);
            this.estado = false;

        }
    }

    private void reanudarTiempo() {
        media[index].seekTo((int) tiempoAct);
        media[index].start();
    }

}


