package net.solvetheriddle.openrndr.experiments.from_others
import org.openrndr.application
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.Shader
import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.internal.Driver

fun main() = application {
    configure {
        width = 640
        height = 480
    }

    program {
        val shader = Shader.createFromCode(
            vsCode = """
                    #version 330

                    in vec3 position;
                    in vec2 texCoord;

                    out vec2 vTexCoord;

                    void main() {
                        gl_Position = vec4(position, 1.0);
                        vTexCoord = texCoord;
                    }
                """,
            fsCode = """
                    #version 330

                    uniform vec2 resolution;
                    uniform float time;
                    uniform vec4 color1;
                    uniform vec4 color2;

                    vec4 gradient(float x, float y) {
                        return mix(color1, color2, length(vec2(x, y)));
                    }

                    in vec2 vTexCoord;
                    out vec4 oColor;

                    void main() {
                        vec2 pos = vTexCoord;
                        float aspect = resolution.x / resolution.y;
                        vec2 center = vec2(0.5, 0.5 / aspect);

                        if (pos.x < 0.5 && pos.y < 0.5 / aspect) {
                            oColor = gradient(pos.x, pos.y) * vec4(1.0, 0.0, 0.0, 1.0);
                        } else if (pos.x >= 0.5 && pos.y < 0.5 / aspect) {
                            oColor = gradient(1.0 - pos.x, pos.y) * vec4(0.0, 1.0, 0.0, 1.0);
                        } else if (pos.x < 0.5 && pos.y >= 0.5 / aspect) {
                            oColor = gradient(pos.x, 1.0 - pos.y) * vec4(0.0, 0.0, 1.0, 1.0);
                        } else {
                            oColor = gradient(1.0 - pos.x, 1.0 - pos.y) * vec4(1.0, 1.0, 0.0, 1.0);
                        }
                    }
                """,
            name = "gradient_shader"
        )

        val vertexBuffers = mutableListOf<VertexBuffer>()

        val bufferWidth: Float = width / 2F
        val bufferHeight: Float = height / 2F

        for (x in 0 until 2) {
            for (y in 0 until 2) {
                val vertexBuffer = VertexBuffer.createDynamic(
                    vertexFormat {
                        position(3)
                        textureCoordinate(2)
                    }, 6
                )
                vertexBuffer.put {
                    write(x * bufferWidth, y * bufferHeight, 0F)
                    write(0F, 0F)

                    write(x * bufferWidth + bufferWidth, y * bufferHeight, 0F)
                    write(1F, 0F)

                    write(x * bufferWidth + bufferWidth, y * bufferHeight + bufferHeight, 0F)
                    write(1F, 1F)

                    write(x * bufferWidth, y * bufferHeight + bufferHeight, 0F)
                    write(0F, 1F)

                    write(x * bufferWidth, y * bufferHeight, 0F)
                    write(0F, 0F)

                    write(x * bufferWidth + bufferWidth, y * bufferHeight + bufferHeight, 0F)
                    write(1F, 1F)
                }
                vertexBuffers.add(vertexBuffer)
            }
        }

        extend {

            Driver.instance.drawVertexBuffer(
                shader,
                vertexBuffers,
                DrawPrimitive.TRIANGLES,
                0,
                6
            )

//            drawer.rectangle(Rectangle.fromCenter(Vector2(width / 4.0, height / 4.0), width / 2.0, height / 2.0))
//            drawer.rectangle(Rectangle.fromCenter(Vector2(width * 3 / 4.0, height / 4.0), width / 2.0, height / 2.0))
//            drawer.rectangle(Rectangle.fromCenter(Vector2(width / 4.0, height * 3 / 4.0), width / 2.0, height / 2.0))
//            drawer.rectangle(Rectangle.fromCenter(Vector2(width * 3 / 4.0, height * 3 / 4.0), width / 2.0, height / 2.0))
        }
    }
}
