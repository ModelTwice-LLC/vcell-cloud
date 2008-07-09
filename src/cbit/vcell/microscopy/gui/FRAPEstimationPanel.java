package cbit.vcell.microscopy.gui;

import java.awt.Color;
import java.awt.Dimension;

import cbit.gui.DialogUtils;
import cbit.vcell.client.task.UserCancelException;
import cbit.vcell.math.gui.ExpressionCanvas;
import cbit.vcell.microscopy.FRAPData;
import cbit.vcell.microscopy.FRAPDataAnalysis;
import cbit.vcell.microscopy.FrapDataAnalysisResults;
import cbit.vcell.microscopy.ROI.RoiType;
import cbit.vcell.modelopt.gui.DataSource;
import cbit.vcell.modelopt.gui.MultisourcePlotPane;
import cbit.vcell.opt.ReferenceData;
import cbit.vcell.opt.SimpleReferenceData;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.ode.FunctionColumnDescription;
import cbit.vcell.solver.ode.ODESolverResultSet;
import cbit.vcell.solver.ode.ODESolverResultSetColumnDescription;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class FRAPEstimationPanel extends JPanel {
	private JTable table;
	private FRAPData initFRAPData;
	private JLabel plotOfAverageLabel;
	private JLabel frapModelParameterLabel;
	private MultisourcePlotPane multisourcePlotPane;
	private ExpressionCanvas expressionCanvas;
	private JComboBox bleachEstimationComboBox;
	private static final String PARAM_EST_EQUATION_STRING = "FRAP Model Parameter Estimation Equation";
	
	public static final String FRAP_PARAMETER_ESTIMATE_VALUES_PROPERTY = "FRAP_PARAMETER_ESTIMATE_VALUES_PROPERTY";
	
	private enum FRAPParameterEstimateEnum {
		DIFFUSION_RATE("Diffusion Rate","um^2/s"),
		MOBILE_FRACTION("Mobile Fraction","1/s"),
		IMMOBILE_FRATION("Immobile Fraction","1/s"),
		START_TIME_RECOVERY("Start Time Recovery","s"),;
		
	    private final String parameterTypeName;
	    private Double value;
	    private String unit;
	    
	    FRAPParameterEstimateEnum(String parameterTypeName,String unit) {
	       this.parameterTypeName = parameterTypeName;
	       this.unit = unit;
	    }
	};

	public static class FRAPParameterEstimateValues{
		public final Double diffusionRate;
		public final Double mobileFraction;
		public final Double startTimeRecovery;
		public FRAPParameterEstimateValues(Double diffusionRate,Double mobileFraction,Double startTimeRecovery){
			this.diffusionRate = diffusionRate;
			this.mobileFraction = mobileFraction;
			this.startTimeRecovery = startTimeRecovery;
		}
	};

	private static int PARAMETER_TYPE_COLUMN = 0;
	private static int VALUE_COLUMN = 1;
	private static int UNIT_COLUMN = 2;
	private static String[] FRAP_ESTIMATE_COLUMN_NAMES = new String[] {"Paramter Type","Estimated Value","Unit"};
	
	

	public FRAPEstimationPanel() {
		super();
		
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,7};
		gridBagLayout.columnWidths = new int[] {7,0};
		setLayout(gridBagLayout);

		final JLabel enterValuesUnderLabel = new JLabel();
		enterValuesUnderLabel.setFont(new Font("", Font.BOLD, 14));
		enterValuesUnderLabel.setText("Choose 'Estimation method' to calculate FRAP Model Paramter estimated values");
		final GridBagConstraints gridBagConstraints_30 = new GridBagConstraints();
		gridBagConstraints_30.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_30.gridy = 0;
		gridBagConstraints_30.gridx = 0;
		gridBagConstraints_30.gridwidth = 2;
		add(enterValuesUnderLabel, gridBagConstraints_30);

		final JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.black, 1, false));
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] {7,0};
		gridBagLayout_1.rowHeights = new int[] {7,7,7,7};
		panel.setLayout(gridBagLayout_1);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 1;
		add(panel, gridBagConstraints);

		final JLabel frapModelParameterLabel_1 = new JLabel();
		frapModelParameterLabel_1.setText("FRAP Model Parameter Estimates");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridwidth = 2;
		gridBagConstraints_3.gridx = 0;
		gridBagConstraints_3.gridy = 0;
		panel.add(frapModelParameterLabel_1, gridBagConstraints_3);

		final JLabel frapParameterEstimatesLabel = new JLabel();
		frapParameterEstimatesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frapParameterEstimatesLabel.setText("Estimation method");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_1.fill = GridBagConstraints.BOTH;
		gridBagConstraints_1.weightx = 0;
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.gridx = 0;
		panel.add(frapParameterEstimatesLabel, gridBagConstraints_1);

		bleachEstimationComboBox = new JComboBox();
		bleachEstimationComboBox.setPreferredSize(new Dimension(225, 25));
		bleachEstimationComboBox.setMinimumSize(new Dimension(225, 25));
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_2.weightx = 1;
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 1;
		gridBagConstraints_2.gridx = 1;
		panel.add(bleachEstimationComboBox, gridBagConstraints_2);

		final JScrollPane scrollPane = new JScrollPane();
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_4.fill = GridBagConstraints.BOTH;
		gridBagConstraints_4.weighty = 1;
		gridBagConstraints_4.weightx = 1;
		gridBagConstraints_4.gridwidth = 2;
		gridBagConstraints_4.gridy = 2;
		gridBagConstraints_4.gridx = 0;
		panel.add(scrollPane, gridBagConstraints_4);

		table = new JTable();
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(table);

		final JButton applyEstimatedValuesButton = new JButton();
		applyEstimatedValuesButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Object[][] rowData = new Object[3][FRAP_ESTIMATE_COLUMN_NAMES.length];
				
				rowData[0][0] =
					FRAPParameterEstimateEnum.DIFFUSION_RATE.parameterTypeName;
				rowData[1][0] =
					FRAPParameterEstimateEnum.MOBILE_FRACTION.parameterTypeName;
				rowData[2][0] =
					FRAPParameterEstimateEnum.START_TIME_RECOVERY.parameterTypeName;
				
				rowData[0][1] =
					FRAPParameterEstimateEnum.DIFFUSION_RATE.value;
				rowData[1][1] =
					FRAPParameterEstimateEnum.MOBILE_FRACTION.value;
				rowData[2][1] =
					FRAPParameterEstimateEnum.START_TIME_RECOVERY.value;

				try{
					int[] result = DialogUtils.showComponentOKCancelTableList(FRAPEstimationPanel.this, "Choose Estimated values to Apply FRAP Model Parameters",
							FRAP_ESTIMATE_COLUMN_NAMES, rowData, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					if(result != null && result.length > 0){
						Double selectedDiffusionRate = null;
						Double selectedMobileFraction = null;
						Double selectedStartTimeRecovery = null;
						
						for (int j = 0; j < result.length; j++) {
							switch (result[j]) {
							case 0:
								selectedDiffusionRate = FRAPParameterEstimateEnum.DIFFUSION_RATE.value;
								break;
							case 1:
								selectedMobileFraction = FRAPParameterEstimateEnum.MOBILE_FRACTION.value;
								break;
							case 2:
								selectedStartTimeRecovery = FRAPParameterEstimateEnum.START_TIME_RECOVERY.value;
								break;
							default:
								break;
							}
						}
						FRAPParameterEstimateValues frapParameterEstimateValues =
							new FRAPParameterEstimateValues(
								selectedDiffusionRate,
								selectedMobileFraction,
								selectedStartTimeRecovery
							);
						firePropertyChange(FRAP_PARAMETER_ESTIMATE_VALUES_PROPERTY, null, frapParameterEstimateValues);
					}
				}catch(UserCancelException e2){
					//ignore
				}
			}
		});
		applyEstimatedValuesButton.setText("Apply Estimated values to Initial FRAP Model Parameters...");
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_5.gridwidth = 2;
		gridBagConstraints_5.gridy = 3;
		gridBagConstraints_5.gridx = 0;
		panel.add(applyEstimatedValuesButton, gridBagConstraints_5);

		final JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(Color.black, 1, false));
		panel_1.setPreferredSize(new Dimension(375, 250));
		panel_1.setMinimumSize(new Dimension(375, 250));
		final GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.rowHeights = new int[] {0,7};
		panel_1.setLayout(gridBagLayout_2);
		final GridBagConstraints gridBagConstraints_24 = new GridBagConstraints();
		gridBagConstraints_24.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_24.gridy = 1;
		gridBagConstraints_24.gridx = 0;
		add(panel_1, gridBagConstraints_24);

		frapModelParameterLabel = new JLabel();
		frapModelParameterLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frapModelParameterLabel.setText(PARAM_EST_EQUATION_STRING);
		final GridBagConstraints gridBagConstraints_25 = new GridBagConstraints();
		gridBagConstraints_25.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_25.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_25.weighty = 0;
		gridBagConstraints_25.gridy = 0;
		gridBagConstraints_25.gridx = 0;
		panel_1.add(frapModelParameterLabel, gridBagConstraints_25);

		expressionCanvas = new ExpressionCanvas();
		final GridBagConstraints gridBagConstraints_26 = new GridBagConstraints();
		gridBagConstraints_26.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_26.fill = GridBagConstraints.BOTH;
		gridBagConstraints_26.weighty = 1;
		gridBagConstraints_26.weightx = 1;
		gridBagConstraints_26.gridy = 1;
		gridBagConstraints_26.gridx = 0;
		panel_1.add(expressionCanvas, gridBagConstraints_26);

		plotOfAverageLabel = new JLabel();
		plotOfAverageLabel.setFont(new Font("", Font.BOLD, 14));
		plotOfAverageLabel.setText("Plot -  'Bleach' ROI average (Experiment and Estimated) vs. Time");
		final GridBagConstraints gridBagConstraints_29 = new GridBagConstraints();
		gridBagConstraints_29.insets = new Insets(20, 4, 4, 4);
		gridBagConstraints_29.gridwidth = 2;
		gridBagConstraints_29.gridy = 2;
		gridBagConstraints_29.gridx = 0;
		add(plotOfAverageLabel, gridBagConstraints_29);

		multisourcePlotPane = new MultisourcePlotPane();
		multisourcePlotPane.setModelDataLabelPrefix("Estimated_");
		multisourcePlotPane.setRefDataLabelPrefix("Data_");
		multisourcePlotPane.setBorder(new LineBorder(Color.black, 1, false));
		multisourcePlotPane.setListVisible(false);
		final GridBagConstraints gridBagConstraints_27 = new GridBagConstraints();
		gridBagConstraints_27.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_27.fill = GridBagConstraints.BOTH;
		gridBagConstraints_27.weighty = 1;
		gridBagConstraints_27.weightx = 1;
		gridBagConstraints_27.gridwidth = 2;
		gridBagConstraints_27.gridy = 3;
		gridBagConstraints_27.gridx = 0;
		add(multisourcePlotPane, gridBagConstraints_27);
		
		initialize();
	}

	private void initTable(){
		TableModel tableModel = 
			new AbstractTableModel() {
			    public String getColumnName(int col) {
			        return FRAP_ESTIMATE_COLUMN_NAMES[col].toString();
			    }
			    public int getRowCount() {
			    	return FRAPParameterEstimateEnum.values().length; }
			    public int getColumnCount() {
			    	return FRAP_ESTIMATE_COLUMN_NAMES.length; }
			    public Object getValueAt(int row, int col) {
			    	if(col == PARAMETER_TYPE_COLUMN){
			    		return FRAPParameterEstimateEnum.values()[row].parameterTypeName;
			    	}else if(col == UNIT_COLUMN){
			    		return FRAPParameterEstimateEnum.values()[row].unit;
			    	}
			        return FRAPParameterEstimateEnum.values()[row].value;
			    }
			    public boolean isCellEditable(int row, int col){
			    	return false;
			    }
			    public void setValueAt(Object value, int row, int col) {
			    	if(col == PARAMETER_TYPE_COLUMN || col == UNIT_COLUMN){
			    		throw new IllegalArgumentException("Can't update 'Parameter Type' or 'Unit' column");
			    	}
			    	FRAPParameterEstimateEnum.values()[row].value = (Double)value;
			        fireTableCellUpdated(row, col);
			    }
			};
		table.setModel(tableModel);
	}
	private void initialize(){
		initTable();
			
		for (int i = 0; i < FrapDataAnalysisResults.BLEACH_TYPE_NAMES.length; i++) {
			bleachEstimationComboBox.insertItemAt(/*"Estimation method '"+*/"'"+FrapDataAnalysisResults.BLEACH_TYPE_NAMES[i]+"'", i);
		}
		bleachEstimationComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if(bleachEstimationComboBox.getSelectedIndex() == FrapDataAnalysisResults.BleachType_CirularDisk){
					//expression on canvas
					try{
						String[] prefixes = new String[] { "I(t) = ", "D = " };
						Expression[] expressions = new Expression[] { new Expression(FRAPDataAnalysis.circularDisk_IntensityFunc_display), new Expression(FRAPDataAnalysis.circularDisk_DiffFunc) };
						String[] suffixes = new String[] { "", "[um2.s-1]" };
						expressionCanvas.setExpressions(expressions,prefixes,suffixes);
					}catch (ExpressionException e2){
						e2.printStackTrace(System.out);
					}					
				}else if(bleachEstimationComboBox.getSelectedIndex() == FrapDataAnalysisResults.BleachType_GaussianSpot){
					//expression on canvas
					try{
						String[] prefixes = new String[] { "I(t) = ", "u(t)= ","D = " };
						Expression[] expressions = new Expression[] { new Expression(FRAPDataAnalysis.gaussianSpot_IntensityFunc), new Expression(FRAPDataAnalysis.gaussianSpot_MuFunc), new Expression(FRAPDataAnalysis.gaussianSpot_DiffFunc) };
						String[] suffixes = new String[] { "", "", "[um2.s-1]" };
						expressionCanvas.setExpressions(expressions,prefixes,suffixes);
					}catch (ExpressionException e2){
						e2.printStackTrace(System.out);
					}
					
				}else if(bleachEstimationComboBox.getSelectedIndex() == FrapDataAnalysisResults.BleachType_HalfCell){
					//expression on canvas
					try{
						String[] prefixes = new String[] { "I(t) = ", "u(t)= ","D = " };
						Expression[] expressions = new Expression[] { new Expression(FRAPDataAnalysis.halfCell_IntensityFunc), new Expression(FRAPDataAnalysis.halfCell_MuFunc), new Expression(FRAPDataAnalysis.halfCell_DiffFunc) };
						String[] suffixes = new String[] { "", "", "[um2.s-1]" };
						expressionCanvas.setExpressions(expressions,prefixes,suffixes);
					}catch (ExpressionException e2){
						e2.printStackTrace(System.out);
					}
				}
				frapModelParameterLabel.setText(
					PARAM_EST_EQUATION_STRING+"  ('"+
					FrapDataAnalysisResults.BLEACH_TYPE_NAMES[bleachEstimationComboBox.getSelectedIndex()]+"')");
//				plotOfAverageLabel.setText(
//					PLOT_TITLE_STRING+"  ('"+
//					FrapDataAnalysisResults.BLEACH_TYPE_NAMES[bleachEstimationComboBox.getSelectedIndex()]+"')");
				try{
					refreshFRAPModelParameterEstimates(initFRAPData);
				}catch (Exception e2){
					e2.printStackTrace();
					DialogUtils.showErrorDialog(
						"Error setting estimation method "+
						FrapDataAnalysisResults.BLEACH_TYPE_NAMES[bleachEstimationComboBox.getSelectedIndex()]+
						"\n"+e2.getMessage());
				}
			}
		});
		bleachEstimationComboBox.setSelectedIndex(FrapDataAnalysisResults.BleachType_CirularDisk);
	}
	private int getBleachTypeMethod(){
		return bleachEstimationComboBox.getSelectedIndex();
	}

	private void displayFit(FrapDataAnalysisResults frapDataAnalysisResults,double[] frapDataTimeStamps){
		if (frapDataAnalysisResults == null){
			FRAPParameterEstimateEnum.DIFFUSION_RATE.value = null;
			FRAPParameterEstimateEnum.MOBILE_FRACTION.value = null;
			FRAPParameterEstimateEnum.IMMOBILE_FRATION.value = null;
			FRAPParameterEstimateEnum.START_TIME_RECOVERY.value = null;
			multisourcePlotPane.setDataSources(null);
		}else{
			FRAPParameterEstimateEnum.DIFFUSION_RATE.value =
				(frapDataAnalysisResults.getRecoveryDiffusionRate() == null
					?null
					:frapDataAnalysisResults.getRecoveryDiffusionRate());
			FRAPParameterEstimateEnum.MOBILE_FRACTION.value =
					(frapDataAnalysisResults.getMobilefraction() == null
						?null
						:frapDataAnalysisResults.getMobilefraction());
			FRAPParameterEstimateEnum.IMMOBILE_FRATION.value =
				(FRAPParameterEstimateEnum.MOBILE_FRACTION.value == null
					?null
					:1.0 - FRAPParameterEstimateEnum.MOBILE_FRACTION.value);

			double[] bleachRegionData = frapDataAnalysisResults.getBleachRegionData();
			int startIndexForRecovery = frapDataAnalysisResults.getStartingIndexForRecovery();
			Expression fittedCurve = frapDataAnalysisResults.getFitExpression();
			ReferenceData expRefData = new SimpleReferenceData(new String[] { "t", "BleachROIAvg" }, new double[] { 1.0, 1.0 }, new double[][] { frapDataTimeStamps, bleachRegionData });
			DataSource expDataSource = new DataSource(expRefData,"experiment");
			ODESolverResultSet fitOdeSolverResultSet = new ODESolverResultSet();
			fitOdeSolverResultSet.addDataColumn(new ODESolverResultSetColumnDescription("t"));
			try {
				fitOdeSolverResultSet.addFunctionColumn(
					new FunctionColumnDescription(
						fittedCurve,
						"('"+FrapDataAnalysisResults.BLEACH_TYPE_NAMES[bleachEstimationComboBox.getSelectedIndex()]+"')",
						null,"recoveryFit",true));
			} catch (ExpressionException e) {
				e.printStackTrace();
			}
			for (int i = startIndexForRecovery; i < frapDataTimeStamps.length; i++) {
				fitOdeSolverResultSet.addRow(new double[] { frapDataTimeStamps[i] });
			}
			//
			// extend if necessary to plot theoretical curve to 4*tau
			//
			double T = frapDataTimeStamps[frapDataTimeStamps.length-1];
			double deltaT = frapDataTimeStamps[frapDataTimeStamps.length-1]-frapDataTimeStamps[frapDataTimeStamps.length-2];
			while (T+deltaT < 6*frapDataAnalysisResults.getRecoveryTau()){
				fitOdeSolverResultSet.addRow(new double[] { T } );
				T += deltaT;
			}
			DataSource fitDataSource = new DataSource(fitOdeSolverResultSet, "fit");
			multisourcePlotPane.setDataSources(new DataSource[] {  expDataSource, fitDataSource } );
			multisourcePlotPane.selectAll();		
		}
		table.repaint();
	}
	
	public void refreshFRAPModelParameterEstimates(FRAPData frapData) throws Exception {
		this.initFRAPData = frapData;
		FrapDataAnalysisResults frapDataAnalysisResults = null;
		double[] frapDataTimeStamps = null;
		bleachEstimationComboBox.setEnabled(false);
		if(frapData != null){
			if(frapData.getRoi(RoiType.ROI_BLEACHED).isAllPixelsZero()){
				displayFit(null,null);
				throw new Exception(
					OverlayEditorPanelJAI.INITIAL_BLEACH_AREA_TEXT+" ROI not defined.\n"+
					"Use ROI tools under '"+FRAPStudyPanel.FRAPDATAPANEL_TABNAME+"' tab to define.");
			}
			frapDataTimeStamps = frapData.getImageDataset().getImageTimeStamps();
			frapDataAnalysisResults =
				FRAPDataAnalysis.fitRecovery2(frapData, getBleachTypeMethod());
			FRAPParameterEstimateEnum.START_TIME_RECOVERY.value = frapDataTimeStamps[frapDataAnalysisResults.getStartingIndexForRecovery()];
			bleachEstimationComboBox.setEnabled(true);
		}

		displayFit(frapDataAnalysisResults,frapDataTimeStamps);
	}
}
