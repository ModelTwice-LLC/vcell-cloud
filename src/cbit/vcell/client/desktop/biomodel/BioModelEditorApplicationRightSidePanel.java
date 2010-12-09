package cbit.vcell.client.desktop.biomodel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vcell.util.gui.EditorScrollTable;

import cbit.vcell.client.desktop.biomodel.BioModelEditor.BioModelEditorSelection;
import cbit.vcell.mapping.SimulationContext;

@SuppressWarnings("serial")
public abstract class BioModelEditorApplicationRightSidePanel<T> extends JPanel {
	protected static final String PROPERTY_NAME_SIMULATION_CONTEXT = "simulationContext";
	
	protected JButton addButton = null;
	protected JButton deleteButton = null;
	protected EditorScrollTable table;
	protected BioModelEditorApplicationRightSideTableModel<T> tableModel = null;
	protected SimulationContext simulationContext;
	protected JTextField textFieldSearch = null;
	protected BioModelEditorSelection bioModelEditorSelection = null;
	private InternalEventHandler eventHandler = new InternalEventHandler();
	
	private class InternalEventHandler implements ActionListener, PropertyChangeListener, DocumentListener, ListSelectionListener {

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == BioModelEditorApplicationRightSidePanel.this && evt.getPropertyName().equals(PROPERTY_NAME_SIMULATION_CONTEXT)) {
				simulationContextChanged();
			}
		}
		
		public void insertUpdate(DocumentEvent e) {
			searchTable();
		}

		public void removeUpdate(DocumentEvent e) {
			searchTable();
		}

		public void changedUpdate(DocumentEvent e) {
			searchTable();
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addButton) {
				newButtonPressed();
			} else if (e.getSource() == deleteButton) {
				deleteButtonPressed();
			}
		}
		
		public void valueChanged(ListSelectionEvent e) {
			if (simulationContext == null || e.getValueIsAdjusting()) {
				return;
			}
			if (e.getSource() == table.getSelectionModel()) {
				tableSelectionChanged();
			}
			
		}
	}
	
	public BioModelEditorApplicationRightSidePanel() {
		super();
		addPropertyChangeListener(eventHandler);
		initialize();
	}

	private void initialize(){
		addButton = new JButton("New");
		deleteButton = new JButton("Delete");
		textFieldSearch = new JTextField(10);
		table = new EditorScrollTable();
		tableModel = createTableModel();
		table.setModel(tableModel);

		addButton.addActionListener(eventHandler);
		deleteButton.addActionListener(eventHandler);
		deleteButton.setEnabled(false);
		textFieldSearch.getDocument().addDocumentListener(eventHandler);
		table.getSelectionModel().addListSelectionListener(eventHandler);
	}
	
	public void setSimulationContext(SimulationContext newValue) {
		SimulationContext oldValue = simulationContext;
		simulationContext = newValue;		
		firePropertyChange(PROPERTY_NAME_SIMULATION_CONTEXT, oldValue, newValue);
	}
	
	private void searchTable() {
		String text = textFieldSearch.getText();
		tableModel.setSearchText(text);
	}
	
	protected abstract BioModelEditorApplicationRightSideTableModel<T> createTableModel();
	protected abstract void newButtonPressed();
	protected abstract void deleteButtonPressed();
	
	protected void simulationContextChanged() {
		tableModel.setSimulationContext(simulationContext);
	}

	protected void tableSelectionChanged() {
		int[] rows = table.getSelectedRows();
		deleteButton.setEnabled(rows != null && rows.length > 0 && (rows.length > 1 || rows[0] < tableModel.getDataSize()));
		if (rows != null && rows.length == 1 && rows[0] < tableModel.getDataSize()) {
			setBioModelEditorSelection(new BioModelEditorSelection(simulationContext, tableModel.getValueAt(rows[0])));
		} else {
			setBioModelEditorSelection(new BioModelEditorSelection(simulationContext, null));
		}
	}
	
	public void select(T selection) {
		for (int i = 0; i < tableModel.getDataSize(); i ++) {
			if (tableModel.getValueAt(i) == selection) {
				table.setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	private final void setBioModelEditorSelection(BioModelEditorSelection newValue) {
		BioModelEditorSelection oldValue = this.bioModelEditorSelection;
		this.bioModelEditorSelection = newValue;
		firePropertyChange(BioModelEditor.PROPERTY_NAME_BIOMODEL_EDITOR_SELECTION, oldValue, newValue);
	}
}
