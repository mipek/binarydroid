package de.thwildau.mpekar.binarydroid.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;

import java.util.ArrayList;
import java.util.List;

import de.thwildau.mpekar.binarydroid.AestheticColorGenerator;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;

@SuppressWarnings("WeakerAccess")
public class BinaryListViewModel extends ViewModel {
    private MutableLiveData<List<BinaryFile>> binaryList;

    // TODO: add app-private and so on..
    private final String AppDirectory = "/data/app";

    LiveData<List<BinaryFile>> getBinaries() {
        if (binaryList == null) {
            binaryList = new MutableLiveData<>();
            loadInstalledApps();
        }
        return binaryList;
    }

    private void loadInstalledApps() {
        if (RootManager.getInstance().obtainPermission()) {
            Result r = RootManager.getInstance().runCommand("ls " + AppDirectory);
            if (r.getResult()) {
                List<BinaryFile> binaries = new ArrayList<>();

                // Enumerate all apps..
                String[] apps = r.getMessage().split("\n");
                for (String packageName: apps) {
                    packageName = sanitizeFileName(packageName);

                    loadAppBinaries(packageName, binaries);
                }

                binaryList.postValue(binaries);
            }
        }
        //List<ApplicationInfo> localApps =
        //        getApplication().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }

    private void loadAppBinaries(String packageName, List<BinaryFile> outList) {
        Log.d("BinaryDroid", packageName);
        int randomColor = new AestheticColorGenerator().generateRandomColor();
        // list available architectures
        String path = String.format("ls %s/%s/lib", AppDirectory, packageName);
        Result r = RootManager.getInstance().runCommand(path);
        if (r.getResult()) {
            String architectures = r.getMessage();


            if (r.getResult() && !architectures.isEmpty()) {
                for (String arch: architectures.split("\n")) {
                    arch = sanitizeFileName(arch);

                    // TODO: properly handle "no such file or directory"..
                    if (arch.contains("Nosuchfileordirectory")) continue;

                    Log.d("BinaryDroid", "Arch: " + arch);

                    // list all binaries that are available for this architecture.
                    r = RootManager.getInstance().runCommand(String.format(
                            "%s/%s",
                            path, arch));

                    String binaries = r.getMessage();
                    if (r.getResult() && !binaries.isEmpty()) {
                        for (String binary: binaries.split("\n")) {
                            binary = sanitizeFileName(binary);

                            Log.d("BinaryDroid", " Bin: " + binary);

                            BinaryFile binaryFile =
                                    new BinaryFile(packageName, arch, binary, randomColor);
                            outList.add(binaryFile);
                        }
                    }
                }
            }
        }

        Log.d("BinaryDroid", "-------------");
    }

    // make filenames great again.
    // only allow a-z, A-Z, 0-9, '.' and '-'
    private static String sanitizeFileName(String packageName) {
        return packageName.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
    }
}
