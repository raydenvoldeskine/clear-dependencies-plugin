import org.jetbrains.annotations.NotNull;

public class Dependency {

    public enum Type{
        DEPENDENCY, SEPARATOR, MESSAGE
    }

    private String name;

    private Type type;

    public Dependency(@NotNull String name, @NotNull Type type){
        this.name = name;
        this.type = type;
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

}
