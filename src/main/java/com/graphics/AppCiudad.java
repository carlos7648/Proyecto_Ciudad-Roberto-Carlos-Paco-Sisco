package com.graphics;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class AppCiudad{

    protected long ventana;
    private int programa,vao,vbo;
    private int ancho=1100,alto=760;
    private float orbita=.6f;
    protected boolean modoNoche=false;

    private Ciudad ciudad;
    private Semaforo semaforo;
    private Iluminacion iluminacion;
    private Minimapa minimapa;

    protected static final float LIMITE=55;
    private static final float CELDA=10;
    private final Map<String,Integer> uniforms=new HashMap<>();

    protected static final int[][] MAPA={
        {0,0,0,0,0,0,0,0,0,0,0},
        {0,1,0,1,0,2,0,1,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0},
        {0,1,0,2,0,1,0,1,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0},
        {0,2,0,1,0,1,0,2,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0},
        {0,1,0,1,0,1,0,1,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0},
        {0,1,0,1,0,1,0,1,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0}
    };

    public static void main(String[] args){new AppCiudad().run();}

    public void run(){
        GLFWErrorCallback errores=GLFWErrorCallback.createPrint(System.err);
        errores.set();
        try{iniciar();loop();}
        finally{
            limpiar();
            glfwSetErrorCallback(null);
            errores.free();
        }
    }

    protected  void iniciar(){
        if(!GLFW.glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE,GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR,3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR,3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT,GLFW.GLFW_TRUE);

        ventana=GLFW.glfwCreateWindow(
            ancho,alto,
            "Ciudad | N Dia/Noche | M Minimapa | Flechas Orbitar | ESC Salir",
            0,0
        );

        if(ventana==0) throw new IllegalStateException("No se pudo crear la ventana");

        GLFW.glfwMakeContextCurrent(ventana);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(ventana);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        glfwSetKeyCallback(ventana,(v,key,scancode,action,mods)->{
            if(action==GLFW_PRESS) tecla(key);
        });

        crearPrograma();
        crearCubo();

        ciudad=new Ciudad(this);
        semaforo=new Semaforo(this);
        iluminacion=new Iluminacion(this);
        minimapa=new Minimapa();
    }

    private void crearPrograma(){
        int vertex=glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex,vertexShader());
        glCompileShader(vertex);

        if(glGetShaderi(vertex,GL_COMPILE_STATUS)==GL_FALSE)
            throw new RuntimeException("Error Vertex Shader:\n"+glGetShaderInfoLog(vertex));

        int fragment=glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment,fragmentShader());
        glCompileShader(fragment);

        if(glGetShaderi(fragment,GL_COMPILE_STATUS)==GL_FALSE)
            throw new RuntimeException("Error Fragment Shader:\n"+glGetShaderInfoLog(fragment));

        programa=glCreateProgram();
        glAttachShader(programa,vertex);
        glAttachShader(programa,fragment);
        glLinkProgram(programa);

        if(glGetProgrami(programa,GL_LINK_STATUS)==GL_FALSE)
            throw new RuntimeException("Error Programa:\n"+glGetProgramInfoLog(programa));

        glDeleteShader(vertex);
        glDeleteShader(fragment);
    }

    private void crearCubo(){
        float[] v={
            -.5f,-.5f,.5f,0,0,1, .5f,-.5f,.5f,0,0,1, .5f,.5f,.5f,0,0,1,
            .5f,.5f,.5f,0,0,1, -.5f,.5f,.5f,0,0,1, -.5f,-.5f,.5f,0,0,1,

            -.5f,-.5f,-.5f,0,0,-1, -.5f,.5f,-.5f,0,0,-1, .5f,.5f,-.5f,0,0,-1,
            .5f,.5f,-.5f,0,0,-1, .5f,-.5f,-.5f,0,0,-1, -.5f,-.5f,-.5f,0,0,-1,

            -.5f,.5f,.5f,-1,0,0, -.5f,.5f,-.5f,-1,0,0, -.5f,-.5f,-.5f,-1,0,0,
            -.5f,-.5f,-.5f,-1,0,0, -.5f,-.5f,.5f,-1,0,0, -.5f,.5f,.5f,-1,0,0,

            .5f,.5f,.5f,1,0,0, .5f,-.5f,-.5f,1,0,0, .5f,.5f,-.5f,1,0,0,
            .5f,-.5f,-.5f,1,0,0, .5f,.5f,.5f,1,0,0, .5f,-.5f,.5f,1,0,0,

            -.5f,.5f,-.5f,0,1,0, -.5f,.5f,.5f,0,1,0, .5f,.5f,.5f,0,1,0,
            .5f,.5f,.5f,0,1,0, .5f,.5f,-.5f,0,1,0, -.5f,.5f,-.5f,0,1,0,

            -.5f,-.5f,-.5f,0,-1,0, .5f,-.5f,.5f,0,-1,0, -.5f,-.5f,.5f,0,-1,0,
            .5f,-.5f,.5f,0,-1,0, -.5f,-.5f,-.5f,0,-1,0, .5f,-.5f,-.5f,0,-1,0
        };

        vao=glGenVertexArrays();
        vbo=glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        glBufferData(GL_ARRAY_BUFFER,v,GL_STATIC_DRAW);

        glVertexAttribPointer(0,3,GL_FLOAT,false,6*Float.BYTES,0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,3,GL_FLOAT,false,6*Float.BYTES,3*Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    private void loop(){
        double anterior=glfwGetTime();
        int cuadros=0,maxCuadros=Integer.getInteger("demo.frames",0);
        int[] w=new int[1],h=new int[1];

        while(!glfwWindowShouldClose(ventana)){
            glfwPollEvents();

            double ahora=glfwGetTime();
            float dt=(float)Math.min(ahora-anterior,.05);
            anterior=ahora;

            actualizar(dt);

            glfwGetFramebufferSize(ventana,w,h);
            ancho=w[0];
            alto=h[0];

            if(ancho>0&&alto>0) dibujarFrame();

            glfwSwapBuffers(ventana);

            cuadros++;
            if(maxCuadros>0&&cuadros>=maxCuadros)
                glfwSetWindowShouldClose(ventana,true);
        }
    }

    private void limpiar(){
        if(programa!=0) glDeleteProgram(programa);
        if(vbo!=0) glDeleteBuffers(vbo);
        if(vao!=0) glDeleteVertexArrays(vao);

        if(ventana!=0){
            Callbacks.glfwFreeCallbacks(ventana);
            glfwDestroyWindow(ventana);
        }

        glfwTerminate();
    }

    protected void tecla(int key){
        if(key==GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(ventana,true);
        if(key==GLFW_KEY_N) modoNoche=!modoNoche;
        if(key==GLFW_KEY_M&&minimapa!=null) minimapa.alternar();
    }

    protected boolean pulsada(int key){
        int estado=glfwGetKey(ventana,key);
        return estado==GLFW_PRESS||estado==GLFW_REPEAT;
    }

    protected void actualizar(float dt){
        if(pulsada(GLFW_KEY_LEFT)) orbita-=dt;
        if(pulsada(GLFW_KEY_RIGHT)) orbita+=dt;
    }

    protected void configurarCamara(){
        float x=(float)Math.sin(orbita)*100;
        float z=(float)Math.cos(orbita)*100;

        vector("uOjo",x,85,z);
        vector("uObjetivo",0,0,0);
        decimal("uAspecto",(float)ancho/alto);
    }

    protected void prepararLuces(){
        if(modoNoche){
            vector("uAmbiente",.10f,.12f,.20f);
            vector("uColorLuz",.25f,.30f,.50f);
        }else{
            vector("uAmbiente",.45f,.45f,.45f);
            vector("uColorLuz",1f,.95f,.85f);
        }

        vector("uDireccionLuz",-.5f,-1f,-.3f);

        if(iluminacion!=null) iluminacion.preparar();

        /*
         * Valores por defecto.
         * AppVehiculo sobrescribe estos valores en prepararLuces().
         */
        entero("uFarosActivos",0);
        vector("uFaro1",0,0,0);
        vector("uFaro2",0,0,0);
        vector("uDirFaro",0,0,-1);
    }

    protected float centro(int i){
        return -LIMITE+CELDA*(i+.5f);
    }

    private void dibujarFrame(){
        glViewport(0,0,ancho,alto);

        if(modoNoche) glClearColor(.015f,.025f,.07f,1);
        else glClearColor(.35f,.65f,.90f,1);

        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        glUseProgram(programa);
        glBindVertexArray(vao);

        entero("uMapa",0);
        configurarCamara();
        prepararLuces();
        escena();

        if(minimapa!=null&&minimapa.visible()){
            int tam=minimapa.tamanio(ancho,alto);
            int mx=minimapa.x(ancho,alto);
            int my=minimapa.y(ancho,alto);

            glEnable(GL_SCISSOR_TEST);
            glScissor(mx,my,tam,tam);

            glClearColor(.04f,.05f,.07f,1);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

            glViewport(mx,my,tam,tam);
            entero("uMapa",1);

            escena();

            entero("uMapa",0);

            glDisable(GL_SCISSOR_TEST);
            glViewport(0,0,ancho,alto);
        }

        int error=glGetError();
        if(error!=GL_NO_ERROR)
            throw new IllegalStateException("Error OpenGL: "+error);
    }

    protected void escena(){
        caja(0,-.25f,0,110,.5f,110,.16f,.19f,.23f);

        for(int f=0;f<MAPA.length;f++){
            for(int c=0;c<MAPA[f].length;c++){

                float x=centro(c);
                float z=centro(f);
                int tipo=MAPA[f][c];

                if(tipo==0){
                    dibujarMarcasCalle(f,c,x,z);
                }else{
                    caja(x,.15f,z,10,.3f,10,.60f,.64f,.66f);

                    if(tipo==1){
                        float altura=5+(f*3+c*7)%9;
                        float rojo=.28f+c*.045f;
                        float azul=.48f+f*.025f;

                        caja(x,altura/2+.3f,z,7,altura,7,rojo,.40f,azul);
                        caja(x,altura+.45f,z,7.3f,.3f,7.3f,.20f,.26f,.32f);
                    }else{
                        caja(x,.32f,z,9,.1f,9,.20f,.45f,.28f);
                    }
                }
            }
        }

        if(ciudad!=null) ciudad.dibujar();
        if(semaforo!=null) semaforo.dibujar();
    }

    private void dibujarMarcasCalle(int f,int c,float x,float z){
        if(f%2==0&&c%2==1){
            for(int d=-3;d<=3;d+=3)
                caja(x+d,.025f,z,1.6f,.03f,.13f,1,.84f,.35f);
        }

        if(c%2==0&&f%2==1){
            for(int d=-3;d<=3;d+=3)
                caja(x,.025f,z+d,.13f,.03f,1.6f,1,.84f,.35f);
        }
    }

    protected void caja(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b){
        cajaGirada(x,y,z,sx,sy,sz,r,g,b,0);
    }

    protected void cajaGirada(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float giro){
        vector("uPos",x,y,z);
        vector("uEscala",sx,sy,sz);
        vector("uColor",r,g,b);
        decimal("uGiro",giro);
        decimal("uEmision",0);
        glDrawArrays(GL_TRIANGLES,0,36);
    }

    protected void cajaConEmision(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float giro,float emision){
        vector("uPos",x,y,z);
        vector("uEscala",sx,sy,sz);
        vector("uColor",r,g,b);
        decimal("uGiro",giro);
        decimal("uEmision",emision);

        glDrawArrays(GL_TRIANGLES,0,36);

        decimal("uEmision",0);
    }

    private int uniform(String n){
        if(!uniforms.containsKey(n))
            uniforms.put(n,glGetUniformLocation(programa,n));

        return uniforms.get(n);
    }
    protected void vector(String n,float x,float y,float z){
        glUniform3f(uniform(n),x,y,z);
    }

    protected void decimal(String n,float v){
        glUniform1f(uniform(n),v);
    }

    protected void entero(String n,int v){
        glUniform1i(uniform(n),v);
    }

    private String vertexShader(){
        return """
        #version 330 core

        layout(location=0) in vec3 aPos;
        layout(location=1) in vec3 aNormal;

        uniform vec3 uPos;
        uniform vec3 uEscala;
        uniform vec3 uOjo;
        uniform vec3 uObjetivo;

        uniform float uGiro;
        uniform float uAspecto;
        uniform int uMapa;

        out vec3 vMundo;
        out vec3 vNormal;

        void main(){
            float c=cos(uGiro);
            float s=sin(uGiro);

            mat3 giro=mat3(
                c,0,-s,
                0,1,0,
                s,0,c
            );

            vMundo=giro*(aPos*uEscala)+uPos;
            vNormal=normalize(giro*(aNormal/uEscala));

            if(uMapa==1){
                float px=vMundo.x/60.0;
                float py=-vMundo.z/60.0;
                float profundidad=-vMundo.y/100.0;

                gl_Position=vec4(
                    px,
                    py,
                    profundidad,
                    1
                );
            }else{
                vec3 frente=normalize(uObjetivo-uOjo);
                vec3 derecha=normalize(cross(frente,vec3(0,1,0)));
                vec3 arriba=cross(derecha,frente);
                vec3 d=vMundo-uOjo;

                float vx=dot(d,derecha);
                float vy=dot(d,arriba);
                float vz=-dot(d,frente);

                float factor=1.0/tan(radians(55.0)*.5);
                float cerca=.1;
                float lejos=250.0;

                float cx=vx*factor/uAspecto;
                float cy=vy*factor;

                float cz=(lejos+cerca)/(cerca-lejos)*vz;
                cz+=2.0*lejos*cerca/(cerca-lejos);

                gl_Position=vec4(
                    cx,
                    cy,
                    cz,
                    -vz
                );
            }
        }
        """;
    }

    private String fragmentShader(){
        return """
        #version 330 core

        uniform vec3 uColor;
        uniform vec3 uAmbiente;
        uniform vec3 uColorLuz;
        uniform vec3 uDireccionLuz;

        uniform float uEmision;

        uniform int uMapa;
        uniform int uNoche;

        // 9 FAROLAS
        uniform vec3 uPostes[9];

        // 2 FAROS DEL VEHICULO
        uniform int uFarosActivos;
        uniform vec3 uFaro1;
        uniform vec3 uFaro2;
        uniform vec3 uDirFaro;

        in vec3 vMundo;
        in vec3 vNormal;

        out vec4 color;

        void main(){

            // MINIMAPA: colores directos
            if(uMapa==1){
                color=vec4(uColor,1);
                return;
            }

            vec3 normal=normalize(vNormal);
            vec3 dir=normalize(-uDireccionLuz);

            float difusa=max(
                dot(normal,dir),
                0.0
            );

            vec3 resultado=
                uAmbiente*uColor+
                difusa*uColorLuz*uColor;

            // ==========================================
            // FAROLAS
            // ==========================================

            if(uNoche==1){
                for(int i=0;i<9;i++){
                    vec3 haciaLuz=uPostes[i]-vMundo;
                    float distancia=length(haciaLuz);

                    if(distancia<20.0){
                        vec3 L=normalize(haciaLuz);

                        float d=max(
                            dot(normal,L),
                            0.0
                        );

                        float atenuacion=
                            1.0/
                            (
                                1.0+
                                .10*distancia+
                                .025*distancia*distancia
                            );

                        resultado+=
                            d*
                            atenuacion*
                            vec3(1.0,.78,.38)*
                            uColor*
                            3.0;
                    }
                }
            }

            // ==========================================
            // FAROS DEL VEHICULO
            // ==========================================

            if(uFarosActivos==1){

                vec3 direccionFaro=
                    normalize(uDirFaro);

                vec3 luces[2]=vec3[2](
                    uFaro1,
                    uFaro2
                );

                for(int i=0;i<2;i++){

                    vec3 desdeFaro=
                        vMundo-luces[i];

                    float distancia=
                        length(desdeFaro);

                    if(distancia<22.0){

                        vec3 direccion=
                            normalize(desdeFaro);

                        float cono=
                            dot(
                                direccion,
                                direccionFaro
                            );

                        if(cono>.72){

                            vec3 haciaFaro=
                                normalize(
                                    luces[i]-vMundo
                                );

                            float dif=
                                max(
                                    dot(
                                        normal,
                                        haciaFaro
                                    ),
                                    0.0
                                );

                            float atenuacion=
                                1.0/
                                (
                                    1.0+
                                    .08*distancia+
                                    .035*
                                    distancia*
                                    distancia
                                );

                            float borde=
                                smoothstep(
                                    .72,
                                    .90,
                                    cono
                                );

                            resultado+=
                                uColor*
                                vec3(1.0,.95,.72)*
                                dif*
                                atenuacion*
                                borde*
                                5.0;
                        }
                    }
                }
            }

            // Ventanas, bombillas y semaforos
            resultado+=uColor*uEmision;

            color=vec4(resultado,1);
        }
        """;
    }
}