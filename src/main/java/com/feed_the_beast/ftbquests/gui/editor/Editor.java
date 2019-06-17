package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconRenderer;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Editor
{
	private static Stage mainWindow = null;
	private static boolean loaded = false;
	private static TabPane tabPane = null;
	private static Tab tabMain = null;

	public static void runLater(Runnable runnable)
	{
		Platform.runLater(runnable);
	}

	public static void runLater(boolean runLater, Runnable runnable)
	{
		if (runLater)
		{
			runLater(runnable);
		}
		else
		{
			runnable.run();
		}
	}

	public static void open(boolean reload)
	{
		//Have to use some hacks here to trick JavaFX into working together with LWJGL

		if (reload || !loaded)
		{
			ClientQuestFile.INSTANCE.clearCachedData();
			IconRenderer.clearCache();
		}

		if (!loaded)
		{
			loaded = true;
			new JFXPanel();
			Platform.setImplicitExit(false);
			runLater(() -> {
				mainWindow = new Stage();
				mainWindow.setTitle("FTB Quests Editor [Warning: Nearly nothing works properly yet!]");
				mainWindow.setMinWidth(500);
				mainWindow.setMinHeight(300);
				mainWindow.getIcons().add(new Image("/assets/" + FTBQuests.MOD_ID + "/textures/logotransparent.png"));
				reloadMainScene();
				mainWindow.show();
			});
		}
		else if (mainWindow != null)
		{
			runLater(() -> {
				if (reload)
				{
					ClientQuestFile.INSTANCE.clearCachedData();
					reloadMainScene();
				}

				mainWindow.show();
			});
		}
	}

	public static Stage getMainWindow()
	{
		return mainWindow;
	}

	@Nullable
	public static ImageView iconView(@Nullable Image image)
	{
		if (image == null)
		{
			return null;
		}

		ImageView view = new ImageView(image);
		view.setFitWidth(16);
		view.setFitHeight(16);
		return view;
	}

	public static void loadIcon(Labeled node, @Nullable Icon icon)
	{
		IconRenderer.load(icon, (queued, image) -> runLater(queued, () -> {
			node.setGraphic(iconView(image));

			if (queued)
			{
				node.requestLayout();
			}
		}));
	}

	public static void loadIcon(MenuItem node, @Nullable Icon icon)
	{
		IconRenderer.load(icon, (queued, image) -> runLater(queued, () -> node.setGraphic(iconView(image))));
	}

	public static void loadIcon(Tab node, @Nullable Icon icon)
	{
		IconRenderer.load(icon, (queued, image) -> runLater(queued, () -> node.setGraphic(iconView(image))));
	}

	private static void reloadMainScene()
	{
		tabMain = new Tab("FTB Quests");
		tabMain.setClosable(false);
		refreshMainTabContent();
		mainWindow.setScene(new Scene(tabPane = new TabPane(tabMain), 1000, 600));
	}

	public static void refreshMainTabContent()
	{
		VBox mainBox = new VBox();
		HBox buttonBox = new HBox();
		buttonBox.setSpacing(5);
		buttonBox.setPadding(new Insets(10, 10, 10, 10));

		buttonBox.getChildren().add(button(I18n.format("ftbquests.gui.wiki"), GuiIcons.INFO, () -> System.out.println("Opening wiki...")));
		buttonBox.getChildren().add(button(I18n.format("ftbquests.gui.save_as_file"), GuiIcons.DOWN, () -> System.out.println("Saving as file...")));

		if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
		{
			buttonBox.getChildren().add(button("", GuiIcons.REFRESH, () -> {
				IconRenderer.clearCache();
				reloadMainScene();
			}));
		}

		mainBox.getChildren().add(buttonBox);

		QuestObjectBaseTreeItem rootNode = new QuestObjectBaseTreeItem(ClientQuestFile.INSTANCE);

		for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
		{
			if (chapter.hasGroup())
			{
				continue;
			}

			QuestObjectBaseTreeItem chapterNode = new QuestObjectBaseTreeItem(chapter);
			rootNode.getChildren().add(chapterNode);

			for (QuestChapter chapter1 : chapter.getChildren())
			{
				QuestObjectBaseTreeItem subChapterNode = new QuestObjectBaseTreeItem(chapter1);
				chapterNode.getChildren().add(subChapterNode);

				for (Quest quest : chapter1.quests)
				{
					subChapterNode.getChildren().add(new QuestObjectBaseTreeItem(quest));
				}
			}

			for (Quest quest : chapter.quests)
			{
				chapterNode.getChildren().add(new QuestObjectBaseTreeItem(quest));
			}

			chapterNode.getChildren().sort(null);
		}

		rootNode.getChildren().sort(null);
		addSeperators(rootNode);

		TreeView<QuestObjectBase> objectTree = new TreeView<>();
		objectTree.setCellFactory(callback -> new TreeCell<QuestObjectBase>()
		{
			@Override
			public void updateItem(QuestObjectBase object, boolean empty)
			{
				super.updateItem(object, empty);

				if (empty)
				{
					setText(null);
					setGraphic(null);
					setContextMenu(null);
				}
				else if (object == null || object.invalid)
				{
					setText("---");
					setGraphic(null);
					setContextMenu(null);
				}
				else
				{
					setText(object.getUnformattedTitle());
					loadIcon(this, object.getIcon());

					ContextMenu menu = new ContextMenu();
					menu.getItems().add(menuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> openTab(new TabEditObject(object))));

					if (!(object instanceof QuestFile))
					{
						menu.getItems().add(menuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> scheduleMessage(new MessageDeleteObject())));
					}

					Menu changeProgressMenu = new Menu();
					changeProgressMenu.setText(I18n.format("ftbquests.gui.change_progress"));
					loadIcon(changeProgressMenu, GuiIcons.TIME);

					changeProgressMenu.getItems().add(menuItem(I18n.format("ftbquests.gui.change_progress.reset"), GuiIcons.REFRESH, () -> {}));
					changeProgressMenu.getItems().add(menuItem(I18n.format("ftbquests.gui.change_progress.reset_deps"), GuiIcons.REFRESH, () -> {}));
					changeProgressMenu.getItems().add(menuItem(I18n.format("ftbquests.gui.change_progress.complete"), GuiIcons.ACCEPT, () -> {}));
					changeProgressMenu.getItems().add(menuItem(I18n.format("ftbquests.gui.change_progress.complete_deps"), GuiIcons.ACCEPT, () -> {}));

					menu.getItems().add(changeProgressMenu);

					setContextMenu(menu);
				}
			}
		});

		objectTree.setRoot(rootNode);
		objectTree.setShowRoot(true);
		rootNode.setExpanded(true);
		tabMain.setContent(objectTree);
	}

	private static void addSeperators(QuestObjectBaseTreeItem item)
	{
		QuestObjectType type = null;

		List<TreeItem<QuestObjectBase>> list = new ArrayList<>();

		for (TreeItem<QuestObjectBase> item1 : item.getChildren())
		{
			if (item1 instanceof QuestObjectBaseTreeItem)
			{
				QuestObjectType t = item1.getValue().getObjectType();

				if (type == null)
				{
					type = t;
				}
				else if (type != t)
				{
					type = t;
					list.add(new TreeItem<>());
				}

				addSeperators((QuestObjectBaseTreeItem) item1);
			}

			list.add(item1);
		}

		item.getChildren().clear();
		item.getChildren().addAll(list);
	}

	public static void openTab(Tab tab)
	{
		tab.setClosable(true);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
	}

	private static Button button(String title, @Nullable Icon icon, Runnable click)
	{
		Button button = new Button();
		button.setText(title);
		loadIcon(button, icon);
		button.setOnAction(new RunnableCallback(click));
		return button;
	}

	private static MenuItem menuItem(String title, @Nullable Icon icon, Runnable click)
	{
		MenuItem item = new MenuItem();
		item.setText(title);
		loadIcon(item, icon);
		item.setOnAction(new RunnableCallback(click));
		return item;
	}

	public static String toString(ItemStack stack)
	{
		return StringUtils.unformatted(stack.getDisplayName());
	}

	public static void schedule(Runnable runnable)
	{
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	public static void scheduleMessage(MessageToServer message)
	{
		schedule(message::sendToServer);
	}

	public static void scheduleObjectEdit(QuestObjectBase object)
	{
		scheduleMessage(new MessageEditObjectDirect(object));
	}
}