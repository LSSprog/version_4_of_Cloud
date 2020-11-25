import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import utils.FileUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private String clientDir = "Client_dir";
    private String serverDir = "Server_dir";
    public ListView<String> client;
    public ListView<String> server;
    private ObjectDecoderInputStream in;
    private ObjectEncoderOutputStream out;

    @FXML
    TextField pathFieldClient, pathFieldServer;

    public void upload(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        String fileName = client.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(Paths.get(getCurrentPathClient(), fileName));
        out.writeObject(message);
        out.flush();
        out.writeObject(new ListRequest());
        out.flush();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = server.getSelectionModel().getSelectedItem();
        FileRequest request = new FileRequest(fileName);
        out.writeObject(request);
        out.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 1313);
            in = new ObjectDecoderInputStream(socket.getInputStream());
            out = new ObjectEncoderOutputStream(socket.getOutputStream());

            if (!Files.exists(Paths.get(clientDir))) {
                Files.createDirectory(Paths.get(clientDir));
            }
            updateListServer();
            pathFieldClient.setText(Paths.get(clientDir).normalize().toAbsolutePath().toString());
            pathFieldServer.setText(serverDir);

            client.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        Path newPath = Paths.get(pathFieldClient.getText())
                                .resolve(client.getSelectionModel().getSelectedItem());
                        if (Files.isDirectory(newPath)) {
                            updateListClient(newPath);
                        }
                    }
                }
            });

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Object message = in.readObject();
                        System.out.println("пришло сообшение от сервера");
                        if (message instanceof ListResponse) {
                            ListResponse list = (ListResponse) message;
                            System.out.println("обновляем список сервера");
                            server.getItems().clear();
                            Platform.runLater(() -> {
                                server.getItems().addAll(list.getFilesData());
                            });
                        }
                        if (message instanceof FileMessage) {
                            FileMessage file = (FileMessage) message;
                            Path newPath = Paths.get(getCurrentPathClient());
                            Files.write(Paths.get(getCurrentPathClient(), file.getFileName()), file.getData(), StandardOpenOption.CREATE);
                            updateListClient(newPath);
                            //client.getItems().clear();
                            //client.getItems().addAll(FileUtils.getFiles(Paths.get(clientDir)));
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
            updateListClient(Paths.get(getCurrentPathClient()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateListClient(Path newPath)  {
        pathFieldClient.setText(newPath.normalize().toAbsolutePath().toString());
        client.getItems().clear();
            Platform.runLater(() -> {
                try {
                    client.getItems().addAll(FileUtils.getFiles(newPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private void updateListServer() {
        try {
            server.getItems().clear();
            out.writeObject(new ListRequest());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getCurrentPathClient() {
        return pathFieldClient.getText();
    }

    public void btnExitOnClose(ActionEvent actionEvent) {
        Platform.exit();

    }

    public void changeDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateListClient(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void bthPathUpAction(ActionEvent actionEvent) {
        Path upPath = Paths.get(pathFieldClient.getText()).getParent();
        if (upPath != null) {
            updateListClient(upPath);
        }
    }

    public void btnRenameFileClient(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Переименование файла");
        dialog.setHeaderText("Введите новое имя файла");
        dialog.setContentText("Новое Имя файла");
        Optional<String> result = dialog.showAndWait();
        String newNameFile = result.orElse(null);

        if (newNameFile == null || newNameFile.equals("")
                || getSelectedFileNameClient() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Введите новое имя файла и выберите файл, который надо переименовать", ButtonType.OK);
            alert.showAndWait();

        } else {
            Path pathTo = Paths.get(getCurrentPathClient(), newNameFile);
            Path pathFrom = Paths.get(getCurrentPathClient(), getSelectedFileNameClient());
            try {
                Files.move(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateListClient(Paths.get(getCurrentPathClient()));
        }

    }

    public void btnDeleteFileServer(ActionEvent actionEvent) {
        String fileName = server.getSelectionModel().getSelectedItem();
        DeleteFile deleteFile = new DeleteFile(fileName);
        try {
            out.writeObject(deleteFile);
            updateListServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
            }

    public void btnRenameFileServer(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Переименование файла");
        dialog.setHeaderText("Введите новое имя файла");
        dialog.setContentText("Новое Имя файла");
        Optional<String> result = dialog.showAndWait();
        String newNameFile = result.orElse(null);

        if (newNameFile == null || newNameFile.equals("")
                || getSelectedFileNameServer() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Введите новое имя файла и выберите файл, который надо переименовать", ButtonType.OK);
            alert.showAndWait();
            }
        RenameFile message = new RenameFile(getSelectedFileNameServer(), newNameFile);
        try {
            out.writeObject(message);
            out.flush();
            updateListServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
        }

    public String getSelectedFileNameClient() {
        if (!client.isFocused()) {
            return null;
        }
        return client.getSelectionModel().getSelectedItem();
    }

    public String getSelectedFileNameServer() {
        if (!server.isFocused()) {
            return null;
        }
        return server.getSelectionModel().getSelectedItem();
    }


}
