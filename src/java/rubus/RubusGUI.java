package rubus;
/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


public class RubusGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList<File> listOutput;
	private  JList<File> list;
	private JTextArea textArea;

	public void setupFileChooser(JLabel inputPanel, final JList<?> listInput) {

	}

	private JPanel contentPane;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private DefaultListModel<File> inputListModel = new DefaultListModel<File>();
	private DefaultListModel<File> outputListModel = new DefaultListModel<File>();
	private ArrayList<File> files;
    private JLabel labelOutputDirectory;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RubusGUI frame = new RubusGUI();
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JRadioButton rdbtnAutoAnalysis;
	private JRadioButton rdbtnManualAnalysi;
	private JRadioButton rdbtnMenusalautoAnalysis;
	private JCheckBox exportSource;
	private JCheckBox openGeneratedFile;
	private JCheckBox showLogs;
	private JCheckBox clearDestinationBeforeRun;
	protected File outputDir;
	/**
	 * Create the frame.
	 */
	public RubusGUI() {
		setTitle("Rubus");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 758, 716);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setToolTipText("Delete all files from output directory before run");
		contentPane.add(panel, BorderLayout.EAST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Analysis",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		rdbtnAutoAnalysis = new JRadioButton("Auto");
		GridBagConstraints gbc_rdbtnAutoAnalysis = new GridBagConstraints();
		gbc_rdbtnAutoAnalysis.anchor = GridBagConstraints.WEST;
		gbc_rdbtnAutoAnalysis.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnAutoAnalysis.gridx = 0;
		gbc_rdbtnAutoAnalysis.gridy = 0;
		panel_1.add(rdbtnAutoAnalysis, gbc_rdbtnAutoAnalysis);
		rdbtnAutoAnalysis.setSelected(true);
		rdbtnAutoAnalysis.setToolTipText("Perform auto analysis only");
		buttonGroup.add(rdbtnAutoAnalysis);

		rdbtnManualAnalysi = new JRadioButton("Manual");
		GridBagConstraints gbc_rdbtnManualAnalysi = new GridBagConstraints();
		gbc_rdbtnManualAnalysi.anchor = GridBagConstraints.WEST;
		gbc_rdbtnManualAnalysi.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnManualAnalysi.gridx = 0;
		gbc_rdbtnManualAnalysi.gridy = 1;
		panel_1.add(rdbtnManualAnalysi, gbc_rdbtnManualAnalysi);
		rdbtnManualAnalysi
				.setToolTipText("Perform manual annotation based analysis");
		buttonGroup.add(rdbtnManualAnalysi);

		rdbtnMenusalautoAnalysis = new JRadioButton("Both");
		GridBagConstraints gbc_rdbtnMenusalautoAnalysis = new GridBagConstraints();
		gbc_rdbtnMenusalautoAnalysis.anchor = GridBagConstraints.WEST;
		gbc_rdbtnMenusalautoAnalysis.gridx = 0;
		gbc_rdbtnMenusalautoAnalysis.gridy = 2;
		panel_1.add(rdbtnMenusalautoAnalysis, gbc_rdbtnMenusalautoAnalysis);
		rdbtnMenusalautoAnalysis
				.setToolTipText("Perform both auto and manual analysis");
		buttonGroup.add(rdbtnMenusalautoAnalysis);
		rdbtnManualAnalysi.setSelected(true);
		

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Export",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		panel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		exportSource = new JCheckBox("Export Source");
		exportSource.setSelected(true);
		
		GridBagConstraints gbc_exportSource = new GridBagConstraints();
		gbc_exportSource.anchor = GridBagConstraints.WEST;
		gbc_exportSource.insets = new Insets(0, 0, 5, 0);
		gbc_exportSource.gridx = 0;
		gbc_exportSource.gridy = 0;
		panel_2.add(exportSource, gbc_exportSource);
		exportSource
				.setToolTipText("Export generated kernel and executor code");

		openGeneratedFile = new JCheckBox("Open Source File");
		openGeneratedFile.setSelected(true);
		GridBagConstraints gbc_openGeneratedFile = new GridBagConstraints();
		gbc_openGeneratedFile.anchor = GridBagConstraints.WEST;
		gbc_openGeneratedFile.gridx = 0;
		gbc_openGeneratedFile.gridy = 1;
		panel_2.add(openGeneratedFile, gbc_openGeneratedFile);
		openGeneratedFile
				.setToolTipText("Open generated kernel and executor file");

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Others",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 4;
		panel.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		showLogs = new JCheckBox("Show Logs");
		showLogs.setSelected(true);
		GridBagConstraints gbc_showLogs = new GridBagConstraints();
		gbc_showLogs.anchor = GridBagConstraints.WEST;
		gbc_showLogs.insets = new Insets(0, 0, 5, 0);
		gbc_showLogs.gridx = 0;
		gbc_showLogs.gridy = 0;
		panel_3.add(showLogs, gbc_showLogs);

		clearDestinationBeforeRun = new JCheckBox(
				"Clear Destination");
		GridBagConstraints gbc_clearDestinationBeforeRun = new GridBagConstraints();
		gbc_clearDestinationBeforeRun.anchor = GridBagConstraints.WEST;
		gbc_clearDestinationBeforeRun.gridx = 0;
		gbc_clearDestinationBeforeRun.gridy = 1;
		panel_3.add(clearDestinationBeforeRun, gbc_clearDestinationBeforeRun);

		JButton btnStart = new JButton("Compile");
		btnStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(files==null || files.size()==0){
					JOptionPane.showMessageDialog(RubusGUI.this, "Please select some input file(s)");
					return;
				}
				
				if(RubusGUI.this.outputDir ==null){
					JOptionPane.showMessageDialog(RubusGUI.this, "Please select output directory");
	        	return;
				}
				
				ArrayList<String> paramters = new ArrayList<String>();
				if(rdbtnAutoAnalysis.isSelected())
					paramters.add("-auto");
				else if(rdbtnManualAnalysi.isSelected())
					paramters.add("-manual");
				else if(rdbtnMenusalautoAnalysis.isSelected()){
					paramters.add("-auto");
					paramters.add("-manual");
				}
				
				
				if(exportSource.isSelected())
					paramters.add("-export");
				if(openGeneratedFile.isSelected())
					paramters.add("-open");
				
				if(showLogs.isSelected())
					paramters.add("-debug");
				if(clearDestinationBeforeRun.isSelected())
					paramters.add("-clean");
				
				try {
					paramters.add("--dist");
					paramters.add(outputDir.getCanonicalPath().toString());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				//String options =toString(paramters," "); 
			//	paramters.clear();
			//	paramters.add(options);
			//	String filesPath="";
				for(int i = 0 ; i < files.size() ; i++){
					//filesPath += files.get(i).getAbsolutePath() + ",";
					paramters.add( files.get(i).getAbsolutePath());
				}
				//filesPath = filesPath.substring(0, filesPath.length() - 1);
				
			//	paramters.add(filesPath);
				
				execute(paramters);
					
			}

//			private String toString(ArrayList<String> paramters, String seprator) {
//				if(paramters==null)
//				return null;
//				String str = "";
//				for (String option : paramters) {
//					str = str+option+seprator;
//				}
//				return str; 
//			}
		});
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridheight = 2;
		gbc_btnStart.ipady = 10;
		gbc_btnStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStart.insets = new Insets(0, 0, 5, 0);
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 7;
		panel.add(btnStart, gbc_btnStart);

		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					inputListModel.clear();
					RubusGUI.this.files = null;
					labelOutputDirectory.setText("<html><center>Drop input file(s)/directory here <br><br> ---------------OR--------------- <br><br>Click to select input file(s)</center></html>");

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.anchor = GridBagConstraints.BASELINE;
		gbc_btnClear.ipady = 10;
		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClear.gridx = 0;
		gbc_btnClear.gridy = 9;
		panel.add(btnClear, gbc_btnClear);

		JPanel panel_4 = new JPanel();
		contentPane.add(panel_4, BorderLayout.CENTER);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 0, 0 };
		gbl_panel_4.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "Input", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.insets = new Insets(0, 0, 5, 0);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 0;
		panel_4.add(panel_6, gbc_panel_6);
		panel_6.setLayout(new GridLayout(1, 0, 0, 0));

		JLabel lbldropInputDirectory = new JLabel(
				"<html><center>Drop input file(s)/directory here <br><br> ---------------OR--------------- <br><br>Click to select input file(s)</center></html>");
		lbldropInputDirectory.setHorizontalAlignment(SwingConstants.CENTER);
		panel_6.add(lbldropInputDirectory);

		list = new JList();
		list.setModel(inputListModel);
		list.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Input Files",
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(list);
		panel_6.add(scrollPane);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Output",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 1;
		panel_4.add(panel_5, gbc_panel_5);
		panel_5.setLayout(new GridLayout(1, 0, 0, 0));

		 labelOutputDirectory = new JLabel(
				"<html><center>Drop output directory here <br><br>--------------OR--------------<br><br> Click to select output directory</center></html>");
		labelOutputDirectory.setHorizontalAlignment(SwingConstants.CENTER);
		panel_5.add(labelOutputDirectory);
		JScrollPane scrollPane2 = new JScrollPane();

		listOutput = new JList();
		listOutput.setModel(outputListModel);
		listOutput.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Transformed Files",
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));

		scrollPane2.setViewportView(listOutput);
		panel_5.add(scrollPane2);

		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(null, "Logs", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		panel_7.setPreferredSize(new Dimension(200, 200));
		contentPane.add(panel_7, BorderLayout.SOUTH);
		panel_7.setLayout(new GridLayout(1, 1));

		textArea = new JTextArea();
		panel_7.add(textArea);

		new FileDrop(lbldropInputDirectory,true, new FileDrop.Listener() {

			@Override
			public void filesDropped(File[] files) {
                if(files==null || files.length==0) return;
				if(RubusGUI.this.files == null){
					RubusGUI.this.files = new ArrayList<File>();
				}
				RubusGUI.this.files.addAll(toList(files));
				inputListModel.clear();
				for (int i = 0; i < RubusGUI.this.files.size(); i++) {

					inputListModel.add(i, RubusGUI.this.files.get(i));
				}
				list.setModel(inputListModel);
			}

		});
		
		
		new FileDrop(labelOutputDirectory ,false, new FileDrop.Listener() {

			@Override
			public void filesDropped(File[] files) {
				if(files!=null && files.length>0){
				 RubusGUI.this.outputDir = files[0];
				 if(!RubusGUI.this.outputDir.isDirectory()){
					 RubusGUI.this.outputDir = RubusGUI.this.outputDir.getParentFile();
				 }
					 try {
						labelOutputDirectory.setText(toMultiLineHtml(outputDir.getCanonicalPath().toString(),35));
					} catch (IOException e) {
						e.printStackTrace();
					}
				 
				}
		
			}

		});

	}

	protected String toMultiLineHtml(String string, int i) {
		if(string==null)
		return null;
		 String multiLine = "<html>";
		for (int j = 0; j < string.length(); j++) {
			if(j%i == 0)
				multiLine+="<br>";
			multiLine+=string.charAt(j);
		}
		multiLine+="</html>";
		return multiLine;
	}

	protected Collection<? extends File> toList(File[] files) {
		if(files == null)
		return null;
		ArrayList<File>  list= new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			list.add(files[i]);
		}
		return list;
	}

	protected void execute(ArrayList<String> paramters) {
		System.out.println(paramters);
		Rubus.uiLogger = new UILogger() {
			
			@Override
			public void Log(String txt) {
			textArea.setText(textArea.getText()+"\n"+txt);				
			}
		};
		Rubus.transformedFilesLogger = new UILogger() {
			
			@Override
			public void Log(String txt) {
				
			}
		};
		try{
		Rubus.main(paramters.toArray(new String[paramters.size()]));
		}catch(Exception e){
			e.printStackTrace();
			Rubus.uiLogger.Log(e.getStackTrace().toString());
		}
	}
	

}
