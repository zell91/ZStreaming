package com.zstreaming.gui.download;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;

public class DownloadTableRow extends TableRow<DownloadValues> {
	
	private Node selectedNode;
	
	public DownloadTableRow() {
		super();
		this.setOnMouseDragged(e->multiSelectionPolicy(e));
		this.emptyProperty().addListener(emptyListener());
	}
	
	private ChangeListener<? super Boolean> emptyListener() {		
		return new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {				
				if(!newValue) {	
					DownloadValues downloadValues = getItem();
					
					downloadValues.getProgressWrapper().getChildren().forEach(child-> child.setOnMouseClicked(e->selectionPolicy(e)));
				}
			}
		};
	}
	
	private void selectionPolicy(MouseEvent event) {
		Node n = event.getPickResult().getIntersectedNode().getParent().getParent().getParent().getParent();

		if(n.equals(this)) this.getTableView().getSelectionModel().clearAndSelect(this.getIndex());
	}	
	
	
	
	private void multiSelectionPolicy(MouseEvent event) {
		try {	
			Node _node = event.getPickResult().getIntersectedNode().getParent();

			if(_node instanceof TableRow) {
				if(this.selectedNode != null && this.selectedNode.equals(_node)) return;

				this.selectedNode = _node;

				@SuppressWarnings("unchecked")
				TableRow<DownloadValues> tableRow = (TableRow<DownloadValues>) selectedNode;

				if(this.getTableView().getSelectionModel().isSelected(tableRow.getIndex() - 1)) {
					this.getTableView().getSelectionModel().clearSelection(tableRow.getIndex() - 1);
				}else
					this.getTableView().getSelectionModel().select(tableRow.getItem());
			}
		}catch(NullPointerException ex) {	}	
	}
}
