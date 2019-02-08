package de.thwildau.mpekar.binarydroid.ui.main;

import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

/**
 * Provides the link between the background search process and the frontend.
 */
public interface SymbolSearchInterface {
    /**
     * Called for every symbol that matches
     * @param binary            Binary that contains the symbol
     * @param symbolItem        Found symbol
     */
    void onSymbolMatch(BinaryFile binary, SymbolItem symbolItem);

    /**
     * Called after the search is finished.
     * @param symbolCount       Amount of symbols that have been found
     */
    void onSearchComplete(int symbolCount);

    /**
     * This method will be called to check if the specified application
     * should be skipped during symbol search.
     * @param packageName       Package name of the application in question
     * @return                  True to skip this app, false to do a search in it.
     */
    boolean shouldSkipApp(String packageName);
}
