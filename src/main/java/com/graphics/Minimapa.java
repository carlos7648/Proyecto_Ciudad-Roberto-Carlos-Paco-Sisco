package com.graphics;

public class Minimapa{

    private boolean visible=true;

    public void alternar(){
        visible=!visible;
    }

    public boolean visible(){
        return visible;
    }

    public int tamanio(int ancho,int alto){
        return Math.max(140,Math.min(ancho,alto)/4);
    }

    public int x(int ancho,int alto){
        return ancho-tamanio(ancho,alto)-15;
    }

    public int y(int ancho,int alto){
        return alto-tamanio(ancho,alto)-15;
    }
}