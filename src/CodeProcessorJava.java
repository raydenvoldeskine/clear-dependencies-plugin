import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public class CodeProcessorJava extends CodeProcessor {

    private String[] exclusions = {
            "java.",
            "android.",
            "org."
    };

    private PsiJavaFile psiJavaFile;
    CodeProcessorJava(PsiJavaFile psiFile, ProjectAnalyser analyser) {
        super(analyser);
        psiJavaFile = psiFile;
    }

    @Override
    public Optional<ArrayList<Dependency>> getOutgoingList() {
        ArrayList<Dependency> outgoing = new ArrayList<>();
        Optional<String> ownPackageID = analyser.getCorrespondingPackageID(psiJavaFile.getPackageName());

        PsiImportList importList = psiJavaFile.getImportList();
        if (importList != null){
            PsiImportStatementBase[] importBase = importList.getImportStatements();
            for (PsiImportStatementBase base: importBase){
                PsiJavaCodeReferenceElement ref = base.getImportReference();
                if (ref != null){
                    String fullName = ref.getQualifiedName();
                    if (!isExclusionReference(fullName)){
                        if (ownPackageID.isPresent() && fullName.startsWith(ownPackageID.get())){
                            String[] nameParts= fullName.split("\\.");
                            if (nameParts.length > 0){
                                String className = nameParts[nameParts.length - 1];
                                outgoing.add(new Dependency(className, Dependency.Type.DEPENDENCY));
                            }
                        }
                    }
                }

            }
        }


        try {
            Collection<PsiJavaCodeReferenceElement> embeddedRefs = PsiTreeUtil.collectElementsOfType(psiJavaFile.getOriginalElement(), PsiJavaCodeReferenceElement.class);
            for (PsiJavaCodeReferenceElement element: embeddedRefs){
                if (element.getQualifiedName().startsWith(psiJavaFile.getPackageName())){
                    PsiElement resolved = element.resolve();
                    if (resolved instanceof PsiClass){ // go through classes only
                        PsiClass psiClass = (PsiClass)resolved;
                        String className = psiClass.getName();
                        if (className != null){
                            if (!className.equals(getMainClassName(psiJavaFile))) { // skip itself
                                if (psiClass.getContainingClass() == null){ // skip inner classes
                                    if (outgoing.stream().noneMatch(entry -> entry.getName().equals(className))){ // only allow unique items
                                        outgoing.add(new Dependency(className, Dependency.Type.DEPENDENCY));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IndexNotReadyException e) {
        }

        outgoing.sort(new Comparator<Dependency>() {
            public int compare(Dependency entry1, Dependency entry2) {
                return entry1.getName().compareTo(entry2.getName());
            }
        });

        return Optional.of(outgoing);

    }

    @Override
    public Optional<ArrayList<Dependency>> getIncomingList() {
        ArrayList<Dependency> incoming = new ArrayList<>();

        try {
            PsiClass[] classes = psiJavaFile.getClasses();
            PsiClass mainClass = classes.length > 0? classes[0] : null;
            if (mainClass != null){
                Query<PsiReference> refs = ReferencesSearch.search(mainClass);

                for (PsiReference ref: refs){
                    PsiElement element = ref.getElement();
                    PsiClass refClass = PsiUtil.getTopLevelClass(element);
                    PsiFile refFile = element.getContainingFile();
                    if (refClass != null && refFile != null) {
                        if (!refFile.getVirtualFile().getPath().contains("generated")){
                            String className = refClass.getName();
                            if (className != null){
                                if (incoming.stream().noneMatch(entry -> entry.getName().equals(className))) {
                                    incoming.add(new Dependency(className, Dependency.Type.DEPENDENCY));
                                }
                            }
                        }
                    }

                }
            }
        }
        catch (IndexNotReadyException e) {
            return Optional.empty();
        }

        incoming.sort(new Comparator<Dependency>() {
            public int compare(Dependency entry1, Dependency entry2) {
                return entry1.getName().compareTo(entry2.getName());
            }
        });

        return Optional.of(incoming);
    }

    private boolean isExclusionReference(String fullName){
        for (String ex: exclusions){
            if (fullName.startsWith(ex)){
                return true;
            }
        }
        return false;
    }

    private String getMainClassName(PsiJavaFile psiJavaFile){
        PsiClass[] classes = psiJavaFile.getClasses();
        PsiClass mainClass = classes.length > 0? classes[0] : null;
        if (mainClass != null){
            return mainClass.getName();
        }
        return "";
    }
}
