package detection;


import java.util.ArrayList;
import java.util.HashMap;

import classes.HasCodeSmell;
import classes.MethodMetrics;
import classes.Rule;
import gui.mainGUI;

/**
 * Classe responsavel por avaliar uma regra e detetar corretamente as metricas
 * @author Vasco Fontoura
 * @author Tomas Mendes
 * @version 3
 *
 */
public class EvaluateAndDetect {
	private static final String LOC_CLASS = "LOC_class", NOM_CLASS = "NOM_class", WMC_CLASS = "WMC_class",
			LOC_METHOD = "LOC_method";
	
	private static final String IS_LONG_METHOD_DETECTION = "IsLong Method Detection";
	private static final String IS_GOD_CLASS_DETECTION = "IsGod Class Detection";
	private ArrayList<MethodMetrics> actualmetrics;
	private DetectionChooser chooser = new DetectionChooser();

	/**
	 * Getter para as metricas dadas
	 * @return as metricas definidas para a classe
	 */
	public ArrayList<MethodMetrics> getActualmetrics() {
		return actualmetrics;
	}

	/**
	 * Setter para as metricas dadas
	 * @param actualmetrics uma dada lista de metricas
	 */
	public void setActualmetrics(ArrayList<MethodMetrics> actualmetrics) {
		this.actualmetrics = actualmetrics;
	}

	/**
	 * Metodo responsavel por escolher a avaliacao adequada perante uma dada regra
	 * @param thisCurrentRule uma dada regra
	 * @return um mapa com os resultados
	 */
	public HashMap<String,ArrayList<HasCodeSmell>> evaluationChooser(Rule thisCurrentRule) {
		if (thisCurrentRule.getMethod1().equals(LOC_METHOD))
			return evaluateLocMethod(thisCurrentRule);
		else if (isWMCAndNOM(thisCurrentRule))
			return evaluateGodClassWithWMC_NOM(thisCurrentRule);
		else if (isWMCAndLOC(thisCurrentRule))
			return evaluateGodClassWithWMC_LOC(thisCurrentRule);
		else if (isNOMAndLOC(thisCurrentRule))
			return evaluateGodClassWithNOM_LOC(thisCurrentRule);
		else if (isGodClass(thisCurrentRule))
			return evaluateGodClassWithWMC_NOM_LOC(thisCurrentRule);
		return null;
	}

	/**
	 * Verifica, perante uma dada regra, se se trata de code smell isGod Class (WMC_class,NOM_class e LOC_class)
	 * @param thisCurrentRule uma dada regra
	 * @return um boleano com o resultado da verificacao
	 */
	public boolean isGodClass(Rule thisCurrentRule) {
		return thisCurrentRule.getMethod1().equals(WMC_CLASS) && thisCurrentRule.getMethod2().equals(NOM_CLASS) && thisCurrentRule.getMethod3().contains("a");
	}

	/**
	 * Verifica, perante uma dada regra, se se trata das metricas NOM_class e LOC_class
	 * @param thisCurrentRule uma dada regra
	 * @return um boleano com o resultado da verificacao
	 */
	public boolean isNOMAndLOC(Rule thisCurrentRule) {
		return thisCurrentRule.getMethod1().equals(NOM_CLASS) && thisCurrentRule.getMethod2().equals(LOC_CLASS) && !thisCurrentRule.getMethod3().contains("a");
	}

	/**
	 * Verifica, perante uma dada regra, se se trata das metricas WMC_class e LOC_class
	 * @param thisCurrentRule uma dada regra
	 * @return um boleano com o resultado da verificacao
	 */
	private boolean isWMCAndLOC(Rule thisCurrentRule) {
		return thisCurrentRule.getMethod1().equals(WMC_CLASS) && thisCurrentRule.getMethod2().equals(LOC_CLASS) 
				&& !thisCurrentRule.getMethod3().contains("a");
	}

	/**
	 * Verifica, perante uma dada regra, se se trata das metricas NOM_class e NOM_class
	 * @param thisCurrentRule uma dada regra
	 * @return um boleano com o resultado da verificacao
	 */
	private boolean isWMCAndNOM(Rule thisCurrentRule) {
		return thisCurrentRule.getMethod1().equals(WMC_CLASS) && thisCurrentRule.getMethod2().equals(NOM_CLASS)
				&& !thisCurrentRule.getMethod3().contains("a");
	}

	// Os seguintes métodos escolhem a deteção adequada perante a regra escolhida
	/**
	 * Realiza a detecao de code smells de uma dada regra para code smell isLong method
	 * @param ruleReceived uma dada regra
	 * @return um mapa com os resultados da detecao de code smells
	 */
	public HashMap<String,ArrayList<HasCodeSmell>> evaluateLocMethod(Rule ruleReceived) {
		HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = new HashMap<String,ArrayList<HasCodeSmell>>();
		ArrayList<HasCodeSmell> hasCodeSmellResult = chooser.chooseDetectionLocMethod(ruleReceived, actualmetrics);
		nameAndResults.put(IS_LONG_METHOD_DETECTION,hasCodeSmellResult);
		return nameAndResults;
	}

	/**
	 * Realiza a detecao de code smells de uma dada regra com as metricas WMC_class e NOM_class
	 * @param ruleReceived uma dada regra
	 * @return um mapa com os resultados da detecao de code smells
	 */
	public HashMap<String, ArrayList<HasCodeSmell>> evaluateGodClassWithWMC_NOM(Rule ruleReceived) {
		HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = new HashMap<String,ArrayList<HasCodeSmell>>();
		ArrayList<HasCodeSmell> hasCodeSmellResult = chooser.chooseDetectionWMC_NOM(ruleReceived, actualmetrics);
		nameAndResults.put(IS_GOD_CLASS_DETECTION,hasCodeSmellResult);
		return nameAndResults;
	}

	/**
	 * Realiza a detecao de code smells de uma dada regra com as metricas WMC_class e LOC_class
	 * @param ruleReceived uma dada regra
	 * @return um mapa com os resultados da detecao de code smells
	 */
	public HashMap<String, ArrayList<HasCodeSmell>> evaluateGodClassWithWMC_LOC(Rule ruleReceived) {
		HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = new HashMap<String,ArrayList<HasCodeSmell>>();
		ArrayList<HasCodeSmell> hasCodeSmellResult = chooser.chooseDetectionWMC_LOC(ruleReceived, actualmetrics);
		nameAndResults.put(IS_GOD_CLASS_DETECTION,hasCodeSmellResult);
		return nameAndResults;
	}

	/**
	 * Realiza a detecao de code smells de uma dada regra com as metricas NOM_class e LOC_class
	 * @param ruleReceived uma dada regra
	 * @return um mapa com os resultados da detecao de code smells
	 */
	public HashMap<String, ArrayList<HasCodeSmell>> evaluateGodClassWithNOM_LOC(Rule ruleReceived) {
		HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = new HashMap<String,ArrayList<HasCodeSmell>>();
		ArrayList<HasCodeSmell> hasCodeSmellResult = chooser.chooseDetectionNOM_LOC(ruleReceived, actualmetrics);
		nameAndResults.put(IS_GOD_CLASS_DETECTION,hasCodeSmellResult);
		return nameAndResults;
	}

	/**
	 * Realiza a detecao de code smells de uma dada regra com as metricas WMC_class, NOM_class e LOC_class
	 * @param ruleReceived uma dada regra
	 * @return um mapa com os resultados da detecao de code smells
	 */
	public HashMap<String, ArrayList<HasCodeSmell>> evaluateGodClassWithWMC_NOM_LOC(Rule ruleReceived) {
		HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = new HashMap<String,ArrayList<HasCodeSmell>>();
		ArrayList<HasCodeSmell> hasCodeSmellResult = chooser.chooseDetectionWMC_NOM_LOC(ruleReceived, actualmetrics);
		nameAndResults.put(IS_GOD_CLASS_DETECTION,hasCodeSmellResult);
		return nameAndResults;
	}
}