package com.graphics;


import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.glfw.Callbacks;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class App {

    // Variables principales manejadas en la clase del video
    private long window;
    private int program;
    private int vao;
    private int vbo;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // Configurar errores de GLFW a la consola
        GLFWErrorCallback.createPrint(System.err).set();

        // Inicializar GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        // Configurar características de la ventana y versión de OpenGL (3.3)
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Crear la ventana
        window = glfwCreateWindow(800, 600, "Programación Gráfica - Triángulo", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Fallo al crear la ventana de GLFW");
        }

        // Centrar la ventana en la pantalla
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        // Hacer actual el contexto de OpenGL
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Crear capacidades de OpenGL (Crítico para LWJGL)
        GL.createCapabilities();

        // Inicializar shaders y la geometría del triángulo
        setupShaders();
        setupTriangle();
    }

    private void setupShaders() {
        // 1. Vertex Shader (GLSL)
        String vertexSrc = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "void main() {\n" +
                "    gl_Position = vec4(aPos, 1.0);\n" +
                "}";

        // 2. Fragment Shader (GLSL) - Color verde sólido
        String fragmentSrc = "#version 330 core\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "    fragColor = vec4(0.2, 0.8, 0.4, 1.0);\n" +
                "}";

        // Compilar Vertex Shader
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "VERTEX");

        // Compilar Fragment Shader
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "FRAGMENT");

        // Crear Programa y enlazar shaders
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        comprobarPrograma(program);

        // Borrar shaders individuales para liberar memoria
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void setupTriangle() {
        // Coordenadas de los 3 vértices del triángulo (X, Y, Z)
        float[] vertices = {
             0.0f,  0.5f, 0.0f, // Arriba al centro
            -0.5f, -0.5f, 0.0f, // Abajo a la izquierda
             0.5f, -0.5f, 0.0f  // Abajo a la derecha
        };

        // Crear BAO
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // Crear VBO
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        // Convertir array a FloatBuffer apto para OpenGL
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();

        // Enviar datos al buffer de la GPU
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // Configurar atributos de los vértices (layout location = 0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Desenlazar
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void loop() {
        // Bucle principal de dibujado
        while (!glfwWindowShouldClose(window)) {
            // Limpiar pantalla con un tono oscuro de fondo
            GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Usar el programa y el VAO
            GL20.glUseProgram(program);
            GL30.glBindVertexArray(vao);

            // Dibujar los triángulos
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

            // Limpiar estados activos
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);

            // Intercambiar buffers y capturar eventos (teclado, cierre, etc.)
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
        new App().run();
    }
}