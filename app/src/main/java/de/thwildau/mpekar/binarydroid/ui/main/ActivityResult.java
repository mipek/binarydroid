package de.thwildau.mpekar.binarydroid.ui.main;

import java.util.List;

import de.thwildau.mpekar.binarydroid.SymbolSearchInterface;

/**
 * This is used to pass large data between different activities, as recommended in
 * the android developer guide:
 * http://wing-linux.sourceforge.net/guide/appendix/faq/framework.html#3
 */
class ActivityResult {
    static List<SymbolSearchInterface.ResultEntry> symbolSearchResults;
}
