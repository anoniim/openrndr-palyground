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
                    
                    in vec3 position;
                    in vec2 vTexCoord;
                    
                    uniform float time;
                    
                    out vec4 oColor;
                    
                    void main() {
                      vec2 uv = vTexCoord.xy;
                      vec2 center = vec2(0.5, 0.5);
                      vec2 dir = normalize(center - uv);
                      float dist = length(center - uv) * 2.0;
                      float wave = sin(dist * 3.0 - time * 2.0) * 0.5 + 0.5;
                      vec3 color = vec3(0.0);
                      if (wave < 0.25) {
                        color = vec3(1.0, 0.0, 0.0);
                      } else if (wave < 0.5) {
                        color = vec3(1.0, 1.0, 0.0);
                      } else if (wave < 0.75) {
                        color = vec3(0.0, 1.0, 0.0);
                      } else {
                        color = vec3(0.0, 0.0, 1.0);
                      }
                      oColor = vec4(color, 1.0);
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
