import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Movingine {
	
	private String engine = "";
	public LinkedList<String> movingine = new LinkedList<>();
	private LinkedList<String> tempmovingine = new LinkedList<>();
	
	File file = null;
	BufferedReader bufferedFile = null;
	
	public Movingine (String fileName) {
		file = new File(fileName);
		constructJuice();
	}
	
	private void constructJuice() {
		try {
			bufferedFile = new BufferedReader(new FileReader(file));
			Operando rOperand;
			Operando lOperand;
			String instr;
			Pattern emptyLine = Pattern.compile("^$|^(\\s*(?:(?<comm>[#]\\w*)|[.]\\w*(?:\\s*(?:[.]|[_]))?\\w*|\\w*[:])\\s*)$");
			Pattern instruction = Pattern.compile("^\\s*(?<instruction>\\w+)\\s*(?<body>.*)$");
			
			Matcher instructionMatcher;
			String line = "";
			
			while ((line = bufferedFile.readLine()) != null) {
				instructionMatcher = emptyLine.matcher(line);
				try {
					if (!instructionMatcher.matches()) {
						instructionMatcher = instruction.matcher(line);
						
						if (instructionMatcher.matches()) {
							instr = instructionMatcher.group("instruction");
							
							if (line.contains(",")) {
								String[] split = instructionMatcher.group("body").split(",");
								lOperand = new Operando(split[0]);
								rOperand = (split.length > 1) ? new Operando(split[1]) : null;
							} else {
								lOperand = new Operando(instructionMatcher.group("body"));
								rOperand = null;
							}
							constuctEngine(instr, lOperand, rOperand);
						}
					}
				} catch (Exception e){
					System.out.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * @param istruzione: tipo di istruzione da cui ricaveremo l'equazione
	 * @param leftParam: parametro sinistro dell'istruzione
	 * @param rightParam: parametro destro dell'istruzione
	 */
	
	private void constuctEngine(String istruzione, Operando leftParam, Operando rightParam) {
		switch (istruzione) {
			case "mov":
				movEquations(leftParam, rightParam);
			case "jmp":
			case "jne":
			case "jnz":
				engine = simplyfyEngine(tempmovingine);
				movingine.add(engine);
				engine = "";
		}
	}
	
	/**
	 * @param leftParam
	 * @param rightParams
	 * @return
	 */
	private void movEquations(Operando leftParam, Operando rightParams){
		
		//mov registro, intero
		if (
			leftParam.typeOperation().compareTo("registro") == 0 &&
			rightParams.typeOperation().compareTo("intero") == 0
		) {
			tempmovingine.add(leftParam.registro1 + " = " + rightParams.numero);
			
		//mov registro, registro
		} else if (
			leftParam.typeOperation().compareTo("registro") == 0 &&
			rightParams.typeOperation().compareTo("registro") == 0
		) {
			tempmovingine.add(leftParam.registro1 + " = " + rightParams.registro1 + ".v");
			
		//mov registro, memoria
		} else if (
			leftParam.typeOperation().compareTo("registro") == 0 &&
			rightParams.typeOperation().compareTo("memoria") == 0
		) {
			String registro1 = rightParams.registro1 + ".value";
			String registro2 = (rightParams.registro2 != null) ? rightParams.registro2 + ".v" : "";
			String scalare1  = (rightParams.scalare1 != null) ? rightParams.scalare1 : "";
			String scalare2  = (rightParams.scalare2 != null) ? "*" + rightParams.scalare2 : "";
			
			String result = leftParam.registro1 + " = " +" m(" + registro1 + " + " + registro2 + scalare2 + " + " + scalare1 + " )";
			tempmovingine.add(result);
		} else if(
			leftParam.typeOperation().compareTo("memoria") == 0 &&
			rightParams.typeOperation().compareTo("intero") == 0
		) {
			String registro1 = leftParam.registro1 + ".v";
			String registro2 = (leftParam.registro2 != null) ? leftParam.registro2 + ".v" : "";
			String scalare1  = (leftParam.scalare1 != null) ? leftParam.scalare1 : "";
			String scalare2  = (leftParam.scalare2 != null) ? "*" + leftParam.scalare2 : "";
			
			String result  = "m(" + registro1 + "+" + registro2 + scalare2 + " " + scalare1 + ")" + " = " + rightParams.numero;
			tempmovingine.add(result);
		} else if (
			leftParam.typeOperation().compareTo("memoria") == 0 &&
			rightParams.typeOperation().compareTo("registro") == 0
		) {
			String registro1 = leftParam.registro1 + ".v";
			String registro2 = (leftParam.registro2 != null) ? leftParam.registro2 + ".v" : "";
			String scalare1  = (leftParam.scalare1 != null) ? leftParam.scalare1 : "";
			String scalare2  = (leftParam.scalare2 != null) ? "*" + leftParam.scalare2 : "";
			
			String result  = "m(" + registro1 + "+" + registro2 + scalare2 + " " + scalare1 + ")" + " = " + rightParams.registro1;
			tempmovingine.add(result);
		} else {
			System.out.println("Errore");
		}
	}
	
	public LinkedList<String> getMovingine(){
		//ritorna la linked list corrispondente
		return movingine;
	}
	
	private String simplyfyEngine(LinkedList<String> tmpEngine){
		
		// registro
		String engine = "";
		HashMap<String,String> tempRegister = new HashMap<>();
		Pattern splitOP= Pattern.compile("^(?<leftOp>\\w*\\s*[=])(?<rightOp>\\s*\\w*)$");
		Matcher instructionMatcher;
		
		for (String s:tmpEngine) {
			try {
				instructionMatcher = splitOP.matcher(s);
				String leftOp  = instructionMatcher.group("leftOp");
				String rightOp = instructionMatcher.group("rightOp");
				if (leftOp.contains("m(") || rightOp.contains("m(")) {
					Set set = tempRegister.entrySet();
					Iterator it = set.iterator();
					
					while(it.hasNext()) {
						Map.Entry me = (Map.Entry) it.next();
						engine += me.getKey().toString() + me.getValue().toString() + ";";
					}
					tempRegister = new HashMap<>();
					engine += s +";";
				} else {
					if (tempRegister.containsKey(leftOp)){
						tempRegister.remove(leftOp);
						tempRegister.put(leftOp,rightOp);
					} else {
						tempRegister.put(leftOp, rightOp);
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	return engine;
	}
	
}
