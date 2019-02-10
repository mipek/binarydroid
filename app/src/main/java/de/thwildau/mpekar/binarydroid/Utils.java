package de.thwildau.mpekar.binarydroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import de.thwildau.mpekar.binarydroid.assembly.Disassembler;

/**
 * Utility functions
 */
public class Utils {
    private static final String HEXCHARS = "0123456789ABCDEF";

    // long to string
    public static String l2s(long value, int pad) {
        return String.format("%0" + pad + "X", value);
    }

    // byte to string
    public static String b2s(byte value) {
        StringBuilder builder = new StringBuilder();
        b2s(builder, value);
        return builder.toString();
    }

    // byte to string (using a existing StringBuilder)
    public static void b2s(StringBuilder builder, byte value) {
        builder.append(HEXCHARS.charAt((value & 0xF0) >> 4))
               .append(HEXCHARS.charAt(value & 0x0F));
    }

    private static Disassembler.Instruction dummy;
    public static Disassembler.Instruction dummyInstruction(final short size) {
        // We use this dummy object to make our life easier because we do not have to
        // check if this is a valid instruction or not.
        if (dummy == null || dummy.size() != size) {
            dummy = new Disassembler.Instruction() {
                @Override
                public String mnemonic() {
                    return "undef";
                }

                @Override
                public String operands() {
                    return "";
                }

                @Override
                public short size() {
                    return size;
                }

                @Override
                public boolean isReturn() {
                    return false;
                }

                @Override
                public String toString() {
                    return mnemonic();
                }
            };
        }
        return dummy;
    }

    // Build the dialog that tells the user that we are about to request SU permissions.
    public static void requestSU(Activity activity, DialogInterface.OnClickListener clickListener) {
        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.rootmsg);
        builder.setPositiveButton(activity.getString(R.string.rootaccept), clickListener);
        builder.setNegativeButton(activity.getString(R.string.rootdeny), clickListener);
        // Show dialog
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Sometimes there will be a number appended after the package name (in /data/apps).
    // We don't want that so just trim it off
    public static String trimPackageNameNumber(String packageName) {
        int i = packageName.lastIndexOf("-");
        if (i > 0) {
            packageName = packageName.substring(0, i);
        }
        return packageName;
    }
}
