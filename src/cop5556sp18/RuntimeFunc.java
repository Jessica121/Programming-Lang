package cop5556sp18;

public class RuntimeFunc {
	//sin, cos, atan, log, abs, pow
	public static String className = "cop5556sp18/RuntimeFunc";

	public static String absSig = "(I)I";
	public static int abs(int arg) {
		return Math.abs(arg);
	}
	
	public static String absSig2 = "(F)F";
	public static float abs2(float arg0) {
		return Math.abs(arg0);
	}
	
	public static final String logSig = "(I)I";
	public static int log(int arg0) {
		double l =  Math.round(Math.log(arg0));
		return (int) l;
	}
	
	public static final String logSig2 = "(F)F";
	public static float log2(float arg0) {
		double l =  Math.round(Math.log(arg0));
		return (float) l;
	}
	// float integer, integer float
	public static String powSig = "(II)I";
	public static int pow(int arg0, int arg1) {
		return (int) Math.pow(arg0, arg1);
	}
	
	public static String powSig2 = "(FF)F";
	public static float pow2(float arg0, float arg1) {
		return (float) Math.pow(arg0, arg1);
	}
	
	public static final String atanSig = "(I)I";
	public static int atan(int arg0) {
		double l =  Math.round(Math.atan(arg0));
		return (int) l;
	}
	
	public static final String atanSig2 = "(F)F";
	public static float atan2(float arg0) {
		double l =  Math.atan(arg0);
		return (float) l;
	}
	
	public static final String cosSig = "(I)I";
	public static int cos(int arg0) {
		double l =  Math.round(Math.cos(arg0));
		return (int) l;
	}
	
	public static final String cosSig2 = "(F)F";
	public static float cos2(float arg0) {
		double l =  Math.cos(arg0);
		return (float) l;
	}
	
	public static final String sinSig = "(I)I";
	public static int sin(int arg0) {
		double l =  Math.round(Math.sin(arg0));
		return (int) l;
	}
	
	public static final String sinSig2 = "(F)F";
	public static float sin2(float arg0) {
		double l =  Math.sin(arg0);
		return (float) l;
	}
	
	public static String aSig = "(II)F";
	public static float polar_a(int x, int y) {
		double  a = Math.atan2(y, x);
		return (float) a;
	}

	public static String rSig = "(II)F";
	public static float polar_r(int x, int y) {
		double  r = Math.hypot(x,y);
		return (float) r;
	}
	
	public static String xSig = "(FF)I";
	public static int cart_x(float r, float theta) {
	     double y = r * Math.cos(theta);
	     return (int) y;
	}
	
	public static String ySig = "(FF)I";
	public static int cart_y(float r, float theta) {
		double y =  r * Math.sin(theta);
		return (int) y;
	}
}
