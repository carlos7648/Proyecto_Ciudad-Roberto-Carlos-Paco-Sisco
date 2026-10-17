package com.graphics;

public class Vehiculo{
    private final AppCiudad app;

    public Vehiculo(AppCiudad app){
        this.app=app;
    }

    // ==========================================
    // VEHICULO
    // ==========================================
    public void dibujar(float x,float z,float angulo,boolean faros){
        float fx=-(float)Math.sin(angulo);
        float fz=-(float)Math.cos(angulo);
        float dx=(float)Math.cos(angulo);
        float dz=-(float)Math.sin(angulo);

        // Carroceria
        app.cajaGirada(x,.65f,z,2.5f,.65f,4,.05f,.20f,.85f,angulo);
        app.cajaGirada(x,1.15f,z+.15f,2,.55f,1.8f,.08f,.15f,.22f,angulo);

        // Ruedas
        rueda(x+dx*1.15f+fx*1.25f,z+dz*1.15f+fz*1.25f,angulo);
        rueda(x-dx*1.15f+fx*1.25f,z-dz*1.15f+fz*1.25f,angulo);
        rueda(x+dx*1.15f-fx*1.25f,z+dz*1.15f-fz*1.25f,angulo);
        rueda(x-dx*1.15f-fx*1.25f,z-dz*1.15f-fz*1.25f,angulo);

        // Faros
        float frenteX=x+fx*2.05f;
        float frenteZ=z+fz*2.05f;

        faro(frenteX+dx*.65f,frenteZ+dz*.65f,angulo,faros);
        faro(frenteX-dx*.65f,frenteZ-dz*.65f,angulo,faros);
    }

    // ==========================================
    // REMOLQUE ARTICULADO
    // ==========================================
    public void dibujarRemolque(float x,float z,float anguloAuto,float anguloRemolque){
        float frenteAutoX=-(float)Math.sin(anguloAuto);
        float frenteAutoZ=-(float)Math.cos(anguloAuto);

        float frenteRemX=-(float)Math.sin(anguloRemolque);
        float frenteRemZ=-(float)Math.cos(anguloRemolque);

        float derechaX=(float)Math.cos(anguloRemolque);
        float derechaZ=-(float)Math.sin(anguloRemolque);

        // Punto de enganche atras del auto
        float engancheX=x-frenteAutoX*2.25f;
        float engancheZ=z-frenteAutoZ*2.25f;

        // Centro del remolque
        float rx=engancheX-frenteRemX*2.6f;
        float rz=engancheZ-frenteRemZ*2.6f;

        
        // ENGANCHE EN V
        
        float baseX=rx+frenteRemX*1.35f;
        float baseZ=rz+frenteRemZ*1.35f;

        // Brazo izquierdo
        dibujarBrazo(
            engancheX,engancheZ,
            baseX+derechaX*.85f,
            baseZ+derechaZ*.85f
        );

        // Brazo derecho
        dibujarBrazo(
            engancheX,engancheZ,
            baseX-derechaX*.85f,
            baseZ-derechaZ*.85f
        );

        // Articulacion
        app.cajaGirada(
            engancheX,.38f,engancheZ,
            .35f,.22f,.35f,
            .25f,.25f,.25f,
            anguloAuto
        );

        // ==========================================
        // CAJA DEL REMOLQUE
        // ==========================================

        // Piso
        app.cajaGirada(
            rx,.48f,rz,
            2.5f,.20f,2.8f,
            .55f,.12f,.08f,
            anguloRemolque
        );

        // Lateral izquierdo
        app.cajaGirada(
            rx+derechaX*1.15f,1.05f,
            rz+derechaZ*1.15f,
            .15f,1.2f,2.8f,
            .65f,.15f,.10f,
            anguloRemolque
        );

        // Lateral derecho
        app.cajaGirada(
            rx-derechaX*1.15f,1.05f,
            rz-derechaZ*1.15f,
            .15f,1.2f,2.8f,
            .65f,.15f,.10f,
            anguloRemolque
        );

        // Parte trasera
        app.cajaGirada(
            rx-frenteRemX*1.3f,1.05f,
            rz-frenteRemZ*1.3f,
            2.5f,1.2f,.15f,
            .65f,.15f,.10f,
            anguloRemolque
        );

        // Ruedas
        app.cajaGirada(
            rx+derechaX*1.35f,.35f,
            rz+derechaZ*1.35f,
            .35f,.65f,.65f,
            .02f,.02f,.02f,
            anguloRemolque
        );

        app.cajaGirada(
            rx-derechaX*1.35f,.35f,
            rz-derechaZ*1.35f,
            .35f,.65f,.65f,
            .02f,.02f,.02f,
            anguloRemolque
        );
    }

    
    // BRAZO DEL ENGANCHE
   
    private void dibujarBrazo(float x1,float z1,float x2,float z2){
        float dx=x2-x1;
        float dz=z2-z1;

        float largo=(float)Math.sqrt(dx*dx+dz*dz);
        float cx=(x1+x2)/2;
        float cz=(z1+z2)/2;

        float ang=(float)Math.atan2(-dx,-dz);

        app.cajaGirada(
            cx,.38f,cz,
            .14f,.14f,largo,
            .55f,.12f,.08f,
            ang
        );
    }

   
    // RUEDAS
   
    private void rueda(float x,float z,float angulo){
        app.cajaGirada(
            x,.42f,z,
            .30f,.60f,.65f,
            .02f,.02f,.02f,
            angulo
        );
    }

    
    // FAROS
    
    private void faro(float x,float z,float angulo,boolean encendido){
        if(encendido)
            app.cajaConEmision(
                x,.68f,z,
                .35f,.25f,.15f,
                1,.95f,.65f,
                angulo,1
            );
        else
            app.cajaGirada(
                x,.68f,z,
                .35f,.25f,.15f,
                .3f,.3f,.25f,
                angulo
            );
    }
}