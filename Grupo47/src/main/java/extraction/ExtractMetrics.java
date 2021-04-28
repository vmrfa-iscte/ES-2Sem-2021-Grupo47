package extraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import classes.MethodIdentity;
import classes.MethodMetrics;
import classes.Metrics;
import classes.NameByFile;

/**
 * @author Vasco Fontoura
 * @author Tomás Mendes
 *
 */
public class ExtractMetrics {

	private NameByFile classNameByFile = new NameByFile();
	private MetricParser metricParser = new MetricParser();
	private int method_id;

	/**
	 * Todas as métricas são inicializadas a 0 com exceção da complexidade ciclomática que é inicializada a 1.
	  	Os contadores utilizados são inicializados também a 0.
	 */
	public static final int METRICS_INITIAL_VALUE = 0, COUNTER_INITIAL_VALUE = 0, COMPLEXITY_INITIAL_VALUE = 1; 
	public static final String SRC_DIR = "src", DEFAULT_PACKAGE = "Default Package", EMPTY_STRING = "", SPACE_STRING = " ", IF_CYCLE = "if",
			FOR_CYCLE = "for", WHILE_CYCLE = "while", CASE_CYCLE = "case", ELSE = "else", JAVA_FILE = ".java", DEFAULT_CYCLE = "default";
	private String className = EMPTY_STRING;
	private int LOC_method,CYCLO_method,LOC_class,NOM_class,WMC_class = METRICS_INITIAL_VALUE;
	private String packageClass;
	private ArrayList<MethodMetrics> extractedMetrics;
	

	/**
	 * @param file um ficheiro java para extrair métricas
	 */
	public ExtractMetrics(File file) {
		classNameByFile.setFileToExtract(file);
		this.packageClass = classNameByFile.getPackageName();
	}

	/**
	 * Obtém o código e extrai métricas do ficheiro dado no construtor e guarda na lista dada
	 * @param extractedMetrics lista de métodos com métricas para guardar os resultados da extração
	 * @param method_id id do último método na lista de métodos com métricas
	 * @return a lista dada com novos resultados acrescentados
	 * @throws FileNotFoundException
	 */
	public ArrayList<MethodMetrics> doExtractMetrics(ArrayList<MethodMetrics> extractedMetrics,int method_id) throws FileNotFoundException {
		this.method_id = method_id;
		this.extractedMetrics = extractedMetrics;
		// Obter o código do ficheiro 'FileToExtract' através do objeto JavaParser
		JavaParser parseCodeFromFile  = new JavaParser();
		ParseResult<CompilationUnit> compilationUnitFromParser = parseCodeFromFile.parse(classNameByFile.getFileToExtract()); 
		if(compilationUnitFromParser.isSuccessful())
			extract(compilationUnitFromParser);
		return extractedMetrics;
	}

	/**
	 * @param compilationUnitFromParser código do ficheiro traduzido por JavaParser
	 * @throws FileNotFoundException
	 */
	private void extract(ParseResult<CompilationUnit> compilationUnitFromParser) throws FileNotFoundException {
		// Cria objeto CompilatioUnit para obter a tradução do código
		CompilationUnit actualCompilationUnit = compilationUnitFromParser.getResult().get();
		//Obtém todas as métricas de todos os métodos de todas as classes (classes, interfaces ou enumerados)
		findAllClassMetricsFromFile(actualCompilationUnit);
		findAllEnumMetricsFromFile(actualCompilationUnit);
	}
	
	/**
	 * Extrai as métricas de class para todas as classes do ficheiro
	 * @param actualCompilationUnit código do ficheiro traduzido por JavaParser
	 * @throws FileNotFoundException
	 */
	private void findAllClassMetricsFromFile(CompilationUnit actualCompilationUnit) throws FileNotFoundException {
		// Percorrer todas as classes e interfaces dentro do ficheiro dado
		for(ClassOrInterfaceDeclaration classTypeFromParser : actualCompilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
			// Obter nome, complexidade ciclomática, número de métodos e número de linhas da classe
			className = classNameByFile.getClassName(classTypeFromParser);
			WMC_class = metricParser.getClassComplexity(classTypeFromParser.getMethods(),classTypeFromParser.getConstructors());
			NOM_class = metricParser.getNOM_class(classTypeFromParser.getMethods(),classTypeFromParser.getConstructors());
			LOC_class = metricParser.getLOC_classFromClass(classTypeFromParser);
			// Adicionar construtores e métodos juntamente com as métricas associadas, à lista de métricas extraídas
			searchConstructorsForClass(classTypeFromParser);
			searchMethodsForClass(classTypeFromParser);
		}
	}
	
	/**
	 * Extrai as métricas de class para todos os Enumerados do ficheiro
	 * @param actualCompilationUnit código do ficheiro traduzido por JavaParser
	 */
	private void findAllEnumMetricsFromFile(CompilationUnit actualCompilationUnit) {
		// Percorrer todos os enumerdos dentro do ficheiro
		for(EnumDeclaration enumTypeFromParser : actualCompilationUnit.findAll(EnumDeclaration.class)) {
			// Obter nome, complexidade ciclomática, número de métodos e número de linhas do enumerado
			className = classNameByFile.getClassNameForEnum(enumTypeFromParser);
			WMC_class = metricParser.getClassComplexity(enumTypeFromParser.getMethods(),enumTypeFromParser.getConstructors());
			NOM_class = enumTypeFromParser.getMethods().size() + enumTypeFromParser.getConstructors().size();
			// Adicionar construtores e métodos juntamente com as métricas associadas à lista de métricas extraídas
			LOC_class = metricParser.getLOC_classFromEnum(enumTypeFromParser);
			searchConstructorsForEnum(enumTypeFromParser);
			searchMethodsForEnum(enumTypeFromParser);
		}
	}

	/**
	 * Percorre todos os métodos na classe Enumerado dada
	 * @param enumTypeFromParser uma classe Enumerado no ficheiro dado
	 */
	private void searchMethodsForEnum(EnumDeclaration enumTypeFromParser) {
		// Percorrer todos os métodos dentro do enumerado
		for(MethodDeclaration methodFromEnum: enumTypeFromParser.getMethods()) {
			extractMethodMetricsForEnum(methodFromEnum);
			// Incrementar method_id
			method_id++;
		}
	}

	/**
	 * Extrai as métricas method para o método dado
	 * @param methodFromEnum um método de uma classe Enumerado
	 */
	private void extractMethodMetricsForEnum(MethodDeclaration methodFromEnum) {
		// Extrair linhas de código e complexidade ciclomática através do objeto metricParser
		LOC_method = metricParser.getLOC_methodMethod(methodFromEnum);
		CYCLO_method = metricParser.getMethodComplexity(methodFromEnum);
		// Criar objeto Metrics e adicioná-lo à lista
		String methodNameWithParameters = getMethodNameWithParameters(methodFromEnum.getNameAsString(), methodFromEnum.getParameters());
		createMetricsAndAdd(methodNameWithParameters);
	}

	
	/**
	 * Percorre todos os métodos na classe Enumerado dada e extrai métricas
	 * @param enumTypeFromParser uma classe Enumerado no ficheiro dado
	 */
	private void searchConstructorsForEnum(EnumDeclaration enumTypeFromParser) {
		// Percorrer todos os construtores dentro do enumerado
		for(ConstructorDeclaration constructorFromEnum: enumTypeFromParser.getConstructors()) {
			extractConstructorsMetricsForEnum(constructorFromEnum);
			//Incrementar method_id
			method_id++;
		}
	}

	/**
	 * Extrai as métricas method para o construtor dado
	 * @param constructorFromEnum um construtor vindo de uma classe Enumerado
	 */
	private void extractConstructorsMetricsForEnum(ConstructorDeclaration constructorFromEnum) {
		// Extrair linhas de código e complexidade ciclomática através do objeto metricParser
		LOC_method = metricParser.getLOC_methodConstructor(constructorFromEnum);
		CYCLO_method = metricParser.getConstructorComplexity(constructorFromEnum);
		// Criar objeto Metrics e adicioná-lo à lista
		String constructorNameWithParameters = getMethodNameWithParameters(constructorFromEnum.getNameAsString(), constructorFromEnum.getParameters());
		createMetricsAndAdd(constructorNameWithParameters);
	}

	/**
	 * Percorre todos os construtores da classe e extrai métricas
	 * @param classTypeFromParser uma classe
	 */
	private void searchConstructorsForClass(ClassOrInterfaceDeclaration classTypeFromParser) {
		// Percorrer todos os construtores dentro da classe
		for(ConstructorDeclaration constructorFromClass: classTypeFromParser.getConstructors()) {
			extractConstructorMetrics(constructorFromClass);
			//Incrementar method_id
			method_id++;
		}
	}

	/**
	 * Extrai as métricas method para o construtor dado
	 * @param constructorFromClass um dado construtor
	 */
	private void extractConstructorMetrics(ConstructorDeclaration constructorFromClass) {
		// Extrair linhas de código e complexidade ciclomática do construtor
		LOC_method = metricParser.getLOC_methodConstructor(constructorFromClass);
		CYCLO_method = metricParser.getConstructorComplexity(constructorFromClass);
		// Criar objeto Metrics com todas as métricas obtidas da classe e do método
		String constructorNameWithParameters = getMethodNameWithParameters(constructorFromClass.getNameAsString(),constructorFromClass.getParameters());
		createMetricsAndAdd(constructorNameWithParameters);
	}
	
	/**
	 * Procura os métodos dentro da classe dada e extrai as métricas
	 * @param classTypeFromParser uma dada classe
	 */
	private void searchMethodsForClass(ClassOrInterfaceDeclaration classTypeFromParser) {
		// Percorrer todos os métodos dentro da classe
		for(MethodDeclaration methodFromClass: classTypeFromParser.getMethods()) {
			extractMethodMetrics(methodFromClass);
			//Incrementar method_id
			method_id++;
		}
	}

	/**
	 * Extrai as métricas method de um dado método
	 * @param methodFromClass um dado método
	 */
	private void extractMethodMetrics(MethodDeclaration methodFromClass) {
		// Extrair linhas de código e complexidade ciclomática através do objeto metricParser
		LOC_method = metricParser.getLOC_methodMethod(methodFromClass);
		CYCLO_method = metricParser.getMethodComplexity(methodFromClass);
		// Criar objeto Metrics e adicioná-lo à lista
		String methodNameWithParameters = getMethodNameWithParameters(methodFromClass.getNameAsString(), methodFromClass.getParameters());
		createMetricsAndAdd(methodNameWithParameters);
	}
	
	/**
	 * @param methodName o nome de um dado método
	 * @param nodeList uma lista com os parâmetros do método dado
	 * @return Retorna o nome do método com os parâmetros dados
	 */
	private String getMethodNameWithParameters(String methodName,NodeList<Parameter> nodeList) {
		// Adicionar os parâmetros ao nome do método para evitar confusões com outros métodos com o mesmo nome e parâmetros diferentes
		// Se não tiver parâmetros então o nome do método será nomeMetodo()
		if(nodeList.isEmpty()) return methodName+ "()";
		// Se tiver parâmetros é necessário abrir parênteses e adicionar o tipo dos parâmetros
		else {
			methodName = methodName+ "(";
			return addParametersToClassName(methodName,nodeList);
		}
	}
	
	/**
	 * @param methodName nome de um dado método
	 * @param parametersList uma lista com os parâmetros do método dado
	 * @return o nome do método com os parâmetros dados
	 */
	private String addParametersToClassName(String methodName,NodeList<Parameter> parametersList) {
		// Percorrer lista de parâmetros, para obter apenas o tipo dos parâmetros é necessário separar os Nodes e ir buscar apenas a primeira posição
		for(Node parameter: parametersList) {
			String separatedParameter[] = parameter.toString().split(SPACE_STRING);
			String parameterType = separatedParameter[0];
			// Caso a posição atual da lista seja a última então será adicionado o tipo do parâmetros juntamente com o parênteses que faltava
			if(parametersList.indexOf(parameter) == parametersList.size()-1) methodName = methodName + parameterType + ")";
			// Se ainda não for a última posição da lista adiciona-se o tipo do parâmetro e uma vírgula para posteriormente adicionar mais um tipo de parâmetro
			else methodName = methodName + parameterType + ",";
		}
		return methodName;
	}

	/**
	 * Cria um objeto MethodMetrics com o nome dado e com as métricas extraidas pelos outros métodos e adicona à lista de resultados.
	 * @param methodName nome de um dado método
	 */
	private void createMetricsAndAdd(String methodName) {
		MethodIdentity currentMethod = new MethodIdentity(methodName, className, packageClass, method_id);
		Metrics metricsForMethod = new Metrics(LOC_method, LOC_class, CYCLO_method, NOM_class, WMC_class);
		MethodMetrics metricToAdd = new MethodMetrics(currentMethod,metricsForMethod);
		extractedMetrics.add(metricToAdd);
	}
	
	/**
	 * @return o id do método após extração das métricas
	 */
	public int getCurrentMethodID() {
		return method_id;
	}
}