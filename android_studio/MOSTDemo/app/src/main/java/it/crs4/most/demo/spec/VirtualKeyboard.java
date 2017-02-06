package it.crs4.most.demo.spec;

import java.util.HashMap;
import java.util.Map;

import it.crs4.most.visualization.augmentedreality.mesh.Mesh;

public class VirtualKeyboard implements KeyboardViewer.keySelectionListener {
    private static final String TAG = "VirtualKeyboard";
    private KeyboardViewer viewer;
    private Map<String, float []> keyMap;
    private KeyboardCoordinatesParser parser;
    private Mesh keyboardMesh;



    interface KeyboardCoordinatesParser {
        Map<String, float []> parse();
    };

    public VirtualKeyboard(KeyboardViewer viewer, Map<String, float []> keyMap, Mesh keyboardMesh) {
        this.parser = parser;
        this.viewer = viewer;
        this.viewer.setKeySelectionListener(this);
        this.keyboardMesh = keyboardMesh;
        this.keyMap = keyMap;
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

    public KeyboardCoordinatesParser getParser() {
        return parser;
    }

    public void setParser(KeyboardCoordinatesParser parser) {
        this.parser = parser;
    }

    public Mesh getKeyboardMesh() {
        return keyboardMesh;
    }

    public void setKeyboardMesh(Mesh keyboardMesh) {
        this.keyboardMesh = keyboardMesh;
    }
}
