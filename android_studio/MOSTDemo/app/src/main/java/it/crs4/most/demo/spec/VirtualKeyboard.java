package it.crs4.most.demo.spec;

import android.view.View;
import android.widget.Button;

import java.util.Map;

import it.crs4.most.demo.models.Room;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;

public class VirtualKeyboard implements KeyboardViewer.keySelectionListener {
    private static final String TAG = "VirtualKeyboard";
    private KeyboardViewer viewer;
    private Map<String, float []> keyMap;
    private KeyboardCoordinatesStore keymapStore;
    private Mesh keyboardMesh;
    private Button saveButton;



    interface KeyboardCoordinatesStore {
        Map<String, float []> read();
        void save(String key, float x, float y, float z);
    };

    public VirtualKeyboard(KeyboardViewer viewer, KeyboardCoordinatesStore keymapStore, Mesh keyboardMesh) {
        this.viewer = viewer;
        this.viewer.setKeySelectionListener(this);
        this.keyboardMesh = keyboardMesh;
        this.keymapStore = keymapStore;
        this.keyMap = keymapStore.read();
    }

    @Override
    public void onKeySelected(String key) {
        float [] coords = keyMap.get(key);
        keyboardMesh.setX(coords[0], false);
        keyboardMesh.setY(coords[1], false);
        keyboardMesh.setZ(coords[2], false);
        keyboardMesh.publishCoordinate();
    }

    public KeyboardViewer getViewer() {
        return viewer;
    }

    public void setViewer(KeyboardViewer viewer) {
        this.viewer = viewer;
    }

    public Map<String, float[]> getKeyMap() {
        return keyMap;
    }

    public void setKeyMap(Map<String, float[]> keyMap) {
        this.keyMap = keyMap;
    }

    public Mesh getKeyboardMesh() {
        return keyboardMesh;
    }

    public void setKeyboardMesh(Mesh keyboardMesh) {
        this.keyboardMesh = keyboardMesh;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public KeyboardCoordinatesStore getKeymapStore() {
        return keymapStore;
    }

    public void setKeymapStore(KeyboardCoordinatesStore keymapStore) {
        this.keymapStore = keymapStore;
    }

    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = viewer.getSelectedKey();
                if (key != null)
                    keymapStore.save(
                            key,
                            keyboardMesh.getX(),
                            keyboardMesh.getY(),
                            keyboardMesh.getZ()
                            );
                }
        });
    }
}
