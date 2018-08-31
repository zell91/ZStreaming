package com.zstreaming.gui.components;

import com.util.locale.ObservableResourceBundle;
import com.util.size.SizePerSecond;
import com.util.time.TimeAdjuster;
import com.zstreaming.download.DownloadActivity;
import com.zstreaming.gui.components.contextmenu.ScaleChoicerContextMenu;
import com.zstreaming.launcher.ZStreaming;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class StatisticsChart extends VBox {
	
	private HBox titleWrapper;
	private ToolBar chartToolbar;
	private ComboBox<String> comboBox;
	private Label titleLbl, toolLbl;
	private Button minusBtn, plusBtn;
	private LineChart<Number, Number> chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	
	private int yTick;
	
	private ObservableList<Series<Number, Number>> seriesList;
	private DownloadActivity activityBound;

	private ListChangeListener<Data<Number, Number>> listener;
	private BooleanProperty bound;
	private boolean binding;
	private boolean toolbarVisible;
	private ScaleChoicerContextMenu contextMenu;
	
	private ObservableResourceBundle bundleFactory;
	
	public StatisticsChart(int yTick, ObservableResourceBundle bundleFactory) {
		this(null, yTick, false, false, bundleFactory);
	}
	
	public StatisticsChart(int yTick, boolean binding, ObservableResourceBundle bundleFactory) {
		this(null, yTick, false, binding, bundleFactory);
	}	
	
	public StatisticsChart(int yTick, boolean toolbar, boolean binding, ObservableResourceBundle bundleFactory) {
		this(null, yTick, toolbar, binding, bundleFactory);
	}
	
	public StatisticsChart(int yTick) {
		this(null, yTick, false, false, null);
	}
	
	public StatisticsChart(int yTick, boolean binding) {
		this(null, yTick, false, binding, null);
	}	
	
	public StatisticsChart(int yTick, boolean toolbar, boolean binding) {
		this(null, yTick, toolbar, binding, null);
	}
	
	public StatisticsChart(String title, int yTick, boolean toolbar, boolean binding) {
		this(title, yTick, toolbar, binding, null);
	}
	
	public StatisticsChart(String title, int yTick, boolean toolbar, boolean bind, ObservableResourceBundle bundleFactory) {
		super(0.0);
		this.bundleFactory = bundleFactory;
		this.toolbarVisible = toolbar;
		this.binding = bind;
		this.bound = new SimpleBooleanProperty(false);
		this.listener = new ListChangeListener<Data<Number, Number>>() {
			@Override
			public void onChanged(Change<? extends Data<Number, Number>> value) {
				if(value.next() && value.wasAdded()) {
					if(binding) getFirstSeries().getData().addAll(value.getAddedSubList());
					setUpperBoundY();
				}
			}			
		};
		
		this.yTick = yTick;
		this.getStyleClass().add("chart-wrapper");
		this.setupTitle(title);
		this.setupToolBar();
		this.setupChart();
		this.setContextMenu();
	}
		
	public void setBinding(boolean binding) {
		if(binding) this.getFirstSeries().getData().removeListener(this.listener);
		this.binding = binding;
	}
	
	public boolean isBound() {
		return this.bound.get();
	}
	
	public ObservableBooleanValue boundProperty() {
		return this.bound;
	}

	private void setupTitle(String title) {
		if(title != null) {
			this.titleWrapper = new HBox();
			this.titleWrapper.getStyleClass().add("title-chart-wrapper");
			this.titleLbl = new Label(title);
			this.titleLbl.getStyleClass().add("chart-title");			
			this.titleWrapper.getChildren().add(this.titleLbl);
			this.titleWrapper.setAlignment(Pos.TOP_RIGHT);
			this.getChildren().add(0, this.titleWrapper);
		}
	}
	
	private void setupToolBar() {
		if(this.chartToolbar == null) {
			this.chartToolbar = new ToolBar();
			this.toolLbl = new Label();
			
			
			this.minusBtn = new Button("-");
			this.plusBtn = new Button("+");	
			this.comboBox = new ComboBox<>();
			
			this.toolLbl.getStyleClass().add("tool-label");
			this.comboBox.getStyleClass().add("scale-choicer");
			this.minusBtn.getStyleClass().add("minus-btn");
			this.plusBtn.getStyleClass().add("plus-btn");
			this.chartToolbar.getStyleClass().add("chart-toolbar");
			
			this.chartToolbar.getItems().addAll(toolLbl, this.comboBox, this.minusBtn, this.plusBtn);
			
			this.comboBox.setOnAction(e->this.changeScale());
			this.plusBtn.setOnAction(e->this.increseScale());
			this.minusBtn.setOnAction(e->this.decreseScale());
			
			if(this.bundleFactory != null) {	
				this.toolLbl.textProperty().bind(this.bundleFactory.getStringBindings("time.range"));
				this.bundleFactory.getResourcesProperty().addListener((observable, oldValue, newValue)->this.comboBox.setItems(this.comboBoxItems()));
				this.comboBox.setValue((String.format("%d %s", 12, this.bundleFactory.getString("minutes"))));
				this.comboBox.setItems(this.comboBoxItems());
			}else {
				this.comboBox.setValue("12 minuti");
				this.comboBox.setItems(this.comboBoxItems());
			}
		}
		
		if(this.toolbarVisible) {
			if(this.getChildren().size() > 1)
				this.getChildren().add(1, this.chartToolbar);
			else
				this.getChildren().add(this.chartToolbar);
		}else {			
			this.getChildren().removeIf(item->item.equals(this.chartToolbar));
		}
	}
	
	public void setToolbarVisible(boolean toolbarVisible) {
		if(this.toolbarVisible != toolbarVisible) {
			this.toolbarVisible = toolbarVisible;			
			this.setupToolBar();
		}
	}
	
	public ComboBox<?> getScaleChoicer() {
		return this.comboBox;
	}
	
	private ObservableList<String> comboBoxItems() {
		if(this.bundleFactory != null) {	
			return FXCollections.observableArrayList(String.format("%d %s", 2, this.bundleFactory.getString("minutes")),
													 String.format("%d %s", 6, this.bundleFactory.getString("minutes")),
													 String.format("%d %s", 12, this.bundleFactory.getString("minutes")),
													 String.format("%d %s", 36, this.bundleFactory.getString("minutes")),
													 String.format("%d %s", 2, this.bundleFactory.getString("hours")),
													 String.format("%d %s", 6, this.bundleFactory.getString("hours")),
													 String.format("%d %s", 12, this.bundleFactory.getString("hours")),
													 String.format("%d %s", 24, this.bundleFactory.getString("hours")));
		}else {
			return FXCollections.observableArrayList( "2 minuti",
													  "6 minuti",
													  "12 minuti",
												  	  "36 minuti",
													  "2 ore",
													  "6 ore",
													  "12 ore",
													  "24 ore");					
		}

	}

	private void setupChart() {
		this.xAxis = new NumberAxis(-720, 0, 60);
		this.xAxis.setSide(Side.BOTTOM);
		this.xAxis.setAutoRanging(false);
		this.xAxis.setMinorTickVisible(false);
		this.xAxis.setTickLength(5);
		this.xAxis.setTickLabelGap(5);
		this.xAxis.getStyleClass().add("statX");
		
		this.yAxis = new NumberAxis(0, 1048576, 1048576/this.yTick);
		this.yAxis.setSide(Side.LEFT);
		this.yAxis.setAutoRanging(false);
		this.yAxis.setMinorTickVisible(false);
		this.yAxis.setTickLength(5);
		this.yAxis.setTickLabelGap(3);
		this.yAxis.getStyleClass().add("statY");
		
		this.chart = new LineChart<Number, Number>(this.xAxis, this.yAxis);
		this.chart.setCreateSymbols(false);
		this.chart.setAnimated(false);
		this.chart.setLegendVisible(false);
		this.chart.getStyleClass().add("stat-chart");
		this.setBoundsAndFormatter();
		
		this.seriesList = FXCollections.observableArrayList();
		this.seriesList.add(new Series<Number, Number>());
		if(!this.binding) this.seriesList.get(0).getData().addListener(this.listener);
		
		this.chart.getData().setAll(this.seriesList);
		
		VBox.setVgrow(this.chart, Priority.ALWAYS);
		
		this.getChildren().add(this.chart);
	}
	
	private void setContextMenu() {
		this.contextMenu = new ScaleChoicerContextMenu(this.chart, this.comboBox);

		this.chart.setOnContextMenuRequested(e->{
			this.contextMenu.show(this.chart, e.getScreenX(), e.getScreenY());
		});
	}
	
	public ObservableList<Series<Number, Number>> getSeries(){
		return this.seriesList;
	}
	
	public Series<Number, Number> getFirstSeries(){
		return this.seriesList.isEmpty() ? null : this.seriesList.get(0);
	}
	
	public void bind(DownloadActivity activity) {
		this.activityBound = activity;
		this.getFirstSeries().getData().addAll(this.getDataBound());
		this.getDataBound().addListener(this.listener);				
		this.bound.set(true);
	}
	
	public void unbind(){		
		this.getDataBound().removeListener(this.listener);
		this.bound.set(false);
		this.activityBound = null;
		this.getFirstSeries().getData().clear();
		ZStreaming.gcClean(1000);
	}
	
	public DownloadActivity getActivityBound() {
		return this.activityBound;
	}

	public ObservableList<Data<Number, Number>> getDataBound() {
		return this.activityBound != null ? this.activityBound.getDataChart() : null;
	}
	
	public void setTickUnitX(double tickUnit) {
		this.xAxis.setTickUnit(tickUnit);
	}
	
	private void setBoundsAndFormatter() {
		this.xAxis.lowerBoundProperty().addListener((observable, oldValue, newValue)->{
			if(this.chart.getData().size() > 0) {
				this.setUpperBoundY();
			}
		});
		
		if(this.yTick > 0) {
			this.yAxis.tickUnitProperty().bind(this.yAxis.upperBoundProperty().divide(this.yTick));
		}

		this.xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public Number fromString(String string) {
				return null;
			}

			@Override
			public String toString(Number time) {
				TimeAdjuster ta = new TimeAdjuster(time.longValue());
				ta.set(-(ta.getSeconds()));
				return ta.optimizeTime();
			}		
		});
		
		this.yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public Number fromString(String string) {				
				String s = string.split(" ")[0].replace(",", ".");
				String unit = string.split("/")[0];
				unit = unit.replaceAll(s + " ", "");

				if(unit.toUpperCase().startsWith("B")) {
					return Double.parseDouble(s);
				}
				if(unit.toUpperCase().startsWith("K")) {
					double kilobyte = Double.parseDouble(s);
					return kilobyte*1024;
				}
				if(unit.toUpperCase().startsWith("M")) {
					double megabyte = Double.parseDouble(s);
					return megabyte*1024*1024;
				}
				if(unit.toUpperCase().startsWith("G")) {
					double gigabyte = Double.parseDouble(s);
					return gigabyte*1024*1024*1024;
				}				
				return null;
			}

			@Override
			public String toString(Number number) {
				SizePerSecond size = new SizePerSecond(number.longValue());	
				return size.optimizeSize();
			}		
		});			
	}
		
	private void setUpperBoundY() {
		try {
			if(this.chart.getData().get(0).getData().size() > 0) {
				double upperYData = Math.max(102400.0,
											chart.getData().get(0).getData()
												.stream()
												.filter(data->data.getXValue().doubleValue() > this.xAxis.getLowerBound())
												.sorted((x,y)->-(Double.compare(x.getYValue().doubleValue(), y.getYValue().doubleValue())))
												.findFirst().get().getYValue().doubleValue());
				
				if(this.yAxis.getUpperBound() != upperYData*1.5) {
					this.yAxis.setUpperBound(upperYData * 1.5);
				}
			}
		}catch(Exception ex) {}
	}
	
	public void changeScale() {		
		int value = this.comboBox.getSelectionModel().getSelectedIndex();

		double lowerBound;
		double tickUnit;
		switch(value) {
			case 0:
				lowerBound = -120;
				tickUnit = 10;
				break;
			case 1:
				lowerBound = -360;
				tickUnit = 30;
				break;
			case 2:
				lowerBound = -720;
				tickUnit = 60;
				break;
			case 3:
				lowerBound = -2160;
				tickUnit = 180;
				break;
			case 4:
				lowerBound = -7200;
				tickUnit = 600;
				break;
			case 5:
				lowerBound = -21600;
				tickUnit = 1800;
				break;
			case 6:
				lowerBound = -43200;
				tickUnit = 3600;
				break;
			case 7:
				lowerBound = -86400;
				tickUnit = 7200;
				break;
			default:
				return;
		}
		
		this.xAxis.setLowerBound(lowerBound);
		this.xAxis.setTickUnit(tickUnit);
		
		if(this.minusBtn.isDisable() != (this.comboBox.getSelectionModel().getSelectedIndex() == 0))
			this.minusBtn.setDisable(this.comboBox.getSelectionModel().getSelectedIndex() == 0);
		
		if(this.plusBtn.isDisable() != (this.comboBox.getSelectionModel().getSelectedIndex() == this.comboBox.getItems().size() - 1))
			this.plusBtn.setDisable(this.comboBox.getSelectionModel().getSelectedIndex() == this.comboBox.getItems().size() - 1);		
	}
	
	private void increseScale( ) {
		ObservableList<String> options = this.comboBox.getItems();
		
		for(int i=0;i<options.size() - 1;i++) {
			if(this.comboBox.getValue().equals(options.get(i))) {
				this.comboBox.getSelectionModel().select(options.get(i+1));
				if(i == options.size() - 1) this.plusBtn.setDisable(true);
				this.minusBtn.setDisable(false);
				break;
			}
		}
	}
	
	private void decreseScale() {		
		ObservableList<String> options = comboBox.getItems();
		
		for(int i=options.size() - 1;i >= 1;i--) {
			if(comboBox.getValue().equals(options.get(i))) {
				comboBox.getSelectionModel().select(options.get(i-1));
				if(i == 1) this.minusBtn.setDisable(true);
				this.plusBtn.setDisable(false);
				break;
			}
		}
	}
		
	public void setTitle(String title) {
		if(this.getChildren().contains(this.titleWrapper)) {
			if(title != null)
				this.titleLbl.setText(title);
			else 
				this.getChildren().remove(this.titleWrapper);
		} else {
			if(this.titleWrapper != null) {
				if(title != null) this.titleLbl.setText(title);
				this.getChildren().add(0, this.titleWrapper);
			}else
				this.setupTitle(title);
		}
	}
	
	public LineChart<Number, Number> getChart() {
		return this.chart;
	}

	public String getTitle() {
		return this.titleLbl.textProperty().get();
	}

	public StringProperty titleProperty() {
		return this.titleLbl.textProperty();
	}
	
	public void disable(boolean disable) {
		super.setDisable(disable);
		
		for(Node node : this.getChildren()) {
			node.setDisable(disable);
		}		
	}
}

