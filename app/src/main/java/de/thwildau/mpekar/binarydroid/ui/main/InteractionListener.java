package de.thwildau.mpekar.binarydroid.ui.main;


import de.thwildau.mpekar.binarydroid.model.BinaryFile;

/**
 * This interface provides to ability to interact with other fragments/activities.
 */
public interface InteractionListener {
    void onSelectBinaryFile(BinaryFile item);
    void onSelectFilePath(String path);
}