package de.thwildau.mpekar.binarydroid.ui.main;

import java.io.Serializable;

import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

/**
 * Provides the link between the background search process and the frontend.
 * @note Calls to the interface are executed from a background thread.
 */
public interface SymbolSearchInterface {
    /**
     * Called when a search has been started,
     */
    void onSearchStarted();

    /**
     * Called for every symbol that matches
     * @param result            Object that contains the binary and symbol
     */
    void onSymbolMatch(ResultEntry result);

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

    class ResultEntry implements Serializable {
        public final BinaryFile binary;
        public final SymbolItem symbol;

        public ResultEntry(BinaryFile binary, SymbolItem symbol) {
            this.binary = binary;
            this.symbol = symbol;
        }
    }
}
