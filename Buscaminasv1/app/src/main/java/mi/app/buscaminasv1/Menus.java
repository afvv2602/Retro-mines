package mi.app.buscaminasv1;

import android.view.View;

public class Menus extends Thread {

    View menuA;
    View menuAB;
    View menuIzq;
    View menuDcho;

    public Menus(View menuA, View menuAB,View menuIzq,View menuDcho) {
        this.menuA = menuA;
        this.menuAB = menuAB;
        this.menuIzq = menuIzq;
        this.menuDcho = menuDcho;
    }


    @Override
    public void run() {
        super.run();
        try{
            while(true) {
                menuA.setBackgroundResource(R.drawable.menurosa);
                menuAB.setBackgroundResource(R.drawable.menurosa);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menurosarojo);
                menuAB.setBackgroundResource(R.drawable.menurosarojo);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menurojo);
                menuAB.setBackgroundResource(R.drawable.menurojo);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menunaranja);
                menuAB.setBackgroundResource(R.drawable.menunaranja);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menuamarillo);
                menuAB.setBackgroundResource(R.drawable.menuamarillo);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menuamarilloverde);
                menuAB.setBackgroundResource(R.drawable.menuamarilloverde);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menuverde);
                menuAB.setBackgroundResource(R.drawable.menuverde);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menuazul);
                menuAB.setBackgroundResource(R.drawable.menuazul);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menuazuloscuro);
                menuAB.setBackgroundResource(R.drawable.menuazuloscuro);
                sleep(2000);
                menuA.setBackgroundResource(R.drawable.menulila);
                menuAB.setBackgroundResource(R.drawable.menulila);
                sleep(2000);
            }
        }catch(InterruptedException e){

        }
    }
}
