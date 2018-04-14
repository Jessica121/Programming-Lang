package cop5556sp18;

import cop5556sp18.AST.Declaration;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

public class Types {

	public static enum Type {
		INTEGER, BOOLEAN, IMAGE, FLOAT, FILE, NONE;
	}

	public static Type getType(Kind kind) {
		switch (kind) {
		case KW_int: {
			return Type.INTEGER;
		}
		case KW_boolean: {
			return Type.BOOLEAN;
		}
		case KW_image: {
			return Type.IMAGE;
		}
		case KW_filename: {
			return Type.FILE;
		}
		case KW_float: {
			return Type.FLOAT;
		}
		default:
			break;
		}
		// should not reach here
		assert false: "invoked getType with Kind that is not a type"; 
		return null;
	}
	
	public static String getJVMType(Type typeName) {
		String localType = new String();
		switch (typeName) {
		case INTEGER:
			localType = "I";
			break;
		case FLOAT:
			localType = "F";
			break;
		case BOOLEAN:
			localType = "Z";
			break;
		case IMAGE:
			localType = "Ljava/awt/image/BufferedImage;";
			break;
		case FILE:
			localType = "Ljava/lang/String;";
			break;
		default:
			break;
	}
		return localType;
	}
	
}
