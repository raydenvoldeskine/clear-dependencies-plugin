import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;

import java.util.*;
import java.util.stream.Collectors;

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

        try {
            Project project = analyser.getProject();
            PsiImportList importList = psiJavaFile.getImportList();
            Optional<PackageId> ownPackageID = analyser.getCorrespondingPackageId(psiJavaFile.getPackageName());
            List<PackageId> allProjectPackages = analyser.getAllPackages();
            HashMap<String, Integer> outgoingPackageReferences = new HashMap<>();

            if (importList != null && project != null && ownPackageID.isPresent()){

                Collection<PsiImportStatementBase> unusedImports = JavaCodeStyleManager.getInstance(project).findRedundantImports(psiJavaFile);
                unusedImports = unusedImports != null? unusedImports : new ArrayList<PsiImportStatementBase>();
                List<String> unused = unusedImports.stream()
                        .map(PsiImportStatementBase::getImportReference)
                        .filter(Objects::nonNull)
                        .map(PsiJavaCodeReferenceElement::getQualifiedName)
                        .collect(Collectors.toList());

                List<Dependency> imports = Arrays.stream(importList.getImportStatements())
                        .map(PsiImportStatementBase::getImportReference)
                        .filter(Objects::nonNull)
                        .filter(ref -> !isExclusionReference(ref.getQualifiedName()))
                        .map(ref -> new AbstractMap.SimpleImmutableEntry<PsiJavaCodeReferenceElement, PackageId>(ref, new PackageId(ref.getQualifiedName())))
                        .filter(entry -> entry.getValue().doesBeginWith(ownPackageID.get()))
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> new AbstractMap.SimpleImmutableEntry<PsiElement, PackageId>(entry.getKey().resolve(), entry.getValue()))
                        .map(entry -> new Dependency(
                                entry.getValue().getLast(),
                                Dependency.Type.OUTGOING,
                                unused.stream().anyMatch(unusedName -> unusedName.equals(entry.getValue().toString())) ? Dependency.Style.GRAYEDOUT : Dependency.Style.DEFAULT,
                                entry.getKey() != null? entry.getKey().getContainingFile().getVirtualFile() : null))
                        .collect(Collectors.toList());


                outgoing.addAll(imports.stream()
                        .filter(ref -> outgoing.stream().noneMatch(entry -> entry.getName().equals(ref.getName())))
                        .collect(Collectors.toList()));

                Arrays.stream(importList.getImportStatements())
                        .map(PsiImportStatementBase::getImportReference)
                        .filter(Objects::nonNull)
                        .filter(ref -> !isExclusionReference(ref.getQualifiedName()))
                        .map(ref -> new PackageId(ref.getQualifiedName()))
                        .filter(packageId -> !packageId.doesBeginWith(ownPackageID.get()))
                        .filter(packageId -> !packageId.isEmpty())
                        .forEach(packageId -> {
                            Optional<PackageId> otherProjectPackage = allProjectPackages
                                    .stream()
                                    .filter(id -> id.hasCommonMoreThatRootPart(packageId))
                                    .findFirst();
                            if (otherProjectPackage.isPresent()){
                                String key = otherProjectPackage.get().toString();
                                if (!outgoingPackageReferences.containsKey(key)){
                                    outgoingPackageReferences.put(key, 0);
                                } else {
                                    outgoingPackageReferences.put(key, outgoingPackageReferences.get(key) + 1 );
                                }
                            }
                         });

                outgoing.addAll(outgoingPackageReferences.entrySet().stream()
                        .map(entry -> new Dependency(entry.getKey() + " " + "(" + entry.getValue() + ")", Dependency.Type.OUTGOING))
                        .collect(Collectors.toList()));

            }

            // Dependencies from the same package are not listed in imports, so they have to be detected separately
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
                                        outgoing.add(new Dependency(className, Dependency.Type.OUTGOING, resolved.getContainingFile().getVirtualFile()));
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
                                    incoming.add(new Dependency(className, Dependency.Type.INCOMING, refFile.getVirtualFile()));
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
