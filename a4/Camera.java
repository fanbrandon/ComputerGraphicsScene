package a4;

import org.joml.*;

public class Camera {
    private Vector3f u, v, n;
	private Vector3f defaultU, defaultV, defaultN;
	private Vector3f location, defaultLocation;
	private Matrix4f view, viewR, viewT;

	private Vector3f oldPosition, newPosition, directionVector;
	private Vector3f rightVector, upVector, fwdVector;

	/** instantiates a Camera object */
	public Camera()
	{	defaultLocation = new Vector3f(0.0f, 0.0f, 1.0f);
		defaultU = new Vector3f(1.0f, 0.0f, 0.0f);
		defaultV = new Vector3f(0.0f, 1.0f, 0.0f);
		defaultN = new Vector3f(0.0f, 0.0f, -1.0f);
		location = new Vector3f(defaultLocation);
		u = new Vector3f(defaultU);
		v = new Vector3f(defaultV);
		n = new Vector3f(defaultN);
		view = new Matrix4f();
		viewR = new Matrix4f();
		viewT = new Matrix4f();
	}

    protected Matrix4f getViewMatrix()
	{	viewT.set(1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		-location.x(), -location.y(), -location.z(), 1.0f);

		viewR.set(u.x(), v.x(), -n.x(), 0.0f,
		u.y(), v.y(), -n.y(), 0.0f,
		u.z(), v.z(), -n.z(), 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f);

		view.identity();
		view.mul(viewR);
		view.mul(viewT);

		return(view);
	}

    /** Move camera forward */
	public void ForwardAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getN();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.add(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Move camera backward */
	public void BackAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getN();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.sub(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Move camera right */
	public void RightAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getU();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.add(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Move camera left */
	public void LeftAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getU();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.sub(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Move camera up*/
	public void UpAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getV();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.add(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Move camera up*/
	public void DownAction(float moveSpeed){
		oldPosition = this.getLocation();
		directionVector = this.getV();
		directionVector.mul(moveSpeed);
		newPosition = oldPosition.sub(directionVector.x(),directionVector.y(),directionVector.z());
		this.setLocation(newPosition);
	}

	/** Turn the camera left*/ 
	public void YawLeftAction(float rotationAmount){
		rightVector = this.getU();
		upVector = this.getV();
		fwdVector = this.getN();
		rightVector.rotateAxis(rotationAmount,upVector.x(),upVector.y(),upVector.z());
		fwdVector.rotateAxis(rotationAmount,upVector.x(),upVector.y(),upVector.z());
		this.setU(rightVector);
		this.setN(fwdVector);
	}

	/** Turn the camera left*/ 
	public void YawRightAction(float rotationAmount){
		rightVector = this.getU();
		upVector = this.getV();
		fwdVector = this.getN();
		rightVector.rotateAxis(-rotationAmount,upVector.x(),upVector.y(),upVector.z());
		fwdVector.rotateAxis(-rotationAmount,upVector.x(),upVector.y(),upVector.z());
		this.setU(rightVector);
		this.setN(fwdVector);
	}
	
	/** Turn the camera up */
	public void PitchUpAction(float pitchAmount) {
		rightVector = this.getU(); 
		upVector = this.getV(); 
		fwdVector = this.getN(); 
		upVector.rotateAxis(pitchAmount, rightVector.x(), rightVector.y(), rightVector.z());
		fwdVector.rotateAxis(pitchAmount, rightVector.x(), rightVector.y(), rightVector.z());
		this.setV(upVector);
		this.setN(fwdVector);
	}

	/** Turn the camera down */
	public void PitchDownAction(float pitchAmount) {
		rightVector = this.getU(); 
		upVector = this.getV(); 
		fwdVector = this.getN(); 
		upVector.rotateAxis(-pitchAmount, rightVector.x(), rightVector.y(), rightVector.z());
		fwdVector.rotateAxis(-pitchAmount, rightVector.x(), rightVector.y(), rightVector.z());
		this.setV(upVector);
		this.setN(fwdVector);
	}

	/** Roll the camera to the right  */
	public void RollRightAction(float rotationAmount) {
		rightVector = this.getU(); 
		upVector = this.getV(); 
		fwdVector = this.getN(); 
		rightVector.rotateAxis(rotationAmount, fwdVector.x(), fwdVector.y(), fwdVector.z());
		upVector.rotateAxis(rotationAmount, fwdVector.x(), fwdVector.y(), fwdVector.z());
		this.setU(rightVector);
		this.setV(upVector);
	}

    /** Roll the camera to the left */
	public void RollLeftAction(float rotationAmount) {
		rightVector = this.getU(); 
		upVector = this.getV(); 
		fwdVector = this.getN(); 
		rightVector.rotateAxis(-rotationAmount, fwdVector.x(), fwdVector.y(), fwdVector.z());
		upVector.rotateAxis(-rotationAmount, fwdVector.x(), fwdVector.y(), fwdVector.z());
		this.setU(rightVector);
		this.setV(upVector);
	}

    /** sets the world location of this Camera */
	public void setLocation(Vector3f l) { location.set(l); }

	/** sets the U (right-facing) vector for this Camera */
	public void setU(Vector3f newU) { u.set(newU); }

	/** sets the V (upward-facing) vector for this Camera */
	public void setV(Vector3f newV) { v.set(newV); }

	/** sets the N (forward-facing) vector for this Camera */
	public void setN(Vector3f newN) { n.set(newN); }

	/** returns the world location of this Camera */
	public Vector3f getLocation() { return new Vector3f(location); }

	/** gets the U (right-facing) vector for this Camera */
	public Vector3f getU() { return new Vector3f(u); }

	/** gets the V (upward-facing) vector for this Camera */
	public Vector3f getV() { return new Vector3f(v); }

	/** gets the N (forward-facing) vector for this Camera */
	public Vector3f getN() { return new Vector3f(n); }
}
