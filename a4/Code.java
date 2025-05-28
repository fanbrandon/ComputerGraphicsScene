package a4;

import java.nio.*;
import java.lang.Math;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import java.awt.event.*;
import org.joml.*;

public class Code extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int renderingProgram;
	private int axesProgram;
	private int renderingProgramCubeMap;
	private int shadowProgram;
	private int reflectiveProgram;
	private int renderingProgram3D;
	private int vao[] = new int[1];
	private int vbo[] = new int[25];
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float objLocX, objLocY, objLocZ;
	private int astroTexture,brickTexture,moonTexture,HBU1Texture,planeTexture, skyboxTexture, HBU2Texture, roverTexture;
	private int lineColor = 0;
	private boolean drawAxes = true;
	private Vector3f initialLightLoc = new Vector3f(2.0f, 2.0f, 5.0f);
	
	private float roverAngle = 0.0f;
	
	private Torus myTorus = new Torus(1.0f, 0.5f, 48);
	private int numTorusVertices, numTorusIndices;
	private Vector3f torusLoc = new Vector3f(0.0f, 0.5f, 2.0f);

	private int prevMouseX, prevMouseY, prevMouseZ;
    private boolean isMouseDragging = false;
	
	private int numObjVertices;
	private ImportedModel myModel, myModel2, myModel3, myModel4, ground, myModel5;

	private int numGroundVertices;
	
	private int heightMap;
	

	private Sphere mySphere;
	private int numSphereVerts;

	private Camera cam;
	private Vector3f cameraPos = new Vector3f(0.0f, 3.0f, 10.0f); // Camera position
	private float cameraSpeed = 0.1f; // Movement speed
	private float cameraRotation = 0.1f;

	// VR stuff
	private float IOD = 0.01f;  // tunable interocular distance   we arrived at 0.01 for this scene by trial-and-error
	private float near = 0.01f;
	private float far = 100.0f;
	private int sizeX = 1920, sizeY = 1080;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse transpose
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private int mLoc, pLoc, vLoc, nLoc, sLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private float aspect;
	private double tf;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

	// 3D texture
	private int stripeTexture;
	private int texWidth = 200;
	private int texHeight = 200;
	private int texDepth = 200;
	private double[][][] tex3Dpattern = new double[texWidth][texHeight][texDepth];
		
	
	// white light properties
	float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
	float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		
	// gold material
	float[] silAmb = Utils.silverAmbient();
	float[] silDif = Utils.silverDiffuse();
	float[] silSpe = Utils.silverSpecular();
	float silShi = Utils.silverShininess();

	// plastic white material
	float[] plasticAmb = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	float[] plasticDif = new float[] {0.55f, 0.55f, 0.55f, 1.0f};
	float[] plasticSpe = new float[] {0.70f, 0.70f, 0.70f, 1.0f};
	float plasticShi = 32.0f;

	private float[] thisAmb, thisDif, thisSpe, matAmb, matDif, matSpe;
	private float thisShi, matShi;

	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();


	public Code()
	{	setTitle("CSC155 - Assignment 4");
		setSize(1920, 1080);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		myCanvas.addKeyListener(this);
		myCanvas.addMouseListener(this);
		myCanvas.addMouseMotionListener(this);
		myCanvas.addMouseWheelListener(this);
	}

	private void computePerspectiveMatrix(float leftRight)
	{	float top = (float)Math.tan(1.0472f / 2.0f) * (float)near;
		float bottom = -top;
		float frustumshift = (IOD / 2.0f) * near / far;
		float left = -aspect * top - frustumshift * leftRight;
		float right = aspect * top - frustumshift * leftRight;
		pMat.setFrustum(left, right, bottom, top, near, far);
	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glViewport(0,0,sizeX, sizeY);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);

		gl.glViewport(0, 0, sizeX/2, sizeY);
		scene(-1.0f);

		gl.glViewport(sizeX/2, 0, sizeX/2, sizeY);
		scene(1.0f);
		
	}

	public void scene(float leftRight){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		elapsedTime = System.currentTimeMillis() - startTime;
		tf = elapsedTime / 1000.0;
		roverAngle += 0.003f; // Update rover rotation angle

		// draw cube map
		 
		gl.glUseProgram(renderingProgramCubeMap);

		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		pLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "p_matrix");
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
		
		if (drawAxes){
			// program to render lines
			gl.glUseProgram(axesProgram);
			int useTextureLoc = gl.glGetUniformLocation(renderingProgram, "useTexture");

			// Set up uniforms for axes
			gl.glUniformMatrix4fv(0, 1, false, vMat.get(vals)); 
			gl.glUniformMatrix4fv(1, 1, false, pMat.get(vals)); 

			int lineColorLoc = gl.glGetUniformLocation(axesProgram,"lineColor");
			gl.glProgramUniform1i(axesProgram,lineColorLoc,lineColor);

			gl.glUniform1i(gl.glGetUniformLocation(axesProgram, "lineColor"), 0); // Set lineColor to 0 (X-axis)
			gl.glDrawArrays(GL_LINES, 0, 2); // Draw X-axis (vertices 0 and 1)

			// Draw Y-axis (blue)
			gl.glUniform1i(gl.glGetUniformLocation(axesProgram, "lineColor"), 1); // Set lineColor to 1 (Y-axis)
			gl.glDrawArrays(GL_LINES, 2, 2); // Draw Y-axis (vertices 2 and 3)

			// Draw Z-axis (green)
			gl.glUniform1i(gl.glGetUniformLocation(axesProgram, "lineColor"), 2); // Set lineColor to 2 (Z-axis)
			gl.glDrawArrays(GL_LINES, 4, 2); // Draw Z-axis (vertices 4 and 5)

			gl.glUseProgram(renderingProgram);

		}

		gl.glUseProgram(reflectiveProgram);
    
		// Set up matrices
		int mvLoc = gl.glGetUniformLocation(reflectiveProgram, "mv_matrix");
		int pLoc = gl.glGetUniformLocation(reflectiveProgram, "p_matrix");
		int nLoc = gl.glGetUniformLocation(reflectiveProgram, "norm_matrix");
		
		mMat.identity();
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.scale(0.5f, 0.5f, 0.5f);
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		
		
		
		// Draw torus
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]); // Assuming vbo[1] has torus vertices
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]); // Assuming vbo[2] has torus normals
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[18]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);

		// Render myModel5 with 3D texture
		gl.glUseProgram(renderingProgram3D);

		int mvLoc3D = gl.glGetUniformLocation(renderingProgram3D, "mv_matrix");
		int pLoc3D = gl.glGetUniformLocation(renderingProgram3D, "p_matrix");
		int normLoc3D = gl.glGetUniformLocation(renderingProgram3D, "norm_matrix");
		int lightPosLoc = gl.glGetUniformLocation(renderingProgram3D, "lightPos");

		mMat.identity();
		mMat.translate(5.0f, 10.0f, -5.0f); 
		mMat.scale(0.5f); 

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc3D, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc3D, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(normLoc3D, 1, false, invTrMat.get(vals));
		gl.glUniform3f(lightPosLoc, currentLightPos.x(), currentLightPos.y(), currentLightPos.z());

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, stripeTexture);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel5.getNumVertices());
		
		// Program to render objects
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(0.0f, 0.0f);		//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}


	public void passOne() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(shadowProgram);
	
		// Set up light view and projection matrices
		currentLightPos.set(initialLightLoc);
		lightVmat.identity().setLookAt(currentLightPos, new Vector3f(0,0,0), new Vector3f(0,1,0));
		lightPmat.identity().setPerspective((float)Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	
		// Clear the shadow map
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		// Draw Astronaut model (myModel4)
		mMat.identity();
		float radius = 4.0f;
		float astroX = (float)(radius * Math.cos(roverAngle) + 1.0f);
		float astroZ = (float)(radius * Math.sin(roverAngle) + 1.0f);
		mMat.translate(astroX, 0.0f, astroZ);
		mMat.rotateY(-(roverAngle));
		mMat.scale(0.5f, 0.5f, 0.5f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		int sLoc = gl.glGetUniformLocation(shadowProgram, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel4.getNumVertices());
	
		// Draw HBU1 model (myModel)
		mMat.identity();
		mMat.translate(objLocX, objLocY, objLocZ);
		mMat.scale(0.02f, 0.02f, 0.02f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices());
	
		// Draw HBU2 model (myModel2)
		mMat.identity();
		mMat.translate(0.13f, 0.05f, 0.01f);
		mMat.scale(0.02f, 0.02f, 0.02f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel2.getNumVertices());
	
		// Draw Rover model (myModel3)
		mMat.identity();
		float roverX = (float)(radius * Math.cos(roverAngle));
		float roverZ = (float)(radius * Math.sin(roverAngle));
		mMat.translate(roverX, 0.7f, roverZ);
		mMat.rotateY(-roverAngle);
		mMat.scale(0.3f, 0.3f, 0.3f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel3.getNumVertices());
	
		
	}

	public void passTwo() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(renderingProgram);
		
		// Set up common uniforms
		mLoc = gl.glGetUniformLocation(renderingProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");
		int sLoc = gl.glGetUniformLocation(renderingProgram, "shadowMVP");
		int useHeightMapLoc = gl.glGetUniformLocation(renderingProgram, "useHeightMap");
		
		// Set up shadow texture (on separate unit)
		gl.glActiveTexture(GL_TEXTURE6);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glUniform1i(gl.glGetUniformLocation(renderingProgram, "shadowTex"), 6);
		
		// Set view matrix from camera
		vMat = cam.getViewMatrix();
		
		// Set up lights
		currentLightPos.set(initialLightLoc);
		installLights();
		
		// Draw HBU1 Model
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, plasticAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, plasticDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, plasticSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, plasticShi);
		
		mMat.identity();
		mMat.translate(objLocX, objLocY, objLocZ);
		mMat.scale(0.02f, 0.02f, 0.02f);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, HBU1Texture);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices());
		
		// Draw HBU2 Model
		mMat.identity();
		mMat.translate(0.13f, 0.05f, 0.01f);
		mMat.scale(0.02f, 0.02f, 0.02f);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, HBU2Texture);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel2.getNumVertices());
		
		// Draw Rover Model
		float radius = 4.0f;
		float roverX = (float)(radius * Math.cos(roverAngle));
		float roverZ = (float)(radius * Math.sin(roverAngle));
		
		mMat.identity();
		mMat.translate(roverX, 0.7f, roverZ);
		mMat.rotateY(-roverAngle);
		mMat.scale(0.3f, 0.3f, 0.3f);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, roverTexture);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel3.getNumVertices());
		
		// Draw Astronaut Model
		float astroX = (float)(radius * Math.cos(roverAngle) + 1.0f);
		float astroZ = (float)(radius * Math.sin(roverAngle) + 1.0f);
		
		mMat.identity();
		mMat.translate(astroX, 0.0f, astroZ);
		mMat.rotateY(-(roverAngle));
		mMat.scale(0.5f, 0.5f, 0.5f);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, astroTexture);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel4.getNumVertices());
	
		// Draw Height-Mapped Terrain
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, silAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, silDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, silSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, silShi);
	
		// Enable height mapping for terrain
		gl.glUniform1i(useHeightMapLoc, 1);
		
		
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, heightMap);
		
		mMat.identity();
		mMat.translate(0.0f, 0.0f, 0.0f);
		mMat.scale(10.0f, 1.0f, 10.0f);
	
		shadowMVP2.identity().mul(b).mul(lightPmat).mul(lightVmat).mul(mMat);
		mMat.invert(invTrMat).transpose(invTrMat);
	
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, planeTexture);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numGroundVertices);
		
		// Disable height mapping for other objects
		gl.glUniform1i(useHeightMapLoc, 0);
	
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();
		myModel = new ImportedModel("HDU1.obj");
		myModel2 = new ImportedModel("HDU2.obj");
		myModel3 = new ImportedModel("rover.obj");
		myModel4 = new ImportedModel("astronaut.obj");
		myModel5 = new ImportedModel("maven6.obj");
		ground = new ImportedModel("grid.obj");
		renderingProgram = Utils.createShaderProgram("a4/vertShader.glsl", "a4/fragShader.glsl");
		axesProgram = Utils.createShaderProgram("a4/vert2Shader.glsl","a4/frag2Shader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("a4/vertCShader.glsl", "a4/fragCShader.glsl");
		shadowProgram = Utils.createShaderProgram("a4/vertSshader.glsl", "a4/fragSShader.glsl");
		reflectiveProgram = Utils.createShaderProgram("a4/vertRShader.glsl", "a4/fragRShader.glsl");
		renderingProgram3D = Utils.createShaderProgram("a4/vert3DShader.glsl", "a4/frag3DShader.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupVertices();
		setupShadowBuffers();

		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
		
		cam = new Camera();
		cam.setLocation(cameraPos);

		generate3Dpattern();    
		stripeTexture = load3DTexture();
		
		astroTexture = Utils.loadTexture("astroColor.jpg");
		roverTexture = Utils.loadTexture("roverColor.png");
		planeTexture = Utils.loadTexture("plane.jpg");
		skyboxTexture = Utils.loadCubeMap("cubeMap");
		HBU1Texture = Utils.loadTexture("HDU_01.jpg");
		HBU2Texture = Utils.loadTexture("HDU_02.jpg");
		heightMap = Utils.loadTexture("height.jpg");
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
	}

	private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void installLights()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		numObjVertices = myModel4.getNumVertices();
		Vector3f[] vertices4 = myModel4.getVertices();
		Vector2f[] texCoords4 = myModel4.getTexCoords();
		Vector3f[] normals4 = myModel4.getNormals();
		
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices4[i]).x();
			pvalues[i*3+1] = (float) (vertices4[i]).y();
			pvalues[i*3+2] = (float) (vertices4[i]).z();
			tvalues[i*2]   = (float) (texCoords4[i]).x();
			tvalues[i*2+1] = (float) (texCoords4[i]).y();
			nvalues[i*3]   = (float) (normals4[i]).x();
			nvalues[i*3+1] = (float) (normals4[i]).y();
			nvalues[i*3+2] = (float) (normals4[i]).z();
		}

		numObjVertices = myModel.getNumVertices();
		Vector3f[] vertices = myModel.getVertices();
		Vector2f[] texCoords = myModel.getTexCoords();
		Vector3f[] normals = myModel.getNormals();
		
		float[] p2values = new float[numObjVertices*3];
		float[] t2values = new float[numObjVertices*2];
		float[] n2values = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	p2values[i*3]   = (float) (vertices[i]).x();
			p2values[i*3+1] = (float) (vertices[i]).y();
			p2values[i*3+2] = (float) (vertices[i]).z();
			t2values[i*2]   = (float) (texCoords[i]).x();
			t2values[i*2+1] = (float) (texCoords[i]).y();
			n2values[i*3]   = (float) (normals[i]).x();
			n2values[i*3+1] = (float) (normals[i]).y();
			n2values[i*3+2] = (float) (normals[i]).z();
		}

		numObjVertices = myModel2.getNumVertices();
		Vector3f[] vertices2 = myModel2.getVertices();
		Vector2f[] texCoords2 = myModel2.getTexCoords();
		Vector3f[] normals2 = myModel2.getNormals();
		
		float[] p3values = new float[numObjVertices*3];
		float[] t3values = new float[numObjVertices*2];
		float[] n3values = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	p3values[i*3]   = (float) (vertices2[i]).x();
			p3values[i*3+1] = (float) (vertices2[i]).y();
			p3values[i*3+2] = (float) (vertices2[i]).z();
			t3values[i*2]   = (float) (texCoords2[i]).x();
			t3values[i*2+1] = (float) (texCoords2[i]).y();
			n3values[i*3]   = (float) (normals2[i]).x();
			n3values[i*3+1] = (float) (normals2[i]).y();
			n3values[i*3+2] = (float) (normals2[i]).z();
		}

		numObjVertices = myModel3.getNumVertices();
		Vector3f[] vertices3 = myModel3.getVertices();
		Vector2f[] texCoords3 = myModel3.getTexCoords();
		Vector3f[] normals3 = myModel3.getNormals();
		
		float[] p4values = new float[numObjVertices*3];
		float[] t4values = new float[numObjVertices*2];
		float[] n4values = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	p4values[i*3]   = (float) (vertices3[i]).x();
			p4values[i*3+1] = (float) (vertices3[i]).y();
			p4values[i*3+2] = (float) (vertices3[i]).z();
			t4values[i*2]   = (float) (texCoords3[i]).x();
			t4values[i*2+1] = (float) (texCoords3[i]).y();
			n4values[i*3]   = (float) (normals3[i]).x();
			n4values[i*3+1] = (float) (normals3[i]).y();
			n4values[i*3+2] = (float) (normals3[i]).z();
		}

		int numObjVertices5 = myModel5.getNumVertices();
		Vector3f[] vertices5 = myModel5.getVertices();
		Vector2f[] texCoords5 = myModel5.getTexCoords();
		Vector3f[] normals5 = myModel5.getNormals();

		float[] p5values = new float[numObjVertices5*3];
		float[] t5values = new float[numObjVertices5*2];
		float[] n5values = new float[numObjVertices5*3];

		for (int i=0; i<numObjVertices5; i++) {
			p5values[i*3]   = (float) (vertices5[i]).x();
			p5values[i*3+1] = (float) (vertices5[i]).y();
			p5values[i*3+2] = (float) (vertices5[i]).z();
			t5values[i*2]   = (float) (texCoords5[i]).x();
			t5values[i*2+1] = (float) (texCoords5[i]).y();
			n5values[i*3]   = (float) (normals5[i]).x();
			n5values[i*3+1] = (float) (normals5[i]).y();
			n5values[i*3+2] = (float) (normals5[i]).z();
		}

		
		numGroundVertices = ground.getNumVertices();
		Vector3f[] groundVertices = ground.getVertices();
		Vector2f[] groundTexCoords = ground.getTexCoords();
		Vector3f[] groundNormals = ground.getNormals();
	
		float[] groundpValues = new float[numGroundVertices*3];
		float[] groundtValues = new float[numGroundVertices*2];
		float[] groundnValues = new float[numGroundVertices*3];
	
		for (int i=0; i<numGroundVertices; i++)
		{	groundpValues[i*3]   = (float) (groundVertices[i]).x();
			groundpValues[i*3+1] = (float) (groundVertices[i]).y();
			groundpValues[i*3+2] = (float) (groundVertices[i]).z();
			groundtValues[i*2]   = (float) (groundTexCoords[i]).x();
			groundtValues[i*2+1] = (float) (groundTexCoords[i]).y();
			groundnValues[i*3]   = (float) (groundNormals[i]).x();
			groundnValues[i*3+1] = (float) (groundNormals[i]).y();
			groundnValues[i*3+2] = (float) (groundNormals[i]).z();
		}

		// Cube Map
		float[] cubeVertexPositions =
			{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
				1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
				1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
				-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
				1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
			};
		
		// torus
	
		numTorusVertices = myTorus.getNumVertices();
		numTorusIndices = myTorus.getNumIndices();
	
		Vector3f[] tvertices = myTorus.getVertices();
		Vector3f[] tnormals = myTorus.getNormals();
		int[] tindices = myTorus.getIndices();
		
		float[] tpvalues = new float[vertices.length*3];
		float[] tnvalues = new float[normals.length*3];

		for (int i=0; i<numTorusVertices; i++)
		{	tpvalues[i*3]   = (float) tvertices[i].x();
			tpvalues[i*3+1] = (float) tvertices[i].y();
			tpvalues[i*3+2] = (float) tvertices[i].z();
			tnvalues[i*3]   = (float) tnormals[i].x();
			tnvalues[i*3+1] = (float) tnormals[i].y();
			tnvalues[i*3+2] = (float) tnormals[i].z();
		}


		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		// Buffer for cubeMap
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);

		//---------- Astronaut Model Buffer ----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		//---------- Model 1 Buffer ----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer vert2Buf = Buffers.newDirectFloatBuffer(p2values);
		gl.glBufferData(GL_ARRAY_BUFFER, vert2Buf.limit()*4, vert2Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer tex2Buf = Buffers.newDirectFloatBuffer(t2values);
		gl.glBufferData(GL_ARRAY_BUFFER, tex2Buf.limit()*4, tex2Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer nor2Buf = Buffers.newDirectFloatBuffer(n2values);
		gl.glBufferData(GL_ARRAY_BUFFER, nor2Buf.limit()*4,nor2Buf, GL_STATIC_DRAW);

		//--------- Model 2 Buffer ----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer vert3Buf = Buffers.newDirectFloatBuffer(p3values);
		gl.glBufferData(GL_ARRAY_BUFFER, vert3Buf.limit()*4, vert3Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer tex3Buf = Buffers.newDirectFloatBuffer(t3values);
		gl.glBufferData(GL_ARRAY_BUFFER, tex3Buf.limit()*4, tex3Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer nor3Buf = Buffers.newDirectFloatBuffer(n3values);
		gl.glBufferData(GL_ARRAY_BUFFER, nor3Buf.limit()*4,nor3Buf, GL_STATIC_DRAW);

		//--------- Model 3 Buffer ----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer vert4Buf = Buffers.newDirectFloatBuffer(p4values);
		gl.glBufferData(GL_ARRAY_BUFFER, vert4Buf.limit()*4, vert4Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer tex4Buf = Buffers.newDirectFloatBuffer(t4values);
		gl.glBufferData(GL_ARRAY_BUFFER, tex4Buf.limit()*4, tex4Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer nor4Buf = Buffers.newDirectFloatBuffer(n4values);
		gl.glBufferData(GL_ARRAY_BUFFER, nor4Buf.limit()*4,nor4Buf, GL_STATIC_DRAW);
		
		//---------- Plane Buffer ----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer groundBuf = Buffers.newDirectFloatBuffer(groundpValues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundBuf.limit()*4, groundBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        FloatBuffer groundTexBuf = Buffers.newDirectFloatBuffer(groundtValues);
        gl.glBufferData(GL_ARRAY_BUFFER, groundTexBuf.limit() * 4, groundTexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
        FloatBuffer groundNorBuf = Buffers.newDirectFloatBuffer(groundnValues);
        gl.glBufferData(GL_ARRAY_BUFFER, groundNorBuf.limit() * 4, groundNorBuf, GL_STATIC_DRAW);

		// Torus Buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		FloatBuffer torBuf = Buffers.newDirectFloatBuffer(tpvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torBuf.limit()*4, torBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		FloatBuffer torNorBuf = Buffers.newDirectFloatBuffer(tnvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torNorBuf.limit()*4, torNorBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[18]);
		IntBuffer idxBuf = Buffers.newDirectIntBuffer(tindices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit()*4, idxBuf, GL_STATIC_DRAW);

		// Model 5 buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);  // Assuming vbo[19] is available
		FloatBuffer vert5Buf = Buffers.newDirectFloatBuffer(p5values);
		gl.glBufferData(GL_ARRAY_BUFFER, vert5Buf.limit()*4, vert5Buf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);  // Next available index
		FloatBuffer tex5Buf = Buffers.newDirectFloatBuffer(t5values);
		gl.glBufferData(GL_ARRAY_BUFFER, tex5Buf.limit()*4, tex5Buf, GL_STATIC_DRAW);

		
	}

	private void generate3Dpattern() {
		for (int x=0; x<texWidth; x++) {
			for (int y=0; y<texHeight; y++) {
				for (int z=0; z<texDepth; z++) {
					if ((y/10)%2 == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
				}
			}
		}
	}

	private void fillDataArray(byte data[]) {
		for (int i=0; i<texWidth; i++) {
			for (int j=0; j<texHeight; j++) {
				for (int k=0; k<texDepth; k++) {
					if (tex3Dpattern[i][j][k] == 1.0) {
						// yellow color
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 255; //red
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 255; //green
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 0;   //blue
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
					}
					else {
						// blue color
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 0;   //red
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 0;   //green
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 255; //blue
						data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
					}
				}
			}
		}
	}

	private int load3DTexture() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		byte[] data = new byte[texWidth*texHeight*texDepth*4];
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, texWidth, texHeight, texDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				texWidth, texHeight, texDepth, GL_RGBA, GL_UNSIGNED_BYTE, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);

		return textureID;
	}
	


	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_W) {
			// Move forward (along -cameraN)
			cam.ForwardAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_S){
			cam.BackAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_D){
			cam.RightAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_A){
			cam.LeftAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_Q){
			cam.UpAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_E){
			cam.DownAction(cameraSpeed);
		}
		if (key == KeyEvent.VK_LEFT){
			cam.YawLeftAction(cameraRotation);
		}
		if (key == KeyEvent.VK_RIGHT){
			cam.YawRightAction(cameraRotation);
		}
		if (key == KeyEvent.VK_UP){
			cam.PitchUpAction(cameraRotation);
		}
		if (key == KeyEvent.VK_DOWN){
			cam.PitchDownAction(cameraRotation);
		}
		if (key == KeyEvent.VK_C){
			cam.RollRightAction(cameraRotation);
		}
		if (key == KeyEvent.VK_Z){
			cam.RollLeftAction(cameraRotation);
		}
		if (key == KeyEvent.VK_SPACE){
			drawAxes = !drawAxes;
		}
		if (key == KeyEvent.VK_ESCAPE){
			System.exit(0);
		}
		
	}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

		@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			prevMouseX = e.getX();
			prevMouseY = e.getY();
			isMouseDragging = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			isMouseDragging = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isMouseDragging) {
			int deltaX = e.getX() - prevMouseX;
			int deltaY = e.getY() - prevMouseY;
			prevMouseX = e.getX();
			prevMouseY = e.getY();

			// Update light position based on mouse movement
			initialLightLoc.x += deltaX * 0.01f;
			initialLightLoc.y -= deltaY * 0.01f; 
			
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getWheelRotation();
		initialLightLoc.z += amount;
		
	}

	// Other required mouse listener methods (can be empty)
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseMoved(MouseEvent e) {}
		

	public static void main(String[] args) { new Code(); }

	public void dispose(GLAutoDrawable drawable) {}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		setupShadowBuffers();
	}
}