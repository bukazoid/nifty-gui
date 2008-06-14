package de.lessvoid.nifty.render;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.lessvoid.nifty.tools.Color;

/**
 * The Nifty RenderEngine.
 * @author void
 */
public class NiftyRenderEngine implements RenderEngine {

  /**
   * RenderDevice.
   */
  private RenderDevice renderDevice;

  /**
   * global position x.
   */
  private float globalPosX = 0;

  /**
   * global position y.
   */
  private float globalPosY = 0;

  /**
   * current x position.
   */
  private float currentX = 0;

  /**
   * current y position.
   */
  private float currentY = 0;

  /**
   * current color.
   */
  private Color color;

  /**
   * color changed.
   */
  private boolean colorChanged = false;

  /**
   * current imageScale.
   */
  private float imageScale = 1.0f;

  /**
   * current textScale.
   */
  private float textScale = 1.0f;

  /**
   * font cache.
   */
  private Map < String, RenderFont > fontCache = new Hashtable < String, RenderFont >();

  /**
   * stack to save data.
   */
  private Stack < Set < RenderStateImpl > > stack = new Stack < Set < RenderStateImpl > >();

  /**
   * renderStates mapping.
   */
  private EnumMap < RenderState, Class < ? extends RenderStateImpl > > renderStatesMap =
    new EnumMap < RenderState, Class < ? extends RenderStateImpl > >(RenderState.class);

  /**
   * create the device.
   * @param renderDeviceParam RenderDevice
   */
  public NiftyRenderEngine(final RenderDevice renderDeviceParam) {
    renderDevice = renderDeviceParam;
    renderStatesMap.put(RenderState.color, RenderStateColor.class);
    renderStatesMap.put(RenderState.imageScale, RenderStateImageScale.class);
    renderStatesMap.put(RenderState.position, RenderStatePosition.class);
    renderStatesMap.put(RenderState.textSize, RenderStateTextSize.class);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#getWidth()
   * @return width
   */
  public int getWidth() {
    return renderDevice.getWidth();
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#getHeight()
   * @return height
   */
  public int getHeight() {
    return renderDevice.getHeight();
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#clear()
   */
  public void clear() {
    renderDevice.clear();
    colorChanged = false;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#createImage(java.lang.String, boolean)
   * @param filename name
   * @param filterLinear filter
   * @return RenderImage
   */
  public RenderImage createImage(final String filename, final boolean filterLinear) {
    return renderDevice.createImage(filename, filterLinear);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#createFont(java.lang.String)
   * @param filename name
   * @return RenderFont
   */
  public RenderFont createFont(final String filename) {
    if (fontCache.containsKey(filename)) {
      return fontCache.get(filename);
    } else {
      RenderFont font = renderDevice.createFont(filename);
      fontCache.put(filename, font);
      return font;
    }
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#renderQuad(int, int, int, int)
   * @param x x
   * @param y y
   * @param width width
   * @param height height
   */
  public void renderQuad(final int x, final int y, final int width, final int height) {
    renderDevice.renderQuad(x + getX(), y + getY(), width, height, color);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#renderImage(de.lessvoid.nifty.render.RenderImage, int, int, int, int)
   * @param image image
   * @param x x
   * @param y y
   * @param width width
   * @param height height
   */
  public void renderImage(final RenderImage image, final int x, final int y, final int width, final int height) {
    image.render(x + getX(), y + getY(), width, height, color, imageScale);
  }

  /**
   * @see
   * de.lessvoid.nifty.render.RenderEngine#renderText(de.lessvoid.nifty.render.RenderFont, java.lang.String, int, int)
   * @param font font
   * @param text text
   * @param x x
   * @param y y
   * @param selectionStart selection start
   * @param selectionEnd selection end
   * @param textSelectionColor textSelectionColor
   */
  public void renderText(
      final RenderFont font,
      final String text,
      final int x,
      final int y,
      final int selectionStart,
      final int selectionEnd,
      final Color textSelectionColor) {
    if (isSelection(selectionStart, selectionEnd)) {
      renderSelectionText(
          font, text, x + getX(), y + getY(), color, textSelectionColor, textScale, selectionStart, selectionEnd);
    } else {
      font.render(text, x + getX(), y + getY(), color, textScale);
    }
  }

  /**
   * Render a Text with some text selected.
   * @param font font
   * @param text text
   * @param x x
   * @param y y
   * @param textColor color
   * @param textSelectionColor textSelectionColor
   * @param textSize text size
   * @param selectionStartParam selection start
   * @param selectionEndParam selection end
   */
  protected void renderSelectionText(
      final RenderFont font,
      final String text,
      final int x,
      final int y,
      final Color textColor,
      final Color textSelectionColor,
      final float textSize,
      final int selectionStartParam,
      final int selectionEndParam) {
    int selectionStart = selectionStartParam;
    int selectionEnd = selectionEndParam;
    if (selectionStart < 0) {
      selectionStart = 0;
    }
    if (selectionEnd < 0) {
      selectionEnd = 0;
    }

    if (isEverythingSelected(text, selectionStart, selectionEnd)) {
      font.render(text, x, y, textSelectionColor, textSize);
    } else if (isSelectionAtBeginning(selectionStart)) {
      String selectedString = text.substring(selectionStart, selectionEnd);
      String unselectedString = text.substring(selectionEnd);

      font.render(selectedString, x, y, textSelectionColor, textSize);
      font.render(unselectedString, x + font.getWidth(selectedString), y, textColor, textSize);
    } else if (isSelectionAtEnd(text, selectionEnd)) {
      String unselectedString = text.substring(0, selectionStart);
      String selectedString = text.substring(selectionStart, selectionEnd);

      font.render(unselectedString, x, y, textColor, textSize);
      font.render(selectedString, x + font.getWidth(unselectedString), y, textSelectionColor, textSize);
    } else {
      String unselectedString1 = text.substring(0, selectionStart);
      String selectedString = text.substring(selectionStart, selectionEnd);
      String unselectedString2 = text.substring(selectionEnd, text.length());

      font.render(unselectedString1, x, y, textColor, textSize);
      int unselectedString1Len = font.getWidth(unselectedString1);
      font.render(selectedString, x + unselectedString1Len, y, textSelectionColor, textSize);
      int selectedStringLen = font.getWidth(selectedString);
      font.render(unselectedString2, x + unselectedString1Len + selectedStringLen, y, textColor, textSize);
    }
  }

  /**
   * Returns true of selection is at the end of the string.
   * @param text text
   * @param selectionEnd selection end
   * @return true or false
   */
  private boolean isSelectionAtEnd(final String text, final int selectionEnd) {
    return selectionEnd == text.length();
  }

  /**
   * Returns true if selection starts at the beginning.
   * @param selectionStart selection start
   * @return true or false
   */
  private boolean isSelectionAtBeginning(final int selectionStart) {
    return selectionStart == 0;
  }

  /**
   * Returns true when everything is selected.
   * @param text text
   * @param selectionStart selection start
   * @param selectionEnd selection end
   * @return true when everything is selected
   */
  private boolean isEverythingSelected(final String text, final int selectionStart, final int selectionEnd) {
    return isSelectionAtBeginning(selectionStart) && isSelectionAtEnd(text, selectionEnd);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#setColor(de.lessvoid.nifty.tools.Color)
   * @param colorParam color
   */
  public void setColor(final Color colorParam) {
    color = colorParam;
    colorChanged = true;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#isColorChanged()
   * @return color changed
   */
  public boolean isColorChanged() {
    return colorChanged;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#moveTo(float, float)
   * @param xParam x
   * @param yParam y
   */
  public void moveTo(final float xParam, final float yParam) {
    this.currentX = xParam;
    this.currentY = yParam;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#enableClip(int, int, int, int)
   * @param x0 x0
   * @param y0 y0
   * @param x1 x1
   * @param y1 y1
   */
  public void enableClip(final int x0, final int y0, final int x1, final int y1) {
    renderDevice.enableClip(x0, y0, x1, y1);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#disableClip()
   */
  public void disableClip() {
    renderDevice.disableClip();
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#setRenderTextSize(float)
   * @param size size
   */
  public void setRenderTextSize(final float size) {
    this.textScale = size;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#setImageScale(float)
   * @param scale scale
   */
  public void setImageScale(final float scale) {
    this.imageScale = scale;
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#setGlobalPosition(float, float)
   * @param xPos x
   * @param yPos y
   */
  public void setGlobalPosition(final float xPos, final float yPos) {
    globalPosX = xPos;
    globalPosY = yPos;
  }

  /**
   * get x.
   * @return x
   */
  private int getX() {
    return (int) (globalPosX + currentX);
  }

  /**
   * get y.
   * @return y
   */
  private int getY() {
    return (int) (globalPosY + currentY);
  }

  /**
   * has selection.
   * @param selectionStart selection start
   * @param selectionEnd selection end
   * @return true or false
   */
  private boolean isSelection(final int selectionStart, final int selectionEnd) {
    return !(selectionStart == -1 && selectionEnd == -1);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#saveState(java.util.Set)
   * @param statesToSave states to save
   */
  public void saveState(final Set < RenderState > statesToSave) {
    Set < RenderStateImpl > renderStateImpl = new HashSet < RenderStateImpl >();

    for (RenderState state : statesToSave) {
      try {
        Class < ? extends RenderStateImpl > clazz = renderStatesMap.get(state);
        renderStateImpl.add(clazz.getConstructor(new Class[] {NiftyRenderEngine.class }).newInstance(this));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    stack.push(renderStateImpl);
  }

  /**
   * @see de.lessvoid.nifty.render.RenderEngine#restoreState()
   */
  public void restoreState() {
    Set < RenderStateImpl > renderStateImpl = stack.pop();

    for (RenderStateImpl impl : renderStateImpl) {
      impl.restore();
    }
  }

  /**
   * RenderStatePositionImpl.
   * @author void
   */
  public final class RenderStatePosition implements RenderStateImpl {

    /**
     * saved x.
     */
    private float x;

    /**
     * saved y.
     */
    private float y;

    /**
     * store this state.
     */
    public RenderStatePosition() {
      this.x = NiftyRenderEngine.this.currentX;
      this.y = NiftyRenderEngine.this.currentY;
    }

    /**
     * restore this state.
     */
    public void restore() {
      NiftyRenderEngine.this.currentX = this.x;
      NiftyRenderEngine.this.currentY = this.y;
    }
  }

  /**
   * RenderStateColor.
   * @author void
   */
  public final class RenderStateColor implements RenderStateImpl {
    /**
     * Color.
     */
    private Color color;

    /**
     * color changed.
     */
    private boolean colorChanged;

    /**
     * save.
     */
    public RenderStateColor() {
      this.color = NiftyRenderEngine.this.color;
      this.colorChanged = NiftyRenderEngine.this.colorChanged;
    }

    /**
     * restore.
     */
    public void restore() {
      NiftyRenderEngine.this.color = color;
      NiftyRenderEngine.this.colorChanged = colorChanged;
    }
  }

  /**
   * RenderStateTextSize.
   * @author void
   */
  public final class RenderStateTextSize implements RenderStateImpl {

    /**
     * textSize.
     */
    private float textSize;

    /**
     * save.
     */
    public RenderStateTextSize() {
      this.textSize = NiftyRenderEngine.this.textScale;
    }

    /**
     * restore.
     */
    public void restore() {
      NiftyRenderEngine.this.textScale = this.textSize;
    }
  }

  /**
   * RenderStateImageScale.
   * @author void
   */
  public final class RenderStateImageScale implements RenderStateImpl {

    /**
     * imageScale.
     */
    private float imageScale;

    /**
     * save.
     */
    public RenderStateImageScale() {
      this.imageScale = NiftyRenderEngine.this.imageScale;
    }

    /**
     * restore.
     */
    public void restore() {
      NiftyRenderEngine.this.imageScale = this.imageScale;
    }
  }
}