package Signal;

import javax.microedition.khronos.opengles.GL10;
import android.graphics.Color;

public class ColorIndex {
	int[]colorIndex; 
	int nColors, colorFrom, colorTo;
	float[]color4f=new float[3]; // rgb in float 0..1
	public ColorIndex() {	createColorIndex(512, Color.RED, Color.BLUE);	}
	public ColorIndex(int nColors, int colorFrom, int colorTo) {	createColorIndex(nColors, colorFrom, colorTo);	}
	public void createColorIndex(int nColors, int colorFrom, int colorTo) {
		this.nColors=nColors; this.colorFrom=colorFrom; this.colorTo=colorTo;	colorIndex=new int[nColors];
		for (int i=0; i<nColors; i++) colorIndex[i]=ColorScale.interpolateColor(colorFrom, colorTo, (float)i/nColors);
	}
	public int getColor(float r) { 	return colorIndex[(int)(nColors*r)%nColors]; }
	public float[]getColorRGBA(float r) { // alpha = 1
		int col=getColor(r);	
		return new float[]{ColorScale.getRedf(col), ColorScale.getGreenf(col), ColorScale.getBluef(col), 1};
	}
	public static int getRed(int color)   {  color&=0x00ffffff; return (color>>16) & 0xff;  }
	public static int getGreen(int color) {  color&=0x00ffffff; return (color>>8)  & 0xff;  }
	public static int getBlue(int color)  {  color&=0x00ffffff; return (color)     & 0xff;  }
	public static float getRedf(int color)   {  return getRed(color)/255f; }
	public static float getGreenf(int color) {  return getGreen(color)/255f; }
	public static float getBluef(int color)  {  return getBlue(color)/255f; }
	public void glColor(GL10 gl, int color) {	gl.glColor4f(getRedf(color), getGreenf(color), getBluef(color), 1);	}
}
