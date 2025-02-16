package yongfa365.mybatis.generator.plugins;

import org.mybatis.generator.api.PluginAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


public class DeleteGeneratedFilesPlugin extends PluginAdapter {
    private boolean deleteJavaModel = true;
    private boolean deleteSqlMap = true;
    private boolean deleteJavaClient = true;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        deleteJavaModel = isTrue(properties.getProperty("deleteJavaModel"));
        deleteSqlMap = isTrue(properties.getProperty("deleteSqlMap"));
        deleteJavaClient = isTrue(properties.getProperty("deleteJavaClient"));
    }

    private boolean isTrue(String value) {
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validate(List<String> list) {
        try {
            deleteIt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void deleteIt() throws Exception {
        if (deleteJavaClient) {
            Path path = Paths.get(
                    context.getJavaClientGeneratorConfiguration().getTargetProject().replace(".", "/"),
                    context.getJavaClientGeneratorConfiguration().getTargetPackage().replace(".", "/"));

            deleteFilesInPath(path, "JavaClient");
        }

        if (deleteJavaModel) {
            Path path = Paths.get(
                    context.getJavaModelGeneratorConfiguration().getTargetProject().replace(".", "/"),
                    context.getJavaModelGeneratorConfiguration().getTargetPackage().replace(".", "/"));

            deleteFilesInPath(path, "JavaModel");
        }

        if (deleteSqlMap) {
            Path path = Paths.get(
                    context.getSqlMapGeneratorConfiguration().getTargetProject().replace(".", "/"),
                    context.getSqlMapGeneratorConfiguration().getTargetPackage().replace(".", "/"));

            deleteFilesInPath(path, "SqlMap");
        }
    }

    private void deleteFilesInPath(Path path, String head) throws IOException {
        System.out.println("[INFO] ================== delete " + head + " =====================");
        Files.list(path).forEach(file -> {
            try {
                Files.deleteIfExists(file);
                System.out.println("[INFO] deleted  " + file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
