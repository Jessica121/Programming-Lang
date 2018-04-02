package cop5556sp18;
import cop5556sp18.AST.Declaration;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class SymbolTable {

	int currentScope, nextScope;
	Stack<Integer> scopeStack = new Stack<>();
	HashMap<String, HashMap<Integer, Declaration>> symbolHashMap = new HashMap<>();

	public SymbolTable() {
		nextScope = 0;
		currentScope = 0;
		scopeStack.push(currentScope);
	}
	
	public void enterScope() {
//		currentScope = ++nextScope;
		currentScope = nextScope++;
		scopeStack.push(currentScope);
	}

	public void leaveScope() {
		currentScope = scopeStack.pop();
//		scopeStack.pop();
//		currentScope = scopeStack.peek();
	}

	public boolean insert(String ident, Declaration dec) {
		HashMap<Integer, Declaration> insMap = new HashMap<>();
		if(symbolHashMap.get(ident) == null) insMap.put(currentScope, dec);
		else {
			insMap = symbolHashMap.get(ident);
			if(insMap.get(currentScope) == null) insMap.put(currentScope, dec);
			else return false;
		}
		symbolHashMap.put(ident, insMap);
		return true;
	}
	
	public Declaration lookup(String ident) {
		Map<Integer, Declaration> map = symbolHashMap.get(ident);
		if(map==null || map.size()==0) return null;
		Declaration declar = null;
		List<Integer> scope = new ArrayList<>();
		while(scopeStack.size() != 0) {
			if(map.get(scopeStack.peek()) != null) {
				declar = map.get(scopeStack.peek());
				if(scope.size() != 0)
					for(int i = scope.size()-1; i >= 0; i--) {
						scopeStack.push(scope.get(i));
					}
				break;
			} else scope.add(scopeStack.pop());
		}
		return declar;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, HashMap<Integer, Declaration>> entry : symbolHashMap.entrySet()) {
			Map<Integer, Declaration> map = entry.getValue();
			sb.append(entry.getKey()).append(":").append(map.toString());
		}
		return sb.toString();
	}

}