package Interface;

import ShellNightmare.Terminal.FileSystem.Type;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;

import java.util.function.Consumer;

public class FileTreeCell extends TreeCell<FileLabel> {
    protected Consumer<FileLabel> createCallback = f -> {};
    protected Consumer<FileLabel> renameCallback = f -> {};
    protected Consumer<FileLabel> permissionCallback = f -> {};
    protected Consumer<FileLabel> editCallback = f -> {};
    protected Consumer<FileLabel> importCallback = f -> {};
    protected Consumer<FileLabel> deleteCallback = f -> {};
    protected Consumer<FileLabel> copyCallback = f -> {};
    protected Consumer<FileLabel> cutCallback = f -> {};
    protected Consumer<FileLabel> pasteCallback = f -> {};

    @Override
    protected void updateItem(FileLabel item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            this.setGraphic(item); // on délègue l'affichage à item lui-même
            setContextMenu(generateContextMenu(item));
        }
    }

    private ContextMenu generateContextMenu(FileLabel content){
        // context menu apparaissant avec le clic droit
        ContextMenu menu = new ContextMenu();

        MenuItem createMenuItem = new MenuItem("Créer");
        menu.getItems().add(createMenuItem);
        createMenuItem.setOnAction(e -> createCallback.accept(content));

        if(!content.required) {
            MenuItem renameMenuItem = new MenuItem("Renommer");
            menu.getItems().add(renameMenuItem);
            renameMenuItem.setOnAction(e -> renameCallback.accept(content));

            MenuItem permissionMenuItem = new MenuItem("Permissions");
            menu.getItems().add(permissionMenuItem);
            permissionMenuItem.setOnAction(e -> permissionCallback.accept(content));
        }

        if(content.type == Type.DATA){
            MenuItem editMenuItem = new MenuItem("Éditer");
            menu.getItems().add(editMenuItem);
            editMenuItem.setOnAction(e -> editCallback.accept(content));
        }
        else if(content.type == Type.BINARY){
            MenuItem importMenuItem = new MenuItem("Importer");
            menu.getItems().add(importMenuItem);
            importMenuItem.setOnAction(e -> importCallback.accept(content));
        }

        if(!content.required){
            MenuItem deleteMenuItem = new MenuItem("Supprimer");
            menu.getItems().add(deleteMenuItem);
            deleteMenuItem.setOnAction(e -> deleteCallback.accept(content));
        }

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem copyMenuItem = new MenuItem("Copier");
        menu.getItems().add(copyMenuItem);
        copyMenuItem.setOnAction(e -> copyCallback.accept(content));

        if(!content.required){
            MenuItem cutMenuItem = new MenuItem("Couper");
            menu.getItems().add(cutMenuItem);
            cutMenuItem.setOnAction(e -> cutCallback.accept(content));
        }

        MenuItem pasteMenuItem = new MenuItem("Coller");
        menu.getItems().add(pasteMenuItem);
        // TODO menu.setEnablePaste(var);
        pasteMenuItem.setOnAction(e -> pasteCallback.accept(content));

        return menu;
    }
}
