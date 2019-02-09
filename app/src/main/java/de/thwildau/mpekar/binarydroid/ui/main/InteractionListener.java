package de.thwildau.mpekar.binarydroid.ui.main;


import de.thwildau.mpekar.binarydroid.model.BinaryFile;

/**
 * This interface provides to ability to interact with other fragments/activities.
 */
public interface InteractionListener {
    /**
     * Called when the user selects a binary file (from a installed application)
     * @param item  selected binary file
     */
    void onSelectBinaryFile(BinaryFile item);

    /**
     * Called when the user tries to open a file from a file path
     * @param path  path to the file
     */
    void onSelectFilePath(String path);
}