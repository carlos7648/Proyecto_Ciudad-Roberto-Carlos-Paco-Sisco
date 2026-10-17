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

public class AppMovimientoTeclado {

    private long window;
    private int program;
    private int vao;
    private int vbo;

    // Variables para el movimiento y límites
    private int offsetLocation;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private static final float VELOCIDAD = 1.0f; // Velocidad de desplazamiento
    private static final float LIMITE = 0.9f;    // Límite para que no salga de la pantalla

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

        window = glfwCreateWindow(800, 600, "Programación Gráfica - Movimiento con Teclado", NULL, NULL);
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

        // Callback para cerrar con la tecla ESCAPE
        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(windowHandle, true);
            }
        });

        setupShaders();
        setupTriangle();
    }

    private void setupShaders() {
        // Vertex Shader con soporte de desplazamiento (uOffset)
        String vertexSrc = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "uniform vec2 uOffset;\n" +
                "void main() {\n" +
                "    vec3 pos = aPos;\n" +
                "    pos.xy += uOffset;\n" +
                "    gl_Position = vec4(pos, 1.0);\n" +
                "}";

        // Fragment Shader (Color verde)
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

        // Obtener la ubicación de la variable uniform 'uOffset' en el shader
        offsetLocation = GL20.glGetUniformLocation(program, "uOffset");
        if (offsetLocation == -1) {
            throw new RuntimeException("No se pudo encontrar el uniform 'uOffset'");
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
        float paso = VELOCIDAD * deltaTime;

        // Movimiento a la izquierda (Flecha izquierda o tecla A)
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            offsetX -= paso;
        }
        // Movimiento a la derecha (Flecha derecha o tecla D)
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            offsetX += paso;
        }
        // Movimiento hacia arriba (Flecha arriba o tecla W)
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            offsetY += paso;
        }
        // Movimiento hacia abajo (Flecha abajo o tecla S)
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            offsetY -= paso;
        }

        // Clamping (límites para que el triángulo no salga de la pantalla)
        offsetX = Math.max(-LIMITE, Math.min(LIMITE, offsetX));
        offsetY = Math.max(-LIMITE, Math.min(LIMITE, offsetY));
    }

    private void loop() {
        float ultimoTiempo = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            // Calcular delta time para un movimiento fluido independiente de los FPS
            float tiempoActual = (float) glfwGetTime();
            float deltaTime = tiempoActual - ultimoTiempo;
            ultimoTiempo = tiempoActual;

            // Procesar entradas del teclado
            processInput(deltaTime);

            // Renderizado
            GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL20.glUseProgram(program);

            // Enviar el offset actualizado al shader
            GL20.glUniform2f(offsetLocation, offsetX, offsetY);

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
        Callbacks.glfwFreeCallbacks(window);
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
        new AppMovimientoTeclado().run();
    }
}