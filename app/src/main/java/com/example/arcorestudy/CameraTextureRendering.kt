package com.example.arcorestudy

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30.*
import com.example.gllibrary.*
import com.google.ar.core.Frame
import java.nio.FloatBuffer

class CameraTextureRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene(){

    private val mVertices: FloatBuffer
    private val mTexCoords: FloatBuffer
    private val mTexCoordsTransformed: FloatBuffer
    private val cameraTexture = CameraTexture()
    private lateinit var program: Program

    init {
        mVertices = createFloatBuffer(QUAD_COORDS.size * java.lang.Float.SIZE / 8)
        mVertices.put(QUAD_COORDS)
        mVertices.position(0)
        mTexCoords = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)
        mTexCoords.put(QUAD_TEXCOORDS)
        mTexCoords.position(0)
        mTexCoordsTransformed = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)

    }

    override fun draw() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture.getId())
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)
        cameraTexture.load()
        program = Program.create(vertexShaderCode, fragmentShaderCode)
        VertexData.applyAttributes(program.getAttributeLocation("aPosition"),3,mVertices)
        VertexData.applyAttributes(program.getAttributeLocation("aTexCoord"),2,mTexCoordsTransformed)
        program.use()
    }

    val textureId: Int
        get() = cameraTexture.getId()

    fun transformDisplayGeometry(frame: Frame) {
        frame.transformDisplayUvCoords(mTexCoords, mTexCoordsTransformed)
    }

    companion object {
        private val QUAD_COORDS =
            floatArrayOf(
                -1.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f
            )
        private val QUAD_TEXCOORDS = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        fun create(context: Context): CameraTextureRendering{
            val resource = context.resources
            return CameraTextureRendering(
                resource.readRawTextFile(R.raw.camera_vertex),
                resource.readRawTextFile(R.raw.camera_fragment)
            )
        }
    }
}