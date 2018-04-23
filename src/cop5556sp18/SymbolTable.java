package cop5556sp18;
import cop5556sp18.AST.Declaration;
import java.util.*;

public class SymbolTable {

	int currentScope, nextScope;
	Map<String, Map<Integer, Declaration>> sybolMap;
	Stack<Integer> scopSta;

	public SymbolTable() {
		sybolMap = new HashMap<>();
		scopSta = new Stack<>();
		currentScope = 0;
		nextScope = 0;
		scopSta.push(currentScope);
	}
	
	public void enterScope() {
		currentScope = nextScope++;
		scopSta.push(currentScope);
	}

	public void leaveScope() {
		currentScope = scopSta.pop();
		currentScope = scopSta.peek();
	}

	public Declaration lookup(String ident) {
		Map<Integer, Declaration> map = sybolMap.get(ident);
		if(map == null || map.size() == 0) return null;
		List<Integer> list = new ArrayList<>();
		Declaration dec = null;
		while(scopSta.size() != 0) {
			if(map.containsKey(scopSta.peek())) {
				dec = map.get(scopSta.peek());
				if(list.size() != 0) {
					for(int k = list.size() - 1; k >= 0; k--) {
						scopSta.push(list.get(k));
					}
				}
				break;
			} else {
				list.add(scopSta.pop());
			}
		}
		return dec;
	}
	
	public boolean insert(String ident, Declaration dec) {
		Map<Integer, Declaration> map = new HashMap<>();
		if(!sybolMap.containsKey(ident)) {
			map.put(currentScope, dec);
		} else {
			map = sybolMap.get(ident);
			if(!map.containsKey(currentScope)) {
				map.put(currentScope, dec);
			} else return false;
		}
		sybolMap.put(ident, map);
		return true;
	}
}