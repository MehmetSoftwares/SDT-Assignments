package presentation.core;

import javafx.scene.control.Alert;

public class AlertNotificationService implements NotificationService {

    @Override
    public void showNotification(String message, String type) {
        Alert.AlertType alertType = switch (type) {
            case "error" -> Alert.AlertType.ERROR;
            case "warning" -> Alert.AlertType.WARNING;
            default -> Alert.AlertType.INFORMATION;
        };
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
