package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.swing.CySwingApplication;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CustomParameter;

@SuppressWarnings("serial")
public class HierarchySettingsDialog extends JDialog implements ActionListener,ItemListener {

    
	private static final String INPUTDELIM = ":::";
	private List<CommunityDetectionAlgorithm> algorithmList;

	private JPanel cards;
	private JEditorPaneFactory editorPaneFac;
	private ImageIcon infoIconSmall;
	private ImageIcon infoIconLarge;
	private Map<String, JPanel> algoCardMap;
	private JComboBox algorithmComboBox;

	public HierarchySettingsDialog(CySwingApplication swingApplication,
		JEditorPaneFactory editorPaneFac) throws Exception {
		super(swingApplication.getJFrame().getOwner(), AppUtils.APP_NAME + " settings",
			ModalityType.MODELESS);
		this.editorPaneFac = editorPaneFac;
		loadImageIcon();
		algoCardMap = new LinkedHashMap<>();
		algorithmList = CDRestClient.getInstance().getAlgorithms();

		cards = new JPanel(new CardLayout());
		algorithmComboBox = new JComboBox();
		algorithmComboBox.setEditable(false);
		algorithmComboBox.addItemListener(this);

		loadAlgorithmCards();
		
		JPanel contentPane = new JPanel();
		contentPane.add(new JLabel("Algorithm: "));
		
		
		contentPane.add(algorithmComboBox);
		
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.add(cards);
		
		JPanel closePanel = new JPanel();
		closePanel.add(getCloseButton());
		
		add(contentPane, BorderLayout.PAGE_START);
		
		add(masterPanel, BorderLayout.CENTER);
		add(closePanel, BorderLayout.PAGE_END);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		pack();
	}

	
	private void loadAlgorithmCards(){
	    algoCardMap.clear();
	    cards.removeAll();
	    algorithmComboBox.removeAllItems();
	    int rowIndex = 1;
	    for (CommunityDetectionAlgorithm cda : algorithmList){
		Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
		if (pMap == null){
		    continue;
		}
		CommunityDetectionRequest request = CDRestClient.getInstance().getRequestForAlgorithm(cda.getName());
		JPanel algoCard = new JPanel();
		algoCard.setName(cda.getName());
		algoCardMap.put(cda.getName(), algoCard);
		algoCard.setLayout(new GridBagLayout());
		algoCard.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Parameters"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
		algorithmComboBox.addItem(cda.getDisplayName());
		for (String key : pMap.keySet()){
		    CustomParameter cp = pMap.get(key);
		    JLabel paramLabel = new JLabel(cp.getDisplayName() + ":");

		    GridBagConstraints labelConstraints = new GridBagConstraints();
		    labelConstraints.gridy = rowIndex;
		    labelConstraints.gridx = 0;
		    labelConstraints.anchor = GridBagConstraints.LINE_END;
		    labelConstraints.insets = new Insets(0, 5, 5, 0);
		    algoCard.add(paramLabel, labelConstraints);

		    JComponent inputComponent = getCustomParameterInput(cda.getName(), request, cp);
		    if (cp.getDescription() != null){
			inputComponent.setToolTipText(cp.getDescription());
			paramLabel.setToolTipText(cp.getDescription());
		    }

		    GridBagConstraints inputConstraints = new GridBagConstraints();
		    inputConstraints.gridy = rowIndex;
		    inputConstraints.gridx = 1;
		    inputConstraints.gridwidth = 1;
		    inputConstraints.weightx = 1.0;
		    inputConstraints.anchor = GridBagConstraints.LINE_START;
		    inputConstraints.insets = new Insets(0, 0, 5, 0);
		    inputConstraints.fill = GridBagConstraints.HORIZONTAL;

		    algoCard.add(inputComponent, inputConstraints);

		    GridBagConstraints infoConstraints = new GridBagConstraints();
		    infoConstraints.gridy = rowIndex;
		    infoConstraints.gridx = 2;
		    infoConstraints.insets = new Insets(0, 0, 5, 0);
		    algoCard.add(getParameterInfoIcon(cp), infoConstraints);

		    rowIndex++;
		}
		GridBagConstraints resetConstraints = new GridBagConstraints();
		resetConstraints.gridy = 0;
		resetConstraints.gridx = 0;
		resetConstraints.insets = new Insets(0, 5, 0, 0);
		resetConstraints.anchor = GridBagConstraints.LINE_START;
		algoCard.add(this.getResetButton(cda.getName()), resetConstraints);
		cards.add(algoCard, cda.getDisplayName());
		this.resetAlgorithmToDefaults(cda.getName());
	    }
	}
	
	private JComponent getCustomParameterInput(final String algorithm, final CommunityDetectionRequest request,
		final CustomParameter parameter){
	    if (parameter.getType() != null && parameter.getType().equalsIgnoreCase("flag")){
		    JCheckBox checkBox = new JCheckBox();
		    checkBox.setName(algorithm + INPUTDELIM + parameter.getName());
		    if (request != null && request.getCustomParameters().containsKey(parameter.getName())){
			checkBox.setSelected(true);
		    }
		    return checkBox;
	    }
	    if (parameter.getDefaultValue() != null){
		JTextField textField = null;
		if (request != null && request.getCustomParameters().containsKey(parameter.getName())){
		    textField = new JTextField(request.getCustomParameters().get(parameter.getName()));
		} else {
		    textField = new JTextField(parameter.getDefaultValue());
		}
		textField.setName(algorithm + INPUTDELIM + parameter.getName());
	    }
	    JTextField textField = new JTextField();
	    textField.setName(algorithm + INPUTDELIM + parameter.getName());
	    return textField;
	}
	
	private void loadImageIcon(){
	    try {
		    File imgFile = File.createTempFile("info_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("info_icon.png");
		Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		infoIconSmall = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		infoIconLarge = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	    }
	    catch (IOException ex){
		    
	    }
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, (String)evt.getItem());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(true);
	}
	
	/**
	 * Gets a UI Component that describes the parameter passed in.
	 * @param parameter
	 * @return 
	 */
	private JEditorPane getCustomParameterHelp(final CustomParameter parameter){
	    if (parameter == null){
		return editorPaneFac.getDescriptionFrame("No parameter set, unable to generate help");
	    }
	    StringBuilder sb = new StringBuilder();
	    sb.append("<b>Parameter:</b> ");
	    sb.append(parameter.getDisplayName());
	    sb.append(" (");
	    sb.append(parameter.getName());
	    sb.append(")");
	    if (parameter.getDefaultValue() != null){
		sb.append(" [Default: ");
		sb.append(parameter.getDefaultValue());
		sb.append("]");
	    }
	    sb.append("<br/><h3>Description</h3> ");
	    sb.append(parameter.getDescription());
	    if (parameter.getValidationHelp() != null){
		sb.append("<br/>");
		sb.append(parameter.getValidationHelp());
	    }
	    return editorPaneFac.getDescriptionFrame(sb.toString());
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getParameterInfoIcon(final CustomParameter parameter) {
		JLabel paramLabel = new JLabel(infoIconSmall, JLabel.CENTER);
		paramLabel.setToolTipText("Click here for more information about '" +
			parameter.getDisplayName() + "' parameter");

		paramLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				JOptionPane.showMessageDialog(getParent(), getCustomParameterHelp(parameter),
					"Parameter " + parameter.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					infoIconLarge);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		paramLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(getParent(), getCustomParameterHelp(parameter),
					"Parameter " + parameter.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					infoIconLarge);
			}
		});

		return paramLabel;
	}

	/**
	 * Gets close button which is set to close the dialog
	 * @return 
	 */
	private Component getCloseButton() {
		JButton button = new JButton(AppUtils.CLOSE);
		button.setToolTipText("Close this dialog, any changes are recorded");
		button.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			dispose();
		    }
		});
		return button;
	}
	
	/**
	 * Given an internal algorithm name 'algorithm' this method 
	 * sets the UI panel input fields for this algorithm to their defaults
	 * @param algorithm 
	 */
	private void resetAlgorithmToDefaults(final String algorithm){
	    System.out.println("Resetting " + algorithm + " to defaults");
	    if (algoCardMap.containsKey(algorithm) == false){
		return;
	    }
	    CommunityDetectionAlgorithm cda = getCommunityDetectionAlgorithm(algorithm);
	    if (cda == null){
		return;
	    }
	    Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
	    JPanel algoCard = algoCardMap.get(algorithm);
	    for (Component c : algoCard.getComponents()){
		if (c.getName() == null || !c.getName().startsWith(algorithm + ":::")){
		    continue;
		}
		String paramName = c.getName().replaceAll("^.*" + INPUTDELIM, "");
		if (pMap.containsKey(paramName) == false){
		    continue;
		}
		CustomParameter cp = pMap.get(paramName);
		
		if (c instanceof JTextField){
		    JTextField tField = (JTextField)c;
		    if (cp.getDefaultValue() == null){
			tField.setText("");
		    } else {
			tField.setText(cp.getDefaultValue());
		    }
		} else if (c instanceof JCheckBox){
		    JCheckBox checkBox = (JCheckBox)c;
		    checkBox.setSelected(false);
		}
	    }
	}
	
	/**
	 * Given 'algorithm' internal name, this method returns 
	 * {@link org.ndexbio.communitydetection.reste.model.CommunityDetectionAlgorithm}
	 * obtained from constructor of this object.
	 * 
	 * @param algorithm internal algorithm name
	 * @return 
	 */
	private CommunityDetectionAlgorithm getCommunityDetectionAlgorithm(final String algorithm){
	    for (CommunityDetectionAlgorithm cda : this.algorithmList){
		if (cda.getName().equals(algorithm)){
		    return cda;
		}
	    }
	    return null;
	}
	
	/**
	 * Given an 'algorithm' name this method looks at the settings UI for the
	 * parameters for the 'algorithm' The code then gets all the user values
	 * set. The way it works is the {@link javax.swing.JComponent} containing
	 * the algorithm parameters is named 'algorithm' and each JTextField or JCheckBox 
	 * within that pnael is named
	 * "<ALGORITHM>:::<PARAMETER NAME>" so this method looks for the UI
	 * components with that naming convention extracting the values which are
	 * returned as a {@link java.util.Map}
	 * @param algorithm
	 * @return {@link java.util.Map} where the key is parameter name ie --overlap and
	 *         value is the user set value. If value was a checkbox then this is set
	 *         to empty string, otherwise it is not included in {@link java.util.Map}
	 */
	public Map<String, String> getAlgorithmCustomParameters(final String algorithm){
	    JPanel algoCard = algoCardMap.get(algorithm);
	    if (algoCard == null){
		return null;
	    }
	    Map<String, String> cParam = new LinkedHashMap<String, String>();
	    
	    for (Component c : algoCard.getComponents()){
		if (c.getName() == null || !c.getName().startsWith(algorithm + ":::")){
		    continue;
		}
		String paramName = c.getName().replaceAll("^.*" + INPUTDELIM, "");
		
		if (c instanceof JTextField){
		    JTextField tField = (JTextField)c;
		    if (tField.getText() == null || tField.getText().trim().length() == 0){
			continue;
		    }
		    cParam.put(paramName, tField.getText());
		} else if (c instanceof JCheckBox){
		    JCheckBox checkBox = (JCheckBox)c;
		    if (checkBox.isSelected() == true){
			cParam.put(paramName, "");
		    }
		}
	    }
	    return cParam;
	}

	/**
	 * Creates reset button that when clicked resets all parameters for that
	 * algorithm to default values. The link is done by setting the name of the
	 * button to the algorithm name
	 * @param algorithm internal algorithm name
	 * @return reset button
	 */
	private JButton getResetButton(final String algorithm) {
		JButton button = new JButton(AppUtils.RESET);
		button.setName(algorithm);
		button.setToolTipText("Resets all parameters for this "
			+ "algorithm to default values");
		button.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			JComponent c = (JComponent)e.getSource();
			String algorithm = c.getName();
			System.out.println("Reset button clicked "
				+ "on algorithm: " + algorithm);
			resetAlgorithmToDefaults(algorithm);
		    }
		});
		return button;
	}
}
