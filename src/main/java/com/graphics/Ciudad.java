package com.graphics;

public class Ciudad{

    private final AppCiudad app;

    public Ciudad(AppCiudad app){
        this.app=app;
    }

    public void dibujar(){
        dibujarParques();
        dibujarVentanas();
        dibujarFarolas();
        dibujarPasosPeatonales();
    }

    // =========================
    // PARQUES
    // =========================

    private void dibujarParques(){
        for(int fila=0;fila<AppCiudad.MAPA.length;fila++){
            for(int col=0;col<AppCiudad.MAPA[fila].length;col++){

                if(AppCiudad.MAPA[fila][col]==2){

                    float x=app.centro(col);
                    float z=app.centro(fila);

                    arbol(x-2.5f,z-2.5f);
                    arbol(x+2.5f,z-2.5f);
                    arbol(x-2.5f,z+2.5f);
                    arbol(x+2.5f,z+2.5f);

                    banco(x-2,z);
                    banco(x+2,z);
                }
            }
        }
    }

    private void arbol(float x,float z){

        app.caja(
            x,1.1f,z,
            .45f,2,.45f,
            .38f,.20f,.08f
        );

        app.caja(
            x,2.6f,z,
            1.8f,1.8f,1.8f,
            .08f,.42f,.12f
        );

        app.caja(
            x,3.5f,z,
            1.3f,1.2f,1.3f,
            .10f,.55f,.15f
        );
    }

    private void banco(float x,float z){

        app.caja(
            x,.65f,z,
            2.2f,.20f,.65f,
            .42f,.22f,.08f
        );

        app.caja(
            x,1.05f,z+.28f,
            2.2f,.65f,.15f,
            .42f,.22f,.08f
        );

        app.caja(
            x-.75f,.35f,z,
            .15f,.60f,.15f,
            .15f,.15f,.15f
        );

        app.caja(
            x+.75f,.35f,z,
            .15f,.60f,.15f,
            .15f,.15f,.15f
        );
    }

    // =========================
    // VENTANAS
    // =========================

    private void dibujarVentanas(){

        for(int fila=0;fila<AppCiudad.MAPA.length;fila++){
            for(int col=0;col<AppCiudad.MAPA[fila].length;col++){

                if(AppCiudad.MAPA[fila][col]==1){

                    float x=app.centro(col);
                    float z=app.centro(fila);

                    float altura=
                        5+(fila*3+col*7)%9;

                    ventanasEdificio(
                        x,z,altura
                    );
                }
            }
        }
    }

    private void ventanasEdificio(
        float x,
        float z,
        float altura
    ){

        float emision=
            app.modoNoche?.8f:0;

        for(float y=2;y<altura;y+=2.5f){

            for(float d=-2;d<=2;d+=2){

                app.cajaConEmision(
                    x+d,y,z-3.52f,
                    .9f,1,.08f,
                    1,.82f,.35f,
                    0,
                    emision
                );

                app.cajaConEmision(
                    x+d,y,z+3.52f,
                    .9f,1,.08f,
                    1,.82f,.35f,
                    0,
                    emision
                );

                app.cajaConEmision(
                    x-3.52f,y,z+d,
                    .08f,1,.9f,
                    1,.82f,.35f,
                    0,
                    emision
                );

                app.cajaConEmision(
                    x+3.52f,y,z+d,
                    .08f,1,.9f,
                    1,.82f,.35f,
                    0,
                    emision
                );
            }
        }
    }

    // =========================
    // FAROLAS
    // =========================

    private void dibujarFarolas(){

        for(float[] p:Iluminacion.FAROLAS){
            farola(p[0],p[1]);
        }
    }

    private void farola(float x,float z){

        // Poste
        app.caja(
            x,2.5f,z,
            .25f,5,.25f,
            .15f,.15f,.17f
        );

        // Parte superior
        app.caja(
            x,5,z,
            .8f,.20f,.8f,
            .12f,.12f,.14f
        );

        // Bombilla
        app.cajaConEmision(
            x,4.85f,z,
            .50f,.35f,.50f,
            1,.85f,.40f,
            0,
            app.modoNoche?1:0
        );
    }

    // =========================
    // PASOS PEATONALES
    // =========================

   private void dibujarPasosPeatonales(){

    pasoHorizontal(-30,-37.5f);
    pasoHorizontal(10,-37.5f);
    pasoHorizontal(-30,2.5f);

    pasoVertical(37.5f,10);
    pasoVertical(17.5f,30);
}

    private void pasoHorizontal(
        float x,
        float z
    ){

        for(int i=-3;i<=3;i++){

            app.caja(
                x+i,
                .07f,
                z,
                .55f,.03f,2.8f,
                .95f,.95f,.95f
            );
        }
    }

    private void pasoVertical(
        float x,
        float z
    ){

        for(int i=-3;i<=3;i++){

            app.caja(
                x,
                .07f,
                z+i,
                4,.03f,.55f,
                .95f,.95f,.95f
            );
        }
    }
}