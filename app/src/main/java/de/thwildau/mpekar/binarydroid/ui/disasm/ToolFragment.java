package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.support.v4.app.Fragment;

/**
 * Encapsulates a disassembler tool.
 */
public class ToolFragment extends Fragment {
    public static final int CMD_REFRESHVIEW = 1;

    /**
     * Fired when the disassembler communicates with a fragment
     */
    public void onRunCommand(int commandId) {
    }
}
