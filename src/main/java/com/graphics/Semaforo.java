package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class Semaforo{

    private final AppCiudad app;

    public Semaforo(AppCiudad app){
        this.app=app;
    }

    public void dibujar(){

        // Semaforos ubicados sobre las aceras
        semaforo(-15.7f,-15.7f,0);
        semaforo(15.7f,15.7f,(float)Math.PI);

        semaforo(-15.7f,15.7f,(float)Math.PI/2);
        semaforo(15.7f,-15.7f,-(float)Math.PI/2);
    }

    private void semaforo(float x,float z,float giro){

        double t=glfwGetTime()%9.0;

        // Rojo 4 s -> Verde 4 s -> Amarillo 1 s
        boolean rojo=t<4;
        boolean verde=t>=4&&t<8;
        boolean amarillo=t>=8;

        // Poste
        app.caja(
            x,2.0f,z,
            .22f,4.0f,.22f,
            .12f,.12f,.12f
        );

        // Cabezal
        app.cajaGirada(
            x,4.1f,z,
            .85f,2.3f,.65f,
            .04f,.04f,.04f,
            giro
        );

        // Luces
        luz(x,4.75f,z,giro,1,0,0,rojo);
        luz(x,4.10f,z,giro,1,.75f,0,amarillo);
        luz(x,3.45f,z,giro,0,1,.15f,verde);
    }

    private void luz(
        float x,float y,float z,
        float giro,
        float r,float g,float b,
        boolean encendida
    ){

        float dx=(float)Math.sin(giro)*.36f;
        float dz=(float)Math.cos(giro)*.36f;

        if(encendida){

            app.cajaConEmision(
                x-dx,y,z-dz,
                .38f,.38f,.15f,
                r,g,b,
                giro,
                1
            );

        }else{

            app.cajaGirada(
                x-dx,y,z-dz,
                .38f,.38f,.15f,
                r*.15f,g*.15f,b*.15f,
                giro
            );
        }
    }
}