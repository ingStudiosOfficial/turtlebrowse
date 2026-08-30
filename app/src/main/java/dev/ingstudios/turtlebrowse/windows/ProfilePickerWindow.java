package dev.ingstudios.turtlebrowse.windows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.controlsfx.control.PopOver;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import dev.ingstudios.turtlebrowse.managers.DiscordPresenceManager;
import dev.ingstudios.turtlebrowse.managers.WindowsManager;
import dev.ingstudios.turtlebrowse.managers.WindowsManager.WindowItem;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ProfilePickerWindow extends Stage {
	private final MainDatabase db = MainDatabase.getInstance();
	private final List<ProfileStructureWithId> profiles;
	private final HBox profilesBox;

	public ProfilePickerWindow() {
		WindowsManager.getInstance().addWindow(new WindowItem("profile_picker_window", ProfilePickerWindow.class));

		setTitle("Turtlebrowse");

		profiles = db.getAllProfiles();

		final BorderPane root = new BorderPane();
		root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, null, null));
		}, Main.mainMaterialColorScheme.getSurface()));

		final VBox profilePickerBox = new VBox();
		profilePickerBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		profilePickerBox.setAlignment(Pos.CENTER);

		final Scene profilePickerScene = new Scene(root, 800, 600);
		profilePickerScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

		final Label profilesLabel = new Label("Who's using Turtlebrowse today?");
		profilesLabel.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 25));

		profilesBox = new HBox();
		profilesBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		profilesBox.setAlignment(Pos.CENTER);
		profilesBox.setFillHeight(true);

		for (final ProfileStructureWithId profile : profiles) {
			final JFXButton profileButton = createProfileButton(profile);
			profilesBox.getChildren().add(profileButton);
		}

		final JFXButton newProfileButton = new JFXButton();
		newProfileButton.setStyle("-fx-padding: 10px;");
		newProfileButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurfaceContainer()));

		final VBox newProfileBox = new VBox();
		newProfileBox.setAlignment(Pos.CENTER);
		newProfileBox.setOnMouseEntered(event -> {
			newProfileBox.setCursor(Cursor.HAND);
		});
		newProfileBox.setOnMouseDragExited(event -> {
			newProfileBox.setCursor(Cursor.DEFAULT);
		});
		newProfileBox.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				new NewProfileWindow();
		});

		final Label createLabel = new Label("Add");
		createLabel.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 25));

		final FontIcon newIcon = new FontIcon(Material2OutlinedAL.ADD_CIRCLE_OUTLINE);
		newIcon.setIconSize(100);

		newProfileBox.getChildren().addAll(newIcon, createLabel);

		newProfileButton.setGraphic(newProfileBox);
		newProfileButton.setMaxHeight(Double.MAX_VALUE);

		profilesBox.getChildren().add(newProfileButton);

		final JFXButton guestButton = new JFXButton("Browse as guest");
		guestButton.setOnMouseEntered(event -> {
			guestButton.setCursor(Cursor.HAND);
		});
		guestButton.setOnMouseDragExited(event -> {
			guestButton.setCursor(Cursor.DEFAULT);
		});
		guestButton.setOnMouseClicked(event -> {
			event.consume();

			if (event.getButton() == MouseButton.PRIMARY) {
				final ProfileStructureWithId guestProfile = new ProfileStructureWithId("Guest",
						Platform.getPreferences().getAccentColor(), UUID.randomUUID());

				final Path profileCefCachePath = Main.getStoragePath("cef-cache", guestProfile.getIdAsString());
				final Path profileStoragePath = Main.getStoragePath("profiles", guestProfile.getIdAsString());

				final File profileCefCacheDir = profileCefCachePath.toFile();
				final File profileStorageDir = profileStoragePath.toFile();

				if (!profileCefCacheDir.exists()) {
					profileCefCacheDir.mkdirs();
				}

				if (!profileStorageDir.exists()) {
					profileStorageDir.mkdirs();
				}

				Main.createMainWindow(guestProfile, true);
			}
		});

		profilePickerBox.getChildren().addAll(profilesLabel, profilesBox, guestButton);

		root.setCenter(profilePickerBox);

		setScene(profilePickerScene);

		setOnCloseRequest(event -> closeWindow());

		DiscordPresenceManager.getInstance().updateDiscordPresence("In the profile picker menu");
	}

	private JFXButton createProfileButton(ProfileStructureWithId profile) {
		final JFXButton profileButton = new JFXButton();
		profileButton.setStyle("-fx-padding: 10px;");
		profileButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurfaceContainer()));

		final VBox profileBox = new VBox();
		profileBox.setStyle("-fx-spacing: 10px;");
		profileBox.setAlignment(Pos.CENTER);
		profileBox.setOnMouseEntered(event -> {
			profileBox.setCursor(Cursor.HAND);
		});
		profileBox.setOnMouseDragExited(event -> {
			profileBox.setCursor(Cursor.DEFAULT);
		});
		profileBox.setOnMouseClicked(event -> {
			event.consume();

			if (event.getButton() == MouseButton.PRIMARY)
				Main.createMainWindow(profile);
		});

		final ContextMenu profileButtonMenu = new ContextMenu();

		final MenuItem deleteMenuItem = new MenuItem("Remove");
		deleteMenuItem.setOnAction(event -> {
			showConfirmDeleteProfile(profile, profileButton);
		});

		final MenuItem editMenuItem = new MenuItem("Edit");
		editMenuItem.setOnAction(event -> {
			new NewProfileWindow(true, profile);
		});

		profileButtonMenu.getItems().addAll(deleteMenuItem, editMenuItem);
		profileButton.setOnContextMenuRequested(event -> {
			profileButtonMenu.show(profileBox, event.getScreenX(), event.getScreenY());
		});

		final Path profileAvatarPath = Main.getStoragePath("profiles", profile.getIdAsString(), "avatar");
		System.out.println("Profile avatar path: " + profileAvatarPath.toAbsolutePath());
		final File profileAvatarFile = profileAvatarPath.toFile();

		if (profileAvatarFile.exists() && profileAvatarFile.isFile()) {
			final String profileAvatarPathString = profileAvatarPath.toUri().toString();
			System.out.println("Avatar path string: " + profileAvatarPathString);

			final ImageView profileImageView = new ImageView(profileAvatarPathString);
			profileImageView.setFitWidth(100);
			profileImageView.setFitHeight(100);
			profileImageView.setPreserveRatio(false);
			profileImageView.setSmooth(true);

			final Circle imageClip = new Circle(50, 50, 50);
			profileImageView.setClip(imageClip);
			profileBox.getChildren().add(profileImageView);
		} else {
			final FontIcon defualtAccountIcon = new FontIcon(Material2OutlinedAL.ACCOUNT_CIRCLE);
			defualtAccountIcon.setIconSize(100);
			profileBox.getChildren().add(defualtAccountIcon);
		}

		final Label profileName = new Label(profile.name());
		profileName.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 25));

		profileBox.getChildren().add(profileName);

		profileButton.setGraphic(profileBox);
		profileButton.setMaxHeight(Double.MAX_VALUE);

		return profileButton;
	}

	private void closeWindow() {
		System.out.println("Removing profile picker window...");
		WindowsManager.getInstance().removeWindow("profile_picker_window");
		if (WindowsManager.getInstance().getWindows().size() == 0) {
			Platform.runLater(() -> {
				Platform.exit();
			});
			MainDatabase.getInstance().closeDb();
			System.exit(0);
		}
	}

	public void showProfilePickerWindow(boolean alwaysOpen) {
		System.out.println("Showing profile picker window...");

		if (profiles.size() <= 1 && alwaysOpen == false) {
			final ProfileStructureWithId profile = db.getFirstProfile();
			Main.createMainWindow(profile);
			closeWindow();
			return;
		}

		show();
	}

	private void showConfirmDeleteProfile(ProfileStructureWithId profile, Button profileButton) {
		final PopOver popOver = new PopOver();
		popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
		popOver.setAutoHide(true);

		final JFXButton yesButton = new JFXButton("Yes");
		yesButton.setOnAction(event -> {
			profilesBox.getChildren().remove(profileButton);
			MainDatabase.getInstance().removeProfile(profile.id());
			deleteProfileData(profile);
			popOver.hide();
		});

		final JFXButton noButton = new JFXButton("No");
		noButton.setOnAction(event -> {
			popOver.hide();
		});

		final HBox buttons = new HBox(10, noButton, yesButton);

		final VBox content = new VBox(10,
				new Label("Are you sure you want to delete profile '%s'?".formatted(profile.name())), buttons);
		popOver.setContentNode(content);

		popOver.show(profileButton);
	}

	public static void deleteProfileData(ProfileStructureWithId profile) {
		final String profileId = profile.getIdAsString();

		final Path profileCefCachePath = Main.getStoragePath("cef-cache", profileId);
		final Path profileStoragePath = Main.getStoragePath("profiles", profileId);

		final File profileCefCacheDir = profileCefCachePath.toFile();
		final File profileStorageDir = profileStoragePath.toFile();

		if (profileCefCacheDir.exists()) {
			System.out.println("Profile CEF cache dir exists, deleting...");
			deleteDirectoryIfExists(profileCefCachePath);
		}

		if (profileStorageDir.exists()) {
			System.out.println("Profile storage dir exists, deleting...");
			deleteDirectoryIfExists(profileStoragePath);
		}
	}

	private static void deleteDirectoryIfExists(Path path) {
		if (!Files.exists(path)) {
			return;
		}

		System.out.println("Deleting directory: " + path);
		try (Stream<Path> walk = Files.walk(path)) {
			walk.sorted(Comparator.reverseOrder())
					.forEach(p -> {
						try {
							Files.delete(p);
						} catch (IOException e) {
							System.err.println("Failed to delete " + p + ": " + e.getMessage());
						}
					});
		} catch (IOException e) {
			System.err.println("Failed to walk directory " + path + ": " + e.getMessage());
		}
	}
}
