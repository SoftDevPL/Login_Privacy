package lg.sec.loginprivacy.resourcesConfigGenerator;

import com.google.common.base.Charsets;
import lg.sec.loginprivacy.LoginPrivacy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConfigGenerator {

    private final List<String> fileListNames = new ArrayList<>();
    private final List<File> fileList = new ArrayList<>();
    public LoginPrivacy pluginInstance;
    private File configsFolder;

    public void init() {
        this.pluginInstance = LoginPrivacy.getInstance();
        this.configsFolder = new File(this.pluginInstance.getDataFolder() + "/configs");
        getAllFilesFromConfigFolder();
        getAllFilesAsFiles();
        generateAllConfigs();
    }

    private void getAllFilesFromConfigFolder() {
        if (configsFolder.exists()) {
            for (File file : configsFolder.listFiles()) {
                fileListNames.add(file.getName());
            }
        }
    }

    private void getAllFilesAsFiles() {
        if (configsFolder.exists()) {
            fileList.addAll(Arrays.asList(configsFolder.listFiles()));
        }
    }

    private void generateAllConfigs() {
        CodeSource src = this.pluginInstance.getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = null;
            try {
                zip = new ZipInputStream(jar.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                ZipEntry e = null;
                try {
                    assert zip != null;
                    e = zip.getNextEntry();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith("configs/")) {
                    String[] separatedWords = name.split("/", 2);
                    if (!separatedWords[1].equals("")) {
                        if (!fileListNames.contains(separatedWords[1])) {
                            this.pluginInstance.saveResource("configs/" + separatedWords[1], false);
                        } else {
                            if (configsFolder.exists()) {
                                File file = new File(configsFolder + "/" + separatedWords[1]);
                                reloadConfig(file.getName(), file);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("Not found!");
        }
    }

    private void reloadConfig(String fileName, File configFile) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        InputStream defConfigStream = this.pluginInstance.getResource("configs/" + fileName);
        if (defConfigStream != null) {
            yml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
        }
    }
}
