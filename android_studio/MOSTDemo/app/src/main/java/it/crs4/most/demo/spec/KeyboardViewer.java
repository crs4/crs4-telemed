package it.crs4.most.demo.spec;


public abstract class KeyboardViewer {
    interface keySelectionListener {
        void onKeySelected(String key);
    };

    private keySelectionListener keySelectionListener;
    private String [] keys;
    private String selectedKey;

    protected void onKeySelected(String key){
        if (this.keySelectionListener != null)
            this.keySelectionListener.onKeySelected(key);
            selectedKey = key;
    }

    public KeyboardViewer.keySelectionListener getKeySelectionListener() {
        return keySelectionListener;
    }

    public void setKeySelectionListener(KeyboardViewer.keySelectionListener keySelectionListener) {
        this.keySelectionListener = keySelectionListener;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String getSelectedKey() {
        return selectedKey;
    }
}
