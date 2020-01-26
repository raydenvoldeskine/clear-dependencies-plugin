import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.android.util.AndroidUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class ProjectAnalyserAndroid extends ProjectAnalyser {

    private ArrayList<String> packageIDs = new ArrayList<>();

    public ProjectAnalyserAndroid(@Nullable Project project){
        super(project);
        packageIDs = getAllPackageIds(project);
    }
    public Optional<String> getCorrespondingPackageID(String codePackageID){
        Optional<String> ownPackageID = packageIDs
                .stream()
                .filter(codePackageID::startsWith)
                .findFirst();
        return ownPackageID;
    }

    private ArrayList<String> getAllPackageIds(Project project){
        ArrayList<String> all = new ArrayList<>();
        for (Module module: ModuleManager.getInstance(project).getModules()){
            for (Facet facet: FacetManager.getInstance(module).getAllFacets()){
                if (facet instanceof AndroidFacet){
                    AndroidFacet androidFacet = (AndroidFacet)facet;
                    VirtualFile manifestFile = AndroidRootUtil.getManifestFile(androidFacet);
                    if (manifestFile != null){
                        Manifest manifest = AndroidUtils.loadDomElement(module, manifestFile, Manifest.class);
                        if (manifest != null){
                            GenericAttributeValue<String> pack = manifest.getPackage();
                            if (pack != null){
                                all.add(pack.toString());
                            }
                        }
                    }
                }
            }
        }
        return all;
    }


}
