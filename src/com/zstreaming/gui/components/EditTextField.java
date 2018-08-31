package com.zstreaming.gui.components;

import java.io.File;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class EditTextField extends GridPane{
	
	private Button editBtn;	
	private TextArea textField;
	private Label title;
	
	private IntegerProperty maxLengthProperty;
	private ObjectProperty<TextAlignment> textAlignmnetProperty;
	private BooleanProperty resizableProperty;
	private ObjectProperty<EventHandler<ActionEvent>> onAction;
	private ObjectProperty<VPos> editBtnValignment;
		
	public EditTextField() {
		super();
		this.editBtnValignment = new SimpleObjectProperty<>(VPos.CENTER);
		this.maxLengthProperty = new SimpleIntegerProperty(-1);
		this.resizableProperty = new SimpleBooleanProperty();
		this.textAlignmnetProperty = new SimpleObjectProperty<>(TextAlignment.LEFT);
		this.onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>() {	       
			@Override 
	        protected void invalidated() {
	            setEventHandler(ActionEvent.ACTION, get());
	        }
	        
			@Override
			public Object getBean() {
				return this;
			}

			@Override
			public String getName() {
				return "onAction";
			}
		};
		
		
		this.title = new Label();
		this.editBtn = new Button();
		this.textField = new TextArea() {
			
			@Override
			public void paste() {
				if(getMaxLength() < 0) {
					super.paste();
					return;
				}
				int length = this.getText().length() - this.getSelectedText().length();
				
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = null;

				if(clipboard.hasString()) {
					final String text = clipboard.getString();					
					String _text = text.replaceAll("\r\n", " ");

					if((_text.length() + length) > getMaxLength()) {
						int _l = getMaxLength() - length;						
						_text = _text.substring(0, _l);	
					}
					
					if(!_text.isEmpty()) {
						content = new ClipboardContent();
						content.putString(_text);
						clipboard.setContent(content);
					}else {
						return;
					}
					
					super.paste();					
					
					if(content != null) {
						content = new ClipboardContent();
						content.putString(text);
						clipboard.setContent(content);
					}	
				}			
			}
		};
		
		this.editBtnValignment.addListener((observable, oldValue, newValue)->{
			GridPane.setValignment(this.editBtn, newValue);			
		});
				
		this.setupTitle();
		this.setupEditButton();
		this.setupTextField();
		
		this.setPrefWidth(0);
		this.setMinWidth(0);
		
		this.title.getStyleClass().add("edit-text-title");
		this.editBtn.getStyleClass().add("edit-text-btn");
		this.textField.getStyleClass().add("edit-text-field");
		this.getStyleClass().add("edit-text-field-wrapper");
		
		
		this.setConstraints();		
		this.getChildren().addAll(this.textField, this.editBtn);
		
	}
	
	private void setConstraints() {
		GridPane.setConstraints(this.title, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER, new Insets(0, 10, 0, 10));
		GridPane.setConstraints(this.textField, 0, 1, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS,  new Insets(0));
		GridPane.setConstraints(this.editBtn, 1, 1, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER,  new Insets(0));

		RowConstraints row0 = new RowConstraints();
		RowConstraints row1 = new RowConstraints();

		ColumnConstraints col0 = new ColumnConstraints();
		ColumnConstraints col1 = new ColumnConstraints();

		row0.setVgrow(Priority.NEVER);
		row1.setVgrow(Priority.ALWAYS);
		
		col0.setHgrow(Priority.ALWAYS);
		col1.setHgrow(Priority.NEVER);
		
		this.getRowConstraints().addAll(row0, row1);
		this.getColumnConstraints().addAll(col0, col1);		
	}

	private void setupTitle() {
		this.titleProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null && !newValue.isEmpty()) {
				if(!this.getChildren().contains(this.title)) {
					GridPane.setRowIndex(this.editBtn, 0);
					GridPane.setColumnSpan(this.textField, 2);
					this.getChildren().add(this.title);
				}
			}else {
				if(this.getChildren().contains(this.title)) {
					GridPane.setRowIndex(this.editBtn, 1);
					GridPane.setColumnSpan(this.textField, 1);
					this.getChildren().remove(this.title);
				}
			}
		});

		this.title.setPadding( new Insets(0, 0, -10, 0));
	}

	private void setupEditButton() {
		ImageView imageView = new ImageView(new Image("file:" + new File("images/edit_pen.png").getAbsolutePath(), true));
		imageView.setPreserveRatio(true);
		this.editBtn.setGraphic(imageView);
		this.editBtn.setBackground(Background.EMPTY);
		this.editBtn.setCursor(Cursor.HAND);
		this.editBtn.setOnAction(e->this.textField.setEditable(true));
	}	

	private void setupTextField() {
		this.textField.setEditable(false);
		this.textField.setBackground(Background.EMPTY);	
		this.textField.setWrapText(true);
		this.textField.setMinWidth(0);

		this.textField.selectedTextProperty().addListener((observable, oldValue, newValue)->{
			if(!this.textField.isEditable()) {
				this.textField.deselect();
			}
		});	

		this.textField.skinProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null) {
				Text text = (Text) ((ScrollPane)((TextArea)newValue.getNode()).getChildrenUnmodifiable().get(0)).getContent().lookup("Text");
				ScrollPane scrollPane = (ScrollPane)this.textField.getChildrenUnmodifiable().get(0);
				Region content = (Region) scrollPane.getContent();
				ImageView graphicBtn = (ImageView) this.editBtn.getGraphic();

				this.textField.setCursor(Cursor.DEFAULT);
				this.textField.getSkin().getNode().setCursor(Cursor.DEFAULT);				
				
				scrollPane.skinProperty().addListener((obs, oldVal, newVal)->{
					((Region)scrollPane.getChildrenUnmodifiable().get(0)).setBackground(Background.EMPTY);
					this.textField.getSkin().getNode().lookup(".content").setCursor(Cursor.DEFAULT);
					this._layout();
				});
				
				scrollPane.setBackground(Background.EMPTY);
				content.setBackground(Background.EMPTY);

				if(text != null) {
					text.textAlignmentProperty().bind(this.textAlignmnetProperty);
					text.getStyleClass().add("edit-text");
					text.setCursor(Cursor.DEFAULT);	
				}
				
				if(this.getResizable()) {
					HBox.setHgrow(this.textField, Priority.ALWAYS);
					graphicBtn.setFitWidth(19);
				}else
					this.bindSize();
				
			}
		});
		
		this.textField.editableProperty().addListener((observable, oldValue, newValue)->{
			Cursor cursor = newValue ? Cursor.TEXT : Cursor.DEFAULT;			
			ScrollPane scrollPane = (ScrollPane)this.textField.getChildrenUnmodifiable().get(0);
			Region content = (Region) scrollPane.getContent();

			this.editBtn.setDisable(newValue);
			this.textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("editing"), newValue);
			this.textField.setCursor(cursor);
			this.textField.getSkin().getNode().setCursor(cursor);
			this.textField.getSkin().getNode().lookup("Text").setCursor(cursor);
			this.textField.getSkin().getNode().lookup(".content").setCursor(cursor);
			this.textField.positionCaret(this.textField.getText().length());
			this.textField.requestFocus();			

			if(!newValue) {
				scrollPane.setBackground(Background.EMPTY);
				content.setBackground(Background.EMPTY);
				this.textField.setBackground(Background.EMPTY);
				this.textField.setText(this.textField.getText().trim());
				this.fireEvent(new ActionEvent(this, this));
				this.blurAnimation().play();
				this.textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
				this._layout();
			}
		});
		
		this.textField.setOnKeyPressed(e->{
			if(e.getCode().equals(KeyCode.ENTER)) {
				this.textField.setEditable(false);
			}
		});
				
		this.textField.setOnKeyTyped(e->{		
			this._layout();
			
			if(this.getMaxLength() < 0) return;
			
			if(this.textField.getText().length() - this.textField.getSelectedText().length() >= this.getMaxLength()) {
				e.consume();
			}
		});
		
		this.textField.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue)
				this.textField.setEditable(false);
		});
		
		this.resizableProperty.addListener((observable, oldValue, newValue)->{
			if(newValue) {
				HBox.setHgrow(this.textField, Priority.ALWAYS);
				this.textField.minHeightProperty().unbind();
				this.minHeightProperty().unbind();
				this.maxHeightProperty().unbind();
			}else
				this.bindSize();
		});
	}
	
	private Timeline blurAnimation() {
		MotionBlur blur = new MotionBlur();
		blur.setAngle(0);
		blur.setRadius(0);
		this.textField.setScaleX(.9);
		this.textField.setEffect(blur);
		
		Timeline timeline = new Timeline();
		timeline.setAutoReverse(false);
		timeline.setCycleCount(1);
		
		WritableValue<Double> writable0 = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return blur.getAngle();
			}
			
			@Override
			public void setValue(Double value) {
				blur.setAngle(value);	
			}		
		};
		
		WritableValue<Double> writable1 = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return blur.getRadius();
			}
			
			@Override
			public void setValue(Double value) {
				blur.setRadius(value);	
			}		
		};
		
		WritableValue<Double> writable2 = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return textField.getScaleX();
			}
			
			@Override
			public void setValue(Double value) {
				textField.setScaleX(value);
			}		
		};
		
		KeyValue key0 = new KeyValue(writable0, 10.0);
		KeyValue key1 = new KeyValue(writable1, 50.0);
		KeyValue key2 = new KeyValue(writable2, 1.0);

		KeyFrame keyFrame0 = new KeyFrame(Duration.millis(150), key0);
		KeyFrame keyFrame1 = new KeyFrame(Duration.millis(150), key1);
		KeyFrame keyFrame2 = new KeyFrame(Duration.millis(100), key2);

		timeline.getKeyFrames().addAll(keyFrame0, keyFrame1, keyFrame2);
		timeline.play();
		
		timeline.setOnFinished(e->this.textField.setEffect(null));
		
		return timeline;
	}
	
	@SuppressWarnings("unused")
	private FadeTransition lightAnimation() {
		MotionBlur blur = new MotionBlur();
		blur.setAngle(-50);
		blur.setRadius(100);
		
		Region lightRay = new Region();
		lightRay.setPrefHeight(((Text)this.textField.lookup("Text")).getBoundsInLocal().getHeight());
		lightRay.setPrefWidth(((Text)this.textField.lookup("Text")).getWrappingWidth());
		lightRay.setMaxWidth(((Text)this.textField.lookup("Text")).getWrappingWidth());

		lightRay.setStyle("-fx-background-color:rgba(255,255,255,.3); -fx-backgrond-radius:0;");
		lightRay.setEffect(blur);
		lightRay.setOpacity(0.0);
		
		FadeTransition ft = new FadeTransition(Duration.millis(100), lightRay);
		
		ft.setAutoReverse(true);
		ft.setCycleCount(2);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		GridPane.setConstraints(lightRay, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.ALWAYS,  new Insets(0));
		
		this.getChildren().add(lightRay);
				
		ft.setOnFinished(e->this.getChildren().remove(lightRay));
		
		return ft;
	}

	public ObjectProperty<VPos> editBtnValignmentProperty(){
		return this.editBtnValignment;
	}
	
	public void setEditBtnValignment(VPos valignment) {
		this.editBtnValignment.set(valignment);
	}
	
	public VPos getEditBtnValignment() {
		return this.editBtnValignment.get();
	}
	
	public StringProperty titleProperty() {
		return this.title.textProperty();
	}
	
	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public String getTitle() {
		return this.title.getText();
	}
	
	public BooleanProperty resizableProperty() {
		return this.resizableProperty;
	}
	
	public void setResizable(boolean resizable) {
		this.resizableProperty.set(resizable);
	}
	
	public boolean getResizable() {
		return this.resizableProperty.get();
	}
	
	public ObjectProperty<TextAlignment> textAlignmentProperty(){
		return this.textAlignmnetProperty;
	}
	
	public TextAlignment getTextAlignment(){
		return this.textAlignmnetProperty.get();
	}
	
	public void setTextAlignment(TextAlignment textAlignment) {
		this.textAlignmnetProperty.set(textAlignment);
	}
	
	public IntegerProperty maxLengthProperty() {
		return this.maxLengthProperty;
	}
	
	public int getMaxLength() {
		return maxLengthProperty.get();
	}

	public void setMaxLength(int length) {
		this.maxLengthProperty.set(length);
	}
	
	public StringProperty textProperty() {
		return this.textField.textProperty();
	}
	
	public void setText(String text) {
		this.textField.setText(text);
	}
	
	public String getText() {
		return this.textField.getText();
	}

	private void bindSize() {
		Text text = (Text) ((ScrollPane)((TextArea)this.textField.getSkin().getNode()).getChildrenUnmodifiable().get(0)).getContent().lookup("Text");
		ScrollPane scrollPane = (ScrollPane)this.textField.getChildrenUnmodifiable().get(0);
		Region content = (Region) scrollPane.getContent();
		ImageView graphicBtn = (ImageView) this.editBtn.getGraphic();

		this.minHeightProperty().bind(this.textField.heightProperty().add(this.title.getHeight() + this.getPadding().getTop() + this.getPadding().getBottom()));
		this.maxHeightProperty().bind(this.textField.heightProperty().add(this.title.getHeight() + this.getPadding().getTop() + this.getPadding().getBottom()));
		
		if(text != null) {
			this.textField.minHeightProperty().bind(Bindings.createDoubleBinding(()->{																	
				double textMaxY = text.getBoundsInParent().getHeight();
				double textFieldPadding = this.textField.getPadding().getTop() + this.textField.getPadding().getBottom();
				double scrollPanePadding = scrollPane.getPadding().getTop() + scrollPane.getPadding().getBottom();
				double contentPadding = content.getPadding().getTop() + content.getPadding().getBottom();
				double borderHeight = 0; 

				if(this.textField.getBorder() != null) {
					borderHeight = this.textField.getBorder().getInsets().getTop() + this.textField.getBorder().getInsets().getBottom(); 
				}						
				
				double totHeight = textMaxY + textFieldPadding + scrollPanePadding + contentPadding + borderHeight + 1.0;						
				double rowHeight = text.getFont().getSize() + scrollPanePadding + contentPadding + borderHeight + 1.0;						
				
				graphicBtn.setFitHeight(Math.min(19, rowHeight));
				
				this.textField.setMaxHeight(totHeight);
				this.textField.setPrefHeight(totHeight);
				this._layout();
								
				return totHeight;					
			}, text.boundsInParentProperty(), text.textProperty()));
		}
	}
		
	public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
		return this.onAction;
	}
	
	public final EventHandler<ActionEvent> getOnAction(){
		return this.onAction.get();
	}
	
	public final void setOnAction(EventHandler<ActionEvent> value) {
		this.onAction.set(value);
	}
		
	public void clear() {
		this.textField.clear();
	}

	public boolean isEmpty() {
		return this.textField.getText().isEmpty();
	}

	public boolean isEditable() {
		return this.textField.isEditable();
	}

	public BooleanProperty editableProperty() {
		return this.textField.editableProperty();
	}
	
	public String getPromptText() {
		return this.textField.getPromptText();
	}
	
	public StringProperty promptTextProperty() {
		return this.textField.promptTextProperty();
	}
	
	public void setPromptText(String placeholder) {
		this.textField.setText(placeholder);
	}
	
	
	public void _layout() {
		this.layout();
		this.textField.layout();
		Event.fireEvent(this.textField, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
	}
}
