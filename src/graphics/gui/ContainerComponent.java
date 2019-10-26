package graphics.gui;

import java.util.ArrayList;
import java.util.List;

import graphics.Matrix3f;
import graphics.Vector3f;
import graphics.guiRenderer.GuiShader;

/**
 * Die Basisklasse f�r Komponenten, die andere Komponenten enthalten k�nnen.<br>
 * Die enthaltenen Komponenten werden von dieser Klasse automatisch positioniert, gerendert und am Ende gel�scht, sodass sie als eine Art Einheit betrachtet werden k�nnen - ein Aufruf der entsprechenden Methode dieser Klasse reicht bereits aus.
 * <br><br>
 * Andere Klassen k�nnen diese Klasse erweitern, um auch als Container f�r andere Komponenten dienen zu k�nnen. Dazu k�nnen sie die Methoden dieser Klasse �berschrieben, sollten sie dabei aber auch immer mit <code>super.xyz()</code> wieder aufrufen, damit keine Funktionalit�t verloren geht.
 * 
 * @author Ben
 */
public class ContainerComponent extends GuiComponent {
	
	// Liste mit allen Kind-Komponenten
	private List<GuiComponent> childComponents;
	
	private int innerOffsetX;
	private int innerOffsetY;
	
	private int widthMode = WIDTH_STATIC;
	private int heightMode = HEIGHT_STATIC;
	
	/**
	 * Erstellt einen neuen, leeren ContainerComponent
	 * 
	 * @param width Breite in Pixeln
	 * @param height H�he in Pixeln
	 */
	public ContainerComponent(int width, int height) {
		super(width,height);
		childComponents = new ArrayList<GuiComponent>();
	}
	
	/**
	 * F�gt eine Komponente als Kindelement hinzu
	 * @param component die Komponente
	 */
	public void addComponent(GuiComponent component) {
		childComponents.add(component);
		refreshChildPositions();
	}
	
	/**
	 * Updated die Positionen aller Kindelemente. Sollte immer dann aufgerufen werden, wenn die Gr��e oder Position dieses Elements oder Gr��e, Position oder Offset einer Kindkomponente ge�ndert wurden.
	 */
	protected void refreshChildPositions() {
		Matrix3f baseTransformation = super.getTotalTransform();
		int innerWidth = super.getWidth();
		int innerHeight = super.getHeight();
		int flowX = innerOffsetX;
		int flowY = innerOffsetY;
		int contentWidth = 0;
		int contentHeight = 0;
		for (GuiComponent childComponent:childComponents) {
			// bestimmt die Position des Kindelements innerhalb des Elternelements
			Vector3f positionOffset = new Vector3f(0,0,1);
			int childPosition = childComponent.getPosition();
			if (childPosition==POSITION_CENTER) {
				positionOffset.x = (innerWidth-childComponent.getWidth())/2;
				positionOffset.y = (innerHeight-childComponent.getHeight())/2;
			}else if (childPosition==POSITION_CORNER_TOPLEFT){
				positionOffset.x = childComponent.getOffsetX();
				positionOffset.y = childComponent.getOffsetY();
			}else if (childPosition==POSITION_CORNER_TOPRIGHT) {
				positionOffset.x = innerWidth-childComponent.getWidth()-childComponent.getOffsetX();
				positionOffset.y = childComponent.getOffsetY();
			}else if (childPosition==POSITION_CORNER_BOTTOMRIGHT) {
				positionOffset.x = innerWidth-childComponent.getWidth()-childComponent.getOffsetX();
				positionOffset.y = innerHeight-childComponent.getHeight()-childComponent.getOffsetY();
			}else if (childPosition==POSITION_CORNER_BOTTOMLEFT){
				positionOffset.x = childComponent.getOffsetX();
				positionOffset.y = innerHeight-childComponent.getHeight()-childComponent.getOffsetY();
			}else if (childPosition==POSITION_FLOW){
				positionOffset.x = flowX;
				positionOffset.y = flowY;
				contentWidth = Math.max(contentWidth,flowX+childComponent.getWidth()+innerOffsetX);
				contentHeight = Math.max(contentHeight,flowY+childComponent.getHeight()+innerOffsetY);
				flowY += childComponent.getHeight()+childComponent.getOffsetY();
			}
			// wendet die Transformation des Elternelements darauf an, um die absolute Position zu ermitteln
			positionOffset.apply(baseTransformation);
			Matrix3f transform = baseTransformation.copy();
			transform.m20 = positionOffset.x;
			transform.m21 = positionOffset.y;
			childComponent.setTransform(transform);
		}
		if (widthMode==WIDTH_AUTO&&heightMode==HEIGHT_AUTO) {
			super.setSize(contentWidth,contentHeight);
		}else if (widthMode==WIDTH_AUTO) {
			super.setSize(contentWidth,super.getHeight());
		}else if (heightMode==HEIGHT_AUTO) {
			super.setSize(super.getWidth(),contentHeight);
		}
	}
	
	/**
	 * Passt bei Gr��en�nderungen die Positionen der Kindelemente an. Wird von der Klasse GuiComponent aufgerufen, wenn sich die Gr��e des Elements �ndert.<br>
	 * Diese Methode kann von erweiternden Klassen �berschrieben werden, um eigene Anpassungen an die neue Gr��e vorzunehmen, sollte aber stehts �ber <code>super.onSizeChange()</code> diese Methode auch ausf�hren.
	 */
	protected void onSizeChange() {
		refreshChildPositions();
	}
	
	/**
	 * Passt bei Positions�nderungen die Positionen der Kindelemente an. Wird von der Klasse GuiComponent aufgerufen, wenn sich die Gr��e des Elements �ndert.<br>
	 * Diese Methode kann von erweiternden Klassen �berschrieben werden, um eigene Anpassungen an die neue Position vorzunehmen, sollte aber stehts �ber <code>super.onPositionChange()</code> diese Methode auch ausf�hren.
	 */
	protected void onPositionChange() {
		refreshChildPositions();
	}
	
	/**
	 * Updated alle Kindelemente und nimmt gegebenenfalls eine Neupositionierung der Elemente vor.
	 * <br><br>
	 * Erweiternde Klassen k�nnen diese Methode �berschreiben um eigene Funktionalit�t hinzuzuf�gen, sollten sie dabei aber immer nochmal �ber <code>super.update()</code> aufrufen.
	 */
	public void update() {
		boolean wasChanged = false;
		for (GuiComponent childComponent:childComponents) {
			childComponent.update();
			if (childComponent.wasSizeChanged()) {
				wasChanged = true;
				childComponent.clearChangesBuffer();
			}
		}
		if (wasChanged) {
			refreshChildPositions();
		}
	}
	
	/**
	 * Setzt das "innere Offset" des Elements, also den Abstand, den Flow-Kindelemente zum Rand des Containers haben sollen.
	 * 
	 * @param x Abstand links und rechts in Pixeln
	 * @param y Abstand oben und unten in Pixeln
	 */
	public void setInnerOffset(int x, int y) {
		innerOffsetX = x;
		innerOffsetY = y;
		refreshChildPositions();
	}
	
	/**
	 * Setzt den "Width Mode" des Elements - der bestimmt, ob sich das Element der Breite seines Inhalts anpassen soll oder nicht.<br>
	 * 
	 * @param mode eine der Konstanten <code>WIDTH_STATIC</code> oder <code>WIDTH_AUTO</code>
	 */
	public void setWidthMode(int mode) {
		widthMode = mode;
		refreshChildPositions();
	}
	
	/**
	 * Setzt den "Height Mode" des Elements - der bestimmt, ob sich das Element der H�he seines Inhalts anpassen soll oder nicht.<br>
	 * 
	 * @param mode eine der Konstanten <code>HEIGHT_STATIC</code> oder <code>HEIGHT_AUTO</code>
	 */
	public void setHeightMode(int mode) {
		heightMode = mode;
		refreshChildPositions();
	}
	
	/**
	 * Rendert alle Kindelemente.
	 * <br><br>
	 * Erweiternde Klassen, die zus�tzlich noch etwas anderes wie z.B. einen Hintergrund rendern wollen, k�nnen dazu diese Methode �berschreiben, sollten sie aber dabei nochmal �ber <code>super.render(shader)</code> aufrufen, damit die Kindelemente auch gerendert werden.
	 */
	public void render(GuiShader shader) {
		for (GuiComponent childComponent:childComponents) {
			childComponent.render(shader);
		}
	}
	
	/**
	 * L�scht alle Kindelemente, um Ressourcen freizugeben.
	 * <br><br>
	 * Erweiternde Klassen, die ebenfalls Ressourcen freizugeben haben, k�nnen dazu diese Methode �berschreiben, sollten sie aber dabei nochmal �ber <code>super.destroy()</code> aufrufen.
	 */
	public void destroy() {
		for (GuiComponent childComponent:childComponents) {
			childComponent.destroy();
		}
	}
	
}