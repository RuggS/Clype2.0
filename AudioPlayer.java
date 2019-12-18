package data;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioPlayer extends VBox{
	
	private static boolean play = false;
	
	public AudioPlayer(MediaPlayer player) {
		HBox soundBox = new HBox();
		Button btn = new Button();
		btn.setText("Pause/Play");
		Slider slider = new Slider();
		soundBox.getChildren().add(btn);
		soundBox.getChildren().add(slider);
		this.getChildren().add(soundBox);
		slider.setMin(0);
		slider.setMax(120);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, 
					Number oldValue, Number newValue) {
				Duration dur = new Duration( newValue.doubleValue()*1000 );
				player.seek( dur );
			}
			
		});
		
		btn.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent ae ) {
				play = !play;
				if (play) {
					player.play();
				} else {
					player.pause();
				}
			}
		});
		
		
	}
	
	
	
}
