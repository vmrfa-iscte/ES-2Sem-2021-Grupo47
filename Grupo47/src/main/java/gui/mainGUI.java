package gui;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.wb.swt.SWTResourceManager;

import classes.HasCodeSmell;
import classes.NameByFile;
import classes.Rule;
import classes.Statistics;
import detection.EvaluateAndDetect;
import excel.ExcelManip;
import extraction.DirExplorer;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * 
 * Classe que permite ao utilizador extrarir as metricas de um ficheiro, definir
 * e manusear regras a sua vontade e realizar a detecao de codesmells consoante
 * as regras criadas pelo mesmo
 * 
 * @author Guy Turpin
 * @author Tomas Mendes
 * @author Vasco Fontoura
 * @author Rita Silva
 * @version 16
 *
 */
public class mainGUI extends Shell {

	private static final String SELECT_SRC_MESSAGE = "Selecione a pasta 'src'";
	private static final String EXTRACT_PROJECT_MESSAGE = "Escolha um projeto e extraia métricas",
			ALREADY_EMPTY_MESSAGE = "Lista de regras já vazia", INVALID_FILE_MESSAGE = "Ficheiro inválido",
			EMPTY_RULE_LIST_MESSAGE = "Lista de regras vazia",
			INCORRECT_FIELDS = "Preencha corretamente todos os campos", NO_RULE_SELECTED = "Nenhuma regra selecionada",
			REPEATED_RULE = "Regra já imposta.", FIELDS_INCORRECT_MESSAGE = "Preencha corretamente todos os campos.",
			DEFAULT_LIMIT_TEXT = "Limite", INVALID_LIMITS = "Limites inválidos!",
			FOLDER_SELECTION_ERROR = "Escolha uma pasta", CYCLO_METHOD = "CYCLO_method",
			LOGO = "/G47/Grupo47/iscte_logo2.jpg", GUI_NAME = "Interface gráfica- Grupo 47";
	private static final String NO_METRICS_EXTRACTED_ERROR_MESSAGE = "Não foram extraidas métricas ou não existe nenhum projeto selecionado",
			NO_RULE_SELECTED_ERROR_MESSAGE = NO_RULE_SELECTED;
	private static final int NUMBER_OF_PACKAGES_INDEX = 3, NOM_INDEX = 1, NUMBER_OF_CLASSES_INDEX = 2, LOC_INDEX = 0;
	private static final String LOC_CLASS = "LOC_class", NOM_CLASS = "NOM_class", WMC_CLASS = "WMC_class",
			LOC_METHOD = "LOC_method";
	private EvaluateAndDetect evaluateAndDetect = new EvaluateAndDetect();
	private int rulesToShowSelectedIndex, index;
	private Menu menuWithButtons;
	private MenuItem guiInstructionsButton, metricInfoButton;
	private List excelfiles, listrulestoshow;
	private Text projectFolderPath, numOfClasses, numOfPackages, numOfMethods, numOfLines, firstLimit, secondLimit,
			thirdLimit, folderToSavePath;
	private Composite composite;
	private Button defineRuleButton, savehistory, projectSelection, extractMetricsButton, changeRuleButton,
			detectSmellsButton, loadHistoryButton, cleanHistoryList, viewFileButton, choosePathToExtract;
	private File selectedFile = null, folderextraction = null, history;
	private HashMap<String, ArrayList<String>> mapStats = new HashMap<>();
	private ArrayList<Rule> list = new ArrayList<Rule>();
	private ArrayList<File> fileList = new ArrayList<File>();
	private Rule rule, currentRule;
	private Combo thirdMetric, firstSignal, secondSignal, thirdSignal, secondOperator, firstMetric, secondMetric,
			firstOperator;
	private String ruleToShowInList, update;

	/**
	 * Create the shell.
	 * 
	 * @param display display
	 */
	public mainGUI(Display display) {
		super(display, SWT.SHELL_TRIM);
		setMinimumSize(new Point(170, 47));
		setImage(SWTResourceManager.getImage(mainGUI.class, LOGO));
		addElements(display);
		setLayout(null);
		createContents();
	}

	/**
	 * Adiciona os elementos a GUI
	 * 
	 * @param display display
	 */
	private void addElements(Display display) {
		projectFolderPath = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		projectFolderPath.setBounds(10, 67, 345, 26);

		projectSelection = new Button(this, SWT.NONE);
		addProjectSelectionListener();
		projectSelection.setBounds(372, 67, 166, 28);
		projectSelection.setText("Selecionar projeto (src)");

		excelfiles = new List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		excelfilesListener();

		excelfiles.setBounds(10, 108, 345, 164);

		composite = new Composite(this, SWT.NONE);
		composite.setBounds(10, 314, 670, 465);

		Text numOfMethodText = new Text(this, SWT.BORDER);
		numOfMethodText.setEditable(false);
		numOfMethodText.setText("Número de Métodos");
		numOfMethodText.setBounds(372, 198, 166, 26);

		numOfClasses = new Text(this, SWT.BORDER);
		numOfClasses.setEditable(false);
		numOfClasses.setBounds(544, 109, 136, 26);

		Text numOfClassesText = new Text(this, SWT.BORDER);
		numOfClassesText.setEditable(false);
		numOfClassesText.setText("Número de Classes");
		numOfClassesText.setBounds(372, 109, 166, 26);

		Text numOfLinesText = new Text(this, SWT.BORDER);
		numOfLinesText.setEditable(false);
		numOfLinesText.setText("Número de Linhas");
		numOfLinesText.setBounds(372, 246, 166, 26);

		Text numOfPackagesText = new Text(this, SWT.BORDER);
		numOfPackagesText.setEditable(false);
		numOfPackagesText.setText("Número de Packages");
		numOfPackagesText.setBounds(372, 152, 166, 26);

		numOfPackages = new Text(this, SWT.BORDER);
		numOfPackages.setEditable(false);
		numOfPackages.setBounds(544, 152, 136, 26);

		numOfMethods = new Text(this, SWT.BORDER);
		numOfMethods.setEditable(false);
		numOfMethods.setBounds(544, 198, 136, 26);

		numOfLines = new Text(this, SWT.BORDER);
		numOfLines.setEditable(false);
		numOfLines.setBounds(544, 246, 136, 26);

		extractMetricsButton = new Button(this, SWT.NONE);
		extractMetricsButton.setBounds(544, 48, 136, 30);
		extractMetricsListener();
		extractMetricsButton.setText("Extrair métricas");

		firstMetric = new Combo(composite, SWT.READ_ONLY);
		firstMetricListener();
		firstMetric.setBounds(10, 58, 155, 28);
		firstMetric.setText("");
		firstMetric.add(LOC_METHOD);
		firstMetric.add(WMC_CLASS);
		firstMetric.add(NOM_CLASS);

		firstOperator = new Combo(composite, SWT.READ_ONLY);
		firstOperator.setBounds(440, 58, 117, 28);
		firstOperator.setText("");
		firstOperator.add("OR");
		firstOperator.add("AND");

		secondMetric = new Combo(composite, SWT.READ_ONLY);
		secondMetricListener();
		secondMetric.setBounds(10, 92, 155, 28);
		secondMetric.setText("");

		thirdMetric = new Combo(composite, SWT.READ_ONLY);
		thirdMetric.setBounds(10, 126, 155, 28);
		thirdMetric.setText("");
		thirdMetric.add("");
		thirdMetric.add(LOC_CLASS);
		thirdMetric.setVisible(false);

		firstLimit = new Text(composite, SWT.BORDER);
		firstLimit.setBounds(313, 58, 94, 30);
		firstLimit.setText(DEFAULT_LIMIT_TEXT);

		secondLimit = new Text(composite, SWT.BORDER);
		secondLimit.setBounds(313, 92, 94, 28);
		secondLimit.setText(DEFAULT_LIMIT_TEXT);

		listrulestoshow = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listRulesToShowListener();
		listrulestoshow.setLocation(10, 186);
		listrulestoshow.setSize(431, 230);

		defineRuleButton = new Button(composite, SWT.NONE);
		defineRuleButtonListener();
		defineRuleButton.setBounds(447, 185, 142, 30);
		defineRuleButton.setText("Definir regra");

		changeRuleButton = new Button(composite, SWT.NONE);
		changeRuleButtonListener();
		changeRuleButton.setBounds(447, 224, 145, 30);
		changeRuleButton.setText("Alterar regras");

		detectSmellsButton = new Button(composite, SWT.NONE);
		detectSmellsButtonListener();
		detectSmellsButton.setBounds(451, 422, 167, 30);
		detectSmellsButton.setText("Deteção de codesmells");

		loadHistoryButton = new Button(composite, SWT.NONE);
		loadHistoryButtonListener();
		loadHistoryButton.setBounds(10, 422, 212, 30);
		loadHistoryButton.setText("Carregar histórico de regras");

		Label savedRulesLabel = new Label(composite, SWT.NONE);
		savedRulesLabel.setBounds(10, 160, 155, 20);
		savedRulesLabel.setText("Regras guardadas:");

		Label defineRuleLabel = new Label(composite, SWT.NONE);
		defineRuleLabel.setText("Defina/altere uma regra para a deteção de codesmells: ");
		defineRuleLabel.setBounds(10, 32, 397, 20);

		savehistory = new Button(composite, SWT.NONE);
		saveHistoryListener();
		savehistory.setBounds(227, 422, 214, 30);
		savehistory.setText("Guardar histórico");

		firstSignal = new Combo(composite, SWT.READ_ONLY);
		firstSignal.setBounds(196, 58, 80, 28);
		firstSignal.add(">");
		firstSignal.add("<");

		secondOperator = new Combo(composite, SWT.READ_ONLY);
		secondOperator.setBounds(440, 92, 117, 28);
		secondOperator.setText("");
		secondOperator.add("OR");
		secondOperator.add("AND");
		secondOperator.add("");
		secondOperator.setVisible(false);

		thirdSignal = new Combo(composite, SWT.READ_ONLY);
		thirdSignal.setBounds(196, 126, 80, 28);
		thirdSignal.add(">");
		thirdSignal.add("<");
		thirdSignal.add("");
		thirdSignal.setVisible(false);

		thirdLimit = new Text(composite, SWT.BORDER);
		thirdLimit.setText(DEFAULT_LIMIT_TEXT);
		thirdLimit.setBounds(313, 126, 94, 28);
		thirdLimit.setVisible(false);

		secondSignal = new Combo(composite, SWT.READ_ONLY);
		secondSignal.setBounds(195, 92, 81, 28);
		secondSignal.add(">");
		secondSignal.add("<");

		cleanHistoryList = new Button(composite, SWT.NONE);
		cleanHistoryListListener();
		cleanHistoryList.setBounds(449, 267, 140, 30);
		cleanHistoryList.setText("Limpar lista");

		Label chooseProjectLabel = new Label(this, SWT.NONE);
		chooseProjectLabel.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		chooseProjectLabel.setBounds(10, 9, 528, 20);
		chooseProjectLabel.setText("Escolha o projeto java que pretende analisar:");

		Menu helpMenu = new Menu(this, SWT.BAR);
		setMenuBar(helpMenu);

		MenuItem mntmHelp = new MenuItem(helpMenu, SWT.CASCADE);
		mntmHelp.setText("Ajuda");

		menuWithButtons = new Menu(mntmHelp);
		mntmHelp.setMenu(menuWithButtons);

		guiInstructionsButton = new MenuItem(menuWithButtons, SWT.NONE);
		guiInstructionsButtonListener();
		guiInstructionsButton.setText("Utilização da interface");

		metricInfoButton = new MenuItem(menuWithButtons, SWT.NONE);
		metricInfoButtonListener();
		metricInfoButton.setText("Informação sobre métricas");

		viewFileButton = new Button(this, SWT.NONE);
		viewFileButtonListener();
		viewFileButton.setBounds(10, 278, 109, 30);
		viewFileButton.setText("Ver ficheiro");

		folderToSavePath = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		folderToSavePath.setBounds(10, 35, 345, 26);

		choosePathToExtract = new Button(this, SWT.NONE);
		choosePathToExtractListener();
		choosePathToExtract.setBounds(372, 35, 166, 28);
		choosePathToExtract.setText("Selecionar destino");
	}

	/**
	 * Metodo que permite a selecao do ficheiro excel gerado e a escrita das
	 * caracteristicas do mesmo
	 */
	private void excelfilesListener() {
		excelfiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				index = excelfiles.getSelectionIndex();
				if (index != -1) {
					changeSelectedFile();
					writeStatistics();
				}
			}
		});
	}

	/**
	 * Metodo que verifica se a metrica 3 se encontra preenchida
	 * 
	 * @return true or false
	 */
	private boolean isThirdMetricEmpty() {
		if (thirdMetric.isVisible()) {
			return (thirdMetric.getText().isEmpty() && secondOperator.getText().isEmpty()
					&& (thirdLimit.getText().isEmpty() || thirdLimit.getText().equals(DEFAULT_LIMIT_TEXT))
					&& thirdSignal.getText().isEmpty());
		} else
			return true;
	}

	/**
	 * Metodo que permite a definicao de regras previamente criadas pelo utilizador
	 * atraves da gui
	 */
	private void defineRuleButtonListener() {
		defineRuleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isValid(firstLimit.getText()) || !isValid(secondLimit.getText())) {
					JOptionPane.showMessageDialog(null, INVALID_LIMITS);
				} else {
					boolean isRepeated = false;
					if (isFirstMetricCorrect() && isSecondMetricCorrect()) {

						isRepeated = createRuleAndVerify(isRepeated);

						checkExistanceAndAdd(isRepeated);
					} else
						JOptionPane.showMessageDialog(null, FIELDS_INCORRECT_MESSAGE);
				}
			}

		});
	}

	/**
	 * Metodo que permite ao utilizador alterar uma regra previamente selecionada
	 * 
	 */
	private void changeRuleButtonListener() {
		changeRuleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isValid(firstLimit.getText()) || !isValid(secondLimit.getText()))
					JOptionPane.showMessageDialog(null, INVALID_LIMITS);
				else {
					if (isFilledCorrectly()) {
						if (listrulestoshow.isSelected(rulesToShowSelectedIndex)) {
							list.get(rulesToShowSelectedIndex).setLimit1(firstLimit.getText());
							list.get(rulesToShowSelectedIndex).setLimit2(secondLimit.getText());
							list.get(rulesToShowSelectedIndex).setLimit3(thirdLimit.getText());
							list.get(rulesToShowSelectedIndex).setMethod1(firstMetric.getText());
							list.get(rulesToShowSelectedIndex).setMethod2(secondMetric.getText());
							list.get(rulesToShowSelectedIndex).setMethod3(thirdMetric.getText());
							list.get(rulesToShowSelectedIndex).setOperator(firstOperator.getText());
							list.get(rulesToShowSelectedIndex).setOperator2(secondOperator.getText());
							list.get(rulesToShowSelectedIndex).setSinal1(firstSignal.getText());
							list.get(rulesToShowSelectedIndex).setSinal2(secondSignal.getText());
							list.get(rulesToShowSelectedIndex).setSinal3(thirdSignal.getText());
							update = list.get(rulesToShowSelectedIndex).toString();
							updateRule();
							listrulestoshow.remove(rulesToShowSelectedIndex);
							listrulestoshow.add(update, rulesToShowSelectedIndex);
						} else
							JOptionPane.showMessageDialog(null, NO_RULE_SELECTED);
					} else
						JOptionPane.showMessageDialog(null, INCORRECT_FIELDS);
				}
			}
		});
	}

	/**
	 * Metodo que permite que o utilizador realize a detecao de codesmells consoante
	 * a regra previamente selecionada
	 * 
	 */
	private void detectSmellsButtonListener() {
		detectSmellsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Caso não tenham sido extraídas métricas de um projeto, mensagem de erro
				if (listrulestoshow.getSelectionIndex() != -1) {
					if (evaluateAndDetect.getActualmetrics() == null)
						JOptionPane.showMessageDialog(null, NO_METRICS_EXTRACTED_ERROR_MESSAGE);
					else {
						// Caso contrário a existencia de code smells no projeto é averiguada
						if (listrulestoshow.isSelected(rulesToShowSelectedIndex)) {
							// Apenas é averiguada caso exista uma regra selecionada
							String method1 = currentRule.getMethod1();
							String method2 = currentRule.getMethod2();
							HashMap<String, ArrayList<HasCodeSmell>> nameAndResults = evaluateAndDetect
									.evaluationChooser(currentRule);
							if (nameAndResults != null) {
								String name = nameAndResults.keySet().iterator().next();
								ArrayList<HasCodeSmell> results = nameAndResults.values().iterator().next();
								createSecondaryGUI(name, results);
							}
						} else
							JOptionPane.showMessageDialog(null, NO_RULE_SELECTED_ERROR_MESSAGE); // Caso contário é
																									// mostrada
						// uma mensagem de erro
					}
				}
			}

		});
	}

	/**
	 * Metodo que permite ao utilizador carregar num historico para a gui e
	 * trabalhar consoante regras definidas a priori
	 */
	private void loadHistoryButtonListener() {
		loadHistoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listrulestoshow.removeAll();
				list.clear();
				JFileChooser pathpasta = new JFileChooser(".txt");
				FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
				pathpasta.setFileFilter(filter);
				pathpasta.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnValue = pathpasta.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION)
					history = pathpasta.getSelectedFile();
				if (history != null) {
					if (history.getPath().endsWith(".txt")) {
						try {
							readAndGetRules();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else
						JOptionPane.showMessageDialog(null, INVALID_FILE_MESSAGE);
				}

			}

		});
	}

	/**
	 * Metodo que permite ao utilizador guardar, num ficheiro nao volatil, as regras
	 * definidas. Este metodo permite tambem a criacao do ficheiro historico de raiz
	 * 
	 */
	private void saveHistoryListener() {
		savehistory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!list.isEmpty()) {
					SaveHistoryGUI hist = new SaveHistoryGUI(getDisplay(), listrulestoshow, list);
					hist.loadGUI();
				} else
					JOptionPane.showMessageDialog(null, EMPTY_RULE_LIST_MESSAGE);
			}
		});
	}

	/**
	 * Metodo que permite ao utilizador esvaziar a lista das regras
	 * 
	 */
	private void cleanHistoryListListener() {
		cleanHistoryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (listrulestoshow.getItemCount() != 0) {
					listrulestoshow.removeAll();
					list.clear();
				} else
					JOptionPane.showMessageDialog(null, ALREADY_EMPTY_MESSAGE);
			}
		});
	}

	/**
	 * Metodo que permite ao utilizador, atraves de um botao, aceder as informacoes
	 * relativas a utilizacao da interface e as suas funcionalidades
	 * 
	 */
	private void guiInstructionsButtonListener() {
		guiInstructionsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HelpInterface helpinterface = new HelpInterface(getDisplay());
				helpinterface.loadGUI();
			}
		});
	}

	/**
	 * Metodo que permite ao utilizador consultar informacoes relativas as metricas
	 * e consequentemente ao seu funcionamento
	 * 
	 */
	private void metricInfoButtonListener() {
		metricInfoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HelpGUIMetrics hgm = new HelpGUIMetrics(getDisplay());
				hgm.loadGUI();
			}
		});
	}

	/**
	 * Metodo que permite ao utilizador visualizar o ficheiro excel gerado pela
	 * extracao
	 * 
	 */
	private void viewFileButtonListener() {
		viewFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (evaluateAndDetect.getActualmetrics() != null) {
					ShowExcelGUI showGUI = new ShowExcelGUI(getDisplay(), "Visualizar ficheiro",
							evaluateAndDetect.getActualmetrics());
					showGUI.loadGUI();
				} else
					JOptionPane.showMessageDialog(null, EXTRACT_PROJECT_MESSAGE);

			}
		});
	}

	/**
	 * Metodo que permite ao utilizador escolher a pasta destino para o ficheiro a
	 * extrair
	 * 
	 */
	private void choosePathToExtractListener() {
		choosePathToExtract.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JFileChooser pathpasta = new JFileChooser(".");
				pathpasta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = pathpasta.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION)
					folderextraction = pathpasta.getSelectedFile();
				if (folderextraction != null) {
					folderToSavePath.setText(folderextraction.getPath());
					folderextraction.getPath();
				}

			}
		});
	}

	/**
	 * Metodo que permite ao utilizador
	 */
	private void addProjectSelectionListener() {
		projectSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JFileChooser pathpasta = new JFileChooser(".");
				pathpasta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = pathpasta.showOpenDialog(null);
				File choosenFile = null;
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					choosenFile = pathpasta.getSelectedFile();
					String path = choosenFile.getAbsolutePath();
					if (!path.contains("src"))
						JOptionPane.showMessageDialog(null, SELECT_SRC_MESSAGE);
					else
						fileList.add(choosenFile);

				}
				if (choosenFile != null && choosenFile.getAbsolutePath().contains("src")) {
					projectFolderPath.setText(choosenFile.getPath());
					selectedFile = choosenFile;
				}

			}
		});
	}

	/**
	 * Metodo que realiza a extracao das metricas e adiciona o nome do ficheiro
	 * gerado a lista
	 */
	private void extractMetricsListener() {
		extractMetricsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedFile == null || folderextraction == null)
					JOptionPane.showMessageDialog(null, FOLDER_SELECTION_ERROR);
				else {
					try {
						extractAndAddFileToList();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Metodo que inibe o utilizador de escolher a segunda metrica livremente,
	 * estando dependente da primeira metrica
	 */
	private void firstMetricListener() {
		firstMetric.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (firstMetric.getSelectionIndex() != -1)
					changeSecondMetricOptions();
			}
		});
	}

	/**
	 * Metodo que inibe o utilizador de escolher a terceira metrica livremente,
	 * estando dependente da segunda metrica
	 * 
	 */
	private void secondMetricListener() {
		secondMetric.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int secondMetricSelected = secondMetric.getSelectionIndex();
				if (secondMetricSelected != -1) {
					if (secondMetric.getItem(secondMetricSelected).equals(LOC_CLASS))
						setThirdMetricVisible(false);
					else if (secondMetric.getItem(secondMetricSelected).equals(NOM_CLASS))
						setThirdMetricVisible(true);
				}
			}
		});
	}

	/**
	 * Metodo que preenche os campos da regra selecionada
	 */
	private void listRulesToShowListener() {
		listrulestoshow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (listrulestoshow.getSelectionIndex() != -1) {
					rulesToShowSelectedIndex = listrulestoshow.getSelectionIndex();
					currentRule = list.get(rulesToShowSelectedIndex);
					selectFirstMetricByRule();
					changeSecondMetricOptions();
					selectSecondMetricByRule();
					if (!currentRule.getMethod3().isEmpty()) {
						setThirdMetricVisible(true);
						selectThirdMetricByRule();
					} else {
						setThirdMetricVisible(false);
						thirdSignal.deselectAll();
						secondOperator.deselectAll();
						thirdMetric.deselectAll();
						thirdLimit.clearSelection();
					}
					fillWithRule();
				}
			}

		});
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText(GUI_NAME);
		setSize(726, 861);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Metodo que permite a selecao do ficheiro excel gerado
	 */
	private void changeSelectedFile() {
		for (File file : fileList) {
			NameByFile excelNameByFile = new NameByFile();
			excelNameByFile.setFileToExtract(file);
			String fileName = excelNameByFile.getFileName();
			if (fileName.equals(excelfiles.getItem(index)))
				selectedFile = file;
		}
	}

	/**
	 * Metodo que permite a visualizacao das caracteristicas gerais do ficheiro
	 * excel gerado
	 */
	private void writeStatistics() {
		for (Entry<String, ArrayList<String>> entry : mapStats.entrySet()) {
			if (entry.getKey().equals(excelfiles.getItem(index))) {
				ArrayList<String> statsToWrite = entry.getValue();
				numOfLines.setText(statsToWrite.get(LOC_INDEX));
				numOfClasses.setText(statsToWrite.get(NUMBER_OF_CLASSES_INDEX));
				numOfMethods.setText(statsToWrite.get(NOM_INDEX));
				numOfPackages.setText(statsToWrite.get(NUMBER_OF_PACKAGES_INDEX));
				DirExplorer dirEx = new DirExplorer(selectedFile);
				try {
					evaluateAndDetect.setActualmetrics(dirEx.exploreAndExtract());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Calcula as estatisticas de um projeto
	 * 
	 * @return StringStats Retorna a lista das caracteristicas gerais do ficheiro
	 *         excel
	 */
	private ArrayList<String> createStatsList() {
		Statistics stats = new Statistics(evaluateAndDetect.getActualmetrics());
		ArrayList<String> StringStats = new ArrayList<>();
		StringStats.add(String.valueOf(stats.countLinesOfCode()));
		StringStats.add(String.valueOf(stats.countNumberOfMethods()));
		StringStats.add(String.valueOf(stats.countClasses()));
		StringStats.add(String.valueOf(stats.countPackages()));
		return StringStats;
	}

	/**
	 * Metodo que extrai as metricas e adiciona o nome do ficheiro excel a lista na
	 * GUI
	 * 
	 * @throws IOException
	 * 
	 */
	private void extractAndAddFileToList() throws IOException {
		DirExplorer dirEx = new DirExplorer(selectedFile);
		if (folderextraction.exists() && selectedFile.exists()) {
			NameByFile excelFileName = new NameByFile();
			excelFileName.setFileToExtract(selectedFile);
			String fileName = excelFileName.getFileName();
			evaluateAndDetect.setActualmetrics(dirEx.exploreAndExtract());
			ExcelManip em = new ExcelManip(selectedFile);
			em.createExcel(evaluateAndDetect.getActualmetrics(), folderToSavePath.getText());
			ArrayList<String> statsList = createStatsList();
			mapStats.put(fileName, statsList);
			excelfiles.add(fileName);
		}
	}

	/**
	 * @param visible Torna os campos relativos a terceira metrica visiveis ou nao,
	 *                consoante o parametro visible
	 */
	private void setThirdMetricVisible(boolean visible) {
		secondOperator.setVisible(visible);
		thirdSignal.setVisible(visible);
		thirdMetric.setVisible(visible);
		thirdLimit.setVisible(visible);
	}

	/**
	 * Metodo que, consoante a primeira metrica selecionada, permite a escolha da
	 * segunda metrica de forma filtrada, evitando combinacoes invalidas
	 */
	private void changeSecondMetricOptions() {
		int firstMethodSelection = firstMetric.getSelectionIndex();
		if (firstMetric.getItem(firstMethodSelection).equals(LOC_METHOD)) {
			setThirdMetricVisible(false);
			secondMetric.removeAll();
			secondMetric.add(CYCLO_METHOD);
		} else if (firstMetric.getItem(firstMethodSelection).equals(WMC_CLASS)) {
			setThirdMetricVisible(true);
			secondMetric.removeAll();
			secondMetric.add(NOM_CLASS);
			secondMetric.add(LOC_CLASS);
		} else if (firstMetric.getItem(firstMethodSelection).equals(NOM_CLASS)) {
			setThirdMetricVisible(false);
			secondMetric.removeAll();
			secondMetric.add(LOC_CLASS);
		}
	}

	/**
	 * Metodo que permite preencher o campo relativo a metrica 1, com os dados da
	 * metrica selecionada na lista de regras
	 */
	private void selectFirstMetricByRule() {
		for (int arrayIndex = 0; arrayIndex < firstMetric.getItemCount(); arrayIndex++)
			if (firstMetric.getItem(arrayIndex).contentEquals(currentRule.getMethod1()))
				firstMetric.select(arrayIndex);
	}

	/**
	 * Metodo que permite preencher o campo relativo a metrica 2, com os dados da
	 * metrica selecionada na lista de regras
	 */
	private void selectSecondMetricByRule() {
		for (int k = 0; k < secondMetric.getItemCount(); k++)
			if (secondMetric.getItem(k).contentEquals(currentRule.getMethod2()))
				secondMetric.select(k);
	}

	/**
	 * Metodo que permite preencher o campo relativo a metrica 3, com os dados da
	 * metrica selecionada na lista de regras
	 */
	private void selectThirdMetricByRule() {
		for (int z = 0; z < thirdMetric.getItemCount(); z++)
			if (thirdMetric.getItem(z).contentEquals(currentRule.getMethod3()))
				thirdMetric.select(z);
	}

	/**
	 * Metodo que permite preencher os campos relativos aos limites, sinais e
	 * operadores, com os dados da metrica selecionada na lista de regras
	 */
	private void fillWithRule() {
		firstSignal.setText(currentRule.getSinal1());
		firstLimit.setText(currentRule.getLimit1());
		firstOperator.setText(currentRule.getOperator());
		secondSignal.setText(currentRule.getSinal2());
		secondLimit.setText(currentRule.getLimit2());
		secondOperator.setText(currentRule.getOperator2());
		thirdSignal.setText(currentRule.getSinal3());
		thirdLimit.setText(currentRule.getLimit3());
	}

	/**
	 * Verifica e uma String tem apenas numeros
	 * 
	 * @param text Validacao do limite inserido pelo utilizador
	 * @return
	 */
	private boolean isValid(String text) {
		if(text.isEmpty()) return false;
		for (int i = 0; i < text.length(); i++)
			if (!isNumber(text.charAt(i)))
				return false;
		return true;
	}

	/**
	 * Metodo para indicar se um char corresponde a um numero ou nao
	 * 
	 * @param charAt Avalia se um determinado char e um número
	 * @return indicacao de se o char dado e um numero ou nao
	 */
	private boolean isNumber(char charAt) {
		return charAt == '0' || charAt == '1' || charAt == '2' || charAt == '3' || charAt == '4' || charAt == '5'
				|| charAt == '6' || charAt == '7' || charAt == '8' || charAt == '9';
	}

	/**
	 * Metodo de ajuda a criacao da secondaryGUI consoante um nome e os resultados
	 * que devem ser mostrados na tabela
	 * 
	 * @param name             Criar GUI secundaria, importar os resultados da
	 *                         aplicação da regra e lançar GUI
	 * @param detectionResults resultados da detecao de code smells
	 */
	public void createSecondaryGUI(String name, ArrayList<HasCodeSmell> detectionResults) {
		SecondaryGUI codesmells = new SecondaryGUI(getDisplay(), name, detectionResults);
		codesmells.loadGUI();
	}

	/**
	 * Indica se a segunda metrica foi preenchida corretamente
	 * 
	 * @return true ou false, consoante a acao do utilizador no preenchimento dos
	 *         campos relativos a metrica2
	 */
	private boolean isSecondMetricCorrect() {
		return !secondMetric.getText().isEmpty() && !secondLimit.getText().isEmpty() && !secondSignal.getText().isEmpty();
	}

	/**
	 * Indica se a primeira metrica foi preenchida corretamente
	 * 
	 * @return true ou false, consoante a acao do utilizador no preenchimento dos
	 *         campos relativos a metrica1
	 */
	private boolean isFirstMetricCorrect() {
		return !firstMetric.getText().isEmpty() && !firstOperator.getText().isEmpty()
				&& !firstLimit.getText().isEmpty() && !firstSignal.getText().isEmpty();
	}

	/**
	 * Indica se a terceira metrica esta preenchida
	 * 
	 * @return true ou false, consoante a terceira metrica estiver preenchida, ou
	 *         nao
	 */
	private boolean isThirdMetricFilled() {
		if (thirdMetric.isVisible()) {
			return !thirdMetric.getText().isEmpty() && !secondOperator.getText().isEmpty()
					&& isValid(thirdLimit.getText()) && !thirdSignal.getText().isEmpty();
		} else {
			return false;
		}

	}

	/**
	 * Verifica se uma regra existe
	 * 
	 * @param v Metodo que verifica se a regra definida pelo utilizador ja existe ou
	 *          nao
	 * @return resultado da verificacao
	 */
	private boolean verifyRuleExistance(boolean v) {
		ruleToShowInList = rule.toString();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).toString().contentEquals(rule.toString())) {
				JOptionPane.showMessageDialog(null, REPEATED_RULE);
				v = true;
				break;

			}
		}
		return v;
	}

	/**
	 * Adiciona a regra definida a lista de regras
	 * 
	 * @param isRepeated Metodo que adiciona a regra definida a lista de regras,
	 *                   dependendo de se esta ja existe ou nao
	 */
	private void checkExistanceAndAdd(boolean isRepeated) {
		if (isRepeated == false) {
			if (isFilledCorrectly()) {
				listrulestoshow.add(ruleToShowInList);
				list.add(rule);
			} else {
				JOptionPane.showMessageDialog(null, "Campos por preencher");
			}
		}
	}

	/**
	 * Metodo que cria o objeto Rule, apos a verificacao da existencia ou nao do
	 * mesmo
	 * 
	 * @param isRepeated
	 * @return
	 */
	private boolean createRuleAndVerify(boolean isRepeated) {
		if (isThirdMetricFilled()) {
			rule = new Rule(firstMetric.getText(), firstSignal.getText(), firstLimit.getText(), firstOperator.getText(),
					secondMetric.getText(), secondSignal.getText(), secondLimit.getText(), secondOperator.getText(),
					thirdMetric.getText(), thirdSignal.getText(), thirdLimit.getText());
			isRepeated = verifyRuleExistance(isRepeated);
		} else {
			if (isThirdMetricEmpty()) {
				rule = new Rule(firstMetric.getText(), firstSignal.getText(), firstLimit.getText(),
						firstOperator.getText(), secondMetric.getText(), secondSignal.getText(), secondLimit.getText(),
						"", "", "", "");
				isRepeated = verifyRuleExistance(isRepeated);
			} else
				JOptionPane.showMessageDialog(null, FIELDS_INCORRECT_MESSAGE);
		}
		return isRepeated;
	}

	private boolean isFilledCorrectly() {
		return isFirstMetricCorrect() && isSecondMetricCorrect() && (isThirdMetricFilled() || isThirdMetricEmpty());
	}

	/**
	 * Metodo que, consoante a regra escolhida, remove e substitui por uma nova
	 */
	private void updateRule() {
		for (int x = 0; x < list.size(); x++) {
			if (x == rulesToShowSelectedIndex) {
				listrulestoshow.remove(x);
				listrulestoshow.add(update, x);
			}
		}
	}

	/**
	 * Metodo que adiciona uma nova regra a lista de regras definidas pelo
	 * utilizador
	 * 
	 * @param line
	 * @param ruleInHistory
	 */
	private void addRule(String line, Rule ruleInHistory) {
		list.add(ruleInHistory);
		listrulestoshow.add(line);
	}

	/**
	 * Metodo que, le um ficheiro historico do tipo '.txt' e escreve as regras
	 * contidas no mesmo, na GUI
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readAndGetRules() throws FileNotFoundException, IOException {
		FileReader reader = new FileReader(new File(history.getPath()));
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] rules = line.split(" ");
			if (rules.length > 9) {
				Rule ruleInHistory = new Rule(rules[0], rules[1], rules[2], rules[3], rules[4], rules[5], rules[6],
						rules[7], rules[8], rules[9], rules[10]);
				addRule(line, ruleInHistory);
			} else {
				Rule ruleInHistory = new Rule(rules[0], rules[1], rules[2], rules[3], rules[4], rules[5], rules[6], "",
						"", "", "");
				addRule(line, ruleInHistory);
			}

		}
		reader.close();
	}
}
