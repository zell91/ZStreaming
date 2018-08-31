package com.zstreaming.history;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.zstreaming.history.FilterHistory.Type;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.media.Media;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class HistoryFactory {
	
	private MediaHistory mediaHistory;
	private ObservableMap<Integer, List<HistoryEntry>> pages;
	private VBox root;
	private HBox buttonBox;
	private Label page;

	private BooleanProperty selectionMode = new SimpleBooleanProperty();
	private BooleanProperty searchMode = new SimpleBooleanProperty();
	private BooleanProperty selected = new SimpleBooleanProperty();
	
	public HistoryFactory(MediaHistory history) {
		this.mediaHistory = history;
		this.root = new VBox(5);
		this.root.setAlignment(Pos.TOP_CENTER);
		this.root.setId("history");
		VBox.setVgrow(this.root, Priority.ALWAYS);

		this.createButtonBox();
		this.setPages();		

		this.mediaHistory.changeHistoryProperty().addListener((observable, oldValue, newValue)->{
			if(newValue) {
				this.setPages();
			}
		});
	}
	
	public StringProperty pageHistoryTextProperty() {
		return ((Text)this.page.getGraphic()).textProperty();
	}
	
	public BooleanProperty selectedProperty() {
		return this.selected;
	}
	
	public boolean isSearchMode() {
		return this.searchMode.get();
	}

	public BooleanProperty searchModeProperty() {
		return this.searchMode ;
	}

	public void setSearchMode(boolean searchMode) {
		Platform.runLater(()->this.searchMode.set(searchMode));
	}
	
	public BooleanProperty selectionModeProperty() {
		return this.selectionMode;
	}
	
	public boolean isSelectionMode() {
		return this.selectionMode.get();
	}
	
	public void setSelectionMode(boolean selectionMode) {
		Platform.runLater(()->this.selectionMode.set(selectionMode));
	}

	public ObservableMap<Integer, List<HistoryEntry>> getPages() {
		return this.pages;
	}	

	public List<HistoryEntry> getPage(int page) {
		return this.pages.get(page);
	}
	
	public int getPagesSize() {
		return this.pages.size();
	}
	
	public VBox getNode() {
		return this.root;
	}	

	private List<HistoryEntry> getPageFiltered(int num, FilterHistory filter) {
		return this.getPagesFiltered(filter).get(num);
	}
	
	private ObservableMap<Integer, List<HistoryEntry>>  getPagesFiltered(FilterHistory filter) {
		SortedMap<LocalDate, List<HistoryEntry>> fullHistory = this.mediaHistory.getHistory();
		ObservableMap<Integer, List<HistoryEntry>> filteredPages = FXCollections.observableMap(new HashMap<>());
		
		AtomicInteger remain = new AtomicInteger(50);
		AtomicInteger index = new AtomicInteger(1);
		
		for(Entry<LocalDate, List<HistoryEntry>> entry : fullHistory.entrySet()) {
			List<HistoryEntry> filteredList = entry.getValue().stream().filter(history->{
					String query = filter.getQuery();
					LocalDate date = filter.getDate();
					boolean bool = true;		
					
					for(Type t : filter.getTypes()) {
						if(t.equals(Type.URL) || t.equals(Type.NAME) || t.equals(Type.HOSTER)) {
							bool = false;
							break;
						}
					}
					
					for(FilterHistory.Type type : filter.getTypes()) {
						switch(type) {
							case URL:
								bool = bool ||  history.getSource().toString().toLowerCase().contains(query.toLowerCase());
								break;
							case NAME:
								if(history.getMedia().getName() != null) {
									String toCompare = history.getMedia().getName().replaceAll("\\.", " ").toLowerCase();
									bool = bool ||  (history.getMedia().getName().toLowerCase().contains(query.toLowerCase()) || toCompare.contains(query.toLowerCase()));
								}else
									bool = bool || false;
								break;
							case MIN_UNO_MB:
								if(history.getMedia().getSize() != null) 
									bool = bool && history.getMedia().getSize().getRealSize() < 1048576;
								else 
									return false;
								break;
							case UNO_MB_50_MB:
								if(history.getMedia().getSize() != null)
									bool = bool && (history.getMedia().getSize().getRealSize() > 1048576 && history.getMedia().getSize().getRealSize() < 52428800);
								else 
									return false;
								break;
							case CINQUANTA_MB_DUECENTOCINQUANTA_MB:
								if(history.getMedia().getSize() != null) 
									bool = bool && (history.getMedia().getSize().getRealSize() > 52428800 && history.getMedia().getSize().getRealSize() < 262144000);
									else 
										return false;
								break;
							case DUECENTOCINQUANTA_MB_CINQUECENTO_MB:
								if(history.getMedia().getSize() != null) 
									bool = bool && (history.getMedia().getSize().getRealSize() > 262144000 && history.getMedia().getSize().getRealSize() < 524288000);
								else 
									return false;
								break;
							case CINQUECENTO_MB_UNO_GB:
								if(history.getMedia().getSize() != null)
									bool = bool && (history.getMedia().getSize().getRealSize() > 524288000 && history.getMedia().getSize().getRealSize() < 1073741824);
								else 
									return false;
								break;
							case GREAT_UNO_GB:
								if(history.getMedia().getSize() != null)
									bool = bool && history.getMedia().getSize().getRealSize() > 1073741824;
									else 
										return false;									
								break;								
							case HOSTER:
								if(history.getMedia().getHoster() != null)
									bool = bool ||  history.getMedia().getHoster().toLowerCase().contains(query.toLowerCase());
								break;
							case SUCCESSED:
								bool = bool && history.getMedia().isAvalaible(); 
								break;
							case FAILED:
								bool = bool && !history.getMedia().isAvalaible();
								break;
							case DATE:
								bool = bool && date.equals(history.getDate().toLocalDate());								
								break;
							default:
								return true;
						}
					}
					return bool;
				}).collect(Collectors.toList());
						
			this.fillPage(index, remain, filteredList, filteredPages);
		}
		return filteredPages;
	}
	
	private void setPages(){
		SortedMap<LocalDate, List<HistoryEntry>> fullHistory = this.mediaHistory.getHistory();
		this.pages = FXCollections.observableMap(new HashMap<>());
		
		AtomicInteger remain = new AtomicInteger(50);
		AtomicInteger index = new AtomicInteger(1);
		
		for(Entry<LocalDate, List<HistoryEntry>> entry : fullHistory.entrySet()) {
			this.fillPage(index, remain, entry.getValue(), this.pages);
		}
	}
		
	private void fillPage(AtomicInteger index, AtomicInteger remain, List<HistoryEntry> value, Map<Integer, List<HistoryEntry>> pages) {
		List<HistoryEntry> _history = new ArrayList<>();			
		List<HistoryEntry> list = new ArrayList<>(value);
				
		if(value.size() == 0) {
			return;
		}
		
		if(remain.get()  == 0) {
			remain.set(50);
			index.incrementAndGet();
			this.fillPage(index, remain, value, pages);
			return;
		}
		
		int offset = Math.min(value.size() , remain.get());
		
		_history.addAll(value.subList(0, offset));
		list.removeAll(_history);

		
		if(pages.containsKey(index.get()))
			pages.get(index.get()).addAll(_history);
		else
			pages.put(index.get(), _history);
		
		remain.set(50 - pages.get(index.get()).size());
				
		this.fillPage(index, remain, list, pages);	
	}
	
	
	private void createButtonBox() {
		this.buttonBox = new HBox(10);
		this.buttonBox.setAlignment(Pos.CENTER);
		this.page = new Label();
		this.page.setGraphic(new Text());
		Button prev = new Button();
		Button next = new Button();
								
		this.buttonBox.getStyleClass().add("buttons-history-pages");
		this.page.getStyleClass().add("history-page-lbl");
		prev.getStyleClass().add("prev-page-history-btn");
		next.getStyleClass().add("next-page-history-btn");
		
		this.buttonBox.getChildren().addAll(prev, this.page, next);		
		this.root.getChildren().add(this.buttonBox);
		
		VBox.setVgrow(this.buttonBox, Priority.NEVER);
		
		prev.setOnAction(e->{
			if(this.root != null) {
				Object[] userData = (Object[]) this.root.getUserData();
				
				int index = (int)userData[0];
				
				if(userData.length > 1) {
					FilterHistory filter = (FilterHistory)userData[1];
					this.requestPage(index - 1, filter);
				}else
					this.requestPage(index - 1);
			}
		});
		
		next.setOnAction(e->{
			if(this.root != null) {
				Object[] userData = (Object[]) this.root.getUserData();
				
				int index = (int)userData[0];
				
				if(userData.length > 1) {
					FilterHistory filter = (FilterHistory)userData[1];
					this.requestPage(index + 1, filter);
				}else
					this.requestPage(index + 1);
			}
		});
		
		this.root.getChildren().addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(Change<? extends Node> value) {				
				Object[] userData = (Object[]) root.getUserData();
				
				int index = (int)userData[0];
				
				prev.setDisable(index == 1);

				if(userData.length > 1) {
					FilterHistory filter = (FilterHistory)userData[1];
					next.setDisable(index == getPagesFiltered(filter).size());
				}else {
					next.setDisable(index == getPagesSize());
				}
			
				if(root.getScene() != null) {					
					if(value.next() && value.wasAdded()) {
						if(value.getAddedSubList().get(0) instanceof VBox) return;
					}else if(value.wasRemoved()) {
						if(value.getRemoved().get(0) instanceof VBox) return;
					}
					((ScrollPane)root.getScene().lookup("#historyScrollWrapper")).setVvalue(0.0);
				}
			}
		});
	}
	
	public void requestPage(int page) {
		this.requestPage(page, null);
	}
	
	public void requestPage(int num, FilterHistory filter) {
		Object[] userData = null;
		
		if(filter != null)
			userData = new Object[] {num, filter};
		else
			userData = new Object[] {num};

		this.root.getChildren().retainAll(this.buttonBox);
		this.root.setUserData(userData);	
			
		List<HistoryEntry> page = null;
		
		if(filter != null) {
			page = this.getPageFiltered(num, filter);
		}else {
			page = this.getPage(num);
		}
		
		LocalDate currentDay = null;
		
		if(page != null) {
			for(HistoryEntry entry : page) {				
				if(currentDay == null || currentDay.compareTo(entry.getDate().toLocalDate()) != 0) {
					currentDay = entry.getDate().toLocalDate();
					String nameDay = currentDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
					String day = String.format("%s %2d %s %d", nameDay.substring(0, 1).toUpperCase() + nameDay.substring(1), currentDay.getDayOfMonth(), currentDay.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), currentDay.getYear());
					Label dayLbl = new Label(day);

					this.root.getChildren().add(dayLbl);
					dayLbl.setId("dayLbl");
				}
				
				this.createEntryNode(entry);
			}
			this.buttonBox.toFront();
			this.page.setText(String.format("%d", num));
		}
		
		ZStreaming.gcClean(1000);
	}

	private void createEntryNode(HistoryEntry entry) {		
		Media media = entry.getMedia();
		
		GridPane wrapper = new GridPane();
		GridPane box = new GridPane();

		box.setBackground(new Background(new BackgroundFill[] {new BackgroundFill(Color.WHITE, null, null)}));
		box.setPadding(new Insets(20, 10, 20, 10));
		
		Label timeLbl = new Label(entry.getDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
		Label sourceLbl = new Label(String.format("%s", entry.getSource().toString()));
		Button rescanBtn = new Button();		
		
		rescanBtn.setMinWidth(Region.USE_PREF_SIZE);
		timeLbl.setMinWidth(Region.USE_PREF_SIZE);
		sourceLbl.setMinWidth(0.0);
		
		wrapper.setAlignment(Pos.CENTER);
		
		wrapper.setHgap(0);
		box.setHgap(10);
		box.setVgap(0);
		box.setAlignment(Pos.CENTER_LEFT);
		box.add(timeLbl, 0, 0, 1, 3);				
		box.add(sourceLbl, 1, 0, 1, 1);
		box.add(rescanBtn, 3, 0, 1, 3);
		box.setMinWidth(0.0);
		box.setMaxWidth(700.0);
		box.setMinHeight(110);
					
		if(media.isAvalaible()) {
			Label nameLbl = new Label(media.getName() );
			Label sizeLbl = new Label(media.getSize().optimizeSize());
			
			nameLbl.setMinWidth(0.0);
			sizeLbl.setMinWidth(0.0);
			
			box.add(nameLbl, 1, 0, 1, 1);
			box.add(sizeLbl, 1, 2, 1, 1);

			GridPane.setRowIndex(sourceLbl, 1);
			
			nameLbl.getStyleClass().addAll("history-text", "history-name-lbl", "found-history-text");
			sizeLbl.getStyleClass().addAll("history-text", "found-history-text");
			box.getStyleClass().add("found");			
		}else {
			box.getStyleClass().add("not-found");
		}
		
		ColumnConstraints col0 = new ColumnConstraints();
		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();

		col0.setHgrow(Priority.NEVER);
		col1.setHgrow(Priority.ALWAYS);
		col2.setHgrow(Priority.ALWAYS);
				
		RowConstraints row0 = new RowConstraints();
		RowConstraints row1 = new RowConstraints();
		RowConstraints row2 = new RowConstraints();
		
		GridPane.setValignment(rescanBtn, VPos.CENTER);


		GridPane.setHalignment(rescanBtn, HPos.RIGHT);		
		
		box.getColumnConstraints().addAll(col0, col1, col2);
		box.getRowConstraints().addAll(row0, row1, row2);
		
		CheckBox checkbox = new CheckBox();
		
		checkbox.setUserData(entry);
		
		GridPane.setConstraints(checkbox, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.ALWAYS, new Insets(0));
		GridPane.setConstraints(box, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.ALWAYS, new Insets(0));

		wrapper.getColumnConstraints().addAll(col0, col1);
		wrapper.getChildren().addAll(checkbox, box);
		wrapper.setGridLinesVisible(false);
		wrapper.maxWidthProperty().bind(box.maxWidthProperty().add(checkbox.getWidth() + wrapper.getHgap()));
				
		wrapper.setOnMousePressed(e->{
			if(e.getButton().equals(MouseButton.PRIMARY)) {
				Thread waitTask = new Thread(()->{
					try {
						synchronized(Thread.currentThread()) {
							Thread.currentThread().wait(500);
						}					
						this.setSelectionMode(true);		
					} catch (InterruptedException e1) {	}																
				});			
				
				wrapper.setOnMouseReleased(ev->{
					if(ev.getButton().equals(MouseButton.PRIMARY)) {
						if(waitTask.isAlive()) waitTask.interrupt();
						if(this.isSelectionMode()) {
							checkbox.setSelected(!checkbox.isSelected());
							this.changeSelect();
						}
					}
				});
							
				waitTask.setDaemon(true);
				waitTask.start();
			}
		});
	
		checkbox.visibleProperty().bind(this.selectionMode);
		box.disableProperty().bind(checkbox.visibleProperty());
		
		checkbox.visibleProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue) {
				checkbox.setSelected(false);
				this.changeSelect();
			}
		});
		
		checkbox.setOnAction(e->this.changeSelect());
	
		
		checkbox.getStyleClass().add("history-check-box");
		box.getStyleClass().add("history-entry");		
		timeLbl.getStyleClass().addAll("history-text", "history-time-lbl");
		sourceLbl.getStyleClass().addAll("history-text", "history-source-lbl");
		rescanBtn.getStyleClass().addAll("history-rescan-btn", "history-btn");
		wrapper.getStyleClass().add("history-entry-wrapper");
		wrapper.setGridLinesVisible(false);

		for(Node n : box.getChildren()) {
			if(n instanceof Button) {
				Button button = (Button)n;
								
				button.setOnMousePressed(e->wrapper.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false));
			}
		}
		
		this.root.getChildren().add(wrapper);
	}

	public void changeSelect() {
		this.selected.set(true);
		this.selected.set(false);
	}		
}
