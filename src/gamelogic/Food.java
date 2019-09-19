package gamelogic;

import graphics.Matrix3f;
import graphics.Vector3f;

/**
 * Die Klasse f�r das Essen was die Schlange wachsen l�sst.<br>
 * 
 *  @author Jakopo
 */

public class Food {
	
	public static final float BASE_RADIUS = 0.025f;
	
	public Vector3f foodPosition;
	public float radius;
	public Matrix3f foodRotation;
	
		public Food() {
			this.radius = 0;
			//setzt das Essen auf eine zuf�llige Position
			float a = (float) (Math.random()-0.5);
			float b = (float) (Math.random()-0.5);
			float c = (float) (Math.random()-0.5);
			
			foodPosition = new Vector3f(a,b,c);
			
			foodRotation = new Matrix3f();
			foodRotation.rotate(45, 45, 0);
		}
		//errechnet Distanz zum Essen
		public float distanceTo(Vector3f a) {
			Vector3f temp = a.copy();
			temp.scale(-1);
			temp.add(foodPosition);
			//sorgt f�r Kollision mit Essen aus anderen K�sten
			temp.x = ((temp.x+0.5f)%1+1)%1-0.5f;
			temp.y = ((temp.y+0.5f)%1+1)%1-0.5f;
			temp.z = ((temp.z+0.5f)%1+1)%1-0.5f;
			//gibt Distanz zwischen Kopf und essen zur�ck
			return temp.getLength();
		}
		
		public void update() {
			radius += (BASE_RADIUS-radius)/10;
			foodRotation.rotate(0,0,3);
		}
}
