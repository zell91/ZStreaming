package com.zstreaming.gui.download;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.util.locale.ObservableResourceBundle;
import com.util.size.Size;
import com.util.size.SizePerSecond;
import com.util.time.TimeAdjuster;
import com.zstreaming.download.Download;
import com.zstreaming.download.DownloadActivity;
import com.zstreaming.download.DownloadManager;
import com.zstreaming.download.DownloadTask;
import com.zstreaming.gui.components.PriorityIndicator;
import com.zstreaming.gui.components.StatisticsChart;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class DownloadValues {
		
	private Label nameLabel, stateLabel, speedLabel, sizeLabel, workDoneLabel, sizeRemainLabel, timeRemainLabel;
	private Text hosterText, pathText;
	private StackPane progressWrapper;
	private PriorityIndicator priorityIndicator;
	
	private SimpleObjectProperty<Integer> index;	

	private Download download;
	private DownloadActivity activity;
	private ObservableResourceBundle bundleFactory;
			
	public DownloadValues(Download download) {
		this.download = download;
		this.activity = new DownloadActivity(download);
		this.bundleFactory = new ObservableResourceBundle();
		this.createNameLabel();
		this.createStateLabel();
		this.createProgressBar();
		this.speedLabel = this.createSizeLabel(new SizePerSecond());
		this.sizeLabel = this.createSizeLabel(download.getMedia().getSize());
		this.sizeRemainLabel = this.createSizeLabel(new Size());
		this.workDoneLabel = this.createSizeLabel(new Size());
		this.createTimeRemainLabel();
		this.createPriorityIndicator();
		this.hosterText = new Text(download.getMedia().getHoster());
		this.pathText = new Text(download.getNewDestination() != null ? download.getNewDestination().getParent() : download.getDestination().getParent());		
		this.index = new SimpleObjectProperty<Integer>(DownloadManager.getDownloads().indexOf(DownloadManager.getDownloads().get(download) ) + 1);
	}	

	public ObservableResourceBundle getBundleFactory() {
		return this.bundleFactory;
	}

	/*
	 * Name
	 */
	
	private void createNameLabel() {
		File dest = download.getNewDestination() != null ? download.getNewDestination() : download.getDestination();
		this.nameLabel = new Label(dest.getName().endsWith(DownloadTask.DOWNLOAD_EXTENTION) ? dest.getName().split(DownloadTask.DOWNLOAD_EXTENTION)[0] : dest.getName());		
		
		ImageView imgName = new ImageView(new Image(new File("images/down.png").toURI().toString()));
		
		this.nameLabel.setGraphic(imgName);
		
		imgName.setFitWidth(16);
		imgName.setFitHeight(16);		
	}	
	
	public Label getNameLabel() {
		return this.nameLabel;
	}
	
	public void setImageName(String source) {
		Platform.runLater(()->((ImageView)this.nameLabel.getGraphic()).setImage(new Image(source)));
	}

	public void setName(String name) {
		Platform.runLater(()->this.nameLabel.setText(name));
	}
	
	public String getName() {
		return this.nameLabel.getText();
	}
	
	public StringProperty nameProperty() {
		return this.nameLabel.textProperty();
	}
	
	
	/*
	 * State 
	 */
	
	private void createStateLabel() {
		this.stateLabel = new Label();
		
		ImageView imgState = new ImageView();

		this.stateLabel.setGraphic(imgState);

		imgState.setFitWidth(16);
		imgState.setFitHeight(16);	
	}
	
	public Label getStateLabel() {
		return this.stateLabel;
	}	
	
	public void setImageState(String source) {
		if(source != null) {
			Platform.runLater(()->((ImageView)this.stateLabel.getGraphic()).setImage(new Image(source)));
		}else {
			Platform.runLater(()->((ImageView)this.stateLabel.getGraphic()).setImage(null));
		}
	}
	
	public void setState(String state, Locale locale) {
		this.bundleFactory.setResources(locale);
		Platform.runLater(()->this.stateLabel.setText(this.bundleFactory.getString(state)));		
	}
	
	public void setState(String state) {
		Platform.runLater(()->this.stateLabel.setText(state));
	}
	
	public String getState() {
		return this.stateLabel.getText();
	}
	
	public StringProperty stateProperty() {
		return (StringProperty) this.stateLabel.textProperty();
	}
	
	
	/*
	 * Progress
	 */	
	
	private void createProgressBar() {
		SimpleDoubleProperty progress = new SimpleDoubleProperty();

		this.progressWrapper = new StackPane();
		this.progressWrapper.setUserData(progress);		
		this.progressWrapper.getStyleClass().add("progress_wrapper");
		this.progressWrapper.setAlignment(Pos.CENTER);
		ProgressBar progressBar = new ProgressBar(0.0);		
		progressBar.getStyleClass().add("download_progress");
		progressBar.progressProperty().bind(progress);
		progressBar.setMaxWidth(Integer.MAX_VALUE);

		Text text = new Text("0.0%");
		text.getStyleClass().add("percentage_text");
		
		progressBar.progressProperty().bind(progress);
		text.textProperty().bind(progress.multiply(100.0).asString("%.1f").concat("%"));
				
		this.progressWrapper.getChildren().addAll(progressBar, text);
	}
	
	public void setProgress(double progress) {
		Platform.runLater(()->this.getProgress().set(progress));
	}		
	
	public StackPane getProgressWrapper() {
		return this.progressWrapper;
	}
	
	public SimpleDoubleProperty getProgress() {
		return (SimpleDoubleProperty) this.progressWrapper.getUserData();
	}
	
	
	/*
	 * SIZE OBJECT
	 */
	
	private Label createSizeLabel(Size userObj) {		
		Label label = new Label();		
		label.setUserData(userObj);
		label.textProperty().bind(userObj.sizeProperty());

		return label;
	}
	
	
	/*
	 * Speed
	 */
	
	public void setSpeed(double speed) {		
		Platform.runLater(()->this.getSpeed().setSize(speed));
	}
	
	public Label getSpeedLabel() {
		return this.speedLabel;
	}
	
	public SizePerSecond getSpeed() {
		return (SizePerSecond) this.speedLabel.getUserData();
	}
	
	
	/*
	 * Size
	 */
	
	public void setSize(double size) {		
		Platform.runLater(()->this.getSize().setSize(size));
	}
	
	public Label getSizeLabel() {
		return this.sizeLabel;
	}
	
	public Size getSize() {
		return (Size) this.sizeLabel.getUserData();
	}
	
	
	/*
	 * WorkDone
	 */
	
	public void setWorkDone(double workDone) {
		Platform.runLater(()->this.getWorkDone().setSize(workDone));
	}
	
	public Label getWorkDoneLabel() {
		return this.workDoneLabel;
	}
	
	public Size getWorkDone() {
		return (Size) this.workDoneLabel.getUserData();
	}
	
	
	/*
	 * RemainSize
	 */
	
	public void setSizeRemain(double workDone) {		
		Platform.runLater(()->this.getSizeRemain().setSize(workDone));
	}
	
	public Label getSizeRemainLabel() {
		return this.sizeRemainLabel;
	}
	
	public Size getSizeRemain() {
		return (Size) this.sizeRemainLabel.getUserData();
	}
	
	
	/*
	 * TimeRemain
	 */	
	
	private void createTimeRemainLabel() {
		this.timeRemainLabel = new Label();
		TimeAdjuster time = new TimeAdjuster();
		this.timeRemainLabel.setUserData(time);
		this.timeRemainLabel.textProperty().bind(time.timeProperty());
	}
	
	public Label getTimeRemainLabel() {
		return this.timeRemainLabel;
	}
	
	public void setTimeRemain(double timeRemain) {
		Platform.runLater(()->this.getTimeRemain().set(timeRemain));
	}
	
	public TimeAdjuster getTimeRemain() {
		return (TimeAdjuster) this.timeRemainLabel.getUserData();
	}
	
	
	/*
	 * Priority Indicator
	 */
	
	private void createPriorityIndicator() {
		SimpleDoubleProperty priority = new SimpleDoubleProperty();
		this.priorityIndicator = new PriorityIndicator(0.0);
		this.priorityIndicator.setUserData(priority);
		this.priorityIndicator.setPIHeight(20);
		this.priorityIndicator.setPIWidth(35);
		this.priorityIndicator.setPadding(new Insets(1,0,1,0));
		this.priorityIndicator.valueProperty().bindBidirectional(priority);
		
		priority.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				download.setPriority(Download.Priority.valueOf(newValue.doubleValue()));
			}			
		});
	}
	
	public void setPriority(double priority) {
		Platform.runLater(()->this.getPriority().set(priority));
	}		
	
	public PriorityIndicator getPriorityIndicator() {
		return this.priorityIndicator;
	}
	
	public SimpleDoubleProperty getPriority() {
		return (SimpleDoubleProperty) this.priorityIndicator.getUserData();
	}
	
	/*
	 * Hoster
	 */
	
	public Text getHosterText() {
		return this.hosterText;
	}

	public void setHoster(String hoster) {
		Platform.runLater(()->this.hosterText.setText(hoster));
	}
	
	public String getHoster() {
		return this.hosterText.getText();
	}
	
	public StringProperty hosterProperty() {
		return this.hosterText.textProperty();
	}
	
	/*
	 *Path 
	 */
	
	public Text getPathText() {
		return this.pathText;
	}
	
	public void setPath(String path) {
		Platform.runLater(()->this.pathText.setText(path));
	}
	
	public String getPath() {
		return pathText.getText();
	}
	
	public StringProperty pathProperty() {
		return pathText.textProperty();
	}
	
	/*
	 * Index
	 */
	
	public void setIndex(int index) {
		Platform.runLater(()->this.index.set(index));
	}

	public SimpleObjectProperty<Integer> getIndex() {
		return this.index;
	}
	
	public void updateStyleClass(String styleClass) {
		this.removeStyleClass();
		this.progressWrapper.getStyleClass().add(styleClass);
		this.nameLabel.getStyleClass().add(styleClass);
		this.stateLabel.getStyleClass().add(styleClass);
		this.sizeLabel.getStyleClass().add(styleClass);
		this.sizeRemainLabel.getStyleClass().add(styleClass);
		this.workDoneLabel.getStyleClass().add(styleClass);
		this.speedLabel.getStyleClass().add(styleClass);
		this.timeRemainLabel.getStyleClass().add(styleClass);
		this.hosterText.getStyleClass().add(styleClass);
		this.pathText.getStyleClass().add(styleClass);
	}
	
	private void removeStyleClass() {
		String[] styleClasses = new String[] {"progressed", "interrupted", "completed"};		

		for(String clzz : styleClasses) {
			this.progressWrapper.getStyleClass().remove(clzz);
			this.nameLabel.getStyleClass().remove(clzz);
			this.stateLabel.getStyleClass().remove(clzz);
			this.sizeLabel.getStyleClass().remove(clzz);
			this.sizeRemainLabel.getStyleClass().remove(clzz);
			this.workDoneLabel.getStyleClass().remove(clzz);
			this.speedLabel.getStyleClass().remove(clzz);
			this.timeRemainLabel.getStyleClass().remove(clzz);
			this.hosterText.getStyleClass().remove(clzz);
			this.pathText.getStyleClass().remove(clzz);
		}
	}
	
	public void updateStyle(Download.State state) {		
		switch(state) {
			case INTERRUPTED:
				Platform.runLater(()->this.updateStyleClass("interrupted"));
				this.progressWrapper.setVisible(false);
				this.priorityIndicator.setVisible(false);
				break;
			case COMPLETED:
				Platform.runLater(()->this.updateStyleClass("completed"));
				this.priorityIndicator.setVisible(false);
				break;
			case IN_PROGRESS:
				Platform.runLater(()->this.updateStyleClass("progressed"));
				break;
			default:
				Platform.runLater(()->this.removeStyleClass());
		}
	}	
	
	public void bindChart(List<StatisticsChart> enabledListChart) {	
		if(DownloadActivity.isSingleChartActive()) {			
			Platform.runLater(()->{
				for(StatisticsChart chart : enabledListChart) {
					if(!chart.isBound()) {
						chart.bind(this.activity);
						chart.setTitle(this.getName());
						break;
					}
				}
			});
		}
	}
	
	public void unbindChart(List<StatisticsChart> enabledListChart) {		
		Platform.runLater(()->{
			StatisticsChart prevChart = null;

			for(StatisticsChart chart : enabledListChart) {
				if(chart.isBound()) {
					if(prevChart  != null) {
						prevChart.bind(chart.getActivityBound());
						prevChart.setTitle(chart.getTitle());
						prevChart = chart;
						prevChart.unbind();
					}else {
						if(chart.getDataBound().equals(this.activity.getDataChart())) {
							prevChart = chart;
							prevChart.unbind();
						}
					}					
				}
			}
			
			enabledListChart.stream().filter(chart->!chart.isBound()).forEach(chart->chart.setTitle("Download #" + enabledListChart.indexOf(chart)));
		});
	}
	
	public DownloadActivity getActivity() {
		return this.activity;
	}

	public Download getDownload() {
		return download;
	}
	
	public void setProgressValues() {
		this.updateStyle(download.getState());
		if(this.download.isDone() ) {
			this.setSizeRemain(-1);
			
			if(this.download.isCompleted()) {
				this.setState("completed.capitalize", Locale.getDefault());
				this.setImageName(new File("images/dw_cell.png").toURI().toString());
				this.setImageState(new File("images/found2.png").toURI().toString());
				this.setWorkDone(this.getDownload().getMedia().getSize().getRealSize());
				this.setPriority(-1);
				this.setProgress(1.0);
			}else if(this.download.isInterrupted()) {
				this.setImageName(new File("images/down_disabled.png").toURI().toString());
				this.setImageState(new File("images/not_found2.png").toURI().toString());
				this.setState("not.found", Locale.getDefault());
				this.setWorkDone(-1);
				this.setPriority(-1.0);
				this.setProgress(-1.0);
			}else {
				this.setState("");
				this.setPriority(this.download.getPriority().getValue());
				this.setProgress(0.0);
			}
		}else {
			if(this.download.isStopped()) {
				this.setImageName(new File("images/paused.png").toURI().toString());
			}	
			this.setState("");
			this.setWorkDone(this.getDownload().getProgress().getCurrentSize());
			this.setSizeRemain(this.getDownload().getProgress().getRemainingSize());
			this.setPriority(this.download.getPriority().getValue());
			this.setProgress(this.download.getProgress().getPercentage());
		}

	}		
	
	public Comparator<DownloadValues> getComparator(TableColumn<DownloadValues, ?> col) {
		return (x,y)->{						
			if(col.getCellData(x) instanceof Node) {
				Node nodeX = (Node)col.getCellData(x);
				Node nodeY = (Node)col.getCellData(y);
				
				if(nodeX.getUserData() instanceof Size) {
					return Double.compare(((Size)nodeX.getUserData()).getRealSize(), ((Size)nodeY.getUserData()).getRealSize());
				}else if(nodeX.getUserData() instanceof ObservableNumberValue) {
					return Double.compare(((ObservableNumberValue)nodeX.getUserData()).doubleValue(), ((ObservableNumberValue)nodeY.getUserData()).doubleValue());
				}else if(nodeX.getUserData() instanceof ObservableStringValue) {
					return ((ObservableStringValue)nodeX.getUserData()).get().compareTo(((ObservableStringValue)nodeY.getUserData()).get());
				}else {
					return nodeX.getUserData().toString().compareTo(nodeY.getUserData().toString());
				}							
			}else {
				return col.getCellData(x).toString().compareTo(col.getCellData(y).toString());
			}						
		};
	}
}

