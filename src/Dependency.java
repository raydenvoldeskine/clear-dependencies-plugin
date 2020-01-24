import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Dependency {

    public enum Type{
        DEPENDENCY, MESSAGE
    }

    public enum Style{
        DEFAULT, SEPARATOR, GRAYEDOUT
    }

    private String name;
    private Type type;

    private Style style;

    private @Nullable VirtualFile file;

    public Dependency(@NotNull String name, @NotNull Type type){
        this.name = name;
        this.type = type;
        this.style = Style.DEFAULT;
    }

    public Dependency(@NotNull String name, @NotNull Type type, @NotNull Style style){
        this.name = name;
        this.type = type;
        this.style = style;
    }

    public Dependency(@NotNull String name, @NotNull Type type, @Nullable VirtualFile file){
        this.name = name;
        this.type = type;
        this.file = file;
        this.style = Style.DEFAULT;
    }

    public Dependency(@NotNull String name, @NotNull Type type, @NotNull Style style, @Nullable VirtualFile file){
        this.name = name;
        this.type = type;
        this.style = style;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }


    public Style getStyle() {
        return style;
    }

    @Nullable
    public VirtualFile getFile() {
        return file;
    }

    public void setFile(@Nullable VirtualFile file) {
        this.file = file;
    }

}
