package com.graphics;

public class Iluminacion{

    /*
     * 9 farolas colocadas junto a las aceras.
     * Se evita el centro de las calles para que
     * no interfieran con el recorrido del vehiculo.
     */
    public static final float[][] FAROLAS={
    {-35.7f,-35.7f,-1,0},
    {-15.7f,-35.7f,0,-1},
    {24.3f,-35.7f,1,0},

    {-35.7f,-15.7f,-1,0},
    {24.3f,-15.7f,1,0},
    {-35.7f,24.3f,-1,0},

    {24.3f,24.3f,1,0},
    {-15.7f,44.3f,0,1},
    {24.3f,44.3f,0,1}
};
    private final AppCiudad app;

    public Iluminacion(AppCiudad app){
        this.app=app;
    }

    public void preparar(){

        app.entero(
            "uNoche",
            app.modoNoche?1:0
        );

        for(int i=0;i<FAROLAS.length;i++){

            app.vector(
                "uPostes["+i+"]",
                FAROLAS[i][0],
                4.8f,
                FAROLAS[i][1]
            );
        }
    }
}