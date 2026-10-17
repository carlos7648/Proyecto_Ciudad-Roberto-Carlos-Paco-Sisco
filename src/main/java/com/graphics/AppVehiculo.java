package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class AppVehiculo extends AppCiudad{

    private float autoX=-30,autoZ=30,angulo=0,velocidad=0,acelerador=0;
    private float anguloRemolque=0;
    private boolean camaraAerea=true,faros=true;
    private float radioAuto=1.5f;

    private Vehiculo vehiculo;

    @Override
    protected void iniciar(){
        super.iniciar();
        vehiculo=new Vehiculo(this);
    }

    private final float[][] destinos={
        {-30,-30},
        {10,-30},
        {30,10},
        {-30,50}
    };

    private int destinoActual=0,entregas=0;
    private boolean actividadTerminada=false;

    // ==========================================
    // CONTROLES
    // ==========================================
    @Override
    protected void tecla(int key){
        super.tecla(key);

        if(key==GLFW_KEY_C) camaraAerea=!camaraAerea;
        if(key==GLFW_KEY_F) faros=!faros;
        if(key==GLFW_KEY_R||key==GLFW_KEY_K) reiniciar();
    }

    private void reiniciar(){
        autoX=-30;
        autoZ=30;
        angulo=0;
        anguloRemolque=0;//empiesa en cero como el veiculo 
        velocidad=0;
        acelerador=0;
        destinoActual=0;
        entregas=0;
        actividadTerminada=false;
        faros=true;
    }

    // ==========================================
    // MOVIMIENTO
    // ==========================================
    @Override
    protected void actualizar(float dt){
        super.actualizar(dt);

        if(pulsada(GLFW_KEY_W)||pulsada(GLFW_KEY_UP)){
            acelerador+=12*dt;
            if(acelerador>16) acelerador=16;
        }else if(pulsada(GLFW_KEY_S)||pulsada(GLFW_KEY_DOWN)){
            acelerador-=12*dt;
            if(acelerador<-8) acelerador=-8;
        }else{
            acelerador*=.95f;
        }

        if(pulsada(GLFW_KEY_SPACE)){
            if(velocidad>0) velocidad-=25*dt;
            if(velocidad<0) velocidad+=25*dt;
            if(Math.abs(velocidad)<.5f) velocidad=0;
        }else{
            velocidad+=(acelerador-velocidad)*.7f*dt;
        }

        float direccion=0;

        if(pulsada(GLFW_KEY_A)||pulsada(GLFW_KEY_LEFT)) direccion=1;
        if(pulsada(GLFW_KEY_D)||pulsada(GLFW_KEY_RIGHT)) direccion=-1;

        if(Math.abs(velocidad)>.05f)
            angulo+=direccion*velocidad*.35f*dt;

        float frenteX=-(float)Math.sin(angulo);
        float frenteZ=-(float)Math.cos(angulo);

        float nuevoX=autoX+frenteX*velocidad*dt;
        float nuevoZ=autoZ+frenteZ*velocidad*dt;

        // Articulacion del remolque
        float diferencia=angulo-anguloRemolque;

        while(diferencia>(float)Math.PI)
            diferencia-=2*(float)Math.PI;

        while(diferencia<-(float)Math.PI)
            diferencia+=2*(float)Math.PI;

        float nuevoAnguloRemolque=
            anguloRemolque+diferencia*2.5f*dt;

        // Auto y remolque deben poder circular
        if(puedeCircular(nuevoX,nuevoZ) &&
           puedeCircularRemolque(
               nuevoX,nuevoZ,
               angulo,
               nuevoAnguloRemolque
           )){
            autoX=nuevoX;
            autoZ=nuevoZ;
            anguloRemolque=nuevoAnguloRemolque;
        }else{
            velocidad=0;
            acelerador=0;
        }

        comprobarEntrega();
        actualizarTitulo();
    }

    // ==========================================
    // COLISION DEL AUTO
    // ==========================================
    private boolean puedeCircular(float x,float z){
        if(x<-LIMITE+radioAuto||x>LIMITE-radioAuto||
           z<-LIMITE+radioAuto||z>LIMITE-radioAuto)
            return false;

        for(int fila=0;fila<MAPA.length;fila++){
            for(int col=0;col<MAPA[fila].length;col++){

                if(MAPA[fila][col]>0){
                    float cx=centro(col);
                    float cz=centro(fila);

                    float minX=cx-5-radioAuto;
                    float maxX=cx+5+radioAuto;
                    float minZ=cz-5-radioAuto;
                    float maxZ=cz+5+radioAuto;

                    if(x>minX&&x<maxX&&z>minZ&&z<maxZ)
                        return false;
                }
            }
        }

        return true;
    }

    // ==========================================
    // COLISION DEL REMOLQUE
    
    private boolean puedeCircularRemolque(
        float x,float z,
        float anguloAuto,
        float anguloRemolque
    ){
        float frenteAutoX=-(float)Math.sin(anguloAuto);
        float frenteAutoZ=-(float)Math.cos(anguloAuto);

        float frenteRemX=-(float)Math.sin(anguloRemolque);
        float frenteRemZ=-(float)Math.cos(anguloRemolque);

        float engancheX=x-frenteAutoX*2.25f;
        float engancheZ=z-frenteAutoZ*2.25f;

        float remolqueX=engancheX-frenteRemX*2.6f;
        float remolqueZ=engancheZ-frenteRemZ*2.6f;

        return puedeCircular(remolqueX,remolqueZ);
    }

    // ==========================================
    // ENTREGAS
    // ==========================================
    private void comprobarEntrega(){
        if(actividadTerminada) return;

        float dx=autoX-destinos[destinoActual][0];
        float dz=autoZ-destinos[destinoActual][1];

        float distancia=(float)Math.sqrt(dx*dx+dz*dz);

        if(distancia<3.5f){
            entregas++;
            destinoActual++;

            if(destinoActual>=destinos.length){
                actividadTerminada=true;
                destinoActual=destinos.length-1;
                velocidad=0;
                acelerador=0;
            }
        }
    }

    private void actualizarTitulo(){
        String estado=actividadTerminada
            ?"ENTREGAS COMPLETADAS - R para reiniciar"
            :"Destino "+(destinoActual+1)+"/"+destinos.length;

        String luces=faros?"Faros ON":"Faros OFF";
        String noche=modoNoche?"NOCHE":"DIA";

        glfwSetWindowTitle(
            ventana,
            String.format(
                "Ciudad | Vel: %.1f | %s | %s | %s | Entregas: %d/%d | C Camara | F Faros | N Dia/Noche | M Mapa | R Reiniciar",
                velocidad,noche,luces,estado,entregas,destinos.length
            )
        );
    }

    // ==========================================
    // CAMARA
    // ==========================================
    @Override
    protected void configurarCamara(){
        if(camaraAerea){
            super.configurarCamara();
            return;
        }

        float frenteX=-(float)Math.sin(angulo);
        float frenteZ=-(float)Math.cos(angulo);

        vector(
            "uOjo",
            autoX-frenteX*8,
            5.5f,
            autoZ-frenteZ*8
        );

        vector(
            "uObjetivo",
            autoX+frenteX*8,
            1.2f,
            autoZ+frenteZ*8
        );

        decimal("uAspecto",1100f/760f);
    }

    // ==========================================
    // FAROS
    // ==========================================
    @Override
    protected void prepararLuces(){
        super.prepararLuces();

        float frenteX=-(float)Math.sin(angulo);
        float frenteZ=-(float)Math.cos(angulo);

        float derechaX=(float)Math.cos(angulo);
        float derechaZ=-(float)Math.sin(angulo);

        float centroX=autoX+frenteX*2;
        float centroZ=autoZ+frenteZ*2;

        vector(
            "uFaro1",
            centroX+derechaX*.65f,
            .65f,
            centroZ+derechaZ*.65f
        );

        vector(
            "uFaro2",
            centroX-derechaX*.65f,
            .65f,
            centroZ-derechaZ*.65f
        );

        vector(
            "uDirFaro",
            frenteX,
            -.08f,
            frenteZ
        );

        entero("uFarosActivos",faros?1:0);
    }

    // ==========================================
    // ESCENA
    // ==========================================
    @Override
    protected void escena(){
        super.escena();
        dibujarDestino();
        dibujarAuto();
    }

    private void dibujarDestino(){
        if(actividadTerminada) return;

        float x=destinos[destinoActual][0];
        float z=destinos[destinoActual][1];

        cajaConEmision(
            x,.16f,z,
            3,.12f,3,
            .1f,1f,.25f,
            0,
            modoNoche?.8f:.15f
        );

        cajaConEmision(
            x,1.2f,z,
            .25f,2.2f,.25f,
            .1f,1f,.25f,
            0,
            modoNoche?1f:.2f
        );
    }
// ==========================================
// AUTO Y REMOLQUE
// ==========================================
private void dibujarAuto(){
    // Dibuja el auto principal
    vehiculo.dibujar(
        autoX,autoZ,
        angulo,
        faros
    );

    // Dibuja el remolque articulado detras del auto
    vehiculo.dibujarRemolque(
        autoX,autoZ,
        angulo,
        anguloRemolque
    );
}

// Inicia el programa
public static void main(String[] args){
    new AppVehiculo().run();
}

}
