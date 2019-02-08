package de.thwildau.mpekar.binarydroid;

import android.util.Log;

import net.fornwall.jelf.ElfFile;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.Container;
import de.thwildau.mpekar.binarydroid.model.ContainerELF;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;
import de.thwildau.mpekar.binarydroid.ui.main.SymbolSearchInterface;

/**
 * Contains the logic required to perform symbol searching.
 */
public class SymbolSearcher {
    private final List<BinaryFile> binaries;
    private final boolean regexSyntax;
    private final boolean ignoreCase;
    private SymbolSearchInterface listener;
    private Pattern regexPattern;

    public SymbolSearcher(List<BinaryFile> binaries, boolean regexSyntax, boolean ignoreCase) {
        this.binaries = binaries;
        this.regexSyntax = regexSyntax;
        this.ignoreCase = ignoreCase;
    }

    public SymbolSearchInterface getListener() {
        return listener;
    }

    public void setListener(SymbolSearchInterface listener) {
        this.listener = listener;
    }

    public void search(String symbolName) throws PatternSyntaxException {
        SymbolSearchInterface listener = getListener();
        if (listener == null) {
            throw new IllegalStateException("Please provide a progress listener!");
        }

        if (regexSyntax) {
            regexPattern = Pattern.compile(symbolName);
        } else if (ignoreCase) {
            // We only need to do the lower case conversion once
            symbolName = symbolName.toLowerCase();
        }

        listener.onSearchStarted();

        // Take a look at every binary file
        int symbolCount = 0;
        for (BinaryFile binary: binaries) {
            // Is this binary file in a application we care about?
            String packageName = Utils.trimPackageNameNumber(binary.getPackageName());
            if (listener.shouldSkipApp(packageName)) {
                continue;
            }

            Container container = binaryToContainer(binary);
            if (container != null) {
                Log.d("BinaryDroid", "Searching in: " + binary.buildPath());
                List<SymbolItem> symbols = container.getSymbols();
                for (SymbolItem symbol: symbols) {
                    // Check symbol name
                    if (compareSymbol(symbolName, symbol)) {
                        listener.onSymbolMatch(binary, symbol);
                        ++symbolCount;
                    }
                }
            }
        }
        listener.onSearchComplete(symbolCount);
    }

    // Checks if we're interested in the specified symbol.
    private boolean compareSymbol(String symbolName, SymbolItem symbol) {
        if (regexSyntax) {
            return regexPattern.matcher(symbol.name).find();
        } else if (ignoreCase) {
            return symbol.name.toLowerCase().contains(symbolName);
        } else {
            return symbol.name.contains(symbolName);
        }
    }

    // Given a BinaryFile this method tries to parse it into a proper Container.
    private Container binaryToContainer(BinaryFile binary) {
        File file = new File(binary.buildPath());
        if (file.exists()) {
            // TODO: A improvement would be to add different container types (like Windows PEs etc.)
            try {
                ElfFile elf = ElfFile.fromFile(file);
                return new ContainerELF(elf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
