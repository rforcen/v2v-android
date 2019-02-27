package Signal;


import android.graphics.Color;

public class ColorScale { 

	  private static float interpolate(float a, float b, float proportion) {
	    return (a + ((b - a) * proportion));
	  }

	  // Returns an interpoloated color, between a and b
	  public static int interpolateColor(int a, int b, float proportion) {
	    float[] hsva = new float[3];
	    float[] hsvb = new float[3];
	    Color.colorToHSV(a, hsva);
	    Color.colorToHSV(b, hsvb);
	    for (int i = 0; i < 3; i++) {
	      hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
	    }
	    return Color.HSVToColor(hsvb);
	  }
	  // in 0..255 range
	  public static int getRed(int color)     {  color&=0x00ffffff; return (color>>16) & 0xff;  }
	  public static int getGreen(int color)   {  color&=0x00ffffff; return (color>>8)  & 0xff;  }
	  public static int getBlue(int color)    {  color&=0x00ffffff; return (color)     & 0xff;  }
	  public static float getAlpha(int color) {  color&=0xff000000; return (color>>24) & 0xff;	}
	  
	  // int 0..1 range, just divide by 255
	  public static float getRedf(int color)   {  return getRed(color)/255f; }
	  public static float getGreenf(int color) {  return getGreen(color)/255f; }
	  public static float getBluef(int color)  {  return getBlue(color)/255f; }
	  public static float getAlphaf(int color)  {  return getAlpha(color)/255f; }
	 

	public static float[]color2fv(int color) {	return new float[]{getRedf(color),getGreenf(color),getBluef(color),getAlphaf(color)};}
}
