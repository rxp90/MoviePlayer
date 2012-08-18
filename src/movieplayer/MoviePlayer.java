package movieplayer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.pfc.conexion.ConexionArduino;
import com.pfc.datostrama.DatosTrama;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Raul
 */
public class MoviePlayer extends Application {

    private static final String MEDIA_URL = "http://dl.dropbox.com/u/25798506/freddie.mp4";
    private ConexionArduino conexionArduino;

    @Override
    public void start(final Stage primaryStage) {

        conexionArduino = new ConexionArduino();
        conexionArduino.setHost("192.168.2.102");
        conexionArduino.conectaServidor();

        System.out.println(conexionArduino.isConnected());

        // Título de la ventana.
        primaryStage.setTitle("Mister Freddie Mercury");

        // Creo un elemento raíz y un reproductor.
        Group root = new Group();
        Media media = new Media(MEDIA_URL);
        final MediaPlayer player = new MediaPlayer(media);

        MediaView view = new MediaView(player);

        // Animaciones para la entrada y salida del puntero.
        final Timeline slideIn = new Timeline();
        final Timeline slideOut = new Timeline();

        root.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                slideIn.play();
            }
        });
        root.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                slideOut.play();
            }
        });

        // Vertical Box para el slider de tiempo.
        final VBox vbox = new VBox();
        final Slider slider = new Slider();
        vbox.getChildren().add(slider);

        // Añado los elementos a la raíz.
        root.getChildren().add(view);
        root.getChildren().add(vbox);

        // Tamaño por defecto y muestro la ventana.
        Scene scene = new Scene(root, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
        player.play();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Double volume = player.getVolume();
                Double min = 0.7;
                while (true) {
                    DatosTrama datosTrama = conexionArduino.leeDatos();
                    System.out.println(datosTrama);
                    if (datosTrama != null) {
                        Double aceleracionX = datosTrama
                                .getAceleraciones().getX();
                        if (aceleracionX > min) {
                            volume += 0.05;
                        } else if (aceleracionX < -min) {
                            volume -= 0.05;
                        }
                        if (volume > 1.0) {
                            volume = 1.0;
                        } else if (volume < 0.0) {
                            volume = 0.0;
                        }
                        player.setVolume(volume);
                    }


                }
            }
        }).start();

        player.setOnReady(new Runnable() {
            @Override
            public void run() {

                // Ajusto el tamaño de la ventana al vídeo.
                double ancho = player.getMedia().getWidth();
                double alto = player.getMedia().getHeight();

                primaryStage.setMinWidth(ancho);
                primaryStage.setMinHeight(alto);
                vbox.setMinSize(ancho, 100);
                vbox.setTranslateY(alto - 100);

                // Configuro el slider de tiempo.
                slider.setMin(0.0);
                slider.setValue(0.0);
                slider.setMax(player.getTotalDuration().toSeconds());

                // Configuro las animaciones de entrada y salida.
                slideOut.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO, new KeyValue(vbox
                        .translateYProperty(), alto - 100),
                        new KeyValue(vbox.opacityProperty(), 0.9)),
                        new KeyFrame(Duration.millis(300), new KeyValue(vbox
                        .translateYProperty(), alto), new KeyValue(vbox
                        .opacityProperty(), 0.0)));
                slideIn.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO, new KeyValue(vbox
                        .translateYProperty(), alto), new KeyValue(vbox
                        .opacityProperty(), 0.0)),
                        new KeyFrame(Duration.millis(300), new KeyValue(vbox
                        .translateYProperty(), alto - 100),
                        new KeyValue(vbox.opacityProperty(), 0.9)));
            }
        });
        // Listener del tiempo para que se mueva el slider.
        player.currentTimeProperty().addListener(
                new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> ov,
                            Duration duration, Duration current) {
                        slider.setValue(current.toSeconds());
                    }
                });
        // Al hacer clic me muevo al instante de tiempo indicado.
        slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                player.seek(Duration.seconds(slider.getValue()));
            }
        });

    }

    public ConexionArduino getConexionArduino() {
        return conexionArduino;
    }

    public String getHost() {
        return conexionArduino.getHost();
    }

    public void setHost(String host) {
        conexionArduino.setHost(host);
    }

    public int getPuerto() {
        return conexionArduino.getPuerto();
    }

    public void setPuerto(int puerto) {
        conexionArduino.setPuerto(puerto);
    }

    public boolean isConnected() {
        return conexionArduino.isConnected();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
