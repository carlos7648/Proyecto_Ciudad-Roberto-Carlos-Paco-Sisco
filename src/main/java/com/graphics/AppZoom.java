package com.graphics;
import org.lwjgl.glfw.Callbacks;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AppZoom {

    private long window;
    private int program;
    private int vao;
    private int vbo;

    // Variables para el manejo del Zoom
    private int zoomLocation;
    private float zoom = 1.0f; // Escala inicial
    private static final float VELOCIDAD_ZOOM = 1.25f;
    private static final float ZOOM_MIN = 0.25f;
    private static final float ZOOM_MAX = 3.0f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(800, 600, "Programación Gráfica - Zoom", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Fallo al crear la ventana de GLFW");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // 1. Callback para cerrar con ESCAPE
        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(windowHandle, true);
            }
        });

        // 2. Callback para el Scroll del Mouse (Zoom con la rueda)
        glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
            zoom += (float) yoffset * 0.10f;
            zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        });

        setupShaders();
        setupTriangle();
    }

    private void setupShaders() {
        // Vertex Shader adaptado para recibir el uniform 'uZoom' y escalar las posiciones
        String vertexSrc = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "uniform float uZoom;\n" +
                "void main() {\n" +
                "    vec3 pos = aPos;\n" +
                "    pos.xy *= uZoom;\n" +
                "    gl_Position = vec4(pos, 1.0);\n" +
                "}";

        String fragmentSrc = "#version 330 core\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "    fragColor = vec4(0.2, 0.8, 0.4, 1.0);\n" +
                "}";

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "VERTEX");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "FRAGMENT");

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        comprobarPrograma(program);

        // Obtener la ubicación del uniform 'uZoom'
        zoomLocation = GL20.glGetUniformLocation(program, "uZoom");
        if (zoomLocation == -1) {
            throw new RuntimeException("No se encontró el uniform 'uZoom'");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void setupTriangle() {
        float[] vertices = {
             0.0f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();

        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void processInput(float deltaTime) {
        float paso = VELOCIDAD_ZOOM * deltaTime;

        // Zoom Acercar con tecla EQUAL / PLUS (+)
        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS) {
            zoom += paso;
        }
        // Zoom Alejar con tecla MINUS (-)
        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
            zoom -= paso;
        }

        // Aplicar límites (Clamping)
        zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void loop() {
        float ultimoTiempo = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            float tiempoActual = (float) glfwGetTime();
            float deltaTime = tiempoActual - ultimoTiempo;
            ultimoTiempo = tiempoActual;

            processInput(deltaTime);

            GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL20.glUseProgram(program);

            // Enviar el valor actual de zoom al shader
            GL20.glUniform1f(zoomLocation, zoom);

            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        GL20.glDeleteProgram(program);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);

        Callbacks.glfwFreeCallbacks(window);
        
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al compilar el shader " + tipo + ": " + GL20.glGetShaderInfoLog(shader));
        }
    }

    private void comprobarPrograma(int program) {
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar el programa: " + GL20.glGetProgramInfoLog(program));
        }
    }

    public static void main(String[] args) {
        new AppZoom().run();
    }
}