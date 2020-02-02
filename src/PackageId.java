import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.StringJoiner;

public class PackageId {

    private String[] parts;

    public PackageId(@Nonnull String packageName){
        parts = packageName.split("\\.");
    }

    public @Nonnull String getFirst(){
        return parts.length > 0 ? parts[0] : "";
    }

    public @Nonnull String getLast(){
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }

    public int count(){
        return parts.length;
    }

    public @Nullable String getPart(int index){
        return parts.length > index ? parts[index] : null;
    }

    public boolean hasCommonPart(@Nonnull PackageId other){
        return !getCommonPart(other).isEmpty();
    }

    public String getCommonPart(@Nonnull PackageId other){

        StringJoiner joiner = new StringJoiner(".");
        for (int i = 0; i < count(); i++){
            String ownPart = getPart(i);
            String otherPart = other.getPart(i);
            if (ownPart != null && otherPart != null && ownPart.equals(otherPart)){
                joiner.add(ownPart);
            } else {
                return joiner.toString();
            }
        }
        return joiner.toString();
    }

    public boolean doesBeginWith(@NotNull PackageId other){
        for (int i = 0; i < other.count(); i++){
            String ownPart = getPart(i);
            String otherPart = other.getPart(i);
            if (ownPart == null || otherPart == null || !ownPart.equals(otherPart)) {
                return false;
            }
        }
        return true;
    }
}
