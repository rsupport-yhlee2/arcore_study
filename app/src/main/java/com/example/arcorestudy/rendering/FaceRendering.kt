package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30.*
import android.opengl.GLES30
import android.util.Log
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.*
import com.example.arcorestudy.tools.Mesh
import com.example.arcorestudy.tools.VBOData
import com.example.gllibrary.*
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.size
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.lang.Math.sin
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class FaceRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
) {

    private var faceVertex: FloatBuffer? = null
    private var faceIndices: IntBuffer? = null
    private var facePos: Vec3? = null
    private var faceUVS: FloatBuffer? = null
    private var faceNormals: FloatBuffer? = null
    private var faceQaut: Quat? = null
    private var pose: Pose? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: DataVertex? = null
    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
    }

    fun draw() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, diffuse.getId())
        facePos?.let { vec3 ->
            glBindVertexArray(vertexData!!.getVaoId())
            Log.e("test", "x : ${pose!!.qx()} y : ${pose!!.qy()} z : ${pose!!.qz()}")
            val model = glm.translate(Mat4(), vec3) * glm.rotate(
                Mat4(),
                pose!!.qx() * glm.PIf,
                Vec3(1, 0, 0)
            ) * glm.rotate(Mat4(), pose!!.qy() * glm.PIf, Vec3(0, 1, 0)) * glm.rotate(
                Mat4(),
                pose!!.qz() * glm.PIf,
                Vec3(0, 0, 1)
            )
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(GL_TRIANGLE_STRIP, faceIndices!!.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
        }
        facePos = null
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: IntBuffer,
        pos: Vec3,
        uvs: FloatBuffer,
        normals: FloatBuffer,
        quat: Quat,
        pose: Pose
    ) {
        faceVertex = vertex
        faceIndices = indices
        facePos = pos
        faceUVS = uvs
        faceNormals = normals
        faceQaut = quat
        this.pose = pose
        val buffer = createFloatBuffer(vertex.capacity() + uvs.capacity())
        vertex.position(0)
        uvs.position(0)
        while (vertex.hasRemaining()) {
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(uvs.get())
            buffer.put(1 - uvs.get())
        }
        vertexData = DataVertex(buffer, indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
            bind()
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context): FaceRendering {
            val resource = context.resources
            return FaceRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, R.raw.nose_fur)),
            )
        }
    }
}